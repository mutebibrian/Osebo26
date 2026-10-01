package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InitiatePaymentResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String? = null,

    @SerialName("data")
    val data: InitiatePaymentData? = null
)

@Serializable
data class InitiatePaymentData(
    @SerialName("type")
    val type: String? = null,

    @SerialName("message")
    val message: String? = null,

    @SerialName("paymentId")
    val paymentId: String? = null
)


