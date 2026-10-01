package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("url")
    val url: String,

    @SerialName("file_name")
    val fileName: String
)


