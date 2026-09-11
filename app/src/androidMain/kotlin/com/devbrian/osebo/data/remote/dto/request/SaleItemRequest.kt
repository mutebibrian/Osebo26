package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class SaleItemRequest(
    @SerializedName("stock_item_id")
    val stockItemId: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("price")
    val price: Double,

    @SerializedName("discount")
    val discount: Double = 0.0,

    @SerializedName("isCustomPrice")
    val isCustomPrice: Boolean = false,

    @SerializedName("amount")
    val amount: Double
) {
    
    constructor(
        stockItemId: String,
        quantity: Int,
        price: Double,
        discount: Double = 0.0,
        isCustomPrice: Boolean = false
    ) : this(
        stockItemId = stockItemId,
        quantity = quantity,
        price = price,
        discount = discount,
        isCustomPrice = isCustomPrice,
        amount = (price * quantity) - (price * quantity * discount / 100)
    )
}
