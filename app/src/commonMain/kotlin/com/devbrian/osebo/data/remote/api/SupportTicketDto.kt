package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupportTicketDto(
    @SerialName("id")
    val id: String,

    @SerialName("ticket_number")
    val ticketNumber: String,

    @SerialName("category")
    val category: String,

    @SerialName("subject")
    val subject: String,

    @SerialName("status")
    val status: String,

    @SerialName("created_at")
    val createdAt: String
)


