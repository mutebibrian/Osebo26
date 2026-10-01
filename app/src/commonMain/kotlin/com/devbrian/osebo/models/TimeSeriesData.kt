package com.devbrian.osebo.models



data class TimeSeriesData(
    val label: String,
    val sales: Double,
    val expenses: Double,
    val netProfit: Double,
    val profit: Double = 0.0
)

data class TransactionFilter(
    val type: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val category: String? = null,
    val searchQuery: String? = null
)
