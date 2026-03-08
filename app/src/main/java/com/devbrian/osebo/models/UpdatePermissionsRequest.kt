package com.devbrian.osebo.models


import com.google.gson.annotations.SerializedName

data class UpdatePermissionsRequest(
    @SerializedName("permissions")
    val permissions: List<String>
)

