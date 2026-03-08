package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class UserRoleDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("permissions")
    val permissions: List<String>,

    @SerializedName("user_count")
    val userCount: Int,

    @SerializedName("created_at")
    val createdAt: String
)

