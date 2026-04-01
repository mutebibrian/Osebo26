package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.repository.FinanceRepository
import com.devbrian.osebo.models.Expense
import com.devbrian.osebo.models.ExpenseCategory
import com.devbrian.osebo.models.FinancialStatement
import com.devbrian.osebo.models.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    // Transactions
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    // Expenses
    private val _expenses = MutableLiveData<List<Expense>>()
    val expenses: LiveData<List<Expense>> = _expenses

    // Expense Categories
    private val _expenseCategories = MutableLiveData<List<ExpenseCategory>>()
    val expenseCategories: LiveData<List<ExpenseCategory>> = _expenseCategories

    // Financial Statement
    private val _financialStatement = MutableLiveData<FinancialStatement>()
    val financialStatement: LiveData<FinancialStatement> = _financialStatement

    // Shop Info
    private val _currentShopName = MutableLiveData<String>()
    val currentShopName: LiveData<String> = _currentShopName

    // Loading and Error states
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadCurrentShopInfo()
    }

    private fun loadCurrentShopInfo() {
        val shopName = preferenceManager.getCurrentShopName()
        val shopId = preferenceManager.getShopIdentifierForApi()

        _currentShopName.value = if (shopName.isNotEmpty()) shopName else "My Shop"

        if (shopId.isEmpty()) {
            _error.value = "No shop selected. Please select a shop first."
        }
    }

    fun loadTransactions(startDate: String? = null, endDate: String? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getAllTransactions(startDate, endDate)
            result.onSuccess { transactions ->
                _transactions.value = transactions
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load transactions"
            }
            _isLoading.value = false
        }
    }

    fun loadExpenses() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getExpenses()
            result.onSuccess { expenses ->
                _expenses.value = expenses
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load expenses"
            }
            _isLoading.value = false
        }
    }

    fun loadExpenseCategories() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getExpenseCategories()
            result.onSuccess { categories ->
                _expenseCategories.value = categories
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load categories"
            }
            _isLoading.value = false
        }
    }

    fun loadFinancialStatement(period: String = "monthly") {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getFinancialStatement(period = period)
            result.onSuccess { statement ->
                _financialStatement.value = statement
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load financial statement"
            }
            _isLoading.value = false
        }
    }

    fun deleteExpense(expenseId: String, onComplete: (Boolean) -> Unit) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.deleteExpense(expenseId)
            result.onSuccess {
                loadExpenses()
                loadTransactions()
                onComplete(true)
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to delete expense"
                onComplete(false)
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun getCurrentShopName(): String {
        return _currentShopName.value ?: preferenceManager.getCurrentShopName()
    }

    fun hasShopSelected(): Boolean {
        return preferenceManager.hasCurrentShop()
    }
}