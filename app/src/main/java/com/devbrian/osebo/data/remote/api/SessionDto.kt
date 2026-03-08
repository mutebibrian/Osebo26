package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class SessionDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("device")
    val device: String,

    @SerializedName("ip_address")
    val ipAddress: String,

    @SerializedName("last_active")
    val lastActive: String,

    @SerializedName("is_current")
    val isCurrent: Boolean
)

