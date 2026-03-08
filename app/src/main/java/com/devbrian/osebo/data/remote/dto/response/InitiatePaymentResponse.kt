package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class InitiatePaymentResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: InitiatePaymentData? = null
)

data class InitiatePaymentData(
    @SerializedName("type")
    val type: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("paymentId")
    val paymentId: String? = null
)

