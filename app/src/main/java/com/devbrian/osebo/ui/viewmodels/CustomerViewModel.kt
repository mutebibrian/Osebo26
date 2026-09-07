package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.CustomerRepository
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _customers = MutableLiveData<List<Customer>>(emptyList())
    val customers: LiveData<List<Customer>> = _customers

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    init {
        observeCustomers()
        refreshCustomers()
    }

    private fun observeCustomers() {
        viewModelScope.launch {
            repository.getAllCustomers()
                .catch { e ->
                    _errorMessage.postValue("Error loading customers: ${e.message}")
                }
                .collectLatest { customers ->
                    _customers.postValue(customers)
                }
        }
    }

    fun searchCustomers(query: String) {
        viewModelScope.launch {
            repository.searchCustomers(query)
                .catch { e ->
                    _errorMessage.postValue("Error searching customers: ${e.message}")
                }
                .collectLatest { customers ->
                    _customers.postValue(customers)
                }
        }
    }

    fun createCustomer(name: String, phone: String, email: String? = null, location: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createCustomer(name, phone, email, location)
            when (result) {
                is Resource.Success -> {
                    _successMessage.value = "Customer added successfully"
                    
                    refreshCustomers()
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun refreshCustomers() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.refreshCustomers()
            if (result is Resource.Error) {
                _errorMessage.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}


