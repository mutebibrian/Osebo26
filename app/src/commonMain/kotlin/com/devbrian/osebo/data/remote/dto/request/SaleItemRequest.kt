package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaleItemRequest(
    @SerialName("stock_item_id")
    val stockItemId: String,

    @SerialName("quantity")
    val quantity: Int,

    @SerialName("price")
    val price: Double,

    @SerialName("discount")
    val discount: Double = 0.0,

    @SerialName("isCustomPrice")
    val isCustomPrice: Boolean = false,

    @SerialName("amount")
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
