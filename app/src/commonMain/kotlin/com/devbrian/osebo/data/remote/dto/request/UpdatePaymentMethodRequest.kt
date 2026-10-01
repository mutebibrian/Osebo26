package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.Serializable
@Serializable
data class UpdatePaymentMethodRequest(
    val paymentMethodId: String,
    val paymentMethodType: String 
)


