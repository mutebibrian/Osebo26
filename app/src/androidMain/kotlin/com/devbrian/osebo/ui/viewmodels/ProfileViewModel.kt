package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.UpdateProfileRequest
import com.devbrian.osebo.data.remote.dto.response.UserDto
import com.devbrian.osebo.data.repository.AccountRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val accountRepository: AccountRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _profile = MutableLiveData<UserDto?>()
    val profile: LiveData<UserDto?> = _profile

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSavingProfile = MutableLiveData(false)
    val isSavingProfile: LiveData<Boolean> = _isSavingProfile

    private val _isSavingPassword = MutableLiveData(false)
    val isSavingPassword: LiveData<Boolean> = _isSavingPassword

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadProfile()
    }

    fun cachedFirstName(): String = preferenceManager.getFirstName()
    fun cachedLastName(): String = preferenceManager.getLastName()
    fun cachedPhone(): String = preferenceManager.getUserPhone()
    fun cachedEmail(): String = preferenceManager.getUserEmail()

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            // Falls back to cached login data if this (unconfirmed-path) endpoint fails,
            // so the form still populates with real values either way.
            _profile.value = accountRepository.getCurrentUser()
            _isLoading.value = false
        }
    }

    fun saveProfile(title: String?, firstName: String, lastName: String, phone: String) {
        viewModelScope.launch {
            _isSavingProfile.value = true
            try {
                // The endpoint's actual bound DTO (com.devbrian.osebo.data.UpdateProfileRequest)
                // has no title field, so title is kept in the UI but not sent to the server.
                val updated = accountRepository.updateProfile(
                    UpdateProfileRequest(
                        firstName = firstName,
                        lastName = lastName,
                        phone = phone
                    )
                )
                _profile.value = updated
                preferenceManager.saveFirstName(firstName)
                preferenceManager.saveLastName(lastName)
                _successMessage.value = "Personal details updated"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update personal details"
            } finally {
                _isSavingProfile.value = false
            }
        }
    }

    fun savePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isSavingPassword.value = true
            try {
                accountRepository.changePassword(currentPassword, newPassword)
                _successMessage.value = "Password updated"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to update password"
            } finally {
                _isSavingPassword.value = false
            }
        }
    }

    fun clearMessages() {
        _successMessage.value = null
        _errorMessage.value = null
    }
}
