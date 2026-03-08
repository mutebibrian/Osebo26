package com.devbrian.osebo.data.repository


import com.devbrian.osebo.data.mapper.AccountMapper
import com.devbrian.osebo.data.remote.api.OseboApiService
import com.devbrian.osebo.domain.model.Account
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AccountRepository @Inject constructor(
    private val apiService: OseboApiService,
    private val mapper: AccountMapper
) {

    suspend fun getAccountDetails(): Account {
        val response = apiService.getAccountDetails()
        if (response.isSuccessful && response.body() != null) {
            return mapper.toDomain(response.body()!!)
        } else {
            throw Exception("Failed to get account details: ${response.message()}")
        }
    }

    suspend fun updateTwoFactorAuth(enabled: Boolean): Account {
        val request = com.devbrian.osebo.data.remote.dto.request.TwoFactorAuthRequest(enabled)
        val response = apiService.updateTwoFactorAuth(request)
        if (response.isSuccessful && response.body() != null) {
            return mapper.toDomain(response.body()!!)
        } else {
            throw Exception("Failed to update two-factor auth: ${response.message()}")
        }
    }

    suspend fun updateLoginNotifications(enabled: Boolean): Account {
        val request = com.devbrian.osebo.data.remote.dto.request.LoginNotificationsRequest(enabled)
        val response = apiService.updateLoginNotifications(request)
        if (response.isSuccessful && response.body() != null) {
            return mapper.toDomain(response.body()!!)
        } else {
            throw Exception("Failed to update login notifications: ${response.message()}")
        }
    }

    suspend fun deactivateAccount() {
        val response = apiService.deactivateAccount()
        if (!response.isSuccessful) {
            throw Exception("Failed to deactivate account: ${response.message()}")
        }
    }

    suspend fun deleteAccount() {
        val response = apiService.deleteAccount()
        if (!response.isSuccessful) {
            throw Exception("Failed to delete account: ${response.message()}")
        }
    }
}

