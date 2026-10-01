package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentAuthorizationRequest(
    @SerialName("transaction_id")
    val transactionId: String,

    @SerialName("phone_number")
    val phoneNumber: String
)


