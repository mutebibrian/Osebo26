package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.local.entity.ShopEntity
import com.devbrian.osebo.data.mapper.AccountMapper
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.domain.model.Account

class AccountRepository(
    private val apiService: ApiService,
    private val mapper: AccountMapper,
    private val preferenceManager: PreferenceManager,
    private val database: AppDatabase,
) {

    suspend fun getAccountDetails(): Account {
        cachedAccount()?.let { return it }

        val token = preferenceManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            throw Exception("User not authenticated")
        }

        val remoteAccount = runCatching {
            val response = apiService.getShopsWithAuth("Bearer $token")
            if (!response.isSuccessful || response.body()?.success != true) return@runCatching null

            val selectedShopId = preferenceManager.getShopIdentifierForApi()
            response.body()?.data.orEmpty()
                .firstOrNull { it.id == selectedShopId }
                ?.toAccount()
                ?: response.body()?.data.orEmpty().firstOrNull()?.toAccount()
        }.getOrNull()

        return remoteAccount ?: throw Exception("Account details are unavailable")
    }

    private suspend fun cachedAccount(): Account? {
        val selectedShopId = preferenceManager.getShopIdentifierForApi()
        val cachedShop = selectedShopId.takeIf { it.isNotBlank() }
            ?.let { database.shopDao().getShopById(it) }
            ?: database.shopDao().getAllShopsSuspend().firstOrNull()

        return cachedShop?.toAccount() ?: preferenceAccount()
    }

    private fun preferenceAccount(): Account? {
        val businessName = preferenceManager.getCurrentShopName()
            .ifBlank { preferenceManager.getUserName() }
        if (businessName.isBlank()) return null

        return Account(
            id = preferenceManager.getShopIdentifierForApi()
                .ifBlank { preferenceManager.getUserId() },
            businessName = businessName,
            businessType = preferenceManager.getBusinessType(),
            registrationNumber = "",
            taxId = "",
            address = preferenceManager.getShopLocation(),
            status = preferenceManager.getSubscriptionStatus()
                .takeIf { it.equals("inactive", ignoreCase = true).not() }
                ?: "active",
            paymentMethod = preferenceManager.getString("payment_method"),
            billingCycle = preferenceManager.getSubscriptionType(),
            nextBillingDate = preferenceManager.getSubscriptionExpiry(),
            twoFactorEnabled = preferenceManager.getBoolean("two_factor_enabled"),
            loginNotificationsEnabled = preferenceManager.isNotificationEnabled(),
            createdAt = "",
            updatedAt = "",
        )
    }

    private fun ShopDto.toAccount() = Account(
        id = id,
        businessName = name,
        businessType = shopTypeObject?.name
            ?: businessType
            ?: shopType
            ?: preferenceManager.getBusinessType(),
        registrationNumber = registrationNumber.orEmpty(),
        taxId = taxIdentificationNumber.orEmpty(),
        address = address.orEmpty(),
        status = status ?: if (isActive) "active" else "inactive",
        paymentMethod = preferenceManager.getString("payment_method"),
        billingCycle = preferenceManager.getSubscriptionType(),
        nextBillingDate = subscription?.endsAt ?: preferenceManager.getSubscriptionExpiry(),
        twoFactorEnabled = preferenceManager.getBoolean("two_factor_enabled"),
        loginNotificationsEnabled = preferenceManager.isNotificationEnabled(),
        createdAt = createdAt.orEmpty(),
        updatedAt = updatedAt.orEmpty(),
    )

    private fun ShopEntity.toAccount() = Account(
        id = id,
        businessName = name,
        businessType = shopType.orEmpty(),
        registrationNumber = registrationNumber.orEmpty(),
        taxId = taxIdentificationNumber.orEmpty(),
        address = address.orEmpty(),
        status = status,
        paymentMethod = preferenceManager.getString("payment_method"),
        billingCycle = subscriptionType.orEmpty(),
        nextBillingDate = subscriptionExpiry.orEmpty(),
        twoFactorEnabled = preferenceManager.getBoolean("two_factor_enabled"),
        loginNotificationsEnabled = preferenceManager.isNotificationEnabled(),
        createdAt = createdAt.orEmpty(),
        updatedAt = updatedAt.orEmpty(),
    )

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
