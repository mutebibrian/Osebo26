package com.devbrian.osebo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val product: Product,
    var quantity: Int,
    var discount: Double = 0.0,
    var isCustomPrice: Boolean = false,
    var customPrice: Double? = null
) : Parcelable {

    val subtotal: Double
        get() = (customPrice ?: product.price) * quantity * (1 - discount / 100)

    val unitPrice: Double
        get() = customPrice ?: product.price

    val totalDiscount: Double
        get() = ((customPrice ?: product.price) * quantity * discount / 100)

    val profit: Double
        get() {
            val cost = product.cost ?: 0.0
            return (unitPrice - cost) * quantity
        }
}