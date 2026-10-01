package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    @SerialName("id")
    val id: String,

    @SerialName("type")
    val type: String,

    @SerialName("title")
    val title: String,

    @SerialName("message")
    val message: String,

    @SerialName("is_read")
    val isRead: Boolean,

    @SerialName("created_at")
    val createdAt: String
)


