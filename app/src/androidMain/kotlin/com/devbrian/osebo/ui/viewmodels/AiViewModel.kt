package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.AiRepository
import com.devbrian.osebo.data.repository.ElevenLabsRepository
import com.devbrian.osebo.models.AiChatMessage
import com.devbrian.osebo.utils.Resource
import kotlinx.coroutines.launch
import java.io.File

class AiViewModel(
    private val aiRepository: AiRepository,
    private val elevenLabsRepository: ElevenLabsRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<AiChatMessage>>(emptyList())
    val messages: LiveData<List<AiChatMessage>> = _messages

    private val _isTyping = MutableLiveData(false)
    val isTyping: LiveData<Boolean> = _isTyping

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isTranscribing = MutableLiveData(false)
    val isTranscribing: LiveData<Boolean> = _isTranscribing

    private val _transcribedText = MutableLiveData<String?>()
    val transcribedText: LiveData<String?> = _transcribedText

    fun transcribeAudio(audioFile: File) {
        _isTranscribing.value = true
        viewModelScope.launch {
            when (val result = elevenLabsRepository.transcribe(audioFile)) {
                is Resource.Success -> _transcribedText.value = result.data
                is Resource.Error -> _errorMessage.value = result.message
                Resource.Loading -> Unit
            }
            _isTranscribing.value = false
            audioFile.delete()
        }
    }

    fun consumeTranscribedText() {
        _transcribedText.value = null
    }

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
