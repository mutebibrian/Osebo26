package com.devbrian.osebo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateRoleRequest(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("permissions") val permissions: List<String>,
    @SerialName("shop_id") val shopId: String
)
