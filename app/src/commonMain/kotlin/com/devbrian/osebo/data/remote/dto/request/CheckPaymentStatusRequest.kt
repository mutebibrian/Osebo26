package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckPaymentStatusRequest(
    @SerialName("operatorType")
    val operatorType: String,

    @SerialName("invoiceNo")
    val invoiceNo: String
)


