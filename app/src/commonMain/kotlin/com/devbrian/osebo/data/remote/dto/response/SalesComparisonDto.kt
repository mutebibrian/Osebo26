package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SalesComparisonDto(
    @SerialName("current")
    val currentSales: Double = 0.0,

    @SerialName("previous")
    val previousSales: Double = 0.0,

    @SerialName("difference")
    val difference: Double = 0.0,

    @SerialName("percentage")
    val growthPercentage: Double = 0.0,

    @SerialName("isIncrease")
    val isIncrease: Boolean = true,

    @SerialName("range")
    val range: String = "daily",

    @SerialName("timeFilter")
    val timeFilter: TimeFilterDto? = null
) {
    // Computed properties for backward compatibility
    val totalSales: Double
        get() = currentSales

    val previousPeriodSales: Double
        get() = previousSales
}

@Serializable
data class TimeFilterDto(
    @SerialName("current")
    val current: PeriodDto? = null,

    @SerialName("previous")
    val previous: PeriodDto? = null
)

@Serializable
data class PeriodDto(
    @SerialName("start")
    val start: String = "",

    @SerialName("end")
    val end: String = ""
)

@Serializable
data class PeriodData(
    val sales: Double,
    val expenses: Double,
    val profit: Double,
    val transactionCount: Int
)


@Serializable
data class ShopTotalsDto(
    val totalSales: Double,
    val totalExpenses: Double,
    val totalProfit: Double,
    val totalTransactions: Int,
    val averageTransactionValue: Double,
    val bestSellingCategory: String,
    val mostActiveDay: String
)



@Serializable
data class SalesDataPoint(
    @SerialName("label")
    val label: String = "",
    @SerialName("sales")
    val sales: Double = 0.0,
    @SerialName("expenses")
    val expenses: Double = 0.0,
    @SerialName("profit")
    val profit: Double = 0.0
)
