package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRoleDto(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String,

    @SerialName("permissions")
    val permissions: List<String>,

    @SerialName("user_count")
    val userCount: Int,

    @SerialName("created_at")
    val createdAt: String
)


