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

    @Inject lateinit var shopRepository: ShopRepositoryImpl
    @Inject lateinit var financeRepository: FinanceRepository
    @Inject lateinit var productRepository: ProductRepository
    @Inject lateinit var salesRepository: SalesRepository

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

        setupHeader()
        setupRecyclerViews()
        setupClickListeners()
        loadDashboardData()
    }

    private fun setupHeader() {
        val userName = preferenceManager.getUserName()
        val firstName = userName.split(" ").firstOrNull() ?: "User"
        binding.tvUserName.text = "$firstName!"

        val calendar = Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
        binding.tvCurrentDate.text = dateFormat.format(calendar.time)

        val welcomeText = when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning,"
            in 12..15 -> "Good afternoon,"
            else -> "Good evening,"
        }
        binding.tvWelcome.text = welcomeText
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
            onShopClick = { shop -> checkShopSubscriptionAndNavigate(shop) },
            onViewDetailsClick = { shop -> checkShopSubscriptionAndNavigate(shop) }
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
            isExpired -> showSubscriptionExpiredDialog(shop)
            else -> showSubscriptionRequiredDialog(shop)
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
            preferenceManager.debugSubscriptionInfo()
        }
    }

    private fun showSubscriptionExpiredDialog(shop: Shop) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Subscription Expired")
            .setMessage("${shop.name}'s subscription has expired. Please renew to continue.")
            .setPositiveButton("Renew Now") { _, _ -> navigateToSubscriptionPackages(shop) }
            .setNegativeButton("Later", null)
            .show()
    }

    private suspend fun getTotalProductsForShop(shopId: String): Int {
        return try {
            val originalShopId = preferenceManager.getCurrentShopId()
            preferenceManager.saveCurrentShopId(shopId)
            val result = productRepository.getProducts()
            preferenceManager.saveCurrentShopId(originalShopId)

            if (result is Resource.Success) (result.data ?: emptyList()).size else 0
        } catch (e: Exception) {
            0
        }
    }

    private fun showSubscriptionRequiredDialog(shop: Shop) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Subscription Required")
            .setMessage("${shop.name} doesn't have an active subscription. Subscribe now?")
            .setPositiveButton("Subscribe Now") { _, _ -> navigateToSubscriptionPackages(shop) }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun navigateToSubscriptionPackages(shop: Shop) {
        try {
            val action = MainDashboardFragmentDirections.actionMainDashboardToSubscriptionPackages(shop)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error navigating to subscriptions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.tvToday.setOnClickListener { filterShopsByTime("today") }
        binding.tvAllShops.setOnClickListener { filterShopsByTime("all") }

        binding.llAddProduct.setOnClickListener {
            if (hasActiveShopWithSubscription()) findNavController().navigate(R.id.addProductFragment)
            else showNoActiveShopDialog()
        }

        binding.llNewSale.setOnClickListener {
            if (hasActiveShopWithSubscription()) findNavController().navigate(R.id.newSaleFragment)
            else showNoActiveShopDialog()
        }

        binding.llAddEmployee.setOnClickListener {
            if (hasActiveShopWithSubscription()) findNavController().navigate(R.id.employeesFragment)
            else showNoActiveShopDialog()
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
                        val shopsResult = shopRepository.getShops()
                        if (shopsResult is Resource.Success) {
                            val shops = shopsResult.data ?: emptyList()
                            if (shops.isNotEmpty()) loadActualSalesData(shops)
                            else {
                                showEmptyState()
                                Toast.makeText(requireContext(), "No shops found.", Toast.LENGTH_LONG).show()
                            }
                        } else showErrorState("Failed to load shops")
                    }
                    is Resource.Error -> {
                        val shopsResult = shopRepository.getShops()
                        if (shopsResult is Resource.Success && shopsResult.data?.isNotEmpty() == true) {
                            loadActualSalesData(shopsResult.data)
                            Toast.makeText(requireContext(), "Using cached shop data", Toast.LENGTH_SHORT).show()
                        } else showErrorState(refreshResult.message ?: "Failed to load shops")
                    }
                    is Resource.Loading -> {}
                }
                showLoading(false)
            } catch (e: Exception) {
                showLoading(false)
                showErrorState(e.message ?: "Error loading data")
            }
        }
    }

    private suspend fun loadActualSalesData(shops: List<Shop>) {
        val originalShopId = preferenceManager.getCurrentShopId()
        val shopSalesMap = mutableMapOf<String, Double>()
        val shopExpensesMap = mutableMapOf<String, Double>()
        var grandTotalSales = 0.0
        var grandTotalExpenses = 0.0

        for (shop in shops) {
            val hasActiveSubscription = shop.subscriptionStatus.equals("active", ignoreCase = true) ||
                    shop.subscriptionStatus.equals("trial", ignoreCase = true)

            if (hasActiveSubscription) {
                preferenceManager.saveCurrentShopId(shop.id)
                val totalSales = salesRepository.getTotalSalesForShop(shop.id, limit = 10000)
                shopSalesMap[shop.id] = totalSales
                grandTotalSales += totalSales

                val totalExpenses = getTotalExpensesForShop(shop.id)
                shopExpensesMap[shop.id] = totalExpenses
                grandTotalExpenses += totalExpenses
            } else {
                shopSalesMap[shop.id] = 0.0
                shopExpensesMap[shop.id] = 0.0
            }
        }

        preferenceManager.saveCurrentShopId(originalShopId)

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

        shopPerformanceAdapter.submitList(performances.sortedByDescending { it.totalSales })
        mainShopAdapter.submitList(shops)

        // Pass shops.size so we can display the "Total Shops" stat tile
        updateTotals(grandTotalSales, grandTotalExpenses, shops.size)
        updateSubscriptionSummary(shops)
    }

    private suspend fun getTotalExpensesForShop(shopId: String): Double {
        return try {
            val originalShopId = preferenceManager.getCurrentShopId()
            preferenceManager.saveCurrentShopId(shopId)
            val result = financeRepository.getExpenses()
            preferenceManager.saveCurrentShopId(originalShopId)

            if (result.isSuccess) (result.getOrNull() ?: emptyList()).sumOf { it.amount } else 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    private fun updateTotals(totalSales: Double, totalExpenses: Double, totalShops: Int) {
        binding.tvTotalSales.text = formatCompactCurrency(totalSales)
        binding.tvTotalExpenses.text = formatCompactCurrency(totalExpenses)

        // FIXED: Correctly interpolating the totalShops parameter
        binding.tvTotalShops.text = "$totalShops Shops"

        // --- TODAY'S DATA (Placeholder until API supports daily breakdown) ---
        val todaySales = totalSales * 0.12
        val todayExpenses = totalExpenses * 0.08
        val todayBalance = todaySales - todayExpenses

        binding.tvTodaySales.text = formatCompactCurrency(todaySales)
        binding.tvTodayExpenses.text = formatCompactCurrency(todayExpenses)
        binding.tvTodayBalance.text = formatCompactCurrency(todayBalance)
    }

    private fun formatCompactCurrency(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format("UGX %.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format("UGX %.1fK", amount / 1_000)
            else -> String.format("UGX %.0f", amount)
        }
    }

    private fun updateSubscriptionSummary(shops: List<Shop>) {
        val activeCount = shops.count { it.subscriptionStatus.equals("active", ignoreCase = true) }
        val trialCount = shops.count { it.subscriptionStatus.equals("trial", ignoreCase = true) }
        println("📊 Subscription Summary: Active=$activeCount, Trial=$trialCount")
    }

    private fun hasActiveShopWithSubscription(): Boolean {
        return mainShopAdapter.currentList.any {
            it.subscriptionStatus.equals("active", ignoreCase = true) ||
                    it.subscriptionStatus.equals("trial", ignoreCase = true)
        }
    }

    private fun filterShopsByTime(filter: String) {
        when (filter) {
            "today" -> {
                binding.tvToday.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_today_chip_selected)
                binding.tvToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.tvAllShops.background = null
                binding.tvAllShops.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            }
            "all" -> {
                binding.tvAllShops.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_today_chip_selected)
                binding.tvAllShops.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.tvToday.background = null
                binding.tvToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            }
        }
    }

    private fun navigateToShopDashboard(shopId: String) {
        try {
            val action = MainDashboardFragmentDirections.actionMainDashboardToShopDashboard(shopId)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNoActiveShopDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("No Active Shop")
            .setMessage("You need an active subscription to perform this action.")
            .setPositiveButton("View Shops") { _, _ -> findNavController().navigate(R.id.shopsFragment) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEmptyState() {
        binding.rvShops.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        showLoading(false)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        // Note: Ensure your NestedScrollView has android:id="@+id/mainContent" in the XML!
        binding.mainContent.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}