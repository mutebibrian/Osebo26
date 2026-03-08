package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class SupportTicketDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("ticket_number")
    val ticketNumber: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("subject")
    val subject: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("created_at")
    val createdAt: String
)

