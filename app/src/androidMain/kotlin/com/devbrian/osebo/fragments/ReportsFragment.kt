package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.response.SalesComparisonDto
import com.devbrian.osebo.data.remote.dto.response.ShopSummaryDto
import com.devbrian.osebo.ui.screens.ReportsScreen
import com.devbrian.osebo.ui.screens.ReportsUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.ReportsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class ReportsFragment : Fragment() {
    private val viewModel: ReportsViewModel by viewModel()
    private lateinit var preferenceManager: PreferenceManager
    private var uiState by mutableStateOf(ReportsUiState())

    private val periods = listOf("Daily", "Weekly", "Monthly", "Quarterly", "Yearly")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        preferenceManager = PreferenceManager.getInstance(requireContext())
        val hasShop = preferenceManager.getShopIdentifierForApi().isNotEmpty()
        uiState = uiState.copy(
            shopName = preferenceManager.getCurrentShopName().ifBlank { "My shop" },
            hasShop = hasShop,
        )

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OseboTheme {
                    ReportsScreen(
                        state = uiState,
                        periods = periods,
                        onBackClick = { findNavController().navigateUp() },
                        onPeriodSelected = ::selectPeriod,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()

        if (!uiState.hasShop) {
            Toast.makeText(
                requireContext(),
                "No shop selected. Please select a shop first.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::preferenceManager.isInitialized && uiState.hasShop) {
            loadReports()
        }
    }

    private fun observeViewModel() {
        viewModel.salesComparison.observe(viewLifecycleOwner) { comparison ->
            comparison?.let(::updateSalesComparison)
        }

        viewModel.shopSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let(::updateShopSummary)
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
        viewModel.loadSalesComparison(period.lowercase(Locale.US))
    }

    private fun loadReports() {
        viewModel.loadSalesComparison(uiState.selectedPeriod.lowercase(Locale.US))
        viewModel.loadShopSummary()
    }

    private fun updateSalesComparison(data: SalesComparisonDto) {
        uiState = uiState.copy(
            currentSales = formatCurrency(data.currentSales),
            previousSales = formatCurrency(data.previousSales),
            currentSalesValue = data.currentSales.toFloat(),
            previousSalesValue = data.previousSales.toFloat(),
            growth = String.format(Locale.US, "%.1f%%", abs(data.growthPercentage)),
            isIncrease = data.isIncrease,
        )
    }

    private fun updateShopSummary(summary: ShopSummaryDto) {
        uiState = uiState.copy(
            totalRevenue = formatCurrency(summary.totalRevenue),
            totalExpenses = formatCurrency(summary.totalExpenses),
            netProfit = formatCurrency(summary.netProfit),
            isProfitPositive = summary.netProfit >= 0,
            totalOrders = summary.totalOrders.toString(),
            averageOrderValue = formatCurrency(summary.averageOrderValue),
        )
    }

    private fun formatCurrency(value: Double): String =
        "UGX ${NumberFormat.getNumberInstance(Locale.US).format(value.toInt())}"
}
