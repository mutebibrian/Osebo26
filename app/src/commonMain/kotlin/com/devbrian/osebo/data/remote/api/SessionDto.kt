package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionDto(
    @SerialName("id")
    val id: String,

    @SerialName("device")
    val device: String,

    @SerialName("ip_address")
    val ipAddress: String,

    @SerialName("last_active")
    val lastActive: String,

    @SerialName("is_current")
    val isCurrent: Boolean
)


