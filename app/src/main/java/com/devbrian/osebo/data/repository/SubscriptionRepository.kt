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

    
    
    suspend fun getSubscriptionPackages(): Resource<List<SubscriptionPackage>>

    
    
    suspend fun getSubscriptionDetails(
        shopId: String,
        subscriptionId: String
    ): Resource<Subscription>

    suspend fun getShops(): Resource<List<Shop>>


    suspend fun checkPaymentStatus(
        paymentId: String,
        request: CheckPaymentStatusRequest
    ): Resource<PaymentStatusResponse>



    suspend fun getShopActiveSubscription(shopId: String): Resource<Subscription>

    
    
    suspend fun createSubscription(
        shopId: String,
        request: CreateSubscriptionRequest
    ): Resource<Any>

    

    
    suspend fun initiatePayment(
        shopId: String,
        request: InitiatePaymentRequest
    ): Resource<Any>

    suspend fun getPaymentStatus(shopId: String, paymentId: String): Resource<PaymentStatusResponse>





    
    suspend fun pollPaymentStatus(
        shopId: String,
        request: PollPaymentStatusRequest
    ): Resource<PaymentPollResponse>

    
    
    suspend fun getPaymentHistory(
        shopId: String,
        subscriptionId: String
    ): Resource<List<Payment>>

    
    
    suspend fun checkShopSubscription(shopId: String): Resource<ShopSubscriptionStatusResponse>

    
    
    suspend fun cancelSubscription(
        shopId: String,
        subscriptionId: String
    ): Resource<Unit>

    
    
    suspend fun renewSubscription(
        shopId: String,
        subscriptionId: String,
        request: RenewSubscriptionRequest
    ): Resource<SubscriptionResponse>

    
    
    suspend fun activateFreeTrial(
        shopId: String,
        packageId: String
    ): Resource<SubscriptionResponse>
}

