package com.devbrian.osebo.utils

import com.devbrian.osebo.data.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferenceManager: PreferenceManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get the shop UUID from preferences
        val shopUuid = preferenceManager.getCurrentShopUuid()
        val shopId = preferenceManager.getCurrentShopId()

        // Prefer UUID if it's valid, otherwise use ID
        val shopIdentifier = if (isValidUUID(shopUuid)) {
            shopUuid
        } else if (isValidUUID(shopId)) {
            shopId
        } else {
            println("⚠️ No valid shop UUID found! Using: $shopUuid")
            shopUuid
        }

        println("🔐 AuthInterceptor - Using shop UUID: $shopIdentifier")

        val requestBuilder = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${preferenceManager.getAuthToken()}")
            .header("X-App-Platform", "Android")

        if (shopIdentifier.isNotEmpty()) {
            requestBuilder.header("X-Shop", shopIdentifier)
        }

        return chain.proceed(requestBuilder.build())
    }

    private fun isValidUUID(uuid: String): Boolean {
        return try {
            java.util.UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}