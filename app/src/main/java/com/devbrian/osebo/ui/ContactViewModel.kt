package com.devbrian.osebo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devbrian.osebo.data.repository.ContactRepository
import com.devbrian.osebo.models.contact.*
import com.devbrian.osebo.models.ApiResponse as ContactApiResponse // Alias to avoid conflict

class ContactViewModel(private val repository: ContactRepository) : ViewModel() {

    // Support Tickets
    private val _supportTickets = MutableLiveData<List<SupportTicket>>()
    val supportTickets: LiveData<List<SupportTicket>> = _supportTickets

    private val _ticketDetails = MutableLiveData<SupportTicketResponse>()
    val ticketDetails: LiveData<SupportTicketResponse> = _ticketDetails

    private val _createTicketResult = MutableLiveData<SupportTicket?>()
    val createTicketResult: LiveData<SupportTicket?> = _createTicketResult

    private val _addMessageResult = MutableLiveData<TicketMessage?>()
    val addMessageResult: LiveData<TicketMessage?> = _addMessageResult

    // FAQs
    private val _faqs = MutableLiveData<List<FAQ>>()
    val faqs: LiveData<List<FAQ>> = _faqs

    // Contact Info
    private val _contactInfo = MutableLiveData<ContactInfo>()
    val contactInfo: LiveData<ContactInfo> = _contactInfo

    // Help Categories
    private val _helpCategories = MutableLiveData<List<String>>()
    val helpCategories: LiveData<List<String>> = _helpCategories

    // Contact Form - Use the aliased ApiResponse
    private val _contactFormResult = MutableLiveData<ContactApiResponse?>()
    val contactFormResult: LiveData<ContactApiResponse?> = _contactFormResult

    // Loading States
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Support Tickets
    fun createSupportTicket(
        category: String,
        subject: String,
        message: String,
        priority: String = "normal"
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        repository.createSupportTicket(
            category = category,
            subject = subject,
            message = message,
            priority = priority,
            onSuccess = { ticket ->
                _createTicketResult.value = ticket
                _isLoading.value = false
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    fun getSupportTickets(status: String? = null) {
        _isLoading.value = true
        _errorMessage.value = null

        repository.getSupportTickets(
            status = status,
            onSuccess = { tickets ->
                _supportTickets.value = tickets
                _isLoading.value = false
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    fun getTicketDetails(ticketId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        repository.getTicketDetails(
            ticketId = ticketId,
            onSuccess = { response ->
                _ticketDetails.value = response
                _isLoading.value = false
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    fun addTicketMessage(ticketId: String, message: String) {
        _isLoading.value = true
        _errorMessage.value = null

        repository.addTicketMessage(
            ticketId = ticketId,
            message = message,
            onSuccess = { ticketMessage ->
                _addMessageResult.value = ticketMessage
                _isLoading.value = false

                // Refresh ticket details
                getTicketDetails(ticketId)
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    // FAQs
    fun getFAQs(category: String? = null, language: String = "en") {
        _isLoading.value = true
        _errorMessage.value = null

        repository.getFAQs(
            category = category,
            language = language,
            onSuccess = { faqs ->
                _faqs.value = faqs
                _isLoading.value = false
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    // Contact Information
    fun getContactInfo() {
        _isLoading.value = true
        _errorMessage.value = null

        repository.getContactInfo(
            onSuccess = { info ->
                _contactInfo.value = info
                _isLoading.value = false
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    // Help Categories
    fun getHelpCategories() {
        _isLoading.value = true
        _errorMessage.value = null

        repository.getHelpCategories(
            onSuccess = { categories ->
                _helpCategories.value = categories
                _isLoading.value = false
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    // Contact Form
    fun submitContactForm(
        name: String,
        email: String,
        subject: String,
        message: String,
        phone: String? = null
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        repository.submitContactForm(
            name = name,
            email = email,
            subject = subject,
            message = message,
            phone = phone,
            onSuccess = { response ->
                _contactFormResult.value = response
                _isLoading.value = false
            },
            onError = { error ->
                _errorMessage.value = error
                _isLoading.value = false
            }
        )
    }

    // Clear results
    fun clearCreateTicketResult() {
        _createTicketResult.value = null
    }

    fun clearAddMessageResult() {
        _addMessageResult.value = null
    }

    fun clearContactFormResult() {
        _contactFormResult.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}