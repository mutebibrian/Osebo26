package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class UpdateUserRoleRequest(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("permissions")
    val permissions: List<String>? = null
)

