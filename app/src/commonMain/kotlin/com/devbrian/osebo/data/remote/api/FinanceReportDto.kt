package com.devbrian.osebo.data.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FinanceReportDto(
    @SerialName("period")
    val period: String,

    @SerialName("total_revenue")
    val totalRevenue: Double,

    @SerialName("total_expenses")
    val totalExpenses: Double,

    @SerialName("net_profit")
    val netProfit: Double
)
