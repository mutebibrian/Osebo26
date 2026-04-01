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
import com.devbrian.osebo.data.repository.ShopRepositoryImpl
import com.devbrian.osebo.databinding.FragmentMainDashboardBinding
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.models.ShopPerformance
import com.devbrian.osebo.utils.PermissionManager
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale
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
        // Shop Performance RecyclerView (the table at the top)
        shopPerformanceAdapter = ShopPerformanceAdapter { shopId ->
            // Navigate to individual shop dashboard
            val shop = mainShopAdapter.currentList.find { it.id == shopId }
            shop?.let { checkShopSubscriptionAndNavigate(it) }
        }
        binding.rvShopPerformance.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shopPerformanceAdapter
            isNestedScrollingEnabled = false
        }

        // Main Shops RecyclerView (the card list)
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

    /**
     * Check shop subscription status and handle navigation appropriately
     */
    private fun checkShopSubscriptionAndNavigate(shop: Shop) {
        val isActive = shop.subscriptionStatus.equals("active", ignoreCase = true)
        val isTrial = shop.subscriptionStatus.equals("trial", ignoreCase = true)
        val isExpired = shop.subscriptionStatus.equals("expired", ignoreCase = true)
        val hasActiveSubscription = isActive || isTrial

        when {
            hasActiveSubscription -> {
                // Save current shop info
                saveCurrentShop(shop)
                // Navigate to shop dashboard
                navigateToShopDashboard(shop.id)
            }
            isExpired -> {
                // Show expired dialog with renewal option
                showSubscriptionExpiredDialog(shop)
            }
            else -> {
                // Show subscription required dialog
                showSubscriptionRequiredDialog(shop)
            }
        }
    }

    /**
     * Save current shop information to preferences
     */
    private fun saveCurrentShop(shop: Shop) {
        preferenceManager.saveCurrentShopId(shop.id)
        preferenceManager.saveCurrentShopName(shop.name)
        preferenceManager.saveCurrentShopUuid(shop.id)
        preferenceManager.saveHasShop(true)

        // Save subscription info if active
        if (shop.subscriptionStatus.equals("active", ignoreCase = true) ||
            shop.subscriptionStatus.equals("trial", ignoreCase = true)) {
            preferenceManager.saveSubscriptionStatus(shop.subscriptionStatus.uppercase())
            shop.subscriptionType?.let { preferenceManager.saveSubscriptionType(it) }
            shop.subscriptionExpiry?.let { preferenceManager.saveSubscriptionExpiry(it) }

            println("✅ Saved active shop: ${shop.name} with status: ${shop.subscriptionStatus}")
            preferenceManager.debugSubscriptionInfo()
        }
    }

    /**
     * Show dialog for expired subscription
     */
    private fun showSubscriptionExpiredDialog(shop: Shop) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Subscription Expired")
            .setMessage("${shop.name}'s subscription has expired. Please renew your subscription to continue using this shop.")
            .setPositiveButton("Renew Now") { _, _ ->
                navigateToSubscriptionPackages(shop)
            }
            .setNegativeButton("Later") { _, _ ->
                // Dismiss dialog
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Show dialog for shops without active subscription
     */
    private fun showSubscriptionRequiredDialog(shop: Shop) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Subscription Required")
            .setMessage("${shop.name} doesn't have an active subscription. Would you like to subscribe now to start using this shop?")
            .setPositiveButton("Subscribe Now") { _, _ ->
                navigateToSubscriptionPackages(shop)
            }
            .setNegativeButton("Later") { _, _ ->
                // Dismiss dialog
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Navigate to subscription packages
     */
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
                // First refresh shops from API to get latest data
                val refreshResult = shopRepository.refreshShops()

                when (refreshResult) {
                    is Resource.Success -> {
                        println("✅ Shops refreshed successfully")
                        // Now get shops from database
                        val shopsResult = shopRepository.getShops()

                        when (shopsResult) {
                            is Resource.Success -> {
                                val shops = shopsResult.data ?: emptyList()
                                println("📊 Loaded ${shops.size} shops from database")

                                if (shops.isNotEmpty()) {
                                    updateDashboardWithShops(shops)
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
                        // Try to load from database anyway
                        val shopsResult = shopRepository.getShops()
                        when (shopsResult) {
                            is Resource.Success -> {
                                val shops = shopsResult.data ?: emptyList()
                                if (shops.isNotEmpty()) {
                                    updateDashboardWithShops(shops)
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

    private fun updateDashboardWithShops(shops: List<Shop>) {
        // Find the first active or trial shop to set as current
        val activeShop = shops.find {
            it.subscriptionStatus.equals("active", ignoreCase = true) ||
                    it.subscriptionStatus.equals("trial", ignoreCase = true)
        }

        if (activeShop != null) {
            saveCurrentShop(activeShop)
        }

        // Create shop performance data
        val performances = shops.map { shop ->
            ShopPerformance(
                shopId = shop.id,
                shopName = shop.name,
                location = shop.address ?: "Location not set",
                salesPercentage = calculateSalesPercentage(shop),
                expenses = shop.totalExpenses,
                subscriptionStatus = shop.subscriptionStatus
            )
        }
        shopPerformanceAdapter.submitList(performances)

        // Submit shops to main adapter
        mainShopAdapter.submitList(shops)

        // Update totals
        updateTotals(shops)

        // Update subscription summary
        updateSubscriptionSummary(shops)
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

    private fun calculateSalesPercentage(shop: Shop): Int =
        if (shop.totalRevenue > 0) 100 else 0

    private fun updateTotals(shops: List<Shop>) {
        val totalSales = shops.sumOf { it.totalRevenue }
        val totalExpenses = shops.sumOf { it.totalExpenses }

        binding.tvTotalSales.text = formatCurrency(totalSales)
        binding.tvTotalExpenses.text = formatCurrency(totalExpenses)
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

                // Filter shops for today's data
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
        // Show empty state UI
        binding.rvShops.visibility = View.GONE
        // You might want to add an empty state TextView in your layout
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

    private fun formatCurrency(amount: Double): String = currencyFormatter.format(amount)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}