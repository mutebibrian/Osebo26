package com.devbrian.osebo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "dashboard_summary")
data class DashboardSummaryEntity(
    @PrimaryKey val id: String = "dashboard_summary",
    val employeesCount: Int,
    val suppliersCount: Int,
    val customersCount: Int,
    val totalSales: Double,
    val totalExpenses: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "time_series")
data class TimeSeriesEntity(
    @PrimaryKey val id: String = "time_series",
    val xAxis: String,
    val sales: String,
    val expenses: String,
    val lastUpdated: Long = System.currentTimeMillis()
) {

    fun getXAxisList(): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(xAxis, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getSalesList(): List<Double> {
        return try {
            val type = object : TypeToken<List<Double>>() {}.type
            Gson().fromJson(sales, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getExpensesList(): List<Double> {
        return try {
            val type = object : TypeToken<List<Double>>() {}.type
            Gson().fromJson(expenses, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun fromTimeSeriesDto(
            xAxis: List<String>,
            sales: List<Double>,
            expenses: List<Double>
        ): TimeSeriesEntity {
            val gson = Gson()
            return TimeSeriesEntity(
                xAxis = gson.toJson(xAxis),
                sales = gson.toJson(sales),
                expenses = gson.toJson(expenses)
            )
        }
    }
}

@Entity(tableName = "shop_summary")
data class ShopSummaryEntity(
    @PrimaryKey val shopId: String,
    val shopName: String,
    val totalEmployees: Int,
    val totalCustomers: Int,
    val totalSuppliers: Int,
    val totalSales: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)



@Entity(tableName = "financial_statement")
data class FinancialStatementEntity(
    @PrimaryKey val id: String = "financial_statement",
    val totalSales: Double,
    val totalCreditSales: Double,
    val totalProcurements: Double,
    val totalExpenses: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "top_stock_items")
data class TopStockItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val quantity: Int,
    val sales: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)


