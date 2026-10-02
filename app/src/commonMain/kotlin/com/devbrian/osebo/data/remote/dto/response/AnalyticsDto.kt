package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShopSummaryDto(
    @SerialName("totalEmployees")
    val totalEmployees: Int = 0,

    @SerialName("totalCustomers")
    val totalCustomers: Int = 0,

    @SerialName("totalSuppliers")
    val totalSuppliers: Int = 0,

    @SerialName("totalSales")
    val totalSales: Double = 0.0,

    @SerialName("todaySales")
    val todaySales: Double = 0.0,

    @SerialName("todayExpenses")
    val todayExpenses: Double = 0.0,

    @SerialName("todayCashIn")
    val todayCashIn: Double = 0.0,

    @SerialName("todayCashOut")
    val todayCashOut: Double = 0.0,

    @SerialName("todayPaidSales")
    val todayPaidSales: Double = 0.0,

    @SerialName("advancePayments")
    val advancePayments: Double = 0.0,

    @SerialName("todayCreditSales")
    val todayCreditSales: Double = 0.0,

    @SerialName("oldBalances")
    val oldBalances: Double = 0.0,

    @SerialName("openingBalance")
    val openingBalance: Double = 0.0,

    @SerialName("closingBalance")
    val closingBalance: Double = 0.0,

    @SerialName("todayMargin")
    val todayMargin: Double = 0.0,

    @SerialName("topRevenueStockItem")
    val topRevenueStockItem: StockItemSummaryDto? = null,

    @SerialName("mostSoldStockItem")
    val mostSoldStockItem: StockItemSummaryDto? = null
) {
    val totalRevenue: Double get() = totalSales
    val totalExpenses: Double get() = todayExpenses
    val netProfit: Double get() = totalSales - todayExpenses
    val totalOrders: Int get() = 0 // You'll need a separate API for this
    val averageOrderValue: Double get() = if (totalOrders > 0) totalSales / totalOrders else 0.0
}


@Serializable
data class StockItemSummaryDto(
    @SerialName("name")
    val name: String = "",

    @SerialName("totalRevenue")
    val totalRevenue: Double = 0.0,

    @SerialName("totalQuantity")
    val totalQuantity: Int = 0
)






@Serializable
data class TimeSeriesDto(
    @SerialName("xAxis")
    val xAxis: List<String> = emptyList(),

    @SerialName("sales")
    val sales: List<Double> = emptyList(),

    @SerialName("expenses")
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

@Serializable
data class TimeSeriesDataPoint(
    val label: String,
    val sales: Double,
    val expenses: Double,
    val profit: Double
)

@Serializable
data class FinancialStatementDto(
    @SerialName("totalSales")
    val totalSales: Double = 0.0,

    @SerialName("totalCreditSales")
    val totalCreditSales: Double = 0.0,

    @SerialName("totalProcurements")
    val totalProcurements: Double = 0.0,

    @SerialName("totalExpenses")
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



