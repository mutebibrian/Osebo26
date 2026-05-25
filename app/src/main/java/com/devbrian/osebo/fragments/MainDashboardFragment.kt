package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.MainShopAdapter
import com.devbrian.osebo.adapters.ShopPerformanceAdapter
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.repository.FinanceRepository
import com.devbrian.osebo.data.repository.ProductRepository
import com.devbrian.osebo.data.repository.ShopRepositoryImpl
import com.devbrian.osebo.data.repository.SalesRepository
import com.devbrian.osebo.databinding.FragmentMainDashboardBinding
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.models.ShopPerformance
import com.devbrian.osebo.utils.PermissionManager
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class MainDashboardFragment : Fragment() {

    private var _binding: FragmentMainDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var shopPerformanceAdapter: ShopPerformanceAdapter
    private lateinit var mainShopAdapter: MainShopAdapter

    @Inject
    lateinit var shopRepository: ShopRepositoryImpl

    @Inject
    lateinit var financeRepository: FinanceRepository
    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var salesRepository: SalesRepository

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance().apply {
        maximumFractionDigits = 0
        currency = Currency.getInstance("UGX")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager.getInstance(requireContext())
        permissionManager = PermissionManager(requireContext())

        setupToolbar()
        setupRecyclerViews()
        setupClickListeners()
        loadDashboardData()
    }

    private fun setupToolbar() {
        val userName = preferenceManager.getUserName()
        binding.tvUserName.text = if (userName.isNotEmpty()) userName else "User!"

        val calendar = Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
        binding.tvCurrentDate.text = dateFormat.format(calendar.time)

        val welcomeText = when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning,"
            in 12..15 -> "Good afternoon,"
            else -> "Good evening,"
        }
        binding.tvWelcome.text = welcomeText

        binding.notificationBadge.visibility = if (checkForNotifications()) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerViews() {
        shopPerformanceAdapter = ShopPerformanceAdapter { shopId ->
            val shop = mainShopAdapter.currentList.find { it.id == shopId }
            shop?.let { checkShopSubscriptionAndNavigate(it) }
        }
        binding.rvShopPerformance.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shopPerformanceAdapter
            isNestedScrollingEnabled = false
        }

        mainShopAdapter = MainShopAdapter(
            onShopClick = { shop ->
                checkShopSubscriptionAndNavigate(shop)
            },
            onViewDetailsClick = { shop ->
                checkShopSubscriptionAndNavigate(shop)
            }
        )
        binding.rvShops.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mainShopAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun checkShopSubscriptionAndNavigate(shop: Shop) {
        val isActive = shop.subscriptionStatus.equals("active", ignoreCase = true)
        val isTrial = shop.subscriptionStatus.equals("trial", ignoreCase = true)
        val isExpired = shop.subscriptionStatus.equals("expired", ignoreCase = true)
        val hasActiveSubscription = isActive || isTrial

        when {
            hasActiveSubscription -> {
                saveCurrentShop(shop)
                navigateToShopDashboard(shop.id)
            }
            isExpired -> {
                showSubscriptionExpiredDialog(shop)
            }
            else -> {
                showSubscriptionRequiredDialog(shop)
            }
        }
    }

    private fun saveCurrentShop(shop: Shop) {
        preferenceManager.saveCurrentShopId(shop.id)
        preferenceManager.saveCurrentShopName(shop.name)
        preferenceManager.saveCurrentShopUuid(shop.id)
        preferenceManager.saveHasShop(true)

        if (shop.subscriptionStatus.equals("active", ignoreCase = true) ||
            shop.subscriptionStatus.equals("trial", ignoreCase = true)) {
            preferenceManager.saveSubscriptionStatus(shop.subscriptionStatus.uppercase())
            shop.subscriptionType?.let { preferenceManager.saveSubscriptionType(it) }
            shop.subscriptionExpiry?.let { preferenceManager.saveSubscriptionExpiry(it) }

            println("✅ Saved active shop: ${shop.name} with status: ${shop.subscriptionStatus}")
            preferenceManager.debugSubscriptionInfo()
        }
    }

    private fun showSubscriptionExpiredDialog(shop: Shop) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Subscription Expired")
            .setMessage("${shop.name}'s subscription has expired. Please renew your subscription to continue using this shop.")
            .setPositiveButton("Renew Now") { _, _ ->
                navigateToSubscriptionPackages(shop)
            }
            .setNegativeButton("Later") { _, _ ->
                // Do nothing
            }
            .setCancelable(true)
            .show()
    }

    private suspend fun getTotalProductsForShop(shopId: String): Int {
        return try {
            val originalShopId = preferenceManager.getCurrentShopId()
            preferenceManager.saveCurrentShopId(shopId)

            val result = productRepository.getProducts()

            preferenceManager.saveCurrentShopId(originalShopId)

            if (result is Resource.Success) {
                val products = result.data ?: emptyList()
                println("📊 Shop $shopId has ${products.size} products")
                products.size
            } else {
                0
            }
        } catch (e: Exception) {
            println("❌ Error getting products for shop $shopId: ${e.message}")
            0
        }
    }

    private fun showSubscriptionRequiredDialog(shop: Shop) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Subscription Required")
            .setMessage("${shop.name} doesn't have an active subscription. Would you like to subscribe now to start using this shop?")
            .setPositiveButton("Subscribe Now") { _, _ ->
                navigateToSubscriptionPackages(shop)
            }
            .setNegativeButton("Later") { _, _ ->
                // Do nothing
            }
            .setCancelable(true)
            .show()
    }

    private fun navigateToSubscriptionPackages(shop: Shop) {
        try {
            val action = MainDashboardFragmentDirections.actionMainDashboardToSubscriptionPackages(shop)
            findNavController().navigate(action)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error navigating to subscriptions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.tvToday.setOnClickListener { filterShopsByTime("today") }
        binding.tvAllShops.setOnClickListener { filterShopsByTime("all") }

        binding.ivProfile.setOnClickListener { findNavController().navigate(R.id.accountFragment) }

        binding.ivNotification.setOnClickListener {
            Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show()
        }

        binding.llAddProduct.setOnClickListener {
            if (hasActiveShopWithSubscription()) {
                findNavController().navigate(R.id.addProductFragment)
            } else {
                showNoActiveShopDialog()
            }
        }

        binding.llNewSale.setOnClickListener {
            if (hasActiveShopWithSubscription()) {
                findNavController().navigate(R.id.newSaleFragment)
            } else {
                showNoActiveShopDialog()
            }
        }

        binding.llAddEmployee.setOnClickListener {
            if (hasActiveShopWithSubscription()) {
                findNavController().navigate(R.id.employeesFragment)
            } else {
                showNoActiveShopDialog()
            }
        }

        binding.llReports.setOnClickListener {
            if (hasActiveShopWithSubscription()) {
                Toast.makeText(requireContext(), "Reports coming soon", Toast.LENGTH_SHORT).show()
            } else {
                showNoActiveShopDialog()
            }
        }
    }

    private fun loadDashboardData() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val refreshResult = shopRepository.refreshShops()

                when (refreshResult) {
                    is Resource.Success -> {
                        println("✅ Shops refreshed successfully")
                        val shopsResult = shopRepository.getShops()

                        when (shopsResult) {
                            is Resource.Success -> {
                                val shops = shopsResult.data ?: emptyList()
                                println("📊 Loaded ${shops.size} shops from database")

                                if (shops.isNotEmpty()) {
                                    loadActualSalesData(shops)
                                } else {
                                    showEmptyState()
                                    Toast.makeText(requireContext(), "No shops found. Please create a shop.", Toast.LENGTH_LONG).show()
                                }
                            }
                            is Resource.Error -> {
                                println("❌ Error getting shops: ${shopsResult.message}")
                                showErrorState(shopsResult.message ?: "Failed to load shops")
                            }
                            is Resource.Loading -> {
                                println("⏳ Loading shops...")
                            }
                        }
                    }
                    is Resource.Error -> {
                        println("❌ Error refreshing shops: ${refreshResult.message}")
                        val shopsResult = shopRepository.getShops()
                        when (shopsResult) {
                            is Resource.Success -> {
                                val shops = shopsResult.data ?: emptyList()
                                if (shops.isNotEmpty()) {
                                    loadActualSalesData(shops)
                                    Toast.makeText(requireContext(), "Using cached shop data", Toast.LENGTH_SHORT).show()
                                } else {
                                    showErrorState(refreshResult.message ?: "Failed to load shops")
                                }
                            }
                            else -> {
                                showErrorState(refreshResult.message ?: "Failed to load shops")
                            }
                        }
                    }
                    is Resource.Loading -> {
                        println("⏳ Refreshing shops...")
                    }
                }

                showLoading(false)
            } catch (e: Exception) {
                showLoading(false)
                println("❌ Error loading dashboard data: ${e.message}")
                e.printStackTrace()
                showErrorState(e.message ?: "Error loading data")
            }
        }
    }

    private suspend fun loadActualSalesData(shops: List<Shop>) {
        println("🔍 ===== LOADING ACTUAL SALES AND EXPENSES DATA =====")

        val originalShopId = preferenceManager.getCurrentShopId()
        val shopSalesMap = mutableMapOf<String, Double>()
        val shopExpensesMap = mutableMapOf<String, Double>()
        var grandTotalSales = 0.0
        var grandTotalExpenses = 0.0

        for (shop in shops) {
            try {
                val hasActiveSubscription = shop.subscriptionStatus.equals("active", ignoreCase = true) ||
                        shop.subscriptionStatus.equals("trial", ignoreCase = true)

                if (hasActiveSubscription) {
                    println("📊 Fetching data for shop: ${shop.name} (ID: ${shop.id})")

                    preferenceManager.saveCurrentShopId(shop.id)

                    // Get total sales
                    val totalSales = salesRepository.getTotalSalesForShop(shop.id, limit = 10000)
                    shopSalesMap[shop.id] = totalSales
                    grandTotalSales += totalSales
                    println("   ✅ Shop: ${shop.name} - Total Sales: ${formatCompactCurrency(totalSales)}")

                    // GET TOTAL EXPENSES FOR THIS SHOP
                    val totalExpenses = getTotalExpensesForShop(shop.id)
                    shopExpensesMap[shop.id] = totalExpenses
                    grandTotalExpenses += totalExpenses
                    println("   ✅ Shop: ${shop.name} - Total Expenses: ${formatCompactCurrency(totalExpenses)}")

                } else {
                    shopSalesMap[shop.id] = 0.0
                    shopExpensesMap[shop.id] = 0.0
                    println("📊 Shop: ${shop.name} - No active subscription, sales: 0, expenses: 0")
                }

            } catch (e: Exception) {
                println("❌ Error loading data for shop ${shop.name}: ${e.message}")
                e.printStackTrace()
                shopSalesMap[shop.id] = 0.0
                shopExpensesMap[shop.id] = 0.0
            }
        }

        // Restore original shop
        preferenceManager.saveCurrentShopId(originalShopId)

        // Calculate max sales for percentage
        val maxSales = shopSalesMap.values.maxOrNull() ?: 0.0
        println("📊 Maximum sales across all shops: ${formatCompactCurrency(maxSales)}")
        println("📊 GRAND TOTAL SALES: ${formatCompactCurrency(grandTotalSales)}")
        println("📊 GRAND TOTAL EXPENSES: ${formatCompactCurrency(grandTotalExpenses)}")

        // Create performance list with actual expenses
        val performances = shops.map { shop ->
            val actualSales = shopSalesMap[shop.id] ?: 0.0
            val actualExpenses = shopExpensesMap[shop.id] ?: 0.0

            val percentage = if (maxSales > 0) {
                ((actualSales / maxSales) * 100).toInt()
            } else {
                0
            }

            println("📊 Shop: ${shop.name} - Sales: ${formatCompactCurrency(actualSales)}, Expenses: ${formatCompactCurrency(actualExpenses)}")

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
        shopPerformanceAdapter.submitList(sortedPerformances)
        mainShopAdapter.submitList(shops)

        // Update totals with actual data
        updateTotals(grandTotalSales, grandTotalExpenses)

        updateSubscriptionSummary(shops)

        val shopsWithSales = shopSalesMap.count { it.value > 0 }
        val shopsWithExpenses = shopExpensesMap.count { it.value > 0 }

        Toast.makeText(requireContext(),
            "Total Sales: ${formatCompactCurrency(grandTotalSales)}\nTotal Expenses: ${formatCompactCurrency(grandTotalExpenses)}\nShops with Sales: $shopsWithSales\nShops with Expenses: $shopsWithExpenses",
            Toast.LENGTH_LONG).show()

        println("🔍 ===== DATA LOADING COMPLETE =====")
    }

    private suspend fun getTotalExpensesForShop(shopId: String): Double {
        return try {
            val originalShopId = preferenceManager.getCurrentShopId()
            preferenceManager.saveCurrentShopId(shopId)
            val result = financeRepository.getExpenses()
            preferenceManager.saveCurrentShopId(originalShopId)

            if (result.isSuccess) {
                val expenses = result.getOrNull() ?: emptyList()
                expenses.sumOf { it.amount }
            } else {
                0.0
            }
        } catch (e: Exception) {
            println("❌ Error calculating expenses for shop $shopId: ${e.message}")
            0.0
        }
    }

    private fun updateTotals(totalSales: Double, totalExpenses: Double) {
        binding.tvTotalSales.text = formatCompactCurrency(totalSales)
        binding.tvTotalExpenses.text = formatCompactCurrency(totalExpenses)
    }

    private fun formatCompactCurrency(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format("UGX %.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format("UGX %.1fK", amount / 1_000)
            else -> String.format("UGX %.0f", amount)
        }
    }

    private fun updateSubscriptionSummary(shops: List<Shop>) {
        val activeCount = shops.count {
            it.subscriptionStatus.equals("active", ignoreCase = true)
        }
        val trialCount = shops.count {
            it.subscriptionStatus.equals("trial", ignoreCase = true)
        }
        val expiredCount = shops.count {
            it.subscriptionStatus.equals("expired", ignoreCase = true)
        }
        val inactiveCount = shops.count {
            !it.subscriptionStatus.equals("active", ignoreCase = true) &&
                    !it.subscriptionStatus.equals("trial", ignoreCase = true) &&
                    !it.subscriptionStatus.equals("expired", ignoreCase = true)
        }

        println("📊 Subscription Summary:")
        println("   - Active: $activeCount")
        println("   - Trial: $trialCount")
        println("   - Expired: $expiredCount")
        println("   - Inactive: $inactiveCount")
    }

    private fun hasActiveShopWithSubscription(): Boolean {
        val shops = mainShopAdapter.currentList
        return shops.any {
            it.subscriptionStatus.equals("active", ignoreCase = true) ||
                    it.subscriptionStatus.equals("trial", ignoreCase = true)
        }
    }

    private fun filterShopsByTime(filter: String) {
        when (filter) {
            "today" -> {
                binding.tvToday.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_today_chip_selected)
                binding.tvToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.tvAllShops.background = null
                binding.tvAllShops.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))

                val filteredShops = mainShopAdapter.currentList.filter {
                    it.totalRevenue > 0 || it.totalExpenses > 0
                }
                Toast.makeText(requireContext(), "Showing ${filteredShops.size} shops with activity today", Toast.LENGTH_SHORT).show()
            }

            "all" -> {
                binding.tvAllShops.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_today_chip_selected)
                binding.tvAllShops.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.tvToday.background = null
                binding.tvToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))

                Toast.makeText(requireContext(), "Showing all ${mainShopAdapter.currentList.size} shops", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToShopDashboard(shopId: String) {
        try {
            val action = MainDashboardFragmentDirections.actionMainDashboardToShopDashboard(shopId)
            findNavController().navigate(action)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNoActiveShopDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("No Active Shop")
            .setMessage("You need at least one shop with an active subscription to perform this action. Please activate a subscription for one of your shops.")
            .setPositiveButton("View Shops") { _, _ ->
                findNavController().navigate(R.id.shopsFragment)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEmptyState() {
        binding.rvShops.visibility = View.GONE
        Toast.makeText(requireContext(), "No shops found. Please create a shop.", Toast.LENGTH_LONG).show()
    }

    private fun showErrorState(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        showLoading(false)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.mainContent.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun checkForNotifications(): Boolean = false

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}