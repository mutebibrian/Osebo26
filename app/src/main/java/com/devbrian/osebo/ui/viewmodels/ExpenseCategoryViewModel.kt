package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.FinanceRepository
import com.devbrian.osebo.data.remote.dto.request.CreateExpenseCategoryRequest
import com.devbrian.osebo.models.ExpenseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseCategoryViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _categories = MutableLiveData<List<ExpenseCategory>>()
    val categories: LiveData<List<ExpenseCategory>> = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var allCategories = listOf<ExpenseCategory>()

    fun loadCategories() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getExpenseCategories()
            result.onSuccess { categories ->
                allCategories = categories
                _categories.value = categories
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load categories"
            }
            _isLoading.value = false
        }
    }

    fun filterCategories(query: String) {
        if (query.isEmpty()) {
            _categories.value = allCategories
        } else {
            val filtered = allCategories.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
            _categories.value = filtered
        }
    }

    fun createCategory(name: String, description: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val request = CreateExpenseCategoryRequest(name, description)
            val result = repository.createExpenseCategory(request)
            result.onSuccess {
                loadCategories() // Refresh list
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to create category"
            }
            _isLoading.value = false
        }
    }

    fun updateCategory(id: String, name: String, description: String) {
        _isLoading.value = true
        viewModelScope.launch {
            // Update locally first
            val updatedList = allCategories.map { category ->
                if (category.id == id) {
                    category.copy(name = name, description = description)
                } else {
                    category
                }
            }
            allCategories = updatedList
            _categories.value = allCategories
            _error.value = null
            _isLoading.value = false
        }
    }

    fun deleteCategory(id: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.deleteExpenseCategory(id)
            result.onSuccess {
                loadCategories() // Refresh list
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to delete category"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}