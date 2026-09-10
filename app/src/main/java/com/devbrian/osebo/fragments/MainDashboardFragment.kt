package com.devbrian.osebo.fragments

import android.os.Bundle
import android.util.Log
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
import com.devbrian.osebo.adapters.OnShopClickListener
import com.devbrian.osebo.adapters.ShopsAdapter
import com.devbrian.osebo.adapters.ShopPerformanceAdapter
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.data.repository.DashboardRepository
import com.devbrian.osebo.data.repository.FinanceRepository
import com.devbrian.osebo.data.repository.ProductRepository
import com.devbrian.osebo.data.repository.ShopRepositoryImpl
import com.devbrian.osebo.data.repository.SalesRepository
import com.devbrian.osebo.databinding.FragmentMainDashboardBinding
import com.devbrian.osebo.models.ShopPerformance
import com.devbrian.osebo.ui.MainActivity
import com.devbrian.osebo.utils.PermissionManager
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

private const val TAG = "MainDashboardFragment"

@AndroidEntryPoint
class MainDashboardFragment : Fragment() {

    private var _binding: FragmentMainDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var shopPerformanceAdapter: ShopPerformanceAdapter
    private lateinit var shopsAdapter: ShopsAdapter

    @Inject lateinit var shopRepository: ShopRepositoryImpl
    @Inject lateinit var financeRepository: FinanceRepository
    @Inject lateinit var productRepository: ProductRepository
    @Inject lateinit var salesRepository: SalesRepository
    @Inject lateinit var dashboardRepository: DashboardRepository

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
        // 1. Shop Performance (horizontal)
        shopPerformanceAdapter = ShopPerformanceAdapter { shopId ->
            val shop = shopsAdapter.currentList.find { it.id == shopId }
            shop?.let { navigateToShopBilling(it) }
        }
        binding.rvShopPerformance.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = shopPerformanceAdapter
            isNestedScrollingEnabled = false
        }

        // 2. Your Shops (vertical, using ShopsAdapter)
        shopsAdapter = ShopsAdapter(
            listener = object : OnShopClickListener {
                override fun onShopClick(shop: Shop) {
                    if (shop.isSubscriptionActive) {
                        setActiveShop(shop)
                        navigateToShopDashboard(shop)
                    } else {
                        showSubscriptionRequiredDialog(shop)
                    }
                }

                override fun onEditClick(shop: Shop) {
                    Toast.makeText(requireContext(), "Edit ${shop.name}", Toast.LENGTH_SHORT).show()
                }

                override fun onDeleteClick(shop: Shop) {
                    // optional
                }

                override fun onSetActiveClick(shop: Shop) {
                    setActiveShop(shop)
                }

                override fun onSubscribeClick(shop: Shop) {
                    navigateToSubscriptionPackages(shop)
                }
            },
            context = requireContext()
        )

        binding.rvShops.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shopsAdapter
            isNestedScrollingEnabled = false
        }
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

    // ===== FIXED: No reference to subscriptionPackage =====
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
                packageId = null   // removed subscriptionPackage reference
            )
        } else {
            preferenceManager.clearSubscriptionInfo()
        }

        shopsAdapter.setActiveShopId(shop.id)
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

    private suspend fun getTotalProductsForShop(shopId: String): Int {
        return try {
            val originalShopId = preferenceManager.getCurrentShopId()
            preferenceManager.saveCurrentShopId(shopId)
            val result = productRepository.getProducts()
            preferenceManager.saveCurrentShopId(originalShopId)
            if (result is Resource.Success) (result.data ?: emptyList()).size else 0
        } catch (e: Exception) { 0 }
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

    // ===== CLICK LISTENERS =====
    private fun setupClickListeners() {
        binding.tvToday.setOnClickListener { filterShopsByTime("today") }
        binding.tvAllShops.setOnClickListener { filterShopsByTime("all") }

        binding.tvViewDetails.setOnClickListener {
            findNavController().navigate(R.id.reportsFragment)
        }

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

    // ===== LOAD DATA (PUBLIC) =====
    fun loadDashboardData() {
        showLoading(true)

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

                                // ✅ GRAND TOTALS ACROSS ALL SHOPS
                                val (totalSales, totalExpenses) = loadActualSalesData(shops)

                                updateTotals(totalSales, totalExpenses, shops.size)
                                shopsAdapter.submitList(shops)
                                shopsAdapter.setActiveShopId(selectedShop.id)
                                dashboardRepository.refreshDashboardData()
                            } else {
                                showEmptyState()
                                Toast.makeText(requireContext(), "No shops found.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            showErrorState("Failed to load shops")
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
                            shopsAdapter.submitList(shops)
                            shopsAdapter.setActiveShopId(selectedShop.id)
                            Toast.makeText(requireContext(), "Using cached shop data", Toast.LENGTH_SHORT).show()
                        } else {
                            showErrorState(refreshResult.message ?: "Failed to load shops")
                        }
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

        shopPerformanceAdapter.submitList(performances.sortedByDescending { it.totalSales })
        updateSubscriptionSummary(shops)

        return Pair(grandTotalSales, grandTotalExpenses)
    }

    private fun updateTotals(totalSales: Double, totalExpenses: Double, totalShops: Int) {
        binding.tvTotalSales.text = formatCompactCurrency(totalSales)
        binding.tvTotalExpenses.text = formatCompactCurrency(totalExpenses)
        binding.tvTotalShops.text = "$totalShops Shops"

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
        val activeCount = shops.count { it.isSubscriptionActive }
        val trialCount = shops.count { it.subscriptionStatus.equals("trial", ignoreCase = true) }
        Log.d(TAG, "📊 Subscription Summary: Active=$activeCount, Trial=$trialCount")
    }

    private fun hasActiveShopWithSubscription(): Boolean {
        return shopsAdapter.currentList.any { it.isSubscriptionActive }
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

    private fun showEmptyState() {
        binding.rvShops.visibility = View.GONE
        binding.tvNoShops.visibility = View.VISIBLE
    }

    private fun showErrorState(message: String) {
        val ctx = context ?: return
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()
        showLoading(false)
    }

    private fun showLoading(show: Boolean) {
        if (_binding == null) return
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.mainContent.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}