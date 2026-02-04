package com.devbrian.osebo.data.remote.dto.request

data class DowngradeSubscriptionRequest(
    val planId: String,
    val atPeriodEnd: Boolean = true
)