package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ShopSummaryDto(
    @SerializedName("totalEmployees")
    val totalEmployees: Int = 0,

    @SerializedName("totalCustomers")
    val totalCustomers: Int = 0,

    @SerializedName("totalSuppliers")
    val totalSuppliers: Int = 0,

    @SerializedName("totalSales")
    val totalSales: Double = 0.0,

    @SerializedName("todaySales")
    val todaySales: Double = 0.0,

    @SerializedName("todayExpenses")
    val todayExpenses: Double = 0.0,

    // Cash-book fields: unconfirmed field names, kept nullable so a missing/renamed
    // backend field never crashes Gson parsing - DashboardRepository.fetchCashBookSummary()
    // falls back to 0.0 (or a derived value for closingBalance) when null.
    @SerializedName("openingBalance")
    val openingBalance: Double? = null,

    @SerializedName("closingBalance")
    val closingBalance: Double? = null,

    @SerializedName("todayCreditSales")
    val todayCreditSales: Double? = null,

    @SerializedName("todayCashSales")
    val todayCashSales: Double? = null,

    @SerializedName("depositsAndAdvancePayments")
    val depositsAndAdvancePayments: Double? = null,

    @SerializedName("oldBalancePayments")
    val oldBalancePayments: Double? = null,

    @SerializedName("topRevenueStockItem")
    val topRevenueStockItem: StockItemSummaryDto? = null,

    @SerializedName("mostSoldStockItem")
    val mostSoldStockItem: StockItemSummaryDto? = null
) {
    val totalRevenue: Double get() = totalSales
    val totalExpenses: Double get() = todayExpenses
    val netProfit: Double get() = totalSales - todayExpenses
    val totalOrders: Int get() = 0 // You'll need a separate API for this
    val averageOrderValue: Double get() = if (totalOrders > 0) totalSales / totalOrders else 0.0
}


data class StockItemSummaryDto(
    @SerializedName("name")
    val name: String = "",

    @SerializedName("totalRevenue")
    val totalRevenue: Double = 0.0,

    @SerializedName("totalQuantity")
    val totalQuantity: Int = 0
)






data class TimeSeriesDto(
    @SerializedName("xAxis")
    val xAxis: List<String> = emptyList(),

    @SerializedName("sales")
    val sales: List<Double> = emptyList(),

    @SerializedName("expenses")
    val expenses: List<Double> = emptyList()
) {
    // Computed property for profit (sales - expenses)
    val profit: List<Double>
        get() = sales.zip(expenses).map { it.first - it.second }

    // Get data point at specific index
    fun getDataPoint(index: Int): TimeSeriesDataPoint? {
        return if (index < xAxis.size && index < sales.size && index < expenses.size) {
            TimeSeriesDataPoint(
                label = xAxis[index],
                sales = sales[index],
                expenses = expenses[index],
                profit = sales[index] - expenses[index]
            )
        } else null
    }

    // Get all data points as list
    fun getAllDataPoints(): List<TimeSeriesDataPoint> {
        return xAxis.indices.mapNotNull { getDataPoint(it) }
    }
}

data class TimeSeriesDataPoint(
    val label: String,
    val sales: Double,
    val expenses: Double,
    val profit: Double
)

data class FinancialStatementDto(
    @SerializedName("totalSales")
    val totalSales: Double = 0.0,

    @SerializedName("totalCreditSales")
    val totalCreditSales: Double = 0.0,

    @SerializedName("totalProcurements")
    val totalProcurements: Double = 0.0,

    @SerializedName("totalExpenses")
    val totalExpenses: Double = 0.0
) {
    // Computed properties
    val netProfit: Double
        get() = totalSales - totalExpenses

    val grossProfit: Double
        get() = totalSales - totalProcurements

    val profitMargin: Double
        get() = if (totalSales > 0) (netProfit / totalSales) * 100 else 0.0
}



