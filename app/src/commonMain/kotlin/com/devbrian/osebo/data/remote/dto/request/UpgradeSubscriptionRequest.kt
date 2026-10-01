package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.Serializable
@Serializable
data class UpgradeSubscriptionRequest(
    val planId: String,
    val immediate: Boolean = true,
    val prorate: Boolean = true
)


