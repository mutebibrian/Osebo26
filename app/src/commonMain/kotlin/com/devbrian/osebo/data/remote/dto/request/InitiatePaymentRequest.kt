package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InitiatePaymentRequest(
    @SerialName("amount")
    val amount: Double,

    @SerialName("currency")
    val currency: String,

    @SerialName("phoneNumber")
    val phoneNumber: String,

    @SerialName("provider")
    val provider: String,

    @SerialName("shopId")
    val shopId: String,

    @SerialName("packageId")
    val packageId: String,

    @SerialName("months")
    val months: Int,

    @SerialName("metadata")
    val metadata: Map<String, String> = emptyMap()
)


