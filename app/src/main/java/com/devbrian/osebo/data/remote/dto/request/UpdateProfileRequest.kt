package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("title")
    val title: String? = null,

    @SerializedName("first_name")
    val firstName: String? = null,

    @SerializedName("last_name")
    val lastName: String? = null,

    @SerializedName("phone_number")
    val phoneNumber: String? = null
)

