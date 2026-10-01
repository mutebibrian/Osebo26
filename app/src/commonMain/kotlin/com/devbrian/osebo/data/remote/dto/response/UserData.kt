package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    @SerialName("id")
    val id: String,

    @SerialName("firstName")
    val firstName: String,

    @SerialName("lastName")
    val lastName: String,

    @SerialName("email")
    val email: String,

    @SerialName("phone")
    val phone: String? = null,

    @SerialName("title")
    val title: String? = null,

    @SerialName("isActive")
    val isActive: Boolean,

    @SerialName("isVerified")
    val isVerified: Boolean? = false,



    @SerialName("photo")
    val photo: String? = null,

    @SerialName("role")
    val role: RoleData? = null
)


