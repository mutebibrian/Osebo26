package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("id")
    val id: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("isActive")
    val isActive: Boolean,

    @SerializedName("isVerified")
    val isVerified: Boolean,

    @SerializedName("photo")
    val photo: String? = null,

    @SerializedName("role")
    val role: RoleData? = null
)

