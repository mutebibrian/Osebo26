package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.dto.response.CustomerDto
import com.devbrian.osebo.ui.screens.CustomerUi

/**
 * First repository wired to the Ktor client (KtorOseboApiService), proving
 * the full real-data pipeline: Ktor -> real backend -> DTO -> UI model ->
 * screen, on both Android and iOS. Picked Customers as the starting point
 * since it's the simplest screen (no sub-dialogs' worth of state to map).
 *
 * Named "Shared" (not just CustomerRepository) because androidMain already
 * has its own, unrelated, Retrofit-based CustomerRepository in this same
 * package — reusing that name here collided and broke the Android build.
 */
class SharedCustomerRepository(private val api: KtorOseboApiService) {
    suspend fun getCustomers(shopId: String): ApiResult<List<CustomerUi>> {
        return when (val result = api.getCustomers(shopId)) {
            is ApiResult.Success -> ApiResult.Success(
                data = result.data.data.orEmpty().map { it.toCustomerUi() },
                httpCode = result.httpCode,
            )
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
    }
}

private fun CustomerDto.toCustomerUi(): CustomerUi {
    val initials = name.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

    return CustomerUi(
        id = id,
        name = name,
        initials = initials,
        email = email ?: "",
        phone = phone ?: "",
        totalSpentLabel = "UGX ${(totalSales ?: 0.0).toInt()}",
        lastPurchaseLabel = "—",
        customerSinceLabel = createdAt ?: "—",
        loyaltyPointsLabel = "0",
        purchaseCountLabel = numberOfSales ?: "0",
        isVip = (totalSales ?: 0.0) > 500000.0,
    )
}
