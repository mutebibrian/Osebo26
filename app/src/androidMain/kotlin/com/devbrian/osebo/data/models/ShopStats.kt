package com.devbrian.osebo.models

data class ShopStats(
    val totalSales: Double,
    val totalExpenses: Double,
    val profit: Double,
    val inventoryValue: Double,
    val totalCustomers: Int,
    val totalEmployees: Int,
    val lowStockItems: Int
)


