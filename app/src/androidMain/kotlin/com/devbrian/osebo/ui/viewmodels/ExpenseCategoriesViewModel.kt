package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.remote.dto.request.UpdateExpenseCategoryRequest
import com.devbrian.osebo.data.repository.FinanceRepository
import com.devbrian.osebo.models.ExpenseCategory
import kotlinx.coroutines.launch

class ExpenseCategoriesViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _categories = MutableLiveData<List<ExpenseCategory>>()
    val categories: LiveData<List<ExpenseCategory>> = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    fun loadCategories() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getExpenseCategories()
            result.onSuccess { categories ->
                _categories.value = categories
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load categories"
            }
            _isLoading.value = false
        }
    }

    fun createCategory(name: String, description: String?) {
        _isLoading.value = true
        viewModelScope.launch {
            val request = com.devbrian.osebo.data.remote.dto.request.CreateExpenseCategoryRequest(
                name = name,
                description = description
            )
            val result = repository.createExpenseCategory(request)
            result.onSuccess { category ->
                _successMessage.value = "Category created successfully"
                loadCategories()
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to create category"
            }
            _isLoading.value = false
        }
    }

    fun updateCategory(category: ExpenseCategory) {
        _isLoading.value = true
        viewModelScope.launch {
            val request = UpdateExpenseCategoryRequest(
                name = category.name,
                description = category.description
            )
            val result = repository.updateExpenseCategory(category.id, request)
            result.onSuccess {
                _successMessage.value = "Category updated successfully"
                loadCategories()
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to update category"
            }
            _isLoading.value = false
        }
    }

    fun deleteCategory(categoryId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.deleteExpenseCategory(categoryId)
            result.onSuccess {
                _successMessage.value = "Category deleted successfully"
                loadCategories()
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to delete category"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
