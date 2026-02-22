package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.dto.request.CheckPaymentStatusRequest
import com.devbrian.osebo.data.remote.dto.request.CreateSubscriptionRequest
import com.devbrian.osebo.data.remote.dto.request.InitiatePaymentRequest
import com.devbrian.osebo.data.remote.dto.response.InitiatePaymentResponse
import com.devbrian.osebo.data.remote.dto.response.PaymentPollResponse
import com.devbrian.osebo.data.remote.dto.response.PaymentStatusResponse
import com.devbrian.osebo.data.remote.dto.response.ShopSubscriptionStatusResponse
import com.devbrian.osebo.data.remote.dto.response.SubscriptionResponse
import com.devbrian.osebo.models.*
import com.devbrian.osebo.utils.Resource

interface SubscriptionRepository {

    // ==================== SUBSCRIPTION PACKAGES ====================
    // No shop ID needed - packages are global
    suspend fun getSubscriptionPackages(): Resource<List<SubscriptionPackage>>

    // ==================== SUBSCRIPTION DETAILS ====================
    // UPDATED: Added shopId parameter (passed in header)
    suspend fun getSubscriptionDetails(
        shopId: String,
        subscriptionId: String
    ): Resource<Subscription>

    suspend fun getShops(): Resource<List<Shop>>


    suspend fun checkPaymentStatus(
        paymentId: String,
        request: CheckPaymentStatusRequest
    ): Resource<PaymentStatusResponse>

    // ==================== SHOP SUBSCRIPTION ====================
    // Already has shopId
    suspend fun getShopActiveSubscription(shopId: String): Resource<Subscription>

    // ==================== CREATE SUBSCRIPTION ====================
    // UPDATED: Shop ID moved from request body to method parameter (will be passed as header)
    suspend fun createSubscription(
        shopId: String,
        request: CreateSubscriptionRequest
    ): Resource<Any>

    // ==================== PAYMENT METHODS ====================

    // UPDATED: Added shopId parameter (passed in header)
    suspend fun initiatePayment(
        shopId: String,
        request: InitiatePaymentRequest
    ): Resource<Any>

    // UPDATED: Added shopId parameter (passed in header)
    suspend fun getPaymentStatus(
        shopId: String,
        transactionId: String
    ): Resource<PaymentStatusResponse>

    // UPDATED: Added shopId parameter (passed in header)
    suspend fun pollPaymentStatus(
        shopId: String,
        request: PollPaymentStatusRequest
    ): Resource<PaymentPollResponse>

    // ==================== PAYMENT HISTORY ====================
    // UPDATED: Added shopId parameter (passed in header)
    suspend fun getPaymentHistory(
        shopId: String,
        subscriptionId: String
    ): Resource<List<Payment>>

    // ==================== SUBSCRIPTION STATUS ====================
    // Already has shopId
    suspend fun checkShopSubscription(shopId: String): Resource<ShopSubscriptionStatusResponse>

    // ==================== CANCEL SUBSCRIPTION ====================
    // UPDATED: Added shopId parameter (passed in header)
    suspend fun cancelSubscription(
        shopId: String,
        subscriptionId: String
    ): Resource<Unit>

    // ==================== RENEW SUBSCRIPTION ====================
    // UPDATED: Added shopId parameter (passed in header)
    suspend fun renewSubscription(
        shopId: String,
        subscriptionId: String,
        request: RenewSubscriptionRequest
    ): Resource<SubscriptionResponse>

    // ==================== FREE TRIAL ====================
    // Already has shopId
    suspend fun activateFreeTrial(
        shopId: String,
        packageId: String
    ): Resource<SubscriptionResponse>
}