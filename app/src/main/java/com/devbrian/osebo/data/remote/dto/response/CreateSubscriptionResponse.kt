package com.devbrian.osebo.data.remote.dto.response

import com.devbrian.osebo.models.Subscription
import com.google.gson.annotations.SerializedName

data class CreateSubscriptionResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("transaction_id")
    val transactionId: String? = null,

    @SerializedName("subscription")
    val subscription: Subscription? = null
)


data class SubscriptionStatusResponse(
    @SerializedName("status")
    val status: String, 

    @SerializedName("type")
    val type: String? = null, 

    @SerializedName("expiry_date")
    val expiryDate: String? = null,

    @SerializedName("is_active")
    val isActive: Boolean = false
)


