package com.devbrian.osebo.data.remote.dto.response


import com.google.gson.annotations.SerializedName

data class ShopSummaryDto(
    @SerializedName("totalEmployees") val totalEmployees: Int,
    @SerializedName("totalCustomers") val totalCustomers: Int,
    @SerializedName("totalSuppliers") val totalSuppliers: Int,
    @SerializedName("totalSales") val totalSales: Double
)

data class TimeSeriesDto(
    @SerializedName("xAxis") val xAxis: List<String>,
    @SerializedName("sales") val sales: List<Double>,
    @SerializedName("expenses") val expenses: List<Double>
)



data class FinancialStatementDto(
    @SerializedName("totalSales") val totalSales: Double,
    @SerializedName("totalCreditSales") val totalCreditSales: Double,
    @SerializedName("totalProcurements") val totalProcurements: Double,
    @SerializedName("totalExpenses") val totalExpenses: Double
)
