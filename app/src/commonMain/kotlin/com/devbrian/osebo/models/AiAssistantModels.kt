package com.devbrian.osebo.models

data class AiConversation(
    val id: String,
    val title: String,
    val updatedAt: String = "",
    val messages: List<AiChatMessage> = emptyList(),
)

data class AiAssistantReadiness(
    val isReady: Boolean,
    val message: String = "",
)

data class AiAssistantUiState(
    val conversations: List<AiConversation> = emptyList(),
    val activeConversationId: String? = null,
    val messages: List<AiChatMessage> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
)
