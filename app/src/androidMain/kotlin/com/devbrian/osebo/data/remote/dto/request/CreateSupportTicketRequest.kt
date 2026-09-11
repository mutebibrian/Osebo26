package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class CreateSupportTicketRequest(
    @SerializedName("category")
    val category: String,

    @SerializedName("subject")
    val subject: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("priority")
    val priority: String = "normal"
)


