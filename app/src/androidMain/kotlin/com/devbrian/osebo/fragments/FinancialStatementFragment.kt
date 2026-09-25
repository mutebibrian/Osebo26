package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.models.FinancialStatement
import com.devbrian.osebo.ui.screens.FinancialStatementScreen
import com.devbrian.osebo.ui.screens.FinancialStatementUiState
import com.devbrian.osebo.ui.screens.StatementMetric
import com.devbrian.osebo.ui.screens.StatementTrendUi
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.FinancialStatementViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FinancialStatementFragment : Fragment() {
    private val viewModel: FinancialStatementViewModel by viewModel()
    private var uiState by mutableStateOf(FinancialStatementUiState())

    private val periods = listOf(
        "This Week",
        "This Month",
        "Last Month",
        "This Quarter",
        "This Year",
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        uiState = uiState.copy(
            shopName = PreferenceManager.getInstance(requireContext())
                .getCurrentShopName()
                .ifBlank { "My shop" },
        )

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OseboTheme {
                    FinancialStatementScreen(
                        state = uiState,
                        periods = periods,
                        onBackClick = { findNavController().navigateUp() },
                        onPeriodSelected = ::selectPeriod,
                        onMetricClick = ::showMetricDetails,
                        onExportClick = ::exportToExcel,
                        onPrintClick = ::printStatement,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        loadData()
    }

    private fun observeViewModel() {
        viewModel.financialStatement.observe(viewLifecycleOwner, ::updateStatement)

        viewModel.timeSeriesData.observe(viewLifecycleOwner) { data ->
            uiState = uiState.copy(
                trend = data.map { point ->
                    StatementTrendUi(
                        label = point.label,
                        value = point.sales.toFloat(),
                    )
                },
            )
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            uiState = uiState.copy(isLoading = isLoading)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun selectPeriod(period: String) {
        if (period == uiState.selectedPeriod) return
        uiState = uiState.copy(selectedPeriod = period)
        loadData()
    }

    private fun loadData() {
        val (startDate, endDate) = getDateRangeForPeriod(uiState.selectedPeriod)
        val chartPeriod = getChartPeriod(uiState.selectedPeriod)
        viewModel.loadFinancialStatement(startDate, endDate)
        viewModel.loadTimeSeriesData(startDate, endDate, chartPeriod)
    }

    private fun updateStatement(statement: FinancialStatement) {
        val profitMargin = if (statement.sales > 0) {
            statement.netProfit / statement.sales * 100
        } else {
            0.0
        }

        uiState = uiState.copy(
            sales = formatCurrency(statement.sales),
            purchases = formatCurrency(statement.purchases),
            expenses = formatCurrency(statement.expenses),
            inventory = formatCurrency(statement.inventory),
            grossMargin = formatCurrency(statement.grossMargin),
            netProfit = formatCurrency(statement.netProfit),
            profitMargin = String.format(Locale.US, "%.1f%%", profitMargin),
            isProfitPositive = statement.netProfit >= 0,
        )
    }

    private fun showMetricDetails(metric: StatementMetric) {
        val (title, details) = when (metric) {
            StatementMetric.Sales -> "Sales" to viewModel.getSalesDetails()
            StatementMetric.Purchases -> "Purchases" to viewModel.getPurchasesDetails()
            StatementMetric.Expenses -> "Expenses" to viewModel.getExpensesDetails()
            StatementMetric.Inventory -> "Inventory" to viewModel.getInventoryDetails()
        }
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun exportToExcel() {
        Toast.makeText(requireContext(), "Exporting to Excel...", Toast.LENGTH_SHORT).show()
        viewModel.exportToExcel()
    }

    private fun printStatement() {
        Toast.makeText(requireContext(), "Printing statement...", Toast.LENGTH_SHORT).show()
    }

    private fun formatCurrency(amount: Double): String =
        "UGX ${NumberFormat.getNumberInstance(Locale.US).format(amount.toInt())}"

    private fun getDateRangeForPeriod(period: String): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return when (period) {
            "This Week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val start = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                start to dateFormat.format(calendar.time)
            }
            "This Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                start to dateFormat.format(calendar.time)
            }
            "Last Month" -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                start to dateFormat.format(calendar.time)
            }
            "This Quarter" -> {
                val quarter = calendar.get(Calendar.MONTH) / 3
                calendar.set(Calendar.MONTH, quarter * 3)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = dateFormat.format(calendar.time)
                calendar.set(Calendar.MONTH, quarter * 3 + 2)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                start to dateFormat.format(calendar.time)
            }
            "This Year" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                val start = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                start to dateFormat.format(calendar.time)
            }
            else -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                start to dateFormat.format(calendar.time)
            }
        }
    }

    private fun getChartPeriod(period: String): String = when (period) {
        "This Week" -> "daily"
        "This Month", "Last Month" -> "weekly"
        "This Quarter" -> "monthly"
        "This Year" -> "quarterly"
        else -> "monthly"
    }
}
