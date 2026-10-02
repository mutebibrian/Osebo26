package com.devbrian.osebo.fragments

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.ui.screens.ShopDashboardProductUi
import com.devbrian.osebo.ui.screens.ShopDashboardScreen
import com.devbrian.osebo.ui.screens.ShopDashboardUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.DashboardViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class ShopDashboardFragment : Fragment() {
    private val args: ShopDashboardFragmentArgs by navArgs()
    private val viewModel: DashboardViewModel by viewModel()
    private var uiState by mutableStateOf(ShopDashboardUiState())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val preferences = PreferenceManager.getInstance(requireContext())
        uiState = uiState.copy(
            shopName = preferences.getCurrentShopName().ifBlank { "Your shop" },
        )

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OseboTheme {
                    ShopDashboardScreen(
                        state = uiState,
                        onBackClick = { findNavController().navigateUp() },
                        onRefreshClick = ::refreshDashboard,
                        onEmployeesClick = { navigateTo(R.id.employeesFragment) },
                        onSuppliersClick = { navigateTo(R.id.suppliersFragment) },
                        onCustomersClick = { navigateTo(R.id.customersFragment) },
                        onSalesClick = { navigateTo(R.id.salesFragment) },
                        onNewSaleClick = { navigateTo(R.id.newSaleFragment) },
                        onAddProductClick = { navigateTo(R.id.addProductFragment) },
                        onAddExpenseClick = { navigateTo(R.id.addExpenseFragment) },
                        onInventoryClick = { navigateTo(R.id.inventoryFragment) },
                        onProductClick = ::openProduct,
                        onCashInClick = {
                            Toast.makeText(requireContext(), "Cash in recording is coming soon", Toast.LENGTH_SHORT).show()
                        },
                        onCashOutClick = {
                            Toast.makeText(requireContext(), "Cash out recording is coming soon", Toast.LENGTH_SHORT).show()
                        },
                        onQuickActionsClick = {
                            Toast.makeText(requireContext(), "Quick actions are coming soon", Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shopId = args.shopId
        Log.d("ShopDashboard", "Loading dashboard for shop: $shopId")

        PreferenceManager.getInstance(requireContext()).apply {
            saveCurrentShopId(shopId)
            saveCurrentShopUuid(shopId)
            uiState = uiState.copy(
                shopName = getCurrentShopName().ifBlank { "Your shop" },
            )
        }

        observeViewModel()
        viewModel.loadDashboardData()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dashboardState.collect { state ->
                when (state) {
                    is DashboardViewModel.DashboardState.Loading -> {
                        uiState = uiState.copy(
                            isLoading = true,
                            errorMessage = null,
                        )
                    }

                    is DashboardViewModel.DashboardState.Success -> {
                        val data = state.data
                        uiState = uiState.copy(
                            hasData = true,
                            isLoading = false,
                            errorMessage = null,
                            employeesCount = data.employeesCount,
                            suppliersCount = data.suppliersCount,
                            customersCount = data.customersCount,
                            totalSales = formatCurrency(data.totalSales),
                            estimatedProfit = formatCurrency(data.totalSales * 0.3),
                            openingBalance = formatCurrency(data.openingBalance),
                            closingBalance = formatCurrency(data.closingBalance),
                            todayTotalSales = formatCurrency(data.todaySales),
                            todayCashFlowExpenses = formatCurrency(data.totalExpenses),
                            depositsAndAdvancePayments = formatCurrency(data.advancePayments),
                            todayCreditSales = formatCurrency(data.todayCreditSales),
                            todayCashSales = formatCurrency(data.todayPaidSales),
                            oldBalancePayments = formatCurrency(data.oldBalances),
                        )
                    }

                    is DashboardViewModel.DashboardState.Error -> {
                        uiState = uiState.copy(
                            isLoading = false,
                            errorMessage = state.message,
                        )
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.timeSeriesData.collect { timeSeries ->
                timeSeries ?: return@collect
                val totalSales = timeSeries.sales.sum()
                val totalExpenses = timeSeries.expenses.sum()
                uiState = uiState.copy(
                    periodSales = formatCurrency(totalSales),
                    periodExpenses = formatCurrency(totalExpenses),
                    periodProfit = formatCurrency(totalSales - totalExpenses),
                    salesTrend = timeSeries.sales.map(Double::toFloat),
                    trendLabels = timeSeries.xAxis,
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topStockItems.collect { products ->
                uiState = uiState.copy(
                    topProducts = products.map { product ->
                        ShopDashboardProductUi(
                            id = product.id,
                            name = product.name,
                            quantity = product.totalQuantitySold,
                            sales = formatCurrency(product.totalSalesAmount),
                        )
                    },
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isOffline.collect { isOffline ->
                uiState = uiState.copy(isOffline = isOffline)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lastUpdated.collect { lastUpdated ->
                uiState = uiState.copy(lastUpdated = lastUpdated)
            }
        }
    }

    private fun refreshDashboard() {
        viewModel.refreshData()
    }

    private fun navigateTo(destinationId: Int) {
        runCatching { findNavController().navigate(destinationId) }
            .onFailure {
                Toast.makeText(requireContext(), "Unable to open this page", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openProduct(productId: String) {
        val bundle = Bundle().apply { putString("productId", productId) }
        runCatching { findNavController().navigate(R.id.productDetailsFragment, bundle) }
            .onFailure {
                Toast.makeText(requireContext(), "Unable to open this product", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatCurrency(amount: Double): String = when {
        amount >= 1_000_000 -> String.format(Locale.US, "UGX %.1fM", amount / 1_000_000)
        amount >= 1_000 -> String.format(Locale.US, "UGX %.1fK", amount / 1_000)
        else -> String.format(Locale.US, "UGX %,.0f", amount)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDataIfNeeded()
    }
}
