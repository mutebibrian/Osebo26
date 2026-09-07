package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName




data class AuthResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: AuthData? = null
)




data class RoleDto(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String?
)