package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendSupportMessageRequest(
    @SerialName("name")
    val name: String,

    @SerialName("email")
    val email: String,

    @SerialName("subject")
    val subject: String,

    @SerialName("category")
    val category: String,

    @SerialName("message")
    val message: String
)


