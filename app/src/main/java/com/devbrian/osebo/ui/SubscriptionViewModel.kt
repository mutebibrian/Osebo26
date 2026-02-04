package com.devbrian.osebo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.SubscriptionRepository
import com.devbrian.osebo.domain.model.Subscription
import com.devbrian.osebo.domain.model.SubscriptionPlan as DomainSubscriptionPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _currentSubscription = MutableLiveData<Subscription?>()
    val currentSubscription: LiveData<Subscription?> get() = _currentSubscription

    private val _availablePlans = MutableLiveData<List<DomainSubscriptionPlan>>()
    val availablePlans: LiveData<List<DomainSubscriptionPlan>> get() = _availablePlans

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _success = MutableLiveData(false)
    val success: LiveData<Boolean> get() = _success

    init {
        loadSubscriptionData()
    }

    fun loadSubscriptionData() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val subscription = subscriptionRepository.getCurrentSubscription()
                _currentSubscription.value = subscription

                val plans = subscriptionRepository.getSubscriptionPlans()
                _availablePlans.value = plans

                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load subscription data"
            } finally {
                _loading.value = false
            }
        }
    }

    fun subscribeToPlan(plan: DomainSubscriptionPlan) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val subscription = subscriptionRepository.subscribeToPlan(
                    planId = plan.id,
                    paymentMethod = "mobile_money"
                )
                _currentSubscription.value = subscription
                _success.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to subscribe to plan"
                _success.value = false
            } finally {
                _loading.value = false
            }
        }
    }

    fun removeSubscription() {
        viewModelScope.launch {
            _loading.value = true
            try {
                subscriptionRepository.cancelSubscription()
                _currentSubscription.value = null
                _success.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to remove subscription"
                _success.value = false
            } finally {
                _loading.value = false
            }
        }
    }

    fun refreshData() {
        loadSubscriptionData()
    }
}
