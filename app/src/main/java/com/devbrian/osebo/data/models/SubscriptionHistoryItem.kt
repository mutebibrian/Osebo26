package com.devbrian.osebo.models


data class SubscriptionHistoryItem(
    val id: String,
    val planName: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: String,
    val status: String,
    val date: String
)


