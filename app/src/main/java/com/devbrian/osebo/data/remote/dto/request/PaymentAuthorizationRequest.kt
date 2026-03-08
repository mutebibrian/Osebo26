package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class PaymentAuthorizationRequest(
    @SerializedName("transaction_id")
    val transactionId: String,

    @SerializedName("phone_number")
    val phoneNumber: String
)

