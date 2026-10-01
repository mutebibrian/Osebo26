package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadResponseDto(
    @SerialName("success")
    val success: Boolean,

    @SerialName("url")
    val url: String,

    @SerialName("file_name")
    val fileName: String,

    @SerialName("file_size")
    val fileSize: Long? = null,

    @SerialName("mime_type")
    val mimeType: String? = null,

    @SerialName("uploaded_at")
    val uploadedAt: String? = null
)


