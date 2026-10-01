package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginNotificationsRequest(
    @SerialName("enabled")
    val enabled: Boolean
)


