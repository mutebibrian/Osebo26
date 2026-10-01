package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable




@Serializable
data class AuthResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String? = null,

    @SerialName("data")
    val data: AuthData? = null
)




@Serializable
data class RoleDto(
    @SerialName("id")
    val id: String?,

    @SerialName("name")
    val name: String?
)