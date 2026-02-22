package com.devbrian.osebo.data.remote.dto.response


import com.devbrian.osebo.models.SaleData
import com.google.gson.annotations.SerializedName

data class SaleResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: SaleData? = null
)



data class SaleItemResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("product_id")
    val productId: String,

    @SerializedName("product_name")
    val productName: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("price")
    val price: Double,

    @SerializedName("discount")
    val discount: Double,

    @SerializedName("subtotal")
    val subtotal: Double
)

data class PaymentResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: PaymentData? = null
)

