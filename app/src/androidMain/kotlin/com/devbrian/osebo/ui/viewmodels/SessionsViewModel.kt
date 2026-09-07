package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.models.Session
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SessionsViewModel : ViewModel() {

    private val _sessions = MutableLiveData<List<Session>>()
    val sessions: LiveData<List<Session>> get() = _sessions

    private val _terminateSuccess = MutableLiveData<Boolean>()
    val terminateSuccess: LiveData<Boolean> get() = _terminateSuccess

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun loadSessions() {
        viewModelScope.launch {
            _loading.value = true

            
            delay(1000)

            
            val mockSessions = listOf(
                Session(
                    id = "1",
                    device = "Samsung Galaxy S21",
                    browser = "Chrome 120.0",
                    ipAddress = "192.168.1.100",
                    location = "Kampala, UG",
                    lastActive = "2 hours ago",
                    isCurrent = true
                ),
                Session(
                    id = "2",
                    device = "iPhone 13 Pro",
                    browser = "Safari 16.0",
                    ipAddress = "192.168.1.101",
                    location = "Kampala, UG",
                    lastActive = "1 day ago",
                    isCurrent = false
                ),
                Session(
                    id = "3",
                    device = "Windows Desktop",
                    browser = "Chrome 119.0",
                    ipAddress = "102.89.32.15",
                    location = "Nairobi, KE",
                    lastActive = "3 days ago",
                    isCurrent = false
                )
            )

            _sessions.value = mockSessions
            _loading.value = false
        }
    }

    fun terminateSession(sessionId: String) {
        viewModelScope.launch {
            
            delay(500)

            
            val currentSessions = _sessions.value ?: emptyList()
            val updatedSessions = currentSessions.filter { it.id != sessionId }
            _sessions.value = updatedSessions

            _terminateSuccess.value = true
        }
    }

    fun terminateAllSessions() {
        viewModelScope.launch {
            
            delay(500)

            
            val currentSessions = _sessions.value ?: emptyList()
            val updatedSessions = currentSessions.filter { it.isCurrent }
            _sessions.value = updatedSessions

            _terminateSuccess.value = true
        }
    }
}


