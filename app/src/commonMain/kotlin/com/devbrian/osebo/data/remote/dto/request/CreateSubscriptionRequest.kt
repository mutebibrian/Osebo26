package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSubscriptionRequest(
    @SerialName("shopId")
    val shopId: String,

    @SerialName("packageIds")
    val packageIds: List<String>,

    @SerialName("customerPhone")
    val customerPhone: String,

    @SerialName("duration")
    val duration: Int,

    @SerialName("isTrial")
    val isTrial: Boolean = false
)