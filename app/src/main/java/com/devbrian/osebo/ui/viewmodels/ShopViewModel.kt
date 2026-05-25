package com.devbrian.osebo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.ShopRepository
import com.devbrian.osebo.models.PaymentHistory
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
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
            _shops.value = Resource.Loading
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.getShops()
                _shops.value = result

                if (result is Resource.Error) {
                    _errorMessage.value = result.message
                }
            } catch (e: Exception) {
                _shops.value = Resource.Error(e.message ?: "Failed to load shops")
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


