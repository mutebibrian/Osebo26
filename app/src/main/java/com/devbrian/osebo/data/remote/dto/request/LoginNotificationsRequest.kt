package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class LoginNotificationsRequest(
    @SerializedName("enabled")
    val enabled: Boolean
)

