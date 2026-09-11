package com.devbrian.osebo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.AccountRepository
import com.devbrian.osebo.domain.model.Account
import kotlinx.coroutines.launch

class AccountViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _accountData = MutableLiveData<Account>()
    val accountData: LiveData<Account> get() = _accountData

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> get() = _updateSuccess

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    init {
        loadAccountData()
    }

    fun loadAccountData() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val account = accountRepository.getAccountDetails()
                _accountData.value = account
                _loading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load account data"
                _loading.value = false
            }
        }
    }

    fun updateTwoFactorAuth(enabled: Boolean) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val updatedAccount = accountRepository.updateTwoFactorAuth(enabled)
                _accountData.value = updatedAccount
                _updateSuccess.value = true
                _loading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update two-factor authentication"
                _loading.value = false
            }
        }
    }

    fun updateLoginNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val updatedAccount = accountRepository.updateLoginNotifications(enabled)
                _accountData.value = updatedAccount
                _updateSuccess.value = true
                _loading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update login notifications"
                _loading.value = false
            }
        }
    }

    fun deactivateAccount() {
        viewModelScope.launch {
            _loading.value = true
            try {
                accountRepository.deactivateAccount()
                _updateSuccess.value = true
                _loading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to deactivate account"
                _loading.value = false
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _loading.value = true
            try {
                accountRepository.deleteAccount()
                _updateSuccess.value = true
                _loading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete account"
                _loading.value = false
            }
        }
    }
}


