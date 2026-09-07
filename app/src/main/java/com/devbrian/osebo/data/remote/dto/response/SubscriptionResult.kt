package com.devbrian.osebo.data.remote.dto.response


import com.google.gson.annotations.SerializedName


data class SubscriptionResult(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: SubscriptionResultData? = null
)

data class SubscriptionResultData(
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