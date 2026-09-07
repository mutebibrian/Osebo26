package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class InitiatePaymentRequest(
    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String,

    @SerializedName("provider")
    val provider: String,

    @SerializedName("shopId")
    val shopId: String,

    @SerializedName("packageId")
    val packageId: String,

    @SerializedName("months")
    val months: Int,

    @SerializedName("metadata")
    val metadata: Map<String, String> = emptyMap()
)


