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
import com.devbrian.osebo.databinding.FragmentMainDashboardBinding
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.models.ShopPerformance
import com.devbrian.osebo.utils.PermissionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale

class MainDashboardFragment : Fragment() {

    private var _binding: FragmentMainDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var shopPerformanceAdapter: ShopPerformanceAdapter
    private lateinit var mainShopAdapter: MainShopAdapter

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
        binding.tvUserName.text = if (userName.isNotEmpty()) userName else "Tom!"

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
            navigateToShopDashboard(shopId)
        }
        binding.rvShopPerformance.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shopPerformanceAdapter
            isNestedScrollingEnabled = false
        }

        // Main Shops RecyclerView (the card list)
        mainShopAdapter = MainShopAdapter(
            onShopClick = { shop ->
                navigateToShopDashboard(shop.id)
            },
            onViewDetailsClick = { shop ->
                navigateToShopDashboard(shop.id)
            }
        )
        binding.rvShops.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mainShopAdapter
            isNestedScrollingEnabled = false
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
                val shops = fetchUserShops()
                updateDashboardWithShops(shops)
                showLoading(false)
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(requireContext(), "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserShops(): List<Shop> = getMockShops()

    private fun getMockShops(): List<Shop> {
        return listOf(
            Shop(
                id = "shop_1",
                name = "BK Enterprise",
                address = "Kampala Road",
                description = "Electronics and gadgets",
                shopType = "Retail",
                phone = "+256700123456",
                totalRevenue = 1000000.0,
                totalExpenses = 0.0,
                profit = 1000000.0,
                totalProducts = 120,
                totalEmployees = 5,
                subscriptionStatus = "active",
                subscriptionType = "premium",
                subscriptionExpiry = "2024-12-31",
                isActive = true,
                ownerId = preferenceManager.getUserId(),
                createdAt = "2024-01-01",
                updatedAt = "2024-01-01"
            ),
            Shop(
                id = "shop_2",
                name = "ABK Electronics",
                address = "Kabalagala",
                description = "Mobile phones and accessories",
                shopType = "Retail",
                phone = "+256700123457",
                totalRevenue = 850000.0,
                totalExpenses = 150000.0,
                profit = 700000.0,
                totalProducts = 85,
                totalEmployees = 3,
                subscriptionStatus = "trial",
                subscriptionType = "basic",
                subscriptionExpiry = "2024-04-15",
                isActive = true,
                ownerId = preferenceManager.getUserId(),
                createdAt = "2024-01-15",
                updatedAt = "2024-01-15"
            ),
            Shop(
                id = "shop_3",
                name = "City Mall",
                address = "City Square",
                description = "Department store",
                shopType = "Retail",
                phone = "+256700123458",
                totalRevenue = 0.0,
                totalExpenses = 0.0,
                profit = 0.0,
                totalProducts = 0,
                totalEmployees = 0,
                subscriptionStatus = "inactive",
                subscriptionType = null,
                subscriptionExpiry = null,
                isActive = false,
                ownerId = preferenceManager.getUserId(),
                createdAt = "2024-02-01",
                updatedAt = "2024-02-01"
            )
        )
    }

    private fun updateDashboardWithShops(shops: List<Shop>) {
        val activeShop = shops.find {
            it.subscriptionStatus.equals("active", ignoreCase = true) ||
                    it.subscriptionStatus.equals("trial", ignoreCase = true)
        }

        if (activeShop != null) {
            preferenceManager.saveCurrentShopId(activeShop.id)
            preferenceManager.saveCurrentShopName(activeShop.name)
            preferenceManager.saveHasShop(true)
        }

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

        mainShopAdapter.submitList(shops)

        updateTotals(shops)
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
        val shops = getMockShops()
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
                Toast.makeText(requireContext(), "Showing today's data", Toast.LENGTH_SHORT).show()
            }

            "all" -> {
                binding.tvAllShops.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_today_chip_selected)
                binding.tvAllShops.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.tvToday.background = null
                binding.tvToday.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                Toast.makeText(requireContext(), "Showing all shops", Toast.LENGTH_SHORT).show()
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