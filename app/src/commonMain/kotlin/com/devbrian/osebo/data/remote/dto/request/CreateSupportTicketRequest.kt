package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSupportTicketRequest(
    @SerialName("category")
    val category: String,

    @SerialName("subject")
    val subject: String,

    @SerialName("message")
    val message: String,

    @SerialName("priority")
    val priority: String = "normal"
)


