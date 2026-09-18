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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.data.repository.DashboardRepository
import com.devbrian.osebo.data.repository.FinanceRepository
import com.devbrian.osebo.data.repository.ProductRepository
import com.devbrian.osebo.data.repository.SalesRepository
import com.devbrian.osebo.data.repository.ShopRepositoryImpl
import com.devbrian.osebo.models.ShopPerformance
import com.devbrian.osebo.ui.MainActivity
import com.devbrian.osebo.ui.screens.DashboardScreen
import com.devbrian.osebo.ui.screens.DashboardShopPerformanceUi
import com.devbrian.osebo.ui.screens.DashboardShopUi
import com.devbrian.osebo.ui.screens.DashboardUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.utils.PermissionManager
import com.devbrian.osebo.utils.Resource
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import org.koin.android.ext.android.inject

private const val TAG = "MainDashboardFragment"

class MainDashboardFragment : Fragment() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var permissionManager: PermissionManager

    private val shopRepository: ShopRepositoryImpl by inject()
    private val financeRepository: FinanceRepository by inject()
    private val productRepository: ProductRepository by inject()
    private val salesRepository: SalesRepository by inject()
    private val dashboardRepository: DashboardRepository by inject()

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance().apply {
        maximumFractionDigits = 0
        currency = Currency.getInstance("UGX")
    }

    private var uiState by mutableStateOf(DashboardUiState())
    private var currentShops: List<Shop> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        preferenceManager = PreferenceManager.getInstance(requireContext())
        permissionManager = PermissionManager(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {
                OseboTheme {
                    DashboardScreen(
                        state = uiState,
                        onViewDetailsClick = { findNavController().navigate(R.id.reportsFragment) },
                        onPeriodFilterChange = { filter -> uiState = uiState.copy(periodFilter = filter) },
                        onShopClick = ::handleShopClick,
                        onAddProductClick = { navigateIfSubscribed(R.id.addProductFragment) },
                        onNewSaleClick = { navigateIfSubscribed(R.id.newSaleFragment) },
                        onAddEmployeeClick = { navigateIfSubscribed(R.id.employeesFragment) },
                        onReportsClick = {
                            if (hasActiveShopWithSubscription()) {
                                Toast.makeText(requireContext(), "Reports coming soon", Toast.LENGTH_SHORT).show()
                            } else {
                                showNoActiveShopDialog()
                            }
                        },
                        onPerformanceClick = { performance ->
                            currentShops.find { it.id == performance.shopId }?.let { navigateToShopBilling(it) }
                        },
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeader()
        loadDashboardData()
    }

    private fun setupHeader() {
        val userName = preferenceManager.getUserName()
        val firstName = userName.split(" ").firstOrNull() ?: "User"

        val calendar = Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
        val greeting = when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning,"
            in 12..15 -> "Good afternoon,"
            else -> "Good evening,"
        }

        uiState = uiState.copy(
            greeting = greeting,
            userName = firstName,
            currentDate = dateFormat.format(calendar.time),
        )
    }

    private fun handleShopClick(shopUi: DashboardShopUi) {
        val shop = currentShops.find { it.id == shopUi.id } ?: return
        if (shop.isSubscriptionActive) {
            setActiveShop(shop)
            navigateToShopDashboard(shop)
        } else {
            showSubscriptionRequiredDialog(shop)
        }
    }

    private fun navigateIfSubscribed(destinationId: Int) {
        if (hasActiveShopWithSubscription()) findNavController().navigate(destinationId)
        else showNoActiveShopDialog()
    }

    // ===== SHOP NAVIGATION / ACTIONS =====
    private fun navigateToShopDashboard(shop: Shop) {
        try {
            val action = MainDashboardFragmentDirections.actionMainDashboardToShopDashboard(shop.id)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToShopBilling(shop: Shop) {
        try {
            val action = MainDashboardFragmentDirections.actionMainDashboardToShopBilling(shop)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e(TAG, "Navigation to ShopBilling failed", e)
            navigateToSubscriptionPackages(shop)
        }
    }

    private fun navigateToSubscriptionPackages(shop: Shop) {
        try {
            val action = MainDashboardFragmentDirections.actionMainDashboardToSubscriptionPackages(shop)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error navigating to subscriptions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setActiveShop(shop: Shop) {
        if (shop.id.isBlank() || !Shop.isValidUUID(shop.id)) {
            Toast.makeText(requireContext(), "Invalid shop ID", Toast.LENGTH_SHORT).show()
            return
        }

        preferenceManager.saveCurrentShop(
            shopId = shop.id,
            shopUuid = shop.id,
            shopName = shop.name
        )

        if (shop.isSubscriptionActive) {
            val status = if (shop.subscription?.isTrial == true) "TRIAL" else "ACTIVE"
            preferenceManager.saveSubscriptionInfo(
                subscriptionId = shop.subscription?.id,
                status = status,
                type = shop.subscription?.packageType,
                expiry = shop.subscription?.endsAt,
                packageId = null
            )
        } else {
            preferenceManager.clearSubscriptionInfo()
        }

        (activity as? MainActivity)?.refreshNavigationMenu()
        Toast.makeText(requireContext(), "${shop.name} is now active", Toast.LENGTH_SHORT).show()
    }

    private fun saveCurrentShop(shop: Shop) {
        val uuid = shop.uuid ?: shop.id
        preferenceManager.saveCurrentShopId(shop.id)
        preferenceManager.saveCurrentShopUuid(uuid)
        preferenceManager.saveCurrentShopName(shop.name)
        preferenceManager.saveHasShop(true)

        if (shop.isSubscriptionActive) {
            val status = if (shop.subscription?.isTrial == true) "TRIAL" else "ACTIVE"
            preferenceManager.saveSubscriptionStatus(status)
            shop.subscriptionType?.let { preferenceManager.saveSubscriptionType(it) }
            shop.subscriptionExpiry?.let { preferenceManager.saveSubscriptionExpiry(it) }
            shop.subscription?.id?.let { preferenceManager.saveSubscriptionId(it) }
        } else {
            preferenceManager.clearSubscriptionInfo()
        }
        Log.d(TAG, "Saved shop UUID: $uuid, ID: ${shop.id}")
    }

    // ===== DIALOGS =====
    private fun showSubscriptionRequiredDialog(shop: Shop) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Subscription Required")
            .setMessage("${shop.name} doesn't have an active subscription. Would you like to subscribe now?")
            .setPositiveButton("Subscribe Now") { _, _ -> navigateToSubscriptionPackages(shop) }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun showNoActiveShopDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("No Active Shop")
            .setMessage("You need an active subscription to perform this action.")
            .setPositiveButton("View Shops") { _, _ -> findNavController().navigate(R.id.shopsFragment) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ===== LOAD DATA =====
    fun loadDashboardData() {
        uiState = uiState.copy(isLoading = true)

        lifecycleScope.launch {
            try {
                val refreshResult = shopRepository.refreshShops()
                when (refreshResult) {
                    is Resource.Success -> {
                        val shopsResult = shopRepository.getShops()
                        if (shopsResult is Resource.Success) {
                            val shops = shopsResult.data ?: emptyList()
                            if (shops.isNotEmpty()) {
                                val selectedShop = shops.find { it.isSubscriptionActive } ?: shops.first()
                                saveCurrentShop(selectedShop)

                                val (totalSales, totalExpenses) = loadActualSalesData(shops)

                                updateTotals(totalSales, totalExpenses, shops.size)
                                submitShops(shops, selectedShop.id)
                                dashboardRepository.refreshDashboardData()
                            } else {
                                submitShops(emptyList(), null)
                                Toast.makeText(requireContext(), "No shops found.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to load shops", Toast.LENGTH_LONG).show()
                        }
                    }
                    is Resource.Error -> {
                        val shopsResult = shopRepository.getShops()
                        if (shopsResult is Resource.Success && shopsResult.data?.isNotEmpty() == true) {
                            val shops = shopsResult.data
                            val selectedShop = shops.find { it.isSubscriptionActive } ?: shops.first()
                            saveCurrentShop(selectedShop)

                            val (totalSales, totalExpenses) = loadActualSalesData(shops)
                            updateTotals(totalSales, totalExpenses, shops.size)
                            submitShops(shops, selectedShop.id)
                            Toast.makeText(requireContext(), "Using cached shop data", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), refreshResult.message ?: "Failed to load shops", Toast.LENGTH_LONG).show()
                        }
                    }
                    is Resource.Loading -> {}
                }
                uiState = uiState.copy(isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false)
                Toast.makeText(requireContext(), e.message ?: "Error loading data", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun submitShops(shops: List<Shop>, activeShopId: String?) {
        currentShops = shops
        uiState = uiState.copy(
            shops = shops.map { shop ->
                DashboardShopUi(
                    id = shop.id,
                    name = shop.name,
                    address = shop.address,
                    isSubscriptionActive = shop.isSubscriptionActive,
                )
            }
        )
    }

    // ===== FETCH SALES FOR EACH SHOP AND SUM =====
    private suspend fun loadActualSalesData(shops: List<Shop>): Pair<Double, Double> {
        var grandTotalSales = 0.0
        var grandTotalExpenses = 0.0
        val shopSalesMap = mutableMapOf<String, Double>()
        val shopExpensesMap = mutableMapOf<String, Double>()

        for (shop in shops) {
            val shopUuid = shop.uuid ?: shop.id
            val summary = dashboardRepository.fetchShopSummary(shopUuid)
            val sales = summary?.totalSales ?: 0.0
            val expenses = summary?.totalExpenses ?: 0.0

            shopSalesMap[shop.id] = sales
            shopExpensesMap[shop.id] = expenses
            grandTotalSales += sales
            grandTotalExpenses += expenses
        }

        val maxSales = shopSalesMap.values.maxOrNull() ?: 0.0
        val performances = shops.map { shop ->
            val actualSales = shopSalesMap[shop.id] ?: 0.0
            val actualExpenses = shopExpensesMap[shop.id] ?: 0.0
            val percentage = if (maxSales > 0) ((actualSales / maxSales) * 100).toInt() else 0

            ShopPerformance(
                shopId = shop.id,
                shopName = shop.name,
                location = shop.address ?: "Location not set",
                salesPercentage = percentage,
                totalSales = actualSales,
                expenses = actualExpenses,
                subscriptionStatus = shop.subscriptionStatus
            )
        }

        val sortedPerformances = performances.sortedByDescending { it.totalSales }
        uiState = uiState.copy(
            shopPerformances = sortedPerformances.map {
                DashboardShopPerformanceUi(
                    shopId = it.shopId,
                    shopName = it.shopName,
                    location = it.location,
                    salesPercentage = it.salesPercentage,
                )
            }
        )

        return Pair(grandTotalSales, grandTotalExpenses)
    }

    private fun updateTotals(totalSales: Double, totalExpenses: Double, totalShops: Int) {
        val todaySales = totalSales * 0.12
        val todayExpenses = totalExpenses * 0.08
        val todayBalance = todaySales - todayExpenses

        uiState = uiState.copy(
            totalSales = formatCompactCurrency(totalSales),
            totalExpenses = formatCompactCurrency(totalExpenses),
            totalShopsLabel = "$totalShops Shops",
            todaySales = formatCompactCurrency(todaySales),
            todayExpenses = formatCompactCurrency(todayExpenses),
            todayBalance = formatCompactCurrency(todayBalance),
        )
    }

    private fun formatCompactCurrency(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format("UGX %.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format("UGX %.1fK", amount / 1_000)
            else -> String.format("UGX %.0f", amount)
        }
    }

    private fun hasActiveShopWithSubscription(): Boolean {
        return currentShops.any { it.isSubscriptionActive }
    }
}
