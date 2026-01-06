package com.devbrian.osebo.models

data class Sale(
    val id: String,
    val customerName: String,
    val amount: Double,
    val date: String,
    val itemsCount: Int,
    val status: String, // Completed, Pending, Cancelled
    val paymentMethod: String? = null,
    val discount: Double? = null,
    val tax: Double? = null
)