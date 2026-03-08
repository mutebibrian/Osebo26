package com.devbrian.osebo.models

data class TopStockItem(
    val id: String,  // Add this
    val name: String,
    val quantity: Int,
    val sales: Double
)