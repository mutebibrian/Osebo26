package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.R
import com.devbrian.osebo.data.repository.StatisticsRepository
import com.devbrian.osebo.models.SalesDataPoint
import com.devbrian.osebo.models.TopProduct
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: StatisticsRepository
) : ViewModel() {

    // LiveData for statistics summary
    private val _statisticsSummary = MutableLiveData<Resource<StatisticsSummary>>()
    val statisticsSummary: LiveData<Resource<StatisticsSummary>> = _statisticsSummary

    // LiveData for sales chart data
    private val _salesChartData = MutableLiveData<Resource<List<SalesDataPoint>>>()
    val salesChartData: LiveData<Resource<List<SalesDataPoint>>> = _salesChartData

    // LiveData for top products
    private val _topProducts = MutableLiveData<Resource<List<TopProduct>>>()
    val topProducts: LiveData<Resource<List<TopProduct>>> = _topProducts

    // Loading state
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Load all statistics data for a shop
     */
    fun loadStatistics(shopId: String, period: String = "month") {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Load summary stats
                loadStatisticsSummary(shopId, period)

                // Load chart data
                loadSalesChartData(shopId, period)

                // Load top products
                loadTopProducts(shopId, period)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load statistics summary
     */
    fun loadStatisticsSummary(shopId: String, period: String) {
        viewModelScope.launch {
            _statisticsSummary.value = Resource.Loading

            try {
                // In a real app, this would come from the repository
                // val result = repository.getStatisticsSummary(shopId, period)

                // For now, return mock data
                val mockData = getMockStatisticsSummary(period)
                _statisticsSummary.value = Resource.Success(mockData)
            } catch (e: Exception) {
                _statisticsSummary.value = Resource.Error(e.message ?: "Failed to load statistics")
            }
        }
    }

    /**
     * Load sales chart data
     */
    fun loadSalesChartData(shopId: String, period: String) {
        viewModelScope.launch {
            _salesChartData.value = Resource.Loading

            try {
                // In a real app, this would come from the repository
                // val result = repository.getSalesChartData(shopId, period)

                // For now, return mock data
                val mockData = getMockSalesChartData(period)
                _salesChartData.value = Resource.Success(mockData)
            } catch (e: Exception) {
                _salesChartData.value = Resource.Error(e.message ?: "Failed to load chart data")
            }
        }
    }

    /**
     * Load top products
     */
    fun loadTopProducts(shopId: String, period: String) {
        viewModelScope.launch {
            _topProducts.value = Resource.Loading

            try {
                // In a real app, this would come from the repository
                // val result = repository.getTopProducts(shopId, period)

                // For now, return mock data
                val mockData = getMockTopProducts()
                _topProducts.value = Resource.Success(mockData)
            } catch (e: Exception) {
                _topProducts.value = Resource.Error(e.message ?: "Failed to load top products")
            }
        }
    }

    /**
     * Refresh all data
     */
    fun refreshData(shopId: String, period: String) {
        loadStatistics(shopId, period)
    }

    /**
     * Get color resource based on trend
     */
    fun getTrendColor(trend: Double): Int {
        return when {
            trend > 0 -> R.color.success_green
            trend < 0 -> R.color.error_red
            else -> R.color.gray
        }
    }

    /**
     * Get trend icon based on trend
     */
    fun getTrendIcon(trend: Double): Int {
        return when {
            trend > 0 -> R.drawable.ic_trend_up
            trend < 0 -> R.drawable.ic_trend_down
            else -> R.drawable.ic_trend_flat
        }
    }

    /**
     * Mock data for statistics summary
     */
    private fun getMockStatisticsSummary(period: String): StatisticsSummary {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(getCalendarField(period), -1)
        val startDate = calendar.time

        return StatisticsSummary(
            totalSales = 15250.00,
            totalOrders = 245,
            averageOrderValue = 62.24,
            conversionRate = 24.8,
            salesGrowth = 12.5,
            ordersGrowth = 8.2,
            avgOrderGrowth = 3.1,
            conversionGrowth = -2.1,
            periodStart = startDate,
            periodEnd = endDate
        )
    }

    /**
     * Mock data for sales chart
     */
    private fun getMockSalesChartData(period: String): List<SalesDataPoint> {
        return when (period) {
            "day" -> listOf(
                SalesDataPoint("9 AM", 450.0),
                SalesDataPoint("10 AM", 620.0),
                SalesDataPoint("11 AM", 890.0),
                SalesDataPoint("12 PM", 1250.0),
                SalesDataPoint("1 PM", 980.0),
                SalesDataPoint("2 PM", 760.0),
                SalesDataPoint("3 PM", 540.0),
                SalesDataPoint("4 PM", 680.0),
                SalesDataPoint("5 PM", 920.0)
            )
            "week" -> listOf(
                SalesDataPoint("Mon", 1250.0),
                SalesDataPoint("Tue", 1480.0),
                SalesDataPoint("Wed", 1620.0),
                SalesDataPoint("Thu", 1350.0),
                SalesDataPoint("Fri", 1890.0),
                SalesDataPoint("Sat", 2150.0),
                SalesDataPoint("Sun", 980.0)
            )
            "month" -> listOf(
                SalesDataPoint("Week 1", 4250.0),
                SalesDataPoint("Week 2", 5120.0),
                SalesDataPoint("Week 3", 4780.0),
                SalesDataPoint("Week 4", 5620.0)
            )
            "year" -> listOf(
                SalesDataPoint("Jan", 12500.0),
                SalesDataPoint("Feb", 11800.0),
                SalesDataPoint("Mar", 13200.0),
                SalesDataPoint("Apr", 14100.0),
                SalesDataPoint("May", 13800.0),
                SalesDataPoint("Jun", 15200.0),
                SalesDataPoint("Jul", 16500.0),
                SalesDataPoint("Aug", 15800.0),
                SalesDataPoint("Sep", 14900.0),
                SalesDataPoint("Oct", 14300.0),
                SalesDataPoint("Nov", 16100.0),
                SalesDataPoint("Dec", 18500.0)
            )
            else -> emptyList()
        }
    }

    /**
     * Mock data for top products
     */
    private fun getMockTopProducts(): List<TopProduct> {
        return listOf(
            TopProduct(
                id = "prod_001",
                name = "Wireless Headphones",
                sku = "WH-1000XM4",
                quantity = 45,
                revenue = 6750.00,
                imageUrl = null,
                trend = 12.5
            ),
            TopProduct(
                id = "prod_002",
                name = "Smartphone X12",
                sku = "SP-X12",
                quantity = 23,
                revenue = 11500.00,
                imageUrl = null,
                trend = 8.3
            ),
            TopProduct(
                id = "prod_003",
                name = "Laptop Pro 15",
                sku = "LP-15",
                quantity = 12,
                revenue = 18000.00,
                imageUrl = null,
                trend = -2.1
            ),
            TopProduct(
                id = "prod_004",
                name = "Bluetooth Speaker",
                sku = "BT-SPK",
                quantity = 38,
                revenue = 4560.00,
                imageUrl = null,
                trend = 5.7
            ),
            TopProduct(
                id = "prod_005",
                name = "Smart Watch",
                sku = "SW-S9",
                quantity = 29,
                revenue = 8700.00,
                imageUrl = null,
                trend = 15.2
            )
        )
    }

    /**
     * Get calendar field based on period
     */
    private fun getCalendarField(period: String): Int {
        return when (period) {
            "day" -> Calendar.DAY_OF_MONTH
            "week" -> Calendar.WEEK_OF_YEAR
            "month" -> Calendar.MONTH
            "year" -> Calendar.YEAR
            else -> Calendar.MONTH
        }
    }

    /**
     * Data class for statistics summary
     */
    data class StatisticsSummary(
        val totalSales: Double,
        val totalOrders: Int,
        val averageOrderValue: Double,
        val conversionRate: Double,
        val salesGrowth: Double,
        val ordersGrowth: Double,
        val avgOrderGrowth: Double,
        val conversionGrowth: Double,
        val periodStart: Date?,
        val periodEnd: Date?
    )
}