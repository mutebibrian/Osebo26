package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PollPaymentStatusRequest(
    @SerialName("transaction_id")
    val transactionId: String
)


