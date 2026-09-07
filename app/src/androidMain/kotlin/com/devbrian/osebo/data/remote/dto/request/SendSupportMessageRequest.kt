package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class SendSupportMessageRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("subject")
    val subject: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("message")
    val message: String
)


