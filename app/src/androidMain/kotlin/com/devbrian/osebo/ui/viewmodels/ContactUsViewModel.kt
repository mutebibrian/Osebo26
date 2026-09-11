package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devbrian.osebo.models.contact.ContactInfo

class ContactUsViewModel : ViewModel() {

    private val _contactInfo = MutableLiveData<ContactInfo>()
    val contactInfo: LiveData<ContactInfo> get() = _contactInfo

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _messageSent = MutableLiveData<Boolean>()
    val messageSent: LiveData<Boolean> get() = _messageSent

    init {
        loadContactInfo()
    }

    private fun loadContactInfo() {
        _loading.value = true

        
        
        _contactInfo.value = getDefaultContactInfo()
        _loading.value = false

        
        
    }

    fun sendSupportMessage(
        name: String,
        email: String,
        subject: String,
        message: String,
        phone: String? = null
    ) {
        _loading.value = true

        
        
        _loading.value = false
        _messageSent.value = true

        
        
    }

    private fun getDefaultContactInfo(): ContactInfo {
        return ContactInfo(
            email = "support@osebo.com",
            phone = "+256 700 000000", 
            whatsapp = "+256 700 000000",
            address = "Kampala, Uganda",
            workingHours = "Mon-Fri: 8:00 AM - 6:00 PM"
        )
    }

    fun clearError() {
        _error.value = null
    }

    fun clearMessageSent() {
        _messageSent.value = false
    }
}


