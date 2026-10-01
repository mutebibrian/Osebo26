package com.devbrian.osebo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RenewSubscriptionRequest(
    @SerialName("phone_number")
    val phoneNumber: String,

    @SerialName("months")
    val months: Int = 1,

    @SerialName("auto_renew")
    val autoRenew: Boolean = false
)
