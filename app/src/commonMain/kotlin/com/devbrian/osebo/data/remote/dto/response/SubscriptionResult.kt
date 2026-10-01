package com.devbrian.osebo.data.remote.dto.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SubscriptionResult(
    @SerialName("success")
    val success: Boolean = false,

    @SerialName("message")
    val message: String? = null,

    @SerialName("data")
    val data: SubscriptionResultData? = null
)

@Serializable
data class SubscriptionResultData(
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