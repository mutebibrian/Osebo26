package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FinanceSummaryDto(
    @SerialName("total_revenue")
    val totalRevenue: Double,

    @SerialName("total_expenses")
    val totalExpenses: Double,

    @SerialName("net_profit")
    val netProfit: Double,

    @SerialName("pending_payments")
    val pendingPayments: Double,

    @SerialName("currency")
    val currency: String = "UGX"
)


