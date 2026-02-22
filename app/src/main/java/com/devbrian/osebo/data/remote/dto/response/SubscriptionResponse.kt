package com.devbrian.osebo.data.remote.dto.response

import com.devbrian.osebo.models.Subscription
import com.google.gson.annotations.SerializedName

data class SubscriptionResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: SubscriptionData? = null
)

data class SubscriptionData(
    @SerializedName("type")
    val type: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("paymentId")
    val paymentId: String? = null,

    @SerializedName("transactionId")
    val transactionId: String? = null,

    @SerializedName("subscription")
    val subscription: Subscription? = null
)


// Payment.kt
data class Payment(
    @SerializedName("id") val id: String,
    @SerializedName("subscription_id") val subscriptionId: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("status") val status: String, // PENDING, COMPLETED, FAILED
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("transaction_id") val transactionId: String?,
    @SerializedName("paid_at") val paidAt: String?
)