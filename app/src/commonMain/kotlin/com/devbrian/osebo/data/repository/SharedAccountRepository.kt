package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.dto.request.LoginNotificationsRequest
import com.devbrian.osebo.data.remote.dto.request.TwoFactorAuthRequest
import com.devbrian.osebo.data.remote.dto.response.AccountDto
import com.devbrian.osebo.ui.screens.SharedAccountProfileUi

/**
 * Mirrors AccountRepository/AccountViewModel on Android: getAccountDetails,
 * updateTwoFactorAuth, updateLoginNotifications, deactivateAccount,
 * deleteAccount. All five endpoints already existed in KtorOseboApiService,
 * unused. Skips Android's local SQLite/SharedPreferences cache fallback
 * chain — commonMain has no equivalent cache yet, so this just calls the
 * API directly.
 */
class SharedAccountRepository(private val api: KtorOseboApiService) {
    suspend fun getAccount(): ApiResult<SharedAccountProfileUi> =
        when (val result = api.getAccountDetails()) {
            is ApiResult.Success -> result.data.data?.let { ApiResult.Success(it.toUi(), result.httpCode) }
                ?: ApiResult.Error(result.httpCode, "Account details are unavailable")
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }

    suspend fun updateTwoFactorAuth(enabled: Boolean): ApiResult<SharedAccountProfileUi> =
        when (val result = api.updateTwoFactorAuth(TwoFactorAuthRequest(enabled))) {
            is ApiResult.Success -> result.data.data?.let { ApiResult.Success(it.toUi(), result.httpCode) }
                ?: ApiResult.Error(result.httpCode, "Failed to update two-factor authentication")
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }

    suspend fun updateLoginNotifications(enabled: Boolean): ApiResult<SharedAccountProfileUi> =
        when (val result = api.updateLoginNotifications(LoginNotificationsRequest(enabled))) {
            is ApiResult.Success -> result.data.data?.let { ApiResult.Success(it.toUi(), result.httpCode) }
                ?: ApiResult.Error(result.httpCode, "Failed to update login notifications")
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }

    suspend fun deactivateAccount(): ApiResult<Unit> =
        when (val result = api.deactivateAccount()) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.httpCode)
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }

    suspend fun deleteAccount(): ApiResult<Unit> =
        when (val result = api.deleteAccount()) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.httpCode)
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
}

private fun AccountDto.toUi() = SharedAccountProfileUi(
    id = id,
    businessName = businessName,
    businessType = businessType,
    registrationNumber = registrationNumber,
    taxId = taxId,
    address = address,
    status = status,
    paymentMethod = paymentMethod,
    billingCycle = billingCycle,
    nextBillingDate = nextBillingDate,
    twoFactorEnabled = twoFactorEnabled,
    loginNotificationsEnabled = loginNotificationsEnabled,
)
