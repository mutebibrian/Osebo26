package com.devbrian.osebo.ui.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.request.CreateSubscriptionRequest
import com.devbrian.osebo.data.remote.dto.response.PaymentCheckResponse
import com.devbrian.osebo.data.remote.dto.response.ShopSubscriptionStatusResponse
import com.devbrian.osebo.data.remote.dto.response.SubscriptionResponse
import com.devbrian.osebo.data.repository.SubscriptionRepository
import com.devbrian.osebo.models.Payment
import com.devbrian.osebo.models.Subscription
import com.devbrian.osebo.models.SubscriptionPackage
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repository: SubscriptionRepository,
    private val application: Application
) : ViewModel() {

    private val _subscriptionPackages = MutableLiveData<Resource<List<SubscriptionPackage>>>()
    val subscriptionPackages: LiveData<Resource<List<SubscriptionPackage>>> = _subscriptionPackages

    private val _currentSubscription = MutableLiveData<Resource<Subscription?>>()
    val currentSubscription: LiveData<Resource<Subscription?>> = _currentSubscription

    private val _subscriptionDetails = MutableLiveData<Resource<Subscription?>>()
    val subscriptionDetails: LiveData<Resource<Subscription?>> = _subscriptionDetails

    private val _subscriptionResult = MutableLiveData<Resource<SubscriptionResponse>>()
    val subscriptionResult: LiveData<Resource<SubscriptionResponse>> = _subscriptionResult

    private val _createSubscriptionResult = MutableLiveData<Resource<SubscriptionResponse>>()
    val createSubscriptionResult: LiveData<Resource<SubscriptionResponse>> = _createSubscriptionResult

    // Now tracks polling via the real payment-check response shape (shop / packageSubscriptions / payment)
    private val _paymentPollingStatus = MutableLiveData<Resource<PaymentCheckResponse?>>()
    val paymentPollingStatus: LiveData<Resource<PaymentCheckResponse?>> = _paymentPollingStatus

    private val _shopSubscriptionStatus = MutableLiveData<Resource<ShopSubscriptionStatusResponse?>>()
    val shopSubscriptionStatus: LiveData<Resource<ShopSubscriptionStatusResponse?>> = _shopSubscriptionStatus

    private val _paymentHistory = MutableLiveData<Resource<List<Payment>?>>()
    val paymentHistory: LiveData<Resource<List<Payment>?>> = _paymentHistory

    private val _cancelSubscriptionResult = MutableLiveData<Resource<Unit>>()
    val cancelSubscriptionResult: LiveData<Resource<Unit>> = _cancelSubscriptionResult

    private val _renewSubscriptionResult = MutableLiveData<Resource<SubscriptionResponse>>()
    val renewSubscriptionResult: LiveData<Resource<SubscriptionResponse>> = _renewSubscriptionResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isPolling = MutableLiveData(false)
    val isPolling: LiveData<Boolean> get() = _isPolling

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> get() = _successMessage

    private var pollingJob: Job? = null
    private var currentShopId: String? = null

    // ==================== PUBLIC FUNCTIONS ====================

    fun checkPaymentStatus(paymentId: String) {
        viewModelScope.launch {
            _paymentPollingStatus.value = Resource.Loading

            try {
                val result = repository.findSubscriptionByPaymentId(paymentId)

                when (result) {
                    is Resource.Success -> {
                        _paymentPollingStatus.value = Resource.Success(result.data)
                    }
                    is Resource.Error -> {
                        _paymentPollingStatus.value = Resource.Error(result.message)
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _paymentPollingStatus.value = Resource.Error(e.message ?: "Failed to check payment status")
            }
        }
    }

    fun loadSubscriptionPackages() {
        viewModelScope.launch {
            _subscriptionPackages.value = Resource.Loading
            _isLoading.value = true

            try {
                val result = repository.getSubscriptionPackages()

                when (result) {
                    is Resource.Success -> {
                        _subscriptionPackages.value = Resource.Success(result.data)
                        _errorMessage.value = null
                    }
                    is Resource.Error -> {
                        _subscriptionPackages.value = Resource.Error(result.message)
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _subscriptionPackages.value = Resource.Error(e.message ?: "Failed to load packages")
                _errorMessage.value = e.message ?: "Failed to load packages"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getSubscriptionDetails(subscriptionId: String) {
        viewModelScope.launch {
            _subscriptionDetails.value = Resource.Loading
            try {
                val result = repository.getSubscriptionDetails(subscriptionId)
                _subscriptionDetails.value = result
            } catch (e: Exception) {
                _subscriptionDetails.value = Resource.Error(e.message ?: "Failed to load subscription details")
            }
        }
    }

    fun getPaymentHistory(shopId: String, page: Int = 1, limit: Int = 10, provider: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getPaymentHistory(shopId, page, limit, provider)
                when (result) {
                    is Resource.Success -> {
                        _paymentHistory.value = Resource.Success(result.data)
                    }
                    is Resource.Error -> {
                        _paymentHistory.value = Resource.Error(result.message)
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> {
                        _paymentHistory.value = Resource.Loading
                    }
                }
            } catch (e: Exception) {
                _paymentHistory.value = Resource.Error(e.message ?: "Failed to load payment history")
                _errorMessage.value = e.message ?: "Failed to load payment history"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSubscription(
        shopId: String,
        packageIds: List<String>,
        phoneNumber: String,
        months: Int = 1
    ) {
        viewModelScope.launch {
            _subscriptionResult.value = Resource.Loading
            _createSubscriptionResult.value = Resource.Loading
            _isLoading.value = true
            currentShopId = shopId

            try {
                val request = CreateSubscriptionRequest(
                    packageIds = packageIds,
                    customerPhone = phoneNumber,
                    duration = months
                )

                val result = repository.createSubscription(shopId, request)

                when (result) {
                    is Resource.Success -> {
                        val response = result.data
                        _subscriptionResult.value = Resource.Success(response)
                        _createSubscriptionResult.value = Resource.Success(response)

                        if (!response?.paymentId.isNullOrBlank()) {
                            _successMessage.value = "Subscription initiated successfully"
                            response?.paymentId?.let { paymentId ->
                                startPaymentPolling(paymentId, shopId)
                            }
                        } else {
                            _successMessage.value = response?.message ?: "Subscription created successfully"
                            getShopActiveSubscription(shopId)
                        }
                    }
                    is Resource.Error -> {
                        _subscriptionResult.value = Resource.Error(result.message)
                        _createSubscriptionResult.value = Resource.Error(result.message)
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _subscriptionResult.value = Resource.Error(e.message ?: "Failed to create subscription")
                _createSubscriptionResult.value = Resource.Error(e.message ?: "Failed to create subscription")
                _errorMessage.value = e.message ?: "Failed to create subscription"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Polls the real payment-check response (shop / packageSubscriptions / payment) until
     * it reflects a paid, failed, or cancelled state.
     */
    fun startPaymentPolling(paymentId: String, shopId: String) {
        pollingJob?.cancel()
        currentShopId = shopId

        pollingJob = viewModelScope.launch {
            _isPolling.value = true
            var pollCount = 0
            val maxAttempts = 60

            while (pollCount < maxAttempts) {
                delay(5000)
                pollCount++

                try {
                    val result = repository.findSubscriptionByPaymentId(paymentId)

                    when (result) {
                        is Resource.Success -> {
                            val checkResponse = result.data

                            when {
                                checkResponse?.isActive == true -> {
                                    _successMessage.value = "Payment completed successfully!"
                                    _paymentPollingStatus.value = Resource.Success(checkResponse)
                                    refreshAllData(shopId)
                                    pollCount = maxAttempts // stop loop
                                }
                                checkResponse?.isFailed == true || checkResponse?.isCancelled == true -> {
                                    _errorMessage.value = "Payment ${checkResponse.payment?.status}"
                                    _paymentPollingStatus.value = Resource.Success(checkResponse)
                                    pollCount = maxAttempts // stop loop
                                }
                                else -> {
                                    if (pollCount % 3 == 0) {
                                        _successMessage.value = "Waiting for payment confirmation..."
                                    }
                                    _paymentPollingStatus.value = Resource.Success(checkResponse)
                                }
                            }
                        }
                        is Resource.Error -> {
                            if (pollCount % 5 == 0) {
                                _errorMessage.value = result.message ?: "Checking payment status..."
                            }
                        }
                        is Resource.Loading -> {}
                    }
                } catch (e: Exception) {
                    if (pollCount % 5 == 0) {
                        _errorMessage.value = "Checking payment status..."
                    }
                }
            }

            if (pollCount >= maxAttempts) {
                _errorMessage.value = "Payment timeout. Please check your mobile money."
            }
            _isPolling.value = false
        }
    }

    fun stopPaymentPolling() {
        pollingJob?.cancel()
        _isPolling.value = false
    }

    fun checkShopSubscription(shopId: String) {
        viewModelScope.launch {
            _shopSubscriptionStatus.value = Resource.Loading

            try {
                val result = repository.checkShopSubscription(shopId)

                when (result) {
                    is Resource.Success -> {
                        val data = result.data
                        _shopSubscriptionStatus.value = Resource.Success(data)

                        data?.let { statusResponse ->
                            val prefs = PreferenceManager.getInstance(application.applicationContext)
                            val isActive = statusResponse.isActive

                            if (isActive) {
                                prefs.saveSubscriptionStatus(
                                    if (statusResponse.type == "trial") "TRIAL" else "ACTIVE"
                                )
                                statusResponse.subscriptionId?.let {
                                    prefs.saveSubscriptionId(it)
                                    prefs.saveCurrentShopUuid(it)
                                }
                                statusResponse.type?.let { prefs.saveSubscriptionType(it) }
                                statusResponse.expiryDate?.let { prefs.saveSubscriptionExpiry(it) }
                            } else {
                                prefs.saveSubscriptionStatus(statusResponse.status.uppercase())
                            }
                        }
                    }
                    is Resource.Error -> {
                        _shopSubscriptionStatus.value = Resource.Error(result.message)
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _shopSubscriptionStatus.value = Resource.Error(e.message ?: "Failed to check subscription")
                _errorMessage.value = e.message ?: "Failed to check subscription"
            }
        }
    }

    fun getShopActiveSubscription(shopId: String) {
        viewModelScope.launch {
            _currentSubscription.value = Resource.Loading

            try {
                val result = repository.getShopActiveSubscription(shopId)

                when (result) {
                    is Resource.Success -> {
                        result.data?.let { subscription ->
                            val prefs = PreferenceManager.getInstance(application.applicationContext)
                            prefs.saveSubscriptionStatus(
                                if (subscription.isTrial) "TRIAL" else subscription.effectiveStatus
                            )
                            prefs.saveSubscriptionId(subscription.id)
                            prefs.saveCurrentShopUuid(subscription.id)
                            prefs.saveSubscriptionType(subscription.packageType)
                            subscription.endDate?.let { prefs.saveSubscriptionExpiry(it) }
                            _currentSubscription.value = Resource.Success(subscription)
                        } ?: run {
                            _currentSubscription.value = Resource.Error("No active subscription found")
                        }
                    }
                    is Resource.Error -> {
                        _currentSubscription.value = Resource.Error(result.message)
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _currentSubscription.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun cancelSubscription(shopId: String, subscriptionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.cancelSubscription(shopId, subscriptionId)
                when (result) {
                    is Resource.Success -> {
                        _successMessage.value = "Subscription cancelled successfully"
                        getShopActiveSubscription(shopId)
                    }
                    is Resource.Error -> {
                        _errorMessage.value = result.message ?: "Failed to cancel subscription"
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to cancel subscription"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelPackage(shopId: String, subscriptionId: String, packageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.cancelPackage(shopId, subscriptionId, packageId)
                when (result) {
                    is Resource.Success -> {
                        _successMessage.value = "Package cancelled successfully"
                        getShopActiveSubscription(shopId)
                    }
                    is Resource.Error -> {
                        _errorMessage.value = result.message ?: "Failed to cancel package"
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to cancel package"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun renewSubscription(shopId: String, request: com.devbrian.osebo.models.RenewSubscriptionRequest) {
        viewModelScope.launch {
            _renewSubscriptionResult.value = Resource.Loading
            _isLoading.value = true
            try {
                val result = repository.renewSubscription(shopId, request)
                _renewSubscriptionResult.value = result
                when (result) {
                    is Resource.Success -> {
                        _successMessage.value = "Subscription renewed successfully"
                        result.data.paymentId?.let { paymentId ->
                            startPaymentPolling(paymentId, shopId)
                        } ?: getShopActiveSubscription(shopId)
                    }
                    is Resource.Error -> {
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _renewSubscriptionResult.value = Resource.Error(e.message ?: "Failed to renew subscription")
                _errorMessage.value = e.message ?: "Failed to renew subscription"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshAllData(shopId: String? = null) {
        loadSubscriptionPackages()
        shopId?.let { getShopActiveSubscription(it) }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPaymentPolling()
    }
}