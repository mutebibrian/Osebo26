package com.devbrian.osebo.models


import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.data.models.User
import com.google.gson.annotations.SerializedName



data class LoginResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("user")
    val user: User,

    @SerializedName("shop_id")  
    val shopId: String? = null,

    @SerializedName("shop")  
    val shop: Shop? = null
)


