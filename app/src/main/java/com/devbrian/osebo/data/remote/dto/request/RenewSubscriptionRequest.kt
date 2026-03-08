package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class RenewSubscriptionRequest(
    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("months")
    val months: Int = 1,

    @SerializedName("auto_renew")
    val autoRenew: Boolean = false,

    @SerializedName("payment_method")
    val paymentMethod: String = "mobile_money",

    @SerializedName("amount")
    val amount: Double? = null,

    @SerializedName("currency")
    val currency: String = "UGX"
)

