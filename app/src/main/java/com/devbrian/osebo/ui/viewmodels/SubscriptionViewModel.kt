package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.R
import com.devbrian.osebo.data.remote.dto.request.CheckPaymentStatusRequest
import com.devbrian.osebo.data.remote.dto.request.CreateSubscriptionRequest
import com.devbrian.osebo.data.remote.dto.request.InitiatePaymentRequest
import com.devbrian.osebo.data.remote.dto.response.InitiatePaymentResponse
import com.devbrian.osebo.data.remote.dto.response.PaymentPollResponse
import com.devbrian.osebo.data.remote.dto.response.PaymentStatusResponse
import com.devbrian.osebo.data.remote.dto.response.ShopSubscriptionStatusResponse
import com.devbrian.osebo.data.remote.dto.response.SubscriptionResponse
import com.devbrian.osebo.data.repository.SubscriptionRepository
import com.devbrian.osebo.models.Payment
import com.devbrian.osebo.models.RenewSubscriptionRequest
import com.devbrian.osebo.models.Shop
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
    private val repository: SubscriptionRepository
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

    private val _paymentPollingStatus = MutableLiveData<Resource<PaymentPollResponse?>>()
    val paymentPollingStatus: LiveData<Resource<PaymentPollResponse?>> = _paymentPollingStatus

    private val _shopSubscriptionStatus = MutableLiveData<Resource<ShopSubscriptionStatusResponse?>>()
    val shopSubscriptionStatus: LiveData<Resource<ShopSubscriptionStatusResponse?>> = _shopSubscriptionStatus

    private val _paymentHistory = MutableLiveData<Resource<List<Payment>?>>()
    val paymentHistory: LiveData<Resource<List<Payment>?>> = _paymentHistory

    private val _cancelSubscriptionResult = MutableLiveData<Resource<Unit>>()
    val cancelSubscriptionResult: LiveData<Resource<Unit>> = _cancelSubscriptionResult

    private val _renewSubscriptionResult = MutableLiveData<Resource<SubscriptionResponse>>()
    val renewSubscriptionResult: LiveData<Resource<SubscriptionResponse>> = _renewSubscriptionResult

    private val _activateTrialResult = MutableLiveData<Resource<SubscriptionResponse>>()
    val activateTrialResult: LiveData<Resource<SubscriptionResponse>> = _activateTrialResult

    private val _initiatePaymentResult = MutableLiveData<Resource<InitiatePaymentResponse>>()
    val initiatePaymentResult: LiveData<Resource<InitiatePaymentResponse>> = _initiatePaymentResult


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

    private val _paymentStatus = MutableLiveData<Resource<PaymentStatusResponse?>>()
    val paymentStatus: LiveData<Resource<PaymentStatusResponse?>> = _paymentStatus

    fun checkPaymentStatus(paymentId: String) {
        viewModelScope.launch {
            _paymentStatus.value = Resource.Loading

            try {
                val result = repository.getPaymentStatus(currentShopId ?: "", paymentId)

                when (result) {
                    is Resource.Success -> {
                        _paymentStatus.value = Resource.Success(result.data)
                    }
                    is Resource.Error -> {
                        _paymentStatus.value = Resource.Error(result.message)
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _paymentStatus.value = Resource.Error(e.message ?: "Failed to check payment status")
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


    fun createSubscription(
        shopId: String,
        packageId: String,
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
                    packageId = packageId,
                    customerPhone = phoneNumber,
                    duration = months,
                    currency = "UGX"
                )

                val result = repository.createSubscription(shopId, request)

                when (result) {
                    is Resource.Success -> {

                        val response = result.data as? SubscriptionResponse

                        if (response != null) {

                            _subscriptionResult.value = Resource.success(response)
                            _createSubscriptionResult.value = Resource.success(response)

                            if (response.success) {
                                _successMessage.value = "Subscription initiated successfully"

                                val paymentId = response.data?.paymentId
                                if (!paymentId.isNullOrBlank()) {
                                    startPaymentPolling(paymentId, shopId)
                                } else {
                                    _successMessage.value = "Subscription created successfully"
                                    refreshAllData(shopId)
                                }
                            } else {
                                _errorMessage.value = response.message ?: "Failed to create subscription"
                            }
                        } else {
                            _errorMessage.value = "Invalid response format"
                            _subscriptionResult.value = Resource.error("Invalid response format")
                            _createSubscriptionResult.value = Resource.error("Invalid response format")
                        }
                    }
                    is Resource.Error -> {
                        _subscriptionResult.value = Resource.error(result.message)
                        _createSubscriptionResult.value = Resource.error(result.message)
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _subscriptionResult.value = Resource.error(e.message ?: "Failed to create subscription")
                _createSubscriptionResult.value = Resource.error(e.message ?: "Failed to create subscription")
                _errorMessage.value = e.message ?: "Failed to create subscription"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getPaymentHistory(shopId: String, subscriptionId: String) {
        viewModelScope.launch {
            _paymentHistory.value = Resource.Loading

            try {
                val result = repository.getPaymentHistory(shopId, subscriptionId)
                _paymentHistory.value = result
            } catch (e: Exception) {
                _paymentHistory.value = Resource.Error(e.message ?: "Failed to load payment history")
            }
        }
    }

    fun initiatePayment(
        shopId: String,
        amount: Double,
        currency: String,
        phoneNumber: String,
        provider: String,
        packageId: String,
        months: Int
    ) {
        viewModelScope.launch {
            _initiatePaymentResult.value = Resource.Loading
            _isLoading.value = true
            currentShopId = shopId

            try {
                val request = InitiatePaymentRequest(
                    amount = amount,
                    currency = currency,
                    phoneNumber = phoneNumber,
                    provider = provider,
                    shopId = shopId,
                    packageId = packageId,
                    months = months,
                    metadata = mapOf(
                        "app" to "Osebo Android",
                        "version" to "1.0.0"
                    )
                )

                val result = repository.initiatePayment(shopId, request)

                when (result) {
                    is Resource.Success -> {

                        val response = result.data as? InitiatePaymentResponse

                        if (response != null) {

                            _initiatePaymentResult.value = Resource.success(response)


                            val paymentId = response.data?.paymentId
                            if (!paymentId.isNullOrBlank()) {
                                _successMessage.value = "Payment initiated. Check your phone."
                                startPaymentPolling(paymentId, shopId)
                            } else {
                                _errorMessage.value = "No payment ID received"
                            }
                        } else {
                            _errorMessage.value = "Invalid response format"
                            _initiatePaymentResult.value = Resource.error("Invalid response format")
                        }
                    }
                    is Resource.Error -> {
                        _initiatePaymentResult.value = Resource.error(result.message)
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Failed to initiate payment"
                _initiatePaymentResult.value = Resource.error(errorMsg)
                _errorMessage.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }




    // Update startPaymentPolling to use payment endpoints
    fun startPaymentPolling(paymentId: String, shopId: String) {
        pollingJob?.cancel()
        currentShopId = shopId

        pollingJob = viewModelScope.launch {
            _isPolling.value = true
            var pollCount = 0
            val maxAttempts = 60 // 60 * 5 seconds = 5 minutes

            while (pollCount < maxAttempts) {
                delay(5000) // Poll every 5 seconds
                pollCount++

                try {
                    // Use the payment endpoint to check status
                    val result = repository.getPaymentStatus(shopId, paymentId)

                    when (result) {
                        is Resource.Success -> {
                            val response = result.data

                            if (response?.success == true) {
                                val paymentData = response.data
                                val paymentStatus = paymentData?.status?.lowercase()

                                when (paymentStatus) {
                                    "completed", "success" -> {
                                        _successMessage.value = "Payment completed successfully!"

                                        val pollResponse = PaymentPollResponse(
                                            success = true,
                                            message = "Payment completed",
                                            status = "completed",
                                            nextPollSeconds = 0,
                                            transactionId = paymentId,
                                            amount = paymentData?.amount,
                                            currency = paymentData?.currency,
                                            paymentMethod = paymentData?.paymentMethod,
                                            paymentDate = paymentData?.paidAt,
                                            data = null
                                        )

                                        _paymentPollingStatus.value = Resource.success(pollResponse)
                                        refreshAllData(shopId)
                                        break
                                    }
                                    "failed", "cancelled" -> {
                                        _errorMessage.value = "Payment $paymentStatus"

                                        val pollResponse = PaymentPollResponse(
                                            success = false,
                                            message = "Payment $paymentStatus",
                                            status = paymentStatus ?: "failed",
                                            nextPollSeconds = 0,
                                            transactionId = paymentId,
                                            amount = null,
                                            currency = null,
                                            paymentMethod = null,
                                            paymentDate = null,
                                            data = null
                                        )

                                        _paymentPollingStatus.value = Resource.success(pollResponse)
                                        break
                                    }
                                    "pending" -> {
                                        // Still pending, continue polling
                                        if (pollCount % 3 == 0) {
                                            _successMessage.value = "Waiting for payment confirmation..."
                                        }

                                        // Update polling status with pending state
                                        val pollResponse = PaymentPollResponse(
                                            success = true,
                                            message = "Payment pending",
                                            status = "pending",
                                            nextPollSeconds = 5,
                                            transactionId = paymentId,
                                            amount = paymentData?.amount,
                                            currency = paymentData?.currency,
                                            paymentMethod = paymentData?.paymentMethod,
                                            paymentDate = null,
                                            data = null
                                        )

                                        _paymentPollingStatus.value = Resource.success(pollResponse)
                                    }
                                    else -> {
                                        if (pollCount % 3 == 0) {
                                            _successMessage.value = "Waiting for payment confirmation..."
                                        }
                                    }
                                }
                            } else {
                                if (pollCount % 5 == 0) {
                                    _errorMessage.value = response?.message ?: "Checking payment status..."
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

                val timeoutResponse = PaymentPollResponse(
                    success = false,
                    message = "Payment timeout",
                    status = "timeout",
                    nextPollSeconds = 0,
                    transactionId = paymentId,
                    amount = null,
                    currency = null,
                    paymentMethod = null,
                    paymentDate = null,
                    data = null
                )

                _paymentPollingStatus.value = Resource.success(timeoutResponse)
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
                        _shopSubscriptionStatus.value = Resource.Success(result.data)
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
                            // Log the subscription data to verify
                            println("✅ Active subscription loaded: ${subscription.id}")
                            println("✅ Status: ${subscription.status}")
                            println("✅ Package: ${subscription.packageType}")
                            println("✅ End date: ${subscription.endDate}")
                            println("✅ Is trial: ${subscription.isTrial}")

                            _currentSubscription.value = Resource.Success(subscription)
                        } ?: run {
                            println("⚠️ No active subscription found")
                            _currentSubscription.value = Resource.Error("No active subscription found")
                        }
                    }
                    is Resource.Error -> {
                        println("❌ Error loading subscription: ${result.message}")
                        _currentSubscription.value = Resource.Error(result.message ?: "Failed to load subscription")
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                println("❌ Exception: ${e.message}")
                _currentSubscription.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }


    fun getSubscriptionDetails(shopId: String, subscriptionId: String) {
        viewModelScope.launch {
            _subscriptionDetails.value = Resource.Loading

            try {
                val result = repository.getSubscriptionDetails(shopId, subscriptionId)

                when (result) {
                    is Resource.Success -> {
                        _subscriptionDetails.value = Resource.Success(result.data)
                    }
                    is Resource.Error -> {
                        _subscriptionDetails.value = Resource.Error(result.message)
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _subscriptionDetails.value = Resource.Error(e.message ?: "Failed to load details")
            }
        }
    }


    fun renewSubscription(
        shopId: String,
        subscriptionId: String,
        phoneNumber: String,
        months: Int = 1
    ) {
        viewModelScope.launch {
            _renewSubscriptionResult.value = Resource.Loading
            _isLoading.value = true
            currentShopId = shopId

            try {
                val request = RenewSubscriptionRequest(
                    phoneNumber = phoneNumber,
                    months = months,
                    autoRenew = false
                )

                val result = repository.renewSubscription(shopId, subscriptionId, request)

                when (result) {
                    is Resource.Success -> {
                        val response = result.data
                        _renewSubscriptionResult.value = Resource.Success(response)

                        if (response.success) {
                            _successMessage.value = "Subscription renewed successfully"


                            val paymentId = response.data?.paymentId
                            if (!paymentId.isNullOrBlank()) {
                                startPaymentPolling(paymentId, shopId)
                            }
                        } else {
                            _errorMessage.value = response.message ?: "Failed to renew subscription"
                        }
                    }
                    is Resource.Error -> {
                        _renewSubscriptionResult.value = Resource.Error(result.message)
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _renewSubscriptionResult.value = Resource.Error(e.message ?: "Failed to renew")
                _errorMessage.value = e.message ?: "Failed to renew"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun cancelSubscription(shopId: String, subscriptionId: String) {
        viewModelScope.launch {
            _cancelSubscriptionResult.value = Resource.Loading
            _isLoading.value = true

            try {
                val result = repository.cancelSubscription(shopId, subscriptionId)

                when (result) {
                    is Resource.Success -> {
                        _cancelSubscriptionResult.value = Resource.Success(Unit)
                        _successMessage.value = "Subscription cancelled successfully"
                        getSubscriptionDetails(shopId, subscriptionId)
                    }
                    is Resource.Error -> {
                        _cancelSubscriptionResult.value = Resource.Error(result.message)
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _cancelSubscriptionResult.value = Resource.Error(e.message ?: "Failed to cancel")
                _errorMessage.value = e.message ?: "Failed to cancel"
            } finally {
                _isLoading.value = false
            }
        }
    }





    fun activateFreeTrial(shopId: String, packageId: String) {
        viewModelScope.launch {
            _activateTrialResult.value = Resource.Loading
            _isLoading.value = true

            try {
                val result = repository.activateFreeTrial(shopId, packageId)

                when (result) {
                    is Resource.Success -> {
                        val response = result.data
                        _activateTrialResult.value = Resource.Success(response)

                        if (response.success) {
                            _successMessage.value = "Free trial activated successfully"
                            getShopActiveSubscription(shopId)
                        } else {
                            _errorMessage.value = response.message ?: "Failed to activate trial"
                        }
                    }
                    is Resource.Error -> {
                        _activateTrialResult.value = Resource.Error(result.message)
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> {}
                }
            } catch (e: Exception) {
                _activateTrialResult.value = Resource.Error(e.message ?: "Failed to activate trial")
                _errorMessage.value = e.message ?: "Failed to activate trial"
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

    fun getPackageById(packageId: String): SubscriptionPackage? {
        val value = _subscriptionPackages.value
        return if (value is Resource.Success) {
            value.data.find { it.id == packageId }
        } else {
            null
        }
    }

    fun getPackageByTier(tier: String): SubscriptionPackage? {
        val value = _subscriptionPackages.value
        return if (value is Resource.Success) {
            value.data.find {
                it.tier.equals(tier, ignoreCase = true)
            }
        } else {
            null
        }
    }


    data class SubscriptionStatusUi(
        val status: String,
        val displayText: String,
        val colorRes: Int,
        val iconRes: Int,
        val canActivate: Boolean = false
    )

    fun getShopSubscriptionStatus(shop: Shop?): SubscriptionStatusUi {
        return when (shop?.subscriptionStatus?.lowercase()) {
            "active" -> SubscriptionStatusUi(
                status = "Active",
                displayText = "Active",
                colorRes = R.color.green_500,
                iconRes = R.drawable.ic_check_circle,
                canActivate = false
            )
            "trial" -> SubscriptionStatusUi(
                status = "Trial",
                displayText = "Trial",
                colorRes = R.color.blue_500,
                iconRes = R.drawable.ic_trial,
                canActivate = true
            )
            "expired" -> SubscriptionStatusUi(
                status = "Expired",
                displayText = "Expired",
                colorRes = R.color.red_500,
                iconRes = R.drawable.ic_expired,
                canActivate = true
            )
            "pending" -> SubscriptionStatusUi(
                status = "Pending",
                displayText = "Pending",
                colorRes = R.color.yellow_500,
                iconRes = R.drawable.ic_pending,
                canActivate = false
            )
            else -> SubscriptionStatusUi(
                status = "Inactive",
                displayText = "Inactive",
                colorRes = R.color.gray_500,
                iconRes = R.drawable.ic_inactive,
                canActivate = true
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPaymentPolling()
    }
}

