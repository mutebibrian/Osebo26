package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class FinanceSummaryDto(
    @SerializedName("total_revenue")
    val totalRevenue: Double,

    @SerializedName("total_expenses")
    val totalExpenses: Double,

    @SerializedName("net_profit")
    val netProfit: Double,

    @SerializedName("pending_payments")
    val pendingPayments: Double,

    @SerializedName("currency")
    val currency: String = "UGX"
)