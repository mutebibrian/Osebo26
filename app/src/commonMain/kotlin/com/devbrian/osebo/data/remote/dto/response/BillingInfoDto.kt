package com.devbrian.osebo.data.remote.dto.response


import kotlinx.serialization.Serializable
@Serializable
data class BillingInfoDto(
    val paymentMethod: String,
    val paymentMethodLastFour: String?,
    val billingEmail: String,
    val billingAddress: String?,
    val nextBillingDate: String,
    val defaultCurrency: String
)


