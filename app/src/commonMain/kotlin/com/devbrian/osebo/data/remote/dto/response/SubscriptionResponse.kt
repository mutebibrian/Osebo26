package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable



// This matches the actual API response data structure
@Serializable
data class SubscriptionResponse(
    @SerialName("type")
    val type: String? = null,

    @SerialName("message")
    val message: String? = null,

    @SerialName("paymentId")
    val paymentId: String? = null,

    @SerialName("subscriptionId")
    val subscriptionId: String? = null,

    @SerialName("status")
    val status: String? = null
)

// This is the actual data inside the response
@Serializable
data class SubscriptionDataResponse(
    @SerialName("type")
    val type: String? = null,

    @SerialName("message")
    val message: String? = null,

    @SerialName("paymentId")
    val paymentId: String? = null,

    @SerialName("subscriptionId")
    val subscriptionId: String? = null,

    @SerialName("status")
    val status: String? = null
)



@Serializable
data class SubscriptionData(
    @SerialName("type")
    val type: String? = null,

    @SerialName("message")
    val message: String? = null,

    @SerialName("paymentId")
    val paymentId: String? = null,

    @SerialName("subscriptionId")
    val subscriptionId: String? = null,

    @SerialName("status")
    val status: String? = null
)



@Serializable
data class Payment(
    @SerialName("id") val id: String,
    @SerialName("subscription_id") val subscriptionId: String,
    @SerialName("amount") val amount: Double,
    @SerialName("currency") val currency: String,
    @SerialName("status") val status: String, 
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("transaction_id") val transactionId: String?,
    @SerialName("paid_at") val paidAt: String?
)


