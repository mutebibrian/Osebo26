package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName



// This matches the actual API response data structure
data class SubscriptionResponse(
    @SerializedName("type")
    val type: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("paymentId")
    val paymentId: String? = null,

    @SerializedName("subscriptionId")
    val subscriptionId: String? = null,

    @SerializedName("status")
    val status: String? = null
)

// This is the actual data inside the response
data class SubscriptionDataResponse(
    @SerializedName("type")
    val type: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("paymentId")
    val paymentId: String? = null,

    @SerializedName("subscriptionId")
    val subscriptionId: String? = null,

    @SerializedName("status")
    val status: String? = null
)



data class SubscriptionData(
    @SerializedName("type")
    val type: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("paymentId")
    val paymentId: String? = null,

    @SerializedName("subscriptionId")
    val subscriptionId: String? = null,

    @SerializedName("status")
    val status: String? = null
)



data class Payment(
    @SerializedName("id") val id: String,
    @SerializedName("subscription_id") val subscriptionId: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("status") val status: String, 
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("transaction_id") val transactionId: String?,
    @SerializedName("paid_at") val paidAt: String?
)


