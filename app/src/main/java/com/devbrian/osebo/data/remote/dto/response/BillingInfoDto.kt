package com.devbrian.osebo.data.remote.dto.response

data class BillingInfoDto(
    val paymentMethod: String,
    val paymentMethodLastFour: String?,
    val billingEmail: String,
    val billingAddress: String?,
    val nextBillingDate: String,
    val defaultCurrency: String
)