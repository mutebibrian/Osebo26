package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class CreateSubscriptionRequest(
    @SerializedName("packageId")
    val packageId: String,

    @SerializedName("customerPhone")
    val customerPhone: String,

    @SerializedName("duration")
    val duration: Int,

    @SerializedName("currency")
    val currency: String

)
