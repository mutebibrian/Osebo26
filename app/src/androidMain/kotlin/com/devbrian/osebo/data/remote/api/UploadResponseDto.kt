package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class UploadResponseDto(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("url")
    val url: String,

    @SerializedName("file_name")
    val fileName: String,

    @SerializedName("file_size")
    val fileSize: Long? = null,

    @SerializedName("mime_type")
    val mimeType: String? = null,

    @SerializedName("uploaded_at")
    val uploadedAt: String? = null
)


