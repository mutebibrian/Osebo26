package com.devbrian.osebo.data.repository

import android.util.Log
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.local.entity.*
import com.devbrian.osebo.models.TimeSeriesData
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val apiService: ApiService,
    private val preferences: PreferenceManager,
    private val database: AppDatabase
) {
    private val dao = database.dashboardDao()
    private val gson = Gson()

    companion object {
        private const val TAG = "DashboardRepository"
        private const val CACHE_VALIDITY_PERIOD = 30 * 60 * 1000
    }

    fun getDashboardSummary(): Flow<DashboardSummaryEntity?> = dao.getDashboardSummary()
    fun getTimeSeries(): Flow<TimeSeriesEntity?> = dao.getTimeSeries()
    fun getTopStockItems(): Flow<List<TopStockItemEntity>> = dao.getTopStockItems()
    fun getShopSummary(shopId: String): Flow<ShopSummaryEntity?> = dao.getShopSummary(shopId)
    fun getFinancialStatement(): Flow<FinancialStatementEntity?> = dao.getFinancialStatement()

    suspend fun getDashboardSummarySync(): DashboardSummaryEntity? = dao.getDashboardSummarySync()
    suspend fun getTimeSeriesSync(): TimeSeriesEntity? = dao.getTimeSeriesSync()
    suspend fun getTopStockItemsSync(): List<TopStockItemEntity> = dao.getTopStockItemsSync()

    suspend fun hasCachedData(): Boolean = dao.hasDashboardData() > 0
    suspend fun getLastUpdateTime(): Long? = dao.getLastUpdateTime()

    suspend fun isCacheValid(): Boolean {
        val lastUpdate = getLastUpdateTime() ?: return false
        return (System.currentTimeMillis() - lastUpdate) < CACHE_VALIDITY_PERIOD
    }

    suspend fun refreshDashboardData() {
        val shopId = preferences.getCurrentShopId()
        if (shopId.isEmpty()) {
            Log.e(TAG, "No shop selected")
            return
        }

        Log.d(TAG, "Refreshing dashboard data for shop: $shopId")

        try {
            fetchAndSaveShopSummary(shopId)
            fetchAndSaveTimeSeries(shopId)
            fetchAndSaveTopStockItems(shopId)
            fetchAndSaveFinancialStatement(shopId)

            Log.d(TAG, "Dashboard data refreshed successfully")

        } catch (e: UnknownHostException) {
            Log.e(TAG, "Network error - no internet: ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "IO error: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}")
            e.printStackTrace()
        }
    }

    private suspend fun fetchAndSaveShopSummary(shopId: String) {
        try {
            val response = apiService.getShopSummary(shopId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { summary ->
                    val dashboardEntity = DashboardSummaryEntity(
                        employeesCount = summary.totalEmployees,
                        suppliersCount = summary.totalSuppliers,
                        customersCount = summary.totalCustomers,
                        totalSales = summary.totalSales,
                        totalExpenses = summary.totalExpenses  // Use totalExpenses from your DTO
                    )
                    dao.insertDashboardSummary(dashboardEntity)

                    val shopEntity = ShopSummaryEntity(
                        shopId = shopId,
                        shopName = preferences.getCurrentShopName(),
                        totalEmployees = summary.totalEmployees,
                        totalCustomers = summary.totalCustomers,
                        totalSuppliers = summary.totalSuppliers,
                        totalSales = summary.totalSales
                    )
                    dao.insertShopSummary(shopEntity)

                    Log.d(TAG, "Shop summary saved: ${summary.totalEmployees} employees, totalExpenses: ${summary.totalExpenses}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching shop summary: ${e.message}")
        }
    }

    private suspend fun fetchAndSaveTimeSeries(shopId: String) {
        try {
            val response = apiService.getTimeSeries(shopId, "monthly")
            if (response.isSuccessful && response.body()?.success == true) {
                val timeSeriesData = response.body()?.data
                if (timeSeriesData != null && timeSeriesData.xAxis.isNotEmpty()) {
                    val entity = TimeSeriesEntity(
                        xAxis = gson.toJson(timeSeriesData.xAxis),
                        sales = gson.toJson(timeSeriesData.sales),
                        expenses = gson.toJson(timeSeriesData.expenses)
                    )
                    dao.insertTimeSeries(entity)
                    Log.d(TAG, "Time series saved with ${timeSeriesData.xAxis.size} points")
                } else {
                    Log.d(TAG, "No time series data available")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching time series: ${e.message}")
        }
    }

    private suspend fun fetchAndSaveTopStockItems(shopId: String) {
        try {
            val response = apiService.getTopStockItems(shopId)
            if (response.isSuccessful && response.body()?.success == true) {
                val items = response.body()?.data?.items ?: emptyList()
                Log.d(TAG, "📦 API returned ${items.size} top stock items")

                items.forEachIndexed { index, item ->
                    Log.d(TAG, "   API Item[$index]: ${item.name}, quantity: ${item.totalQuantitySold}, sales: ${item.totalSalesAmount}")
                }

                dao.clearTopStockItems()
                if (items.isNotEmpty()) {
                    val entities = items.map { item ->
                        TopStockItemEntity(
                            id = item.id,
                            name = item.name,
                            quantity = item.totalQuantitySold,
                            sales = item.totalSalesAmount
                        )
                    }
                    dao.insertTopStockItems(entities)
                    Log.d(TAG, "✅ Saved ${entities.size} top stock items to DB")

                    val savedItems = dao.getTopStockItemsSync()
                    savedItems.forEachIndexed { index, entity ->
                        Log.d(TAG, "   Saved Entity[$index]: ${entity.name}, qty: ${entity.quantity}, sales: ${entity.sales}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching top stock items: ${e.message}")
        }
    }

    private suspend fun fetchAndSaveFinancialStatement(shopId: String) {
        try {
            val response = apiService.getFinancialStatement(shopId, "yearly")
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { financial ->
                    val entity = FinancialStatementEntity(
                        totalSales = financial.totalSales,
                        totalCreditSales = financial.totalCreditSales,
                        totalProcurements = financial.totalProcurements,
                        totalExpenses = financial.totalExpenses
                    )
                    dao.insertFinancialStatement(entity)
                    Log.d(TAG, "Financial statement saved")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching financial statement: ${e.message}")
        }
    }

    suspend fun clearAllDashboardData() {
        dao.clearDashboardSummary()
        dao.clearTimeSeries()
        dao.clearTopStockItems()
        dao.clearFinancialStatement()
        Log.d(TAG, "All dashboard data cleared")
    }

    suspend fun deleteOldStockItems(cutoffTime: Long) {
        dao.deleteOldStockItems(cutoffTime)
    }

    suspend fun shouldRefreshData(): Boolean {
        return !isCacheValid() || !hasCachedData()
    }
}