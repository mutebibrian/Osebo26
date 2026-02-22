package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class PollPaymentStatusRequest(
    @SerializedName("transaction_id")
    val transactionId: String
)