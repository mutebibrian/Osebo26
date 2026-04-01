package com.devbrian.osebo.models

data class FinancialStatement(
    val sales: Double = 0.0,
    val purchases: Double = 0.0,
    val grossMargin: Double = 0.0,
    val expenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val inventory: Double = 0.0,
    val transfers: Double = 0.0,
    val startDate: String? = null,
    val endDate: String? = null,
    val period: String = "monthly"
)