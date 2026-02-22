package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class ActivateTrialRequest(

    @SerializedName("shopId")
    val shopId: String,

    @SerializedName("tier")
    val tier: String,

    @SerializedName("packageId")
    val packageId: String? = null,





    @SerializedName("months")
    val trialMonths: Int = 1,

    @SerializedName("phone_number")
    val phoneNumber: String? = null
)

