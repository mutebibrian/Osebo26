package com.devbrian.osebo.ui.viewmodels



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.FinanceRepository
import com.devbrian.osebo.models.Transaction
import com.devbrian.osebo.models.TransactionFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _filteredTransactions = MutableLiveData<List<Transaction>>()
    val filteredTransactions: LiveData<List<Transaction>> = _filteredTransactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentFilters: TransactionFilter? = null

    fun loadTransactions() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getAllTransactions()
            result.onSuccess { transactions ->
                _transactions.value = transactions
                applyCurrentFilters()
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load transactions"
            }
            _isLoading.value = false
        }
    }

    fun refreshTransactions() {
        loadTransactions()
    }

    fun applyFilters(filters: TransactionFilter) {
        currentFilters = filters
        applyCurrentFilters()
    }

    private fun applyCurrentFilters() {
        val allTransactions = _transactions.value ?: return
        val filtered = if (currentFilters != null) {
            filterTransactions(allTransactions, currentFilters!!)
        } else {
            allTransactions
        }
        _filteredTransactions.value = filtered
    }

    private fun filterTransactions(transactions: List<Transaction>, filters: TransactionFilter): List<Transaction> {
        var result = transactions

        filters.type?.let { type ->
            result = result.filter { it.type == type }
        }

        filters.startDate?.let { startDate ->
            result = result.filter { it.date >= startDate }
        }

        filters.endDate?.let { endDate ->
            result = result.filter { it.date <= endDate }
        }

        filters.minAmount?.let { min ->
            result = result.filter { it.amount >= min }
        }

        filters.maxAmount?.let { max ->
            result = result.filter { it.amount <= max }
        }

        filters.category?.let { category ->
            result = result.filter { it.category == category }
        }

        filters.searchQuery?.let { query ->
            result = result.filter {
                it.description.contains(query, ignoreCase = true) ||
                        it.id.contains(query, ignoreCase = true)
            }
        }

        return result
    }

    fun deleteTransaction(transactionId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            
            
            val currentList = _transactions.value?.toMutableList() ?: return@launch
            currentList.removeAll { it.id == transactionId }
            _transactions.value = currentList
            applyCurrentFilters()
            _isLoading.value = false
        }
    }

    fun exportTransactions() {
        
    }

    fun clearError() {
        _error.value = null
    }
}
