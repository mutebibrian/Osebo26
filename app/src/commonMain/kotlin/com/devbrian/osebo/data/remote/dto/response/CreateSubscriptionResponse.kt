package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionStatusResponse(
    @SerialName("status")
    val status: String,

    @SerialName("type")
    val type: String? = null,

    @SerialName("expiry_date")
    val expiryDate: String? = null,

    @SerialName("is_active")
    val isActive: Boolean = false
)
