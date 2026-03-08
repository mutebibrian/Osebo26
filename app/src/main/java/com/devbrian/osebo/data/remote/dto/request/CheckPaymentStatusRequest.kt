package com.devbrian.osebo.data.remote.dto.request


import com.google.gson.annotations.SerializedName

data class CheckPaymentStatusRequest(
    @SerializedName("operatorType")
    val operatorType: String,

    @SerializedName("invoiceNo")
    val invoiceNo: String
)

