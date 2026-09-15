package com.devbrian.osebo.data.remote.dto.request


data class SubscribeRequest(
    val planId: String,
    val paymentMethod: String,
    val autoRenew: Boolean = true
)


