package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.repository.FinanceRepository
import com.devbrian.osebo.models.FinancialStatement
import kotlinx.coroutines.launch

class FinanceSettingsViewModel(
    private val financeRepository: FinanceRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _statementReady = MutableLiveData<FinancialStatement?>()
    val statementReady: LiveData<FinancialStatement?> = _statementReady

    fun getCurrentShopName(): String = preferenceManager.getCurrentShopName().ifEmpty { "My Shop" }

    fun loadStatementForExport(startDate: String?, endDate: String?, period: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = financeRepository.getFinancialStatement(startDate, endDate, period)
            result.onSuccess { statement ->
                _statementReady.value = statement
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load financial statement"
            }
            _isLoading.value = false
        }
    }

    fun consumeStatementReady() {
        _statementReady.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
