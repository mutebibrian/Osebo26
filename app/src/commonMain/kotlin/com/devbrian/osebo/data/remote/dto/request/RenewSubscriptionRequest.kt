package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RenewSubscriptionRequest(
    @SerialName("phone_number")
    val phoneNumber: String,

    @SerialName("months")
    val months: Int = 1,

    @SerialName("auto_renew")
    val autoRenew: Boolean = false,

    @SerialName("payment_method")
    val paymentMethod: String = "mobile_money",

    @SerialName("amount")
    val amount: Double? = null,

    @SerialName("currency")
    val currency: String = "UGX"
)


