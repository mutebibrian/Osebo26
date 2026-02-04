package com.devbrian.osebo.models


import com.google.gson.annotations.SerializedName



data class LoginResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("user")
    val user: User,

    @SerializedName("shop_id")  // Add this if your API returns shop_id
    val shopId: String? = null,

    @SerializedName("shop")  // Or if your API returns shop object
    val shop: Shop? = null
)