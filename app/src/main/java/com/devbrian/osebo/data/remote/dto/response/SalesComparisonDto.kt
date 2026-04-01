package com.devbrian.osebo.data.remote.dto.response


data class SalesComparisonDto(
    val currentPeriod: PeriodData,
    val previousPeriod: PeriodData,
    val percentageChange: Double,
    val trend: String  // "up", "down", or "stable"
)

data class PeriodData(
    val sales: Double,
    val expenses: Double,
    val profit: Double,
    val transactionCount: Int
)

// models/ShopTotalsDto.kt
data class ShopTotalsDto(
    val totalSales: Double,
    val totalExpenses: Double,
    val totalProfit: Double,
    val totalTransactions: Int,
    val averageTransactionValue: Double,
    val bestSellingCategory: String,
    val mostActiveDay: String
)