package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.AiRepository
import com.devbrian.osebo.models.AiChatMessage
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<AiChatMessage>>(emptyList())
    val messages: LiveData<List<AiChatMessage>> = _messages

    private val _isTyping = MutableLiveData(false)
    val isTyping: LiveData<Boolean> = _isTyping

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _isTyping.value == true) return

        _messages.value = _messages.value.orEmpty() + AiChatMessage(text = trimmed, isFromUser = true)
        _isTyping.value = true

        viewModelScope.launch {
            when (val result = aiRepository.sendMessage(trimmed)) {
                is Resource.Success -> {
                    _messages.value = _messages.value.orEmpty() + result.data
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                }
                Resource.Loading -> Unit
            }
            _isTyping.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
