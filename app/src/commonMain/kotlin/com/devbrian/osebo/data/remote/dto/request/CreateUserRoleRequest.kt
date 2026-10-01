package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRoleRequest(
    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String,

    @SerialName("permissions")
    val permissions: List<String>
)


