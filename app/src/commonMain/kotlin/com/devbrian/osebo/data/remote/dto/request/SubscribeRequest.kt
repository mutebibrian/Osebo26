package com.devbrian.osebo.data.remote.dto.request



import kotlinx.serialization.Serializable
@Serializable
data class SubscribeRequest(
    val planId: String,
    val paymentMethod: String,
    val autoRenew: Boolean = true
)


