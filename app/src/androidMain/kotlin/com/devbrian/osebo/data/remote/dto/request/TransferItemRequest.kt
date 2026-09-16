package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class TransferItemRequest(
    @SerializedName("stockItemId")
    val stockItemId: String,

    @SerializedName("quantity")
    val quantity: Double
)
