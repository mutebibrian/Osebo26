package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.dto.request.CreateSubscriptionRequest
import com.devbrian.osebo.data.remote.dto.response.PaymentCheckResponse
import com.devbrian.osebo.data.remote.dto.response.PaymentDto
import com.devbrian.osebo.data.remote.dto.response.ShopSubscriptionStatusResponse
import com.devbrian.osebo.data.remote.dto.response.SubscriptionResponse
import com.devbrian.osebo.models.*
import com.devbrian.osebo.utils.Resource

interface SubscriptionRepository {

    suspend fun getSubscriptionPackages(): Resource<List<SubscriptionPackage>>

    suspend fun getShops(): Resource<List<Shop>>

    suspend fun getSubscriptionDetails(subscriptionId: String): Resource<Subscription>

    suspend fun getShopActiveSubscription(shopId: String): Resource<Subscription>

    suspend fun createSubscription(
        shopId: String,
        request: CreateSubscriptionRequest
    ): Resource<SubscriptionResponse>

    suspend fun renewSubscription(
        shopId: String,
        request: RenewSubscriptionRequest
    ): Resource<SubscriptionResponse>

    suspend fun checkShopSubscription(shopId: String): Resource<ShopSubscriptionStatusResponse>

    suspend fun cancelSubscription(
        shopId: String,
        subscriptionId: String
    ): Resource<Unit>

    suspend fun cancelPackage(
        shopId: String,
        subscriptionId: String,
        packageId: String
    ): Resource<Unit>

    suspend fun getPaymentHistory(
        shopId: String,
        page: Int = 1,
        limit: Int = 10,
        provider: String? = null
    ): Resource<List<Payment>>

    suspend fun getUserShopsSubscriptions(): Resource<List<Subscription>>

    suspend fun findSubscriptionByPaymentId(paymentId: String): Resource<PaymentCheckResponse>}