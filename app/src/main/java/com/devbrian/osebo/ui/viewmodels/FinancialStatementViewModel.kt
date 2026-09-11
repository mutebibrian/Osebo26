package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.repository.FinanceRepository
import com.devbrian.osebo.models.FinancialStatement
import com.devbrian.osebo.models.TimeSeriesData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FinancialStatementViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val preferenceManager: PreferenceManager  
) : ViewModel() {

    private val _financialStatement = MutableLiveData<FinancialStatement>()
    val financialStatement: LiveData<FinancialStatement> = _financialStatement

    private val _timeSeriesData = MutableLiveData<List<TimeSeriesData>>()
    val timeSeriesData: LiveData<List<TimeSeriesData>> = _timeSeriesData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadFinancialStatement(startDate: String? = null, endDate: String? = null, period: String = "monthly") {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getFinancialStatement(startDate, endDate, period)
            result.onSuccess { statement ->
                _financialStatement.value = statement
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load financial statement"
            }
            _isLoading.value = false
        }
    }

    fun loadTimeSeriesData(startDate: String? = null, endDate: String? = null, period: String = "monthly") {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val shopId = preferenceManager.getShopIdentifierForApi()
                if (shopId.isEmpty()) {
                    _error.value = "No shop selected"
                    _isLoading.value = false
                    return@launch
                }

                
                
                
                
                
                
                
                
                
                

                
                _timeSeriesData.value = generateMockTimeSeriesData(period)
                _error.value = null

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load time series data"
                _timeSeriesData.value = generateMockTimeSeriesData(period)
            } finally {
                _isLoading.value = false
            }
        }
    }

    
    private fun generateMockTimeSeriesData(period: String): List<TimeSeriesData> {
        return when (period) {
            "daily" -> listOf(
                TimeSeriesData("Mon", 45000.0, 18000.0, 27000.0),
                TimeSeriesData("Tue", 52000.0, 19500.0, 32500.0),
                TimeSeriesData("Wed", 48000.0, 21000.0, 27000.0),
                TimeSeriesData("Thu", 60000.0, 22500.0, 37500.0),
                TimeSeriesData("Fri", 55000.0, 22000.0, 33000.0),
                TimeSeriesData("Sat", 47000.0, 18800.0, 28200.0),
                TimeSeriesData("Sun", 58000.0, 23200.0, 34800.0)
            )
            "weekly" -> listOf(
                TimeSeriesData("Week 1", 125000.0, 45000.0, 80000.0),
                TimeSeriesData("Week 2", 150000.0, 52000.0, 98000.0),
                TimeSeriesData("Week 3", 110000.0, 38000.0, 72000.0),
                TimeSeriesData("Week 4", 175000.0, 61000.0, 114000.0)
            )
            "monthly" -> listOf(
                TimeSeriesData("Jan", 450000.0, 180000.0, 270000.0),
                TimeSeriesData("Feb", 520000.0, 195000.0, 325000.0),
                TimeSeriesData("Mar", 480000.0, 210000.0, 270000.0),
                TimeSeriesData("Apr", 600000.0, 225000.0, 375000.0),
                TimeSeriesData("May", 550000.0, 220000.0, 330000.0),
                TimeSeriesData("Jun", 470000.0, 188000.0, 282000.0)
            )
            "quarterly" -> listOf(
                TimeSeriesData("Q1", 1450000.0, 585000.0, 865000.0),
                TimeSeriesData("Q2", 1680000.0, 630000.0, 1050000.0),
                TimeSeriesData("Q3", 1920000.0, 720000.0, 1200000.0),
                TimeSeriesData("Q4", 2100000.0, 787000.0, 1313000.0)
            )
            "yearly" -> listOf(
                TimeSeriesData("2023", 6500000.0, 2500000.0, 4000000.0),
                TimeSeriesData("2024", 7200000.0, 2800000.0, 4400000.0)
            )
            else -> listOf(
                TimeSeriesData("Jan", 450000.0, 180000.0, 270000.0),
                TimeSeriesData("Feb", 520000.0, 195000.0, 325000.0),
                TimeSeriesData("Mar", 480000.0, 210000.0, 270000.0)
            )
        }
    }

    fun getSalesDetails(): String {
        val statement = _financialStatement.value
        return if (statement != null) {
            "Total Sales: UGX ${NumberFormat.getNumberInstance(Locale.US).format(statement.sales)}\n\n" +
                    "Sales represent all revenue from product sales and services.\n\n" +
                    "Breakdown:\n" +
                    "• Product Sales: 85%\n" +
                    "• Services: 15%"
        } else {
            "No sales data available"
        }
    }

    fun getPurchasesDetails(): String {
        val statement = _financialStatement.value
        return if (statement != null) {
            "Total Purchases: UGX ${NumberFormat.getNumberInstance(Locale.US).format(statement.purchases)}\n\n" +
                    "Purchases include inventory, packaging materials, and other direct costs.\n\n" +
                    "Breakdown:\n" +
                    "• Inventory: 70%\n" +
                    "• Packaging: 20%\n" +
                    "• Other: 10%"
        } else {
            "No purchases data available"
        }
    }

    fun getExpensesDetails(): String {
        val statement = _financialStatement.value
        return if (statement != null) {
            "Total Expenses: UGX ${NumberFormat.getNumberInstance(Locale.US).format(statement.expenses)}\n\n" +
                    "Expenses include rent, utilities, salaries, and other operational costs.\n\n" +
                    "Breakdown:\n" +
                    "• Rent: 35%\n" +
                    "• Utilities: 15%\n" +
                    "• Salaries: 40%\n" +
                    "• Other: 10%"
        } else {
            "No expenses data available"
        }
    }

    fun getInventoryDetails(): String {
        val statement = _financialStatement.value
        return if (statement != null) {
            "Inventory Value: UGX ${NumberFormat.getNumberInstance(Locale.US).format(statement.inventory)}\n\n" +
                    "Current value of all stock items in inventory.\n\n" +
                    "Top Items:\n" +
                    "• Electronics: 45%\n" +
                    "• Clothing: 30%\n" +
                    "• Accessories: 25%"
        } else {
            "No inventory data available"
        }
    }

    fun clearError() {
        _error.value = null
    }
}
