package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.mapper.AccountMapper
import com.devbrian.osebo.data.UpdateProfileRequest
import com.devbrian.osebo.data.remote.dto.request.ChangePasswordRequest
import com.devbrian.osebo.data.remote.dto.response.UserDto
import com.devbrian.osebo.domain.model.Account
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val apiService: ApiService,
    private val mapper: AccountMapper,
    private val preferenceManager: PreferenceManager
) {

    suspend fun getCurrentUser(): UserDto? {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): UserDto {
        val response = apiService.updateProfile(request)
        if (response.isSuccessful && response.body()?.success == true) {
            return response.body()?.data ?: throw Exception("Empty response")
        } else {
            throw Exception(response.body()?.message ?: "Failed to update profile")
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        val response = apiService.changePassword(ChangePasswordRequest(currentPassword, newPassword))
        if (!response.isSuccessful || response.body()?.success != true) {
            throw Exception(response.body()?.message ?: "Failed to change password")
        }
    }

    suspend fun getAccountDetails(): Account {
        val token = preferenceManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            throw Exception("User not authenticated")
        }

        val response = apiService.getAccountDetails("Bearer $token")
        if (response.isSuccessful && response.body()?.success == true) {
            return mapper.toDomain(response.body()!!)
        } else {
            throw Exception(response.body()?.message ?: "Failed to get account details")
        }
    }

    suspend fun updateTwoFactorAuth(enabled: Boolean): Account {
        val token = preferenceManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            throw Exception("User not authenticated")
        }

        val request = com.devbrian.osebo.data.remote.dto.request.TwoFactorAuthRequest(enabled)
        val response = apiService.updateTwoFactorAuth("Bearer $token", request)
        if (response.isSuccessful && response.body()?.success == true) {
            return mapper.toDomain(response.body()!!)
        } else {
            throw Exception(response.body()?.message ?: "Failed to update two-factor auth")
        }
    }

    suspend fun updateLoginNotifications(enabled: Boolean): Account {
        val token = preferenceManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            throw Exception("User not authenticated")
        }

        val request = com.devbrian.osebo.data.remote.dto.request.LoginNotificationsRequest(enabled)
        val response = apiService.updateLoginNotifications("Bearer $token", request)
        if (response.isSuccessful && response.body()?.success == true) {
            return mapper.toDomain(response.body()!!)
        } else {
            throw Exception(response.body()?.message ?: "Failed to update login notifications")
        }
    }

    suspend fun deactivateAccount() {
        val token = preferenceManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            throw Exception("User not authenticated")
        }

        val response = apiService.deactivateAccount("Bearer $token")
        if (!response.isSuccessful || response.body()?.success != true) {
            throw Exception(response.body()?.message ?: "Failed to deactivate account")
        }
    }

    suspend fun deleteAccount() {
        val token = preferenceManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            throw Exception("User not authenticated")
        }

        val response = apiService.deleteAccount("Bearer $token")
        if (!response.isSuccessful || response.body()?.success != true) {
            throw Exception(response.body()?.message ?: "Failed to delete account")
        }
    }
}