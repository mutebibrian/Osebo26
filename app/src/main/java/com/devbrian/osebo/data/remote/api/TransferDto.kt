package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class TransferDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("from_shop_id")
    val fromShopId: String,

    @SerializedName("to_shop_id")
    val toShopId: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("created_at")
    val createdAt: String
)

