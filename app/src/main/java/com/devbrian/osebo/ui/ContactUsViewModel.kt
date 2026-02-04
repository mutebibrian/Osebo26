package com.devbrian.osebo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.remote.api.OseboApiService
import com.devbrian.osebo.data.repository.ContactRepository
import com.devbrian.osebo.models.contact.ContactInfo
import com.devbrian.osebo.models.SupportMessageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactUsViewModel @Inject constructor(
    private val apiService: OseboApiService,
    private val contactRepository: ContactRepository
) : BaseViewModel() {

    private val _contactInfo = MutableLiveData<ContactInfo>()
    val contactInfo: LiveData<ContactInfo> get() = _contactInfo

    private val _messageSent = MutableLiveData<Boolean>()
    val messageSent: LiveData<Boolean> get() = _messageSent

    init {
        loadContactInfo()
    }

    private fun loadContactInfo() {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                // Use the suspend function
                val response = contactRepository.getContactInfoSuspend()
                _contactInfo.postValue(response)
                _loading.postValue(false)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun sendSupportMessage(
        name: String,
        email: String,
        subject: String,
        category: String,
        message: String
    ) {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                val request = SupportMessageRequest(
                    name = name,
                    email = email,
                    subject = subject,
                    category = category,
                    message = message,
                    timestamp = System.currentTimeMillis()
                )

                val response = apiService.sendSupportMessage(request)
                _messageSent.postValue(true)
                _loading.postValue(false)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
}