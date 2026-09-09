package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.mapper.AccountMapper
import com.devbrian.osebo.domain.model.Account

class AccountRepository(
    private val apiService: ApiService,
    private val mapper: AccountMapper,
    private val preferenceManager: PreferenceManager
) {

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