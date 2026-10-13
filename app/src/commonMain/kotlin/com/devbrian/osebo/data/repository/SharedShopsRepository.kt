package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.ui.screens.ShopItemUi

/**
 * First pass, mirroring shopsFragment on Android: just the shop list itself.
 * onAddShopClick/onSubscribeClick aren't wired — no shop-creation or
 * subscription-purchase screens exist in commonMain yet.
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
