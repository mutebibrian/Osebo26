package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.dto.request.CreateShopRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateShopRequest
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.data.remote.dto.response.ShopSubscriptionStatusDto
import com.devbrian.osebo.data.remote.dto.response.ShopTypeDto
import com.devbrian.osebo.ui.screens.ShopItemUi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

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
            is ApiResult.Success -> {
                val shops = result.data.data.orEmpty()
                // ShopDto's own fields (from this /shops list response) don't
                // reliably carry subscription state — GET
                // api/v1/subscription/shop/active per shop is the real source
                // of truth (same endpoint androidMain's subscription gating
                // uses), so fetch it for every shop in parallel.
                val statusByShopId = coroutineScope {
                    shops
                        .map { shop -> async { shop.id to fetchSubscriptionStatus(shop.id) } }
                        .awaitAll()
                }.toMap()
                ApiResult.Success(
                    shops.map { it.toShopItemUi(currentShopId, statusByShopId[it.id]) },
                    result.httpCode,
                )
            }
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }

    private suspend fun fetchSubscriptionStatus(shopId: String): ShopSubscriptionStatusDto? =
        when (val result = api.checkShopSubscription(shopId)) {
            is ApiResult.Success -> result.data
            is ApiResult.Error -> null
            is ApiResult.NetworkError -> null
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

private fun ShopDto.toShopItemUi(currentShopId: String?, subscriptionStatus: ShopSubscriptionStatusDto?) = ShopItemUi(
    id = id,
    name = name,
    type = businessType ?: shopType ?: "Shop",
    location = address ?: "Location not set",
    isSubscriptionActive = subscriptionStatus?.resolvedIsActive ?: hasActiveSubscription,
    isTrial = (subscriptionStatus?.resolvedStatus ?: subscriptionStatusString) == "TRIAL",
    isCurrent = id == currentShopId,
)
