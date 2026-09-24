package com.devbrian.osebo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.ShopRepository
import com.devbrian.osebo.models.PaymentHistory
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.utils.Resource
import kotlinx.coroutines.launch

class ShopViewModel(
    private val repository: ShopRepository
) : ViewModel() {

    private val _shops = MutableLiveData<Resource<List<Shop>>>()
    val shops: LiveData<Resource<List<Shop>>> = _shops

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadShops() {
        viewModelScope.launch {
            val existingShops = (_shops.value as? Resource.Success)?.data.orEmpty()
            if (existingShops.isEmpty()) {
                _shops.value = Resource.Loading
            }
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val cachedResult = repository.getShops()
                val cachedShops = (cachedResult as? Resource.Success)?.data.orEmpty()
                if (cachedShops.isNotEmpty()) {
                    _shops.value = Resource.Success(cachedShops)
                }

                val refreshResult = repository.refreshShops()
                val updatedResult = repository.getShops()
                val updatedShops = (updatedResult as? Resource.Success)?.data.orEmpty()

                when {
                    updatedResult is Resource.Success -> {
                        _shops.value = Resource.Success(updatedShops)
                        if (refreshResult is Resource.Error && updatedShops.isEmpty()) {
                            _errorMessage.value = refreshResult.message
                        }
                    }
                    cachedShops.isNotEmpty() -> _shops.value = Resource.Success(cachedShops)
                    refreshResult is Resource.Error -> {
                        _shops.value = Resource.Error(refreshResult.message)
                        _errorMessage.value = refreshResult.message
                    }
                    else -> {
                        _shops.value = Resource.Error("Failed to load shops")
                        _errorMessage.value = "Failed to load shops"
                    }
                }
            } catch (e: Exception) {
                if (existingShops.isEmpty()) {
                    _shops.value = Resource.Error(e.message ?: "Failed to load shops")
                }
                _errorMessage.value = e.message ?: "Failed to load shops"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteShop(shopId: String) {
        viewModelScope.launch {
            try {
                repository.deleteShop(shopId)
                loadShops()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete shop: ${e.message}"
            }
        }
    }

    
    private val _paymentHistory = MutableLiveData<Resource<List<PaymentHistory>>>()
    val paymentHistory: LiveData<Resource<List<PaymentHistory>>> = _paymentHistory

    
    fun getPaymentHistory(shopId: String, subscriptionId: String) {
        viewModelScope.launch {
            _paymentHistory.value = Resource.Loading

            try {
            
              
            } catch (e: Exception) {
                _paymentHistory.value = Resource.Error(e.message ?: "Failed to load payment history")
            }
        }
    }
}

