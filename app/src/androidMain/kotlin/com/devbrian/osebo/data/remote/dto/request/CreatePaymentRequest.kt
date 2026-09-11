package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class CreatePaymentRequest(
    @SerializedName("amount") val amount: Double,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("subscription_id") val subscriptionId: String? = null,
    @SerializedName("shop_id") val shopId: String? = null,
    @SerializedName("description") val description: String? = null
)


