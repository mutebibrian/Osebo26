package com.devbrian.osebo.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.repository.ShopRepositoryImpl
import com.devbrian.osebo.databinding.ActivityMainBinding
import com.devbrian.osebo.fragments.MainDashboardFragment
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.utils.PermissionManager
import com.devbrian.osebo.utils.Resource
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var navigationView: NavigationView

    @Inject
    lateinit var shopRepository: ShopRepositoryImpl

    private val subscriptionRequiredDestinations = setOf(
        R.id.salesFragment,
        R.id.financeFragment,
        R.id.inventoryFragment,
        R.id.employeesFragment,
        R.id.customersFragment,
        R.id.newSaleFragment,
        R.id.addProductFragment,
        R.id.productDetailsFragment,
        R.id.addExpenseFragment,
        R.id.suppliersFragment,
        R.id.paymentFragment,
        R.id.receiptFragment,
    )

    private val managementRequiredDestinations = setOf(
        R.id.accountFragment,
        R.id.sessionsFragment,
        R.id.userRolesFragment,
        R.id.subscriptionOverviewFragment,
        R.id.subscriptionsFragment,
        R.id.subscriptionDetailsFragment
    )

    private val topLevelDestinationIds = setOf(
        R.id.mainDashboardFragment,
        R.id.shopsFragment,
        R.id.salesFragment,
        R.id.financeFragment,
        R.id.inventoryFragment,
        R.id.employeesFragment,
        R.id.userRolesFragment,
        R.id.customersFragment
    )

    private val bottomNavDestinations = mapOf(
        R.id.mainDashboardFragment to R.id.main_nav_dashboard,
        R.id.shopsFragment to R.id.main_nav_shops,
        R.id.reportsFragment to R.id.main_nav_reports,
        R.id.transactionsFragment to R.id.main_nav_transactions,
        R.id.accountFragment to R.id.main_nav_settings
    )
    private val bottomNavItemToDestination = bottomNavDestinations.entries.associate { (destId, itemId) -> itemId to destId }

    private var isFabMenuOpen = false
    private var subscriptionCheckInProgress = false
    private var currentShop: Shop? = null
    private var isDataLoading = false

    // Header views (cached)
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserInitial: TextView
    private lateinit var tvAppVersion: TextView
    private lateinit var tvSubscriptionBadge: TextView
    private lateinit var tvCurrentShop: TextView
    private lateinit var tvViewingMode: TextView
    private lateinit var ivSettings: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager.getInstance(this)
        permissionManager = PermissionManager(this)
        navigationView = binding.navigationView

        setupToolbar()
        setupNavigation()
        setupHeaderView()
        setupFloatingActionButtons()
        setupBottomNavigation()
        setupNavControllerListener()

        loadInitialData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        binding.flNotificationBell.setOnClickListener {
            Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show()
        }

        binding.flAvatar.setOnClickListener {
            navController.navigate(R.id.accountFragment)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavMain.setOnItemSelectedListener { item ->
            val destinationId = bottomNavItemToDestination[item.itemId] ?: return@setOnItemSelectedListener false
            if (navController.currentDestination?.id == destinationId) {
                return@setOnItemSelectedListener false
            }
            navController.navigate(
                destinationId,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.mainDashboardFragment, destinationId == R.id.mainDashboardFragment)
                    .setLaunchSingleTop(true)
                    .build()
            )
            true
        }
    }

    private fun setupNavigation() {
        Log.d("NavDrawer_DEBUG", "📍 setupNavigation() called")

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        Log.d("NavDrawer_DEBUG", "✅ NavController initialized")

        appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds,
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        navigationView.setNavigationItemSelectedListener(this)
        Log.d("NavDrawer_DEBUG", "✅ NavigationItemSelectedListener set")

        navigationView.isClickable = true
        navigationView.isFocusable = true
        navigationView.isLongClickable = true
        Log.d("NavDrawer_DEBUG", "✅ NavigationView interactive properties set")

        binding.topAppBar.setNavigationOnClickListener {
            Log.d("NavDrawer_DEBUG", "🔵 Hamburger menu clicked - Opening drawer")
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.drawerLayout.addDrawerListener(object : androidx.drawerlayout.widget.DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })

        Log.d("NavDrawer_DEBUG", "✅ setupNavigation() completed successfully")
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null && binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            Log.d("NavDrawer_DEBUG", "📍 dispatchTouchEvent: action=${getActionName(ev.action)}, x=${ev.x}, y=${ev.y}")
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun getActionName(action: Int): String {
        return when (action) {
            MotionEvent.ACTION_DOWN -> "DOWN"
            MotionEvent.ACTION_UP -> "UP"
            MotionEvent.ACTION_MOVE -> "MOVE"
            MotionEvent.ACTION_CANCEL -> "CANCEL"
            else -> "UNKNOWN($action)"
        }
    }

    private fun setupNavControllerListener() {
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            Log.d("NavDrawer_DEBUG", "📍 Destination changed to: ${destination.label} (ID: ${destination.id})")

            if (destination.id == R.id.mainDashboardFragment) {
                showFab()
            } else {
                hideFab()
            }

            when (destination.id) {
                R.id.shopDashboardFragment -> {
                    val shopName = preferenceManager.getCurrentShopName()
                    supportActionBar?.title = shopName.ifEmpty { "Shop Dashboard" }
                }
                R.id.mainDashboardFragment -> {
                    supportActionBar?.title = "Dashboard"
                }
                else -> {
                    supportActionBar?.title = destination.label
                }
            }

            if (destination.id in topLevelDestinationIds) {
                supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_custom)
            }

            val bottomNavItemId = bottomNavDestinations[destination.id]
            if (bottomNavItemId != null) {
                binding.bottomNavMain.visibility = View.VISIBLE
                if (binding.bottomNavMain.selectedItemId != bottomNavItemId) {
                    binding.bottomNavMain.menu.findItem(bottomNavItemId)?.isChecked = true
                }
            } else {
                binding.bottomNavMain.visibility = View.GONE
            }

            if (permissionManager.isShopOwner()) {
                when {
                    destination.id in subscriptionRequiredDestinations -> {
                        if (!hasAccessToBusinessOperations()) {
                            Log.d("NavDrawer_DEBUG", "⚠️ Access blocked - subscription required for ${destination.label}")
                            navController.popBackStack()
                            showSubscriptionRequiredDialog("business operations")
                        }
                    }
                    destination.id in managementRequiredDestinations -> {
                        if (!hasAccessToManagement()) {
                            Log.d("NavDrawer_DEBUG", "⚠️ Access blocked - management access required for ${destination.label}")
                            navController.popBackStack()
                            showSubscriptionRequiredDialog("management features")
                        }
                    }
                }
            }

            handleDestinationArguments(destination.id, arguments)
        }
    }

    private fun setupFloatingActionButtons() {
        binding.mainFab.setOnClickListener { toggleFabMenu() }

        binding.fabSale.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.PROCESS_SALES)) {
                createNewSale()
                toggleFabMenu()
            } else {
                showPermissionDeniedDialog("create sales")
                toggleFabMenu()
            }
        }

        binding.fabProduct.setOnClickListener {
            if (permissionManager.hasPermission(PermissionType.MANAGE_INVENTORY)) {
                addNewProduct()
                toggleFabMenu()
            } else {
                showPermissionDeniedDialog("add products")
                toggleFabMenu()
            }
        }
    }

    // ===== HEADER SETUP – uses navigationView.getHeaderView(0) =====
    private fun setupHeaderView() {
        val headerView = navigationView.getHeaderView(0)

        tvUserName = headerView.findViewById(R.id.tv_user_name)
        tvUserEmail = headerView.findViewById(R.id.tv_user_email)
        tvUserInitial = headerView.findViewById(R.id.tv_user_initial)
        tvAppVersion = headerView.findViewById(R.id.tv_app_version)
        tvSubscriptionBadge = headerView.findViewById(R.id.tv_subscription_badge)
        tvCurrentShop = headerView.findViewById(R.id.tv_current_shop)
        tvViewingMode = headerView.findViewById(R.id.tv_viewing_mode)
        ivSettings = headerView.findViewById(R.id.iv_settings)

        // Set initial values
        val userName = preferenceManager.getUserName().takeIf { it.isNotEmpty() } ?: "User"
        val userEmail = preferenceManager.getUserEmail().takeIf { it.isNotEmpty() } ?: "user@email.com"

        tvUserName.text = userName
        tvUserEmail.text = userEmail
        tvAppVersion.text = "v1.2.4 (Build 498)"

        val initial = if (userName.isNotEmpty()) userName.first().uppercase(Locale.getDefault()) else "A"
        tvUserInitial.text = initial

        // Set subscription badge
        updateSubscriptionBadge()

        // Set current shop
        val shopName = preferenceManager.getCurrentShopName().takeIf { it.isNotEmpty() } ?: "No Shop Selected"
        tvCurrentShop.text = shopName

        // Click listener for settings icon
        ivSettings.setOnClickListener {
            navController.navigate(R.id.accountFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    // ===== HEADER UPDATE METHODS =====
    fun updateHeaderUserInfo(userName: String, userEmail: String) {
        tvUserName.text = userName
        tvUserEmail.text = userEmail

        val initial = if (userName.isNotEmpty()) userName.first().uppercase(Locale.getDefault()) else "A"
        tvUserInitial.text = initial

        val parts = userName.split(" ", limit = 2)
        preferenceManager.saveUserFullData(
            userId = preferenceManager.getUserId(),
            email = userEmail,
            firstName = parts.getOrNull(0) ?: userName,
            lastName = parts.getOrNull(1) ?: "",
            phone = preferenceManager.getUserPhone()
        )
    }

    fun updateHeaderShopInfo(shopName: String) {
        preferenceManager.saveCurrentShopName(shopName)
        tvCurrentShop.text = shopName.ifEmpty { "No Shop Selected" }
        if (preferenceManager.getCurrentShopId().isNotEmpty()) {
            preferenceManager.saveHasShop(true)
        }
        checkSubscriptionStatus()
        updateSubscriptionBadge()
    }

    private fun updateSubscriptionBadge() {
        val status = preferenceManager.getSubscriptionStatus()
        when (status.uppercase()) {
            "ACTIVE", "TRIAL" -> {
                tvSubscriptionBadge.text = status.uppercase()
                tvSubscriptionBadge.visibility = View.VISIBLE
                tvSubscriptionBadge.setBackgroundResource(R.drawable.bg_subscription_badge_active)
            }
            "EXPIRED" -> {
                tvSubscriptionBadge.text = "EXPIRED"
                tvSubscriptionBadge.visibility = View.VISIBLE
                tvSubscriptionBadge.setBackgroundResource(R.drawable.bg_subscription_badge_expired)
            }
            else -> {
                tvSubscriptionBadge.visibility = View.GONE
            }
        }
    }

    // ===== LOAD DATA =====
    private fun loadInitialData() {
        if (isDataLoading) return
        isDataLoading = true
        showDataLoading()

        lifecycleScope.launch {
            try {
                println("🔄 MainActivity - Loading initial data...")
                println("📱 Current shop ID from prefs: ${preferenceManager.getCurrentShopId()}")
                println("📱 Current shop UUID from prefs: ${preferenceManager.getCurrentShopUuid()}")
                println("👤 Current user ID from prefs: ${preferenceManager.getUserId()}")
                println("🔐 Token present: ${preferenceManager.getAuthToken().isNotEmpty()}")

                val result = shopRepository.refreshShops()

                when (result) {
                    is Resource.Success -> {
                        println("✅ Shops refreshed successfully from API")

                        val shopsResult = shopRepository.getShops()
                        when (shopsResult) {
                            is Resource.Success -> {
                                val shopList = shopsResult.data ?: emptyList()
                                println("📊 Found ${shopList.size} shops in local database")

                                if (shopList.isNotEmpty()) {
                                    val currentShopId = preferenceManager.getCurrentShopId()
                                    val currentShopExists = shopList.any { it.id == currentShopId }

                                    if (currentShopId.isEmpty() || !currentShopExists) {
                                        val activeShop = shopList.find {
                                            it.subscriptionStatus.equals("ACTIVE", ignoreCase = true) ||
                                                    it.subscriptionStatus.equals("TRIAL", ignoreCase = true)
                                        }
                                        val selectedShop = activeShop ?: shopList.first()

                                        preferenceManager.saveCurrentShopId(selectedShop.id)
                                        preferenceManager.saveCurrentShopName(selectedShop.name)
                                        preferenceManager.saveCurrentShopUuid(selectedShop.id)
                                        preferenceManager.saveHasShop(true)
                                        preferenceManager.saveSubscriptionStatus(selectedShop.subscriptionStatus.uppercase())
                                        preferenceManager.saveSubscriptionExpiry(selectedShop.subscriptionExpiry ?: "")
                                        preferenceManager.saveSubscriptionType(selectedShop.subscriptionType ?: "")

                                        updateHeaderShopInfo(selectedShop.name)
                                        println("✅ Selected shop: ${selectedShop.name}")
                                    }
                                }

                                loadCurrentShopData()
                                checkSubscriptionStatus()
                                setupNavigationMenu()
                                updateSubscriptionBadge()
                            }
                            else -> {}
                        }
                    }

                    is Resource.Error -> {
                        println("❌ Failed to refresh shops: ${result.message}")

                        val isAuthError = result.message?.let { msg ->
                            msg.contains("Session expired", ignoreCase = true) ||
                                    msg.contains("Invalid token", ignoreCase = true) ||
                                    msg.contains("Unauthorized", ignoreCase = true) ||
                                    msg.contains("401", ignoreCase = true)
                        } ?: false

                        if (isAuthError) {
                            println("🔐 Token expired — forcing logout and redirecting to login")
                            runOnUiThread {
                                preferenceManager.clearAll()
                                Toast.makeText(
                                    this@MainActivity,
                                    "Your session has expired. Please log in again.",
                                    Toast.LENGTH_LONG
                                ).show()
                                forceLogoutToLogin(reason = "session_expired")
                            }
                            return@launch
                        }

                        loadCurrentShopData()
                        checkSubscriptionStatus()
                        setupNavigationMenu()
                    }

                    is Resource.Loading -> {
                        println("⏳ Loading shops...")
                    }
                }
            } catch (e: Exception) {
                println("❌ Error loading initial data: ${e.message}")
                e.printStackTrace()
            } finally {
                isDataLoading = false
                hideDataLoading()
            }
        }
    }

    private fun forceLogoutToLogin(reason: String = "") {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (reason.isNotEmpty()) putExtra("reason", reason)
        }
        startActivity(intent)
        finish()
    }

    private fun loadCurrentShopData() {
        val shopId = preferenceManager.getCurrentShopId()
        val shopName = preferenceManager.getCurrentShopName()
        if (shopId.isNotEmpty()) {
            currentShop = Shop(
                id = shopId,
                name = shopName,
                description = preferenceManager.getShopLocation(),
                address = preferenceManager.getShopLocation(),
                shopType = preferenceManager.getBusinessType(),
                phone = preferenceManager.getShopContact(),
                totalRevenue = 0.0,
                totalExpenses = 0.0,
                profit = 0.0,
                totalProducts = 0,
                totalEmployees = 0,
                logoUrl = null,
                subscription = null,
                subscriptionStatus = preferenceManager.getSubscriptionStatus(),
                subscriptionType = preferenceManager.getSubscriptionType(),
                subscriptionExpiry = preferenceManager.getSubscriptionExpiry(),
                isActive = true
            )
        }
    }

    private fun checkSubscriptionStatus() {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isNotEmpty()) {
            setupNavigationMenu()
            preferenceManager.debugSubscriptionInfo()
        }
    }

    // ===== NAVIGATION MENU =====
    private fun setupNavigationMenu() {
        Log.d("NavDrawer_DEBUG", "📍 setupNavigationMenu() called")

        val menu = navigationView.menu
        val hasShops = preferenceManager.hasShop()
        val isOwner = permissionManager.isShopOwner()

        Log.d("NavDrawer_DEBUG", "📊 Menu setup state: hasShops=$hasShops, isOwner=$isOwner")

        // Always visible
        menu.findItem(R.id.nav_dashboard).isVisible = true
        menu.findItem(R.id.nav_contact_us).isVisible = true
        menu.findItem(R.id.nav_logout).isVisible = true

        // Shop management - only for owners
        menu.findItem(R.id.nav_shops).isVisible = isOwner
        menu.findItem(R.id.nav_shop_home).isVisible = hasShops

        // Business operations based on permissions
        val businessItems = mapOf(
            R.id.nav_sales to PermissionType.VIEW_SALES,
            R.id.nav_finance to PermissionType.VIEW_FINANCE,
            R.id.nav_inventory to PermissionType.VIEW_INVENTORY,
            R.id.nav_employees to PermissionType.VIEW_EMPLOYEES,
            R.id.nav_customers to PermissionType.VIEW_CUSTOMERS
        )
        businessItems.forEach { (itemId, permission) ->
            val isVisible = hasShops && permissionManager.hasPermission(permission)
            menu.findItem(itemId).isVisible = isVisible
            Log.d("NavDrawer_DEBUG", "  Menu item $itemId: visible=$isVisible, permission=$permission")
        }

        menu.findItem(R.id.nav_suppliers).isVisible = hasShops &&
                permissionManager.hasPermission(PermissionType.VIEW_INVENTORY)
        menu.findItem(R.id.nav_transfers).isVisible = hasShops

        // Subscription - only for owners
        val subscriptionItem = menu.findItem(R.id.nav_subscription)
        subscriptionItem.isVisible = hasShops && isOwner
        if (hasShops && isOwner) {
            if (!hasAccessToBusinessOperations()) {
                subscriptionItem.title = "UPGRADE NOW"
                subscriptionItem.icon = ContextCompat.getDrawable(this, R.drawable.ic_upgrade)
            } else {
                subscriptionItem.title = "Subscription"
                subscriptionItem.icon = ContextCompat.getDrawable(this, R.drawable.ic_subscription)
            }
        }

        // Account - only for owners or employees with VIEW_EMPLOYEES
        menu.findItem(R.id.nav_account).isVisible = hasShops &&
                (isOwner || permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES))

        val userRolesItem = menu.findItem(R.id.nav_user_roles)
        userRolesItem.isVisible = hasShops && isOwner
        Log.d("NavDrawer_DEBUG", "  Menu item nav_user_roles: visible=${userRolesItem.isVisible} (isOwner=$isOwner)")

        // Update subscription badge in header
        updateSubscriptionBadge()
        Log.d("NavDrawer_DEBUG", "✅ setupNavigationMenu() completed")
    }

    fun refreshNavigationMenu() {
        setupNavigationMenu()
        navigationView.invalidate()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d("NavDrawer_DEBUG", "🔴 ============ MenuItem CLICKED ============")
        Log.d("NavDrawer_DEBUG", "   itemId: ${item.itemId}")
        Log.d("NavDrawer_DEBUG", "   title: ${item.title}")

        when (item.itemId) {
            R.id.nav_dashboard -> navController.navigate(R.id.mainDashboardFragment)
            R.id.nav_shops -> {
                if (permissionManager.isShopOwner()) navController.navigate(R.id.shopsFragment)
                else showPermissionDeniedDialog("manage shops")
            }
            R.id.nav_shop_home -> {
                if (preferenceManager.hasShop()) navController.navigate(R.id.shopDashboardFragment)
                else showSelectShopFirstDialog()
            }
            R.id.nav_sales -> {
                if (permissionManager.hasPermission(PermissionType.VIEW_SALES)) navController.navigate(R.id.salesFragment)
                else showPermissionDeniedDialog("sales")
            }
            R.id.nav_finance -> {
                if (permissionManager.hasPermission(PermissionType.VIEW_FINANCE)) navController.navigate(R.id.financeFragment)
                else showPermissionDeniedDialog("finance")
            }
            R.id.nav_inventory -> {
                if (permissionManager.hasPermission(PermissionType.VIEW_INVENTORY)) navController.navigate(R.id.inventoryFragment)
                else showPermissionDeniedDialog("inventory")
            }
            R.id.nav_transfers -> Toast.makeText(this, "Transfers coming soon", Toast.LENGTH_SHORT).show()
            R.id.nav_employees -> {
                if (permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)) navController.navigate(R.id.employeesFragment)
                else showPermissionDeniedDialog("employee management")
            }
            R.id.nav_suppliers -> {
                if (permissionManager.hasPermission(PermissionType.VIEW_INVENTORY)) navController.navigate(R.id.suppliersFragment)
                else showPermissionDeniedDialog("suppliers")
            }
            R.id.nav_customers -> {
                if (permissionManager.hasPermission(PermissionType.VIEW_CUSTOMERS)) navController.navigate(R.id.customersFragment)
                else showPermissionDeniedDialog("customer management")
            }
            R.id.nav_subscription -> {
                if (permissionManager.isShopOwner()) navigateToSubscriptionPackages()
                else showPermissionDeniedDialog("subscription management")
            }
            R.id.nav_user_roles -> {
                if (permissionManager.isShopOwner()) navController.navigate(R.id.userRolesFragment)
                else {
                    showPermissionDeniedDialog("user roles management")
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
            }
            R.id.nav_account -> {
                val hasViewPermission = permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)
                val isOwner = permissionManager.isShopOwner()
                if (hasViewPermission || isOwner) navController.navigate(R.id.accountFragment)
                else showPermissionDeniedDialog("account settings")
            }
            R.id.nav_contact_us -> navController.navigate(R.id.contactUsFragment)
            R.id.nav_logout -> logout()
            else -> Toast.makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // ===== FAB METHODS =====
    private fun showFab() {
        binding.mainFab.visibility = View.VISIBLE
        binding.mainFab.alpha = 0f
        binding.mainFab.animate().alpha(1f).setDuration(300).start()
    }

    private fun hideFab() {
        if (isFabMenuOpen) toggleFabMenu()
        binding.mainFab.animate().alpha(0f).setDuration(200).withEndAction {
            binding.mainFab.visibility = View.GONE
            binding.fabSale.visibility = View.GONE
            binding.fabProduct.visibility = View.GONE
        }.start()
    }

    private fun toggleFabMenu() {
        if (isFabMenuOpen) {
            animateFab(binding.fabSale, 0f, false)
            animateFab(binding.fabProduct, 0f, false)
            binding.mainFab.setImageResource(R.drawable.ic_add)
        } else {
            animateFab(binding.fabSale, 1f, true)
            animateFab(binding.fabProduct, 1f, true)
            binding.mainFab.setImageResource(R.drawable.ic_close)
        }
        isFabMenuOpen = !isFabMenuOpen
    }

    private fun animateFab(fab: View, toAlpha: Float, show: Boolean) {
        val scaleX = ObjectAnimator.ofFloat(fab, View.SCALE_X, if (show) 1f else 0f)
        val scaleY = ObjectAnimator.ofFloat(fab, View.SCALE_Y, if (show) 1f else 0f)
        val alpha = ObjectAnimator.ofFloat(fab, View.ALPHA, toAlpha)

        scaleX.duration = 200
        scaleY.duration = 200
        alpha.duration = 200

        scaleX.start()
        scaleY.start()
        alpha.start()

        if (show) {
            fab.visibility = View.VISIBLE
        } else {
            alpha.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    fab.visibility = View.GONE
                }
            })
        }
    }

    private fun addNewProduct() {
        if (!preferenceManager.hasShop()) { showSelectShopFirstDialog(); return }
        navController.navigate(R.id.addProductFragment)
    }

    private fun createNewSale() {
        if (!preferenceManager.hasShop()) { showSelectShopFirstDialog(); return }
        navController.navigate(R.id.newSaleFragment)
    }

    // ===== ACCESS CHECK =====
    private fun hasAccessToBusinessOperations(): Boolean {
        if (subscriptionCheckInProgress) return false
        subscriptionCheckInProgress = true
        return try {
            if (!preferenceManager.hasShop()) return false
            if (permissionManager.isShopOwner()) {
                val hasActiveSub = preferenceManager.hasActiveSubscription()
                val isInTrial = preferenceManager.isTrial()
                val expiry = preferenceManager.getSubscriptionExpiry()
                if (isInTrial && isTrialExpired(expiry)) {
                    preferenceManager.saveSubscriptionStatus("expired")
                    return false
                }
                hasActiveSub || (isInTrial && !isTrialExpired(expiry))
            } else {
                true
            }
        } finally {
            subscriptionCheckInProgress = false
        }
    }

    private fun hasAccessToManagement(): Boolean {
        return if (permissionManager.isShopOwner()) hasAccessToBusinessOperations() else false
    }

    private fun isTrialExpired(expiryDate: String?): Boolean {
        if (expiryDate.isNullOrEmpty()) return false
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiry = format.parse(expiryDate)
            Date().after(expiry)
        } catch (e: Exception) {
            false
        }
    }

    // ===== DIALOGS =====
    private fun showSelectShopFirstDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Shop Selected")
            .setMessage("Please select or create a shop first to continue.")
            .setPositiveButton("Go to Shops") { _, _ -> navController.navigate(R.id.shopsFragment) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSubscriptionRequiredDialog(feature: String = "business operations") {
        AlertDialog.Builder(this)
            .setTitle("Subscription Required")
            .setMessage("You need an active subscription to access $feature. Please subscribe to continue.")
            .setPositiveButton("Subscribe Now") { _, _ -> navigateToSubscriptionPackages() }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Learn More") { _, _ -> showSubscriptionInfoDialog() }
            .show()
    }

    private fun showSubscriptionInfoDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Subscriptions")
            .setMessage(
                "With an active subscription, you can:\n\n" +
                        "✓ Process sales transactions\n" +
                        "✓ Track finances and expenses\n" +
                        "✓ Manage inventory and stock\n" +
                        "✓ Handle employee management\n" +
                        "✓ Generate reports and analytics\n" +
                        "✓ Access premium features\n\n" +
                        "Choose a plan that fits your business needs!"
            )
            .setPositiveButton("View Plans") { _, _ -> navigateToSubscriptionPackages() }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showPermissionDeniedDialog(feature: String) {
        AlertDialog.Builder(this)
            .setTitle("Access Denied")
            .setMessage("You don't have permission to access $feature. Please contact your shop owner.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun navigateToSubscriptionPackages() {
        if (!preferenceManager.hasShop()) { showSelectShopFirstDialog(); return }
        navController.navigate(R.id.subscriptionPackagesFragment)
    }

    // ===== LOGOUT =====
    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ -> performLogout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        permissionManager.clearUserPermissions()
        preferenceManager.clearAll()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        forceLogoutToLogin()
    }

    // ===== UTILITY =====
    private fun getAvatarColor(name: String): Int {
        val colors = listOf(
            android.graphics.Color.parseColor("#FF6B6B"),
            android.graphics.Color.parseColor("#4ECDC4"),
            android.graphics.Color.parseColor("#FFD166"),
            android.graphics.Color.parseColor("#06D6A0"),
            android.graphics.Color.parseColor("#118AB2"),
            android.graphics.Color.parseColor("#EF476F"),
            android.graphics.Color.parseColor("#073B4C")
        )
        val index = if (name.isNotEmpty()) name.hashCode() % colors.size else 0
        return colors[Math.abs(index)]
    }

    private fun showDataLoading() {
        binding.progressOverlay.visibility = View.VISIBLE
    }

    private fun hideDataLoading() {
        binding.progressOverlay.visibility = View.GONE
    }

    private fun handleDestinationArguments(destinationId: Int, arguments: Bundle?) {
        // Placeholder
    }

    fun onSubscriptionUpdated() {
        checkSubscriptionStatus()
        updateSubscriptionBadge()
        Toast.makeText(this, "Subscription updated successfully!", Toast.LENGTH_SHORT).show()
    }

    // ===== PUBLIC METHOD TO REFRESH AND SELECT ACTIVE SHOP =====
    fun refreshAndSelectActiveShop(callback: (() -> Unit)? = null) {
        lifecycleScope.launch {
            try {
                val refreshResult = shopRepository.refreshShops()
                if (refreshResult is Resource.Success) {
                    val shopsResult = shopRepository.getShops()
                    if (shopsResult is Resource.Success) {
                        val shops = shopsResult.data ?: emptyList()
                        val activeShop = shops.find {
                            it.subscriptionStatus.equals("active", ignoreCase = true) ||
                                    it.subscriptionStatus.equals("trial", ignoreCase = true)
                        }
                        if (activeShop != null) {
                            val uuid = activeShop.uuid ?: activeShop.id
                            preferenceManager.saveCurrentShopId(activeShop.id)
                            preferenceManager.saveCurrentShopUuid(uuid)
                            preferenceManager.saveCurrentShopName(activeShop.name)
                            preferenceManager.saveHasShop(true)
                            preferenceManager.saveSubscriptionStatus(activeShop.subscriptionStatus.uppercase())
                            preferenceManager.saveSubscriptionType(activeShop.subscriptionType ?: "")
                            preferenceManager.saveSubscriptionExpiry(activeShop.subscriptionExpiry ?: "")

                            runOnUiThread {
                                updateHeaderShopInfo(activeShop.name)
                                setupNavigationMenu()
                                updateSubscriptionBadge()
                                reloadDashboardFragment()
                            }
                        } else {
                            // fallback
                            val fallback = shops.firstOrNull()
                            if (fallback != null) {
                                preferenceManager.saveCurrentShopId(fallback.id)
                                preferenceManager.saveCurrentShopUuid(fallback.uuid ?: fallback.id)
                                preferenceManager.saveCurrentShopName(fallback.name)
                                runOnUiThread {
                                    updateHeaderShopInfo(fallback.name)
                                    setupNavigationMenu()
                                    updateSubscriptionBadge()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error refreshing shops", e)
            }
            callback?.invoke()
        }
    }



    private fun reloadDashboardFragment() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is MainDashboardFragment) {
            fragment.loadDashboardData()
        }
    }

    // ===== NAVIGATION =====
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        when {
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            isFabMenuOpen -> {
                toggleFabMenu()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}