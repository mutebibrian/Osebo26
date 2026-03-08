package com.devbrian.osebo.models


import com.google.gson.annotations.SerializedName



data class CreateRoleRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("permissions") val permissions: List<String>,
    @SerializedName("shop_id") val shopId: String
)




