package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.dto.request.CreateShopRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateShopRequest
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.data.remote.dto.response.ShopTypeDto
import com.devbrian.osebo.ui.screens.ShopItemUi

/**
 * Mirrors shopsFragment/ShopCreationActivity on Android: list, shop types,
 * and shop creation. onSubscribeClick still isn't wired — no
 * subscription-purchase screen exists in commonMain yet.
 *
 * Named "Shared" because androidMain already has its own, unrelated
 * Retrofit-based ShopRepository in this same package.
 */
class SharedShopsRepository(private val api: KtorOseboApiService) {
    suspend fun getShops(currentShopId: String?): ApiResult<List<ShopItemUi>> =
        when (val result = api.getShops()) {
            is ApiResult.Success -> ApiResult.Success(
                result.data.data.orEmpty().map { it.toShopItemUi(currentShopId) },
                result.httpCode,
            )
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }

    suspend fun getShopTypes(): ApiResult<List<ShopTypeDto>> =
        when (val result = api.getShopTypes()) {
            is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty(), result.httpCode)
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }

    suspend fun createShop(request: CreateShopRequest): ApiResult<ShopDto> =
        when (val result = api.createShop(request)) {
            is ApiResult.Success -> result.data.data?.let { ApiResult.Success(it, result.httpCode) }
                ?: ApiResult.Error(result.httpCode, "No shop returned")
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }

    suspend fun updateShop(shopId: String, request: UpdateShopRequest): ApiResult<ShopDto> =
        when (val result = api.updateShop(shopId, request)) {
            is ApiResult.Success -> result.data.data?.let { ApiResult.Success(it, result.httpCode) }
                ?: ApiResult.Error(result.httpCode, "No shop returned")
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
}

private fun ShopDto.toShopItemUi(currentShopId: String?) = ShopItemUi(
    id = id,
    name = name,
    type = businessType ?: shopType ?: "Shop",
    location = address ?: "Location not set",
    isSubscriptionActive = hasActiveSubscription,
    isTrial = subscriptionStatusString == "TRIAL",
    isCurrent = id == currentShopId,
)
