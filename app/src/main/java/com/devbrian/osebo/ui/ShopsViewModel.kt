package com.devbrian.osebo.ui


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.ShopRepository

import com.devbrian.osebo.models.Shop
import kotlinx.coroutines.launch

class ShopsViewModel(private val repository: ShopRepository) : ViewModel() {

    private val _shops = MutableLiveData<List<Shop>>()
    val shops: LiveData<List<Shop>> = _shops

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadShops() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val shopsList = repository.getShops()
                _shops.value = shopsList
            } catch (e: Exception) {
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
                // Reload shops after deletion
                loadShops()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete shop"
            }
        }
    }
}