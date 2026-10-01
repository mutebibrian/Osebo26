package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.Serializable
@Serializable
data class DowngradeSubscriptionRequest(
    val planId: String,
    val atPeriodEnd: Boolean = true
)


