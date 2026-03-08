package com.devbrian.osebo.models

import com.google.gson.annotations.SerializedName

data class SupportMessageRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("subject")
    val subject: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("timestamp")
    val timestamp: Long
)

