package com.devbrian.osebo.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.AiAssistantRepository
import com.devbrian.osebo.models.AiAssistantUiState
import com.devbrian.osebo.models.AiChatMessage
import com.devbrian.osebo.models.AiConversation
import kotlinx.coroutines.launch

class AiAssistantViewModel(
    private val repository: AiAssistantRepository,
) : ViewModel() {
    private val _uiState = mutableStateOf(AiAssistantUiState())
    val uiState: State<AiAssistantUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            update { it.copy(isLoading = true, errorMessage = null) }
            val readiness = repository.getReadiness()
            val readinessValue = readiness.getOrNull()
            if (readinessValue?.isReady == false) {
                update {
                    it.copy(
                        isLoading = false,
                        errorMessage = readinessValue.message.ifBlank {
                            "Osebo AI is getting ready. Please try again shortly."
                        },
                    )
                }
                return@launch
            }

            // Conversations are the user-facing contract. Readiness is a diagnostic route and
            // may be restricted to operational roles, so a readiness authorization failure must
            // not prevent an otherwise-authorized user from opening their conversations.
            repository.getConversations()
                .onSuccess { conversations ->
                    update {
                        it.copy(
                            conversations = conversations.sortedByDescending(AiConversation::updatedAt),
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                }
        }
    }

    fun startNewConversation() {
        update {
            it.copy(
                activeConversationId = null,
                messages = emptyList(),
                errorMessage = null,
            )
        }
    }

    fun openConversation(conversationId: String) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            update { it.copy(isLoading = true, errorMessage = null) }
            repository.getConversation(conversationId)
                .onSuccess { conversation ->
                    update {
                        it.copy(
                            activeConversationId = conversation.id,
                            messages = conversation.messages,
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    update { it.copy(isLoading = false, errorMessage = error.userMessage()) }
                }
        }
    }

    fun archiveConversation(conversationId: String) {
        viewModelScope.launch {
            repository.archiveConversation(conversationId)
                .onSuccess {
                    update { state ->
                        state.copy(
                            conversations = state.conversations.filterNot { it.id == conversationId },
                            activeConversationId = state.activeConversationId
                                .takeUnless { it == conversationId },
                            messages = if (state.activeConversationId == conversationId) {
                                emptyList()
                            } else {
                                state.messages
                            },
                        )
                    }
                }
                .onFailure { error ->
                    update { it.copy(errorMessage = error.userMessage()) }
                }
        }
    }

    fun sendMessage(rawMessage: String) {
        val message = rawMessage.trim()
        if (message.isBlank() || _uiState.value.isSending) return

        viewModelScope.launch {
            val optimisticMessage = AiChatMessage(
                id = "local-${System.currentTimeMillis()}",
                isFromUser = true,
                text = message,
            )
            update {
                it.copy(
                    messages = it.messages + optimisticMessage,
                    isSending = true,
                    errorMessage = null,
                )
            }

            val conversationId = _uiState.value.activeConversationId
                ?: createConversation(message).getOrElse { error ->
                    removeOptimisticMessage(optimisticMessage.id, error.userMessage())
                    return@launch
                }

            val sendResult = repository.sendMessage(conversationId, message)
            if (sendResult.isFailure) {
                removeOptimisticMessage(
                    optimisticMessage.id,
                    sendResult.exceptionOrNull().userMessage(),
                )
                return@launch
            }

            val canonicalConversation = repository.getConversation(conversationId).getOrNull()
            val responseMessages = canonicalConversation?.messages
                ?.takeIf(List<AiChatMessage>::isNotEmpty)
                ?: mergeMessages(_uiState.value.messages, sendResult.getOrDefault(emptyList()))
            update {
                it.copy(
                    messages = responseMessages,
                    isSending = false,
                    conversations = canonicalConversation?.let { updated ->
                        (it.conversations.filterNot { item -> item.id == updated.id } + updated)
                            .sortedByDescending(AiConversation::updatedAt)
                    } ?: it.conversations,
                )
            }
        }
    }

    fun clearError() {
        update { it.copy(errorMessage = null) }
    }

    private suspend fun createConversation(message: String): Result<String> {
        val title = message.replace("\n", " ").take(48)
        return repository.createConversation(title).map { conversation ->
            update {
                it.copy(
                    activeConversationId = conversation.id,
                    conversations = listOf(conversation) + it.conversations,
                )
            }
            conversation.id
        }
    }

    private fun removeOptimisticMessage(messageId: String, errorMessage: String) {
        update {
            it.copy(
                messages = it.messages.filterNot { message -> message.id == messageId },
                isSending = false,
                errorMessage = errorMessage,
            )
        }
    }

    private fun mergeMessages(
        existing: List<AiChatMessage>,
        received: List<AiChatMessage>,
    ): List<AiChatMessage> {
        if (received.isEmpty()) return existing
        return (existing + received).distinctBy { message ->
            message.id.takeUnless { it.startsWith("local-") }
                ?: "${message.isFromUser}:${message.text}"
        }
    }

    private fun update(transform: (AiAssistantUiState) -> AiAssistantUiState) {
        _uiState.value = transform(_uiState.value)
    }

    private fun Throwable?.userMessage(): String =
        this?.message?.takeIf(String::isNotBlank) ?: "Osebo AI is unavailable. Please try again."
}
