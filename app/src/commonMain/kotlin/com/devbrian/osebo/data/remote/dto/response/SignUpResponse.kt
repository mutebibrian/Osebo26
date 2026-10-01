package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SignUpResponse(
    val success: Boolean,
    val message: String?,
    val data: JsonElement? = null,
    val userId: String? = null
)


