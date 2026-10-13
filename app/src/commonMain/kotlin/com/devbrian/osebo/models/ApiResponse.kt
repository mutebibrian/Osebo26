package com.devbrian.osebo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `status` and `success` are given defaults deliberately: Android's Gson
 * deserializer never required them in the first place (missing fields just
 * come back null/default there), but kotlinx.serialization's generated
 * deserializer throws MissingFieldException for a required field with no
 * default. Real responses from several endpoints (confirmed live — shops,
 * dashboard summary) omit "status" entirely, which was crashing every one
 * of those calls into a NetworkError before this. `success` defaults to
 * true because by the time this is deserialized, execute() has already
 * confirmed the HTTP status itself was 2xx.
 */
@Serializable
data class ApiResponse<T>(
    @SerialName("status")
    val status: String = "",
    @SerialName("message")
    val message: String = "",
    @SerialName("data")
    val data: T? = null,
    @SerialName("success")
    val success: Boolean = true,
)
