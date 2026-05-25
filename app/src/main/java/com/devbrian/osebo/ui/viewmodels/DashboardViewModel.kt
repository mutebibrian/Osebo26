package com.devbrian.osebo.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.DashboardRepository
import com.devbrian.osebo.data.local.entity.DashboardSummaryEntity
import com.devbrian.osebo.data.local.entity.TimeSeriesEntity
import com.devbrian.osebo.data.local.entity.TopStockItemEntity
import com.devbrian.osebo.data.remote.dto.response.TopStockItemDto
import com.devbrian.osebo.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val TAG = "DashboardViewModel"

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _timeSeriesData = MutableStateFlow<TimeSeriesDto?>(null)
    val timeSeriesData: StateFlow<TimeSeriesDto?> = _timeSeriesData.asStateFlow()

    private val _topStockItems = MutableStateFlow<List<TopStockItemDto>>(emptyList())
    val topStockItems: StateFlow<List<TopStockItemDto>> = _topStockItems.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _lastUpdated = MutableStateFlow<String?>(null)
    val lastUpdated: StateFlow<String?> = _lastUpdated.asStateFlow()

    private var isInitialLoadComplete = false

    init {
        Log.d(TAG, "🏁 ViewModel initialized")
        observeDatabase()
    }

    private fun observeDatabase() {
        Log.d(TAG, "👀 Starting database observation")

        combine(
            repository.getDashboardSummary().onEach {
                Log.d(TAG, "📊 Dashboard summary flow emitted: ${it != null}")
            },
            repository.getTimeSeries().onEach {
                Log.d(TAG, "📈 Time series flow emitted: ${it != null}")
            },
            repository.getTopStockItems().onEach { items ->
                Log.d(TAG, "📦 Top stock items flow emitted: ${items.size} items")
                items.forEachIndexed { index, item ->
                    Log.d(TAG, "   📦 Item[$index]: ${item.name}, qty: ${item.quantity}, sales: ${item.sales}")
                }
            }
        ) { summary, timeSeries, topItems ->
            Log.d(TAG, "🔄 Combine triggered - updating UI")
            updateUI(summary, timeSeries, topItems)
        }.launchIn(viewModelScope)
    }

    private fun updateUI(
        summary: DashboardSummaryEntity?,
        timeSeriesEntity: TimeSeriesEntity?,
        topItems: List<TopStockItemEntity>
    ) {
        Log.d(TAG, "🔍 ===== UPDATE UI START =====")
        Log.d(TAG, "   summary: ${summary != null}")
        Log.d(TAG, "   timeSeries: ${timeSeriesEntity != null}")
        Log.d(TAG, "   topItems count: ${topItems.size}")

        topItems.forEachIndexed { index, entity ->
            Log.d(TAG, "   📦 Entity[$index]: ${entity.name}, qty: ${entity.quantity}, sales: ${entity.sales}")
        }

        // Update dashboard state with summary data
        if (summary != null) {
            Log.d(TAG, "📊 Summary data - Employees: ${summary.employeesCount}, Suppliers: ${summary.suppliersCount}, Customers: ${summary.customersCount}, Sales: ${summary.totalSales}, Expenses: ${summary.totalExpenses}")

            _dashboardState.value = DashboardState.Success(
                DashboardData(
                    employeesCount = summary.employeesCount,
                    suppliersCount = summary.suppliersCount,
                    customersCount = summary.customersCount,
                    totalSales = summary.totalSales,
                    totalExpenses = summary.totalExpenses  // FIXED: Added totalExpenses here
                )
            )
            Log.d(TAG, "✅ DashboardState updated to Success")
        }

        // Update time series data
        if (timeSeriesEntity != null) {
            Log.d(TAG, "📈 Time series data - xAxis size: ${timeSeriesEntity.getXAxisList().size}")

            _timeSeriesData.value = TimeSeriesDto(
                xAxis = timeSeriesEntity.getXAxisList(),
                sales = timeSeriesEntity.getSalesList(),
                expenses = timeSeriesEntity.getExpensesList()
            )
            Log.d(TAG, "✅ TimeSeriesData updated")
        }

        // Update top stock items
        if (topItems.isNotEmpty()) {
            Log.d(TAG, "📦 Processing ${topItems.size} top stock items")

            val dtoList = topItems.map { entity ->
                TopStockItemDto(
                    id = entity.id,
                    name = entity.name,
                    totalQuantitySold = entity.quantity,
                    totalSalesAmount = entity.sales
                )
            }

            _topStockItems.value = dtoList
            Log.d(TAG, "✅ _topStockItems updated with ${dtoList.size} items")
            Log.d(TAG, "   First item: ${dtoList.firstOrNull()?.name}")
        } else {
            Log.d(TAG, "⚠️ topItems is empty - clearing _topStockItems")
            _topStockItems.value = emptyList()
        }

        // Mark initial load as complete
        if (summary != null || timeSeriesEntity != null || topItems.isNotEmpty()) {
            isInitialLoadComplete = true
            Log.d(TAG, "✅ Initial load marked as complete")
        }

        // Update last updated timestamp
        viewModelScope.launch {
            try {
                val lastUpdateTime = repository.getLastUpdateTime()
                Log.d(TAG, "⏰ Last update time from repository: $lastUpdateTime")
                _lastUpdated.value = formatLastUpdated(lastUpdateTime)
                Log.d(TAG, "✅ LastUpdated set to: ${_lastUpdated.value}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting last update time: ${e.message}")
            }
        }

        Log.d(TAG, "🔍 ===== UPDATE UI END =====\n")
    }

    private fun formatLastUpdated(timestamp: Long?): String? {
        return timestamp?.let {
            try {
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val formatted = dateFormat.format(Date(it))
                Log.d(TAG, "⏰ Formatted timestamp: $timestamp -> $formatted")
                formatted
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error formatting timestamp: ${e.message}")
                null
            }
        }
    }

    fun loadDashboardData() {
        Log.d(TAG, "🔄 loadDashboardData called")
        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            Log.d(TAG, "⏳ DashboardState set to Loading")

            val isNetworkAvailable = isNetworkAvailable()
            Log.d(TAG, "🌐 Network available: $isNetworkAvailable")
            _isOffline.value = !isNetworkAvailable

            if (!_isOffline.value) {
                Log.d(TAG, "📡 Online - refreshing dashboard data")
                repository.refreshDashboardData()
            } else {
                Log.d(TAG, "📴 Offline - checking cached data")
                val hasData = repository.hasCachedData()
                Log.d(TAG, "💾 Has cached data: $hasData")

                if (!hasData) {
                    Log.d(TAG, "❌ No cached data and offline - showing error")
                    _dashboardState.value = DashboardState.Error("No internet connection and no cached data")
                } else {
                    Log.d(TAG, "✅ Using cached data while offline")
                }
            }
        }
    }

    fun refreshData() {
        Log.d(TAG, "🔄 refreshData called")
        viewModelScope.launch {
            val isNetworkAvailable = isNetworkAvailable()
            Log.d(TAG, "🌐 Network available: $isNetworkAvailable")
            _isOffline.value = !isNetworkAvailable

            if (!_isOffline.value) {
                Log.d(TAG, "📡 Online - refreshing data")
                repository.refreshDashboardData()
                updateLastUpdated()
                Log.d(TAG, "✅ Refresh complete")
            } else {
                Log.d(TAG, "📴 Offline - cannot refresh")
                _isOffline.value = true
            }
        }
    }

    fun refreshDataIfNeeded() {
        Log.d(TAG, "🔄 refreshDataIfNeeded called")
        viewModelScope.launch {
            val isNetworkAvailable = isNetworkAvailable()
            val shouldRefresh = !_isOffline.value && repository.shouldRefreshData()

            Log.d(TAG, "   Network available: $isNetworkAvailable")
            Log.d(TAG, "   Should refresh: $shouldRefresh")

            if (shouldRefresh) {
                Log.d(TAG, "📡 Conditions met - refreshing")
                repository.refreshDashboardData()
                updateLastUpdated()
            } else {
                Log.d(TAG, "⏭️ No refresh needed")
            }
        }
    }

    fun hasCachedData(): Boolean {
        Log.d(TAG, "🔍 hasCachedData called")
        return true
    }

    private suspend fun updateLastUpdated() {
        Log.d(TAG, "⏰ updateLastUpdated called")
        try {
            val lastUpdateTime = repository.getLastUpdateTime()
            Log.d(TAG, "   Last update time: $lastUpdateTime")
            _lastUpdated.value = formatLastUpdated(lastUpdateTime)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating last updated: ${e.message}")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        Log.d(TAG, "🌐 isNetworkAvailable called - returning true (placeholder)")
        return true
    }

    fun getTopStockItemsCount(): Int = _topStockItems.value.size

    fun logCurrentState() {
        Log.d(TAG, "📊 ===== CURRENT STATE =====")
        Log.d(TAG, "   dashboardState: ${_dashboardState.value}")
        Log.d(TAG, "   timeSeriesData: ${_timeSeriesData.value != null}")
        Log.d(TAG, "   topStockItems count: ${_topStockItems.value.size}")
        Log.d(TAG, "   isOffline: ${_isOffline.value}")
        Log.d(TAG, "   lastUpdated: ${_lastUpdated.value}")
        Log.d(TAG, "   isInitialLoadComplete: $isInitialLoadComplete")
        Log.d(TAG, "===========================")
    }

    data class DashboardData(
        val employeesCount: Int,
        val suppliersCount: Int,
        val customersCount: Int,
        val totalSales: Double,
        val totalExpenses: Double  // Keep this field
    ) {
        init {
            Log.d(TAG, "📊 DashboardData created: Emp:$employeesCount, Sup:$suppliersCount, Cust:$customersCount, Sales:$totalSales, Expenses:$totalExpenses")
        }
    }

    data class TimeSeriesDto(
        val xAxis: List<String>,
        val sales: List<Double>,
        val expenses: List<Double>
    ) {
        init {
            Log.d(TAG, "📈 TimeSeriesDto created with ${xAxis.size} points")
        }
    }

    sealed class DashboardState {
        object Loading : DashboardState() {
            init { Log.d(TAG, "⏳ DashboardState: Loading") }
        }
        data class Success(val data: DashboardData) : DashboardState() {
            init { Log.d(TAG, "✅ DashboardState: Success with ${data.employeesCount} employees, Expenses: ${data.totalExpenses}") }
        }
        data class Error(val message: String) : DashboardState() {
            init { Log.d(TAG, "❌ DashboardState: Error - $message") }
        }
    }
}