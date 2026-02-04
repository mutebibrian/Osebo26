package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("url")
    val url: String,

    @SerializedName("file_name")
    val fileName: String
)