package com.devbrian.osebo.data.remote.dto.request

data class UpgradeSubscriptionRequest(
    val planId: String,
    val immediate: Boolean = true,
    val prorate: Boolean = true
)


