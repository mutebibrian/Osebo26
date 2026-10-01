package com.devbrian.osebo.data.remote.dto.response

import com.devbrian.osebo.models.Subscription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSubscriptionResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String,

    @SerialName("transaction_id")
    val transactionId: String? = null,

    @SerialName("subscription")
    val subscription: Subscription? = null
)
