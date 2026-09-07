package com.devbrian.osebo.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.response.SalesComparisonDto
import com.devbrian.osebo.data.remote.dto.response.ShopSummaryDto
import com.devbrian.osebo.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _salesComparison = MutableLiveData<SalesComparisonDto?>()
    val salesComparison: LiveData<SalesComparisonDto?> = _salesComparison

    private val _shopSummary = MutableLiveData<ShopSummaryDto?>()
    val shopSummary: LiveData<ShopSummaryDto?> = _shopSummary

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    companion object {
        private const val TAG = "ReportsViewModel"
    }

    fun loadSalesComparison(period: String = "monthly") {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val shopId = preferenceManager.getShopIdentifierForApi()
                Log.d(TAG, "loadSalesComparison - Period: $period, ShopId: $shopId")

                if (shopId.isEmpty()) {
                    Log.e(TAG, "No shop selected! Cannot load sales comparison")
                    _error.value = "No shop selected. Please select a shop in Settings."
                    _isLoading.value = false
                    return@launch
                }

                val result = repository.getSalesComparison(period)

                result.onSuccess { data ->
                    // These properties should work as they're computed properties in the DTO
                    Log.d(TAG, "Sales comparison loaded successfully: Total Sales=${data.totalSales}, Growth=${data.growthPercentage}%")
                    Log.d(TAG, "Details - Current: ${data.currentSales}, Previous: ${data.previousSales}, Difference: ${data.difference}")
                    _salesComparison.value = data
                    _error.value = null
                }.onFailure { exception ->
                    Log.e(TAG, "Failed to load sales comparison", exception)
                    _error.value = exception.message ?: "Failed to load sales data"
                    _salesComparison.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadSalesComparison", e)
                _error.value = e.message ?: "Unexpected error loading sales data"
                _salesComparison.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadShopSummary() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val shopId = preferenceManager.getShopIdentifierForApi()
                Log.d(TAG, "loadShopSummary - ShopId: $shopId")

                if (shopId.isEmpty()) {
                    Log.e(TAG, "No shop selected! Cannot load shop summary")
                    _error.value = "No shop selected. Please select a shop in Settings."
                    _isLoading.value = false
                    return@launch
                }

                val result = repository.getShopSummary()

                result.onSuccess { data ->
                    Log.d(TAG, "Shop summary loaded successfully: Total Sales=${data.totalSales}, Today Sales=${data.todaySales}, Today Expenses=${data.todayExpenses}")
                    Log.d(TAG, "Revenue=${data.totalRevenue}, Profit=${data.netProfit}")
                    _shopSummary.value = data
                    _error.value = null
                }.onFailure { exception ->
                    Log.e(TAG, "Failed to load shop summary", exception)
                    _error.value = exception.message ?: "Failed to load shop summary"
                    _shopSummary.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadShopSummary", e)
                _error.value = e.message ?: "Unexpected error loading shop summary"
                _shopSummary.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}