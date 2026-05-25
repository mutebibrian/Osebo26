package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.FinanceRepository
import com.devbrian.osebo.data.remote.dto.request.CreateExpenseRequest
import com.devbrian.osebo.models.ExpenseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val financeRepository: FinanceRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AddExpenseViewModel"
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> = _success

    private val _expenseCategories = MutableLiveData<List<ExpenseCategory>>()
    val expenseCategories: LiveData<List<ExpenseCategory>> = _expenseCategories

    private var categoriesList: List<ExpenseCategory> = emptyList()
    private var selectedCategoryId: String? = null
    private var selectedPaymentMethod: String? = null
    private var selectedDate: String? = null
    private var expenseAmount: Double = 0.0
    private var expenseDescription: String = ""

    fun loadExpenseCategories() {
        Log.d(TAG, "loadExpenseCategories called")
        viewModelScope.launch {
            _isLoading.value = true
            val result: Result<List<ExpenseCategory>> = financeRepository.getExpenseCategories()
            result.onSuccess { categories: List<ExpenseCategory> ->
                Log.d(TAG, "Categories loaded: ${categories.size}")
                _expenseCategories.value = categories
                categoriesList = categories
            }.onFailure { exception: Throwable ->
                Log.e(TAG, "Failed to load categories", exception)
                _error.value = exception.message ?: "Failed to load categories"
            }
            _isLoading.value = false
        }
    }

    fun setCategoriesList(categories: List<ExpenseCategory>) {
        categoriesList = categories
    }

    fun selectCategory(position: Int) {
        selectedCategoryId = categoriesList.getOrNull(position)?.id
        Log.d(TAG, "Category selected: $selectedCategoryId at position $position")
    }

    fun setPaymentMethod(method: String) {
        selectedPaymentMethod = method
        Log.d(TAG, "Payment method set: $method")
    }

    fun setDate(date: String) {
        selectedDate = date
        Log.d(TAG, "Date set: $date")
    }

    fun setAmount(amount: Double) {
        expenseAmount = amount
        Log.d(TAG, "Amount set: $amount")
    }

    fun setDescription(description: String) {
        expenseDescription = description
        Log.d(TAG, "Description set: $description")
    }

    fun saveExpense() {
        Log.d(TAG, "========== SAVE EXPENSE ==========")
        Log.d(TAG, "Category ID: $selectedCategoryId")
        Log.d(TAG, "Amount: $expenseAmount")
        Log.d(TAG, "Description: $expenseDescription")
        Log.d(TAG, "Date: $selectedDate")
        Log.d(TAG, "Payment Method: $selectedPaymentMethod")

        if (selectedCategoryId == null) {
            Log.e(TAG, "No category selected")
            _error.value = "Please select a category"
            return
        }

        if (expenseAmount <= 0) {
            Log.e(TAG, "Invalid amount")
            _error.value = "Please enter a valid amount"
            return
        }

        if (expenseDescription.isEmpty()) {
            Log.e(TAG, "No description")
            _error.value = "Please enter a description"
            return
        }

        if (selectedDate.isNullOrEmpty()) {
            Log.e(TAG, "No date selected")
            _error.value = "Please select a date"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            val request = CreateExpenseRequest(
                name = expenseDescription.take(100),  // ADD name field
                description = expenseDescription,
                amount = expenseAmount,
                expenseCategoryId = selectedCategoryId!!,  // CHANGED from categoryId
                date = selectedDate!!,
                paymentMethod = selectedPaymentMethod,
                notes = null,
                receiptUrl = null,
                reference = null
            )

            val result = financeRepository.createExpense(request)
            result.onSuccess { expense ->
                Log.d(TAG, "Success! Expense created: ${expense.id}")
                _success.value = true
            }.onFailure { exception ->
                Log.e(TAG, "Failed to create expense", exception)
                _error.value = exception.message ?: "Failed to save expense"
            }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}