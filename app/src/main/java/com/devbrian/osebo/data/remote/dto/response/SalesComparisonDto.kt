package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName


data class SalesComparisonDto(
    @SerializedName("current")
    val currentSales: Double = 0.0,

    @SerializedName("previous")
    val previousSales: Double = 0.0,

    @SerializedName("difference")
    val difference: Double = 0.0,

    @SerializedName("percentage")
    val growthPercentage: Double = 0.0,

    @SerializedName("isIncrease")
    val isIncrease: Boolean = true,

    @SerializedName("range")
    val range: String = "daily",

    @SerializedName("timeFilter")
    val timeFilter: TimeFilterDto? = null
) {
    // Computed properties for backward compatibility
    val totalSales: Double
        get() = currentSales

    val previousPeriodSales: Double
        get() = previousSales
}

data class TimeFilterDto(
    @SerializedName("current")
    val current: PeriodDto? = null,

    @SerializedName("previous")
    val previous: PeriodDto? = null
)

data class PeriodDto(
    @SerializedName("start")
    val start: String = "",

    @SerializedName("end")
    val end: String = ""
)

data class PeriodData(
    val sales: Double,
    val expenses: Double,
    val profit: Double,
    val transactionCount: Int
)


data class ShopTotalsDto(
    val totalSales: Double,
    val totalExpenses: Double,
    val totalProfit: Double,
    val totalTransactions: Int,
    val averageTransactionValue: Double,
    val bestSellingCategory: String,
    val mostActiveDay: String
)



data class SalesDataPoint(
    @SerializedName("label")
    val label: String = "",
    @SerializedName("sales")
    val sales: Double = 0.0,
    @SerializedName("expenses")
    val expenses: Double = 0.0,
    @SerializedName("profit")
    val profit: Double = 0.0
)
