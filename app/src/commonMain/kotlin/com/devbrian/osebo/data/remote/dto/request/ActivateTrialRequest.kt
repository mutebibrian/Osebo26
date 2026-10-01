package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivateTrialRequest(

    @SerialName("shopId")
    val shopId: String,

    @SerialName("tier")
    val tier: String,

    @SerialName("packageId")
    val packageId: String? = null,





    @SerialName("months")
    val trialMonths: Int = 1,

    @SerialName("phone_number")
    val phoneNumber: String? = null
)



