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
import android.widget.ImageView
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
import com.devbrian.osebo.models.Shop
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
        R.id.paymentStatusFragment
    )

    private val managementRequiredDestinations = setOf(
        R.id.accountFragment,
        R.id.sessionsFragment,
        R.id.userRolesFragment,
        R.id.subscriptionOverviewFragment,
        R.id.subscriptionsFragment,
        R.id.subscriptionDetailsFragment
    )

    private var isFabMenuOpen = false
    private var subscriptionCheckInProgress = false
    private var currentShop: Shop? = null
    private var isDataLoading = false

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager.getInstance(this)
        permissionManager = PermissionManager(this)

        setupToolbar()
        setupNavigation()
        setupHeaderView()
        setupFloatingActionButtons()
        setupNavControllerListener()

        loadInitialData()
    }

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    private fun setupToolbar() {
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    private fun setupNavigation() {
        Log.d("NavDrawer_DEBUG", "📍 setupNavigation() called")

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        Log.d("NavDrawer_DEBUG", "✅ NavController initialized")

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainDashboardFragment,
                R.id.shopsFragment,
                R.id.salesFragment,
                R.id.financeFragment,
                R.id.inventoryFragment,
                R.id.employeesFragment,
                R.id.userRolesFragment,
                R.id.customersFragment
            ),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        // 🔥 SET LISTENER
        binding.navigationView.setNavigationItemSelectedListener(this)
        Log.d("NavDrawer_DEBUG", "✅ NavigationItemSelectedListener set")

        binding.navigationView.itemIconTintList = null
        binding.navigationView.isClickable = true
        binding.navigationView.isFocusable = true
        binding.navigationView.isLongClickable = true
        Log.d("NavDrawer_DEBUG", "✅ NavigationView interactive properties set")

        binding.topAppBar.setNavigationOnClickListener {
            Log.d("NavDrawer_DEBUG", "🔵 Hamburger menu clicked - Opening drawer")
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // 🔥 DEBUG: Log drawer events
        binding.drawerLayout.addDrawerListener(object : androidx.drawerlayout.widget.DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                Log.d("NavDrawer_DEBUG", "📂 Drawer sliding: offset=$slideOffset")
            }

            override fun onDrawerOpened(drawerView: View) {
                Log.d("NavDrawer_DEBUG", "📂 ✅ DRAWER OPENED - Menu items should be clickable now")
            }

            override fun onDrawerClosed(drawerView: View) {
                Log.d("NavDrawer_DEBUG", "📂 Drawer closed")
            }

            override fun onDrawerStateChanged(newState: Int) {
                Log.d("NavDrawer_DEBUG", "📂 Drawer state changed: $newState")
            }
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
                    updateHeaderForShopView(shopName)
                }
                R.id.mainDashboardFragment -> {
                    supportActionBar?.title = "Dashboard"
                    updateHeaderForMainView()
                }
                else -> {
                    supportActionBar?.title = destination.label
                }
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

    private fun setupHeaderView() {
        val headerView = binding.navigationView.getHeaderView(0)

        val tvUserName = headerView.findViewById<TextView>(R.id.tv_user_name)
        val tvUserEmail = headerView.findViewById<TextView>(R.id.tv_user_email)
        val tvUserInitial = headerView.findViewById<TextView>(R.id.tv_user_initial)
        val tvCurrentShop = headerView.findViewById<TextView>(R.id.tv_current_shop)
        val ivSettings = headerView.findViewById<ImageView>(R.id.iv_settings)
        val llUserAvatar = headerView.findViewById<View>(R.id.ll_user_avatar)
        val tvSubscriptionBadge = headerView.findViewById<TextView>(R.id.tv_subscription_badge)

        val userName = preferenceManager.getUserName()
        val userEmail = preferenceManager.getUserEmail()
        val shopName = preferenceManager.getCurrentShopName()

        tvUserName.text = userName.ifEmpty { "Admin User" }
        tvUserEmail.text = userEmail.ifEmpty { "admin@osebo.ai" }
        tvCurrentShop.text = shopName.ifEmpty { "No Shop Selected" }

        updateSubscriptionBadge(tvSubscriptionBadge)

        val initial = if (userName.isNotEmpty()) userName.first().toString() else "A"
        tvUserInitial.text = initial.uppercase()
        llUserAvatar.setBackgroundColor(getAvatarColor(userName))

        // Set up click listeners
        llUserAvatar.setOnClickListener {
            Log.d("NavDrawer_DEBUG", "🔵 Avatar clicked - navigating to profile")
            navigateToProfile()
        }
        ivSettings.setOnClickListener {
            Log.d("NavDrawer_DEBUG", "🔵 Settings clicked - navigating to settings")
            navigateToSettings()
        }
    }

    // -------------------------------------------------------------------------
    // Data loading — with auto-logout on 401
    // -------------------------------------------------------------------------

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
                                updateHeaderWithSubscriptionStatus()
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
            checkForExpiringSubscription()
            preferenceManager.debugSubscriptionInfo()
        }
    }

    private fun checkForExpiringSubscription() {
        // Your existing implementation
    }

    private fun handleDestinationArguments(destinationId: Int, arguments: Bundle?) {
        when (destinationId) {
            R.id.subscriptionPackagesFragment -> { }
        }
    }

    // -------------------------------------------------------------------------
    // Navigation menu visibility
    // -------------------------------------------------------------------------

    private fun setupNavigationMenu() {
        Log.d("NavDrawer_DEBUG", "📍 setupNavigationMenu() called")

        val menu = binding.navigationView.menu
        val hasShops = preferenceManager.hasShop()
        val isOwner = permissionManager.isShopOwner()

        Log.d("NavDrawer_DEBUG", "📊 Menu setup state: hasShops=$hasShops, isOwner=$isOwner")

        // Always visible — never gated by subscription or shop
        menu.findItem(R.id.nav_dashboard).isVisible = true
        menu.findItem(R.id.nav_contact_us).isVisible = true
        menu.findItem(R.id.nav_logout).isVisible = true

        // Owner-only items
        menu.findItem(R.id.nav_shops).isVisible = isOwner
        menu.findItem(R.id.nav_shop_home).isVisible = hasShops

        // Business operations — gated by permission
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

        // Subscription item — label changes to "UPGRADE NOW" when no active subscription
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

        // Account
        menu.findItem(R.id.nav_account).isVisible = hasShops &&
                (isOwner || permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES))

        updateHeaderWithSubscriptionStatus()
        Log.d("NavDrawer_DEBUG", "✅ setupNavigationMenu() completed")
    }

    fun refreshNavigationMenu() {
        setupNavigationMenu()
        binding.navigationView.invalidate()
    }

    // -------------------------------------------------------------------------
    // Navigation item selection
    // ALL menu item IDs have an explicit handler — no silent dead ends
    // -------------------------------------------------------------------------

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d("NavDrawer_DEBUG", "🔴 ============ MenuItem CLICKED ============")
        Log.d("NavDrawer_DEBUG", "   itemId: ${item.itemId}")
        Log.d("NavDrawer_DEBUG", "   title: ${item.title}")
        Log.d("NavDrawer_DEBUG", "   isVisible: ${item.isVisible}")
        Log.d("NavDrawer_DEBUG", "   isEnabled: ${item.isEnabled}")

        when (item.itemId) {

            // HOME
            R.id.nav_dashboard -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_dashboard - Navigating to mainDashboardFragment")
                navController.navigate(R.id.mainDashboardFragment)
            }

            R.id.nav_shops -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_shops - isShopOwner: ${permissionManager.isShopOwner()}")
                if (permissionManager.isShopOwner()) {
                    Log.d("NavDrawer_DEBUG", "→ Navigating to shopsFragment")
                    navController.navigate(R.id.shopsFragment)
                } else {
                    Log.d("NavDrawer_DEBUG", "⚠️ Permission denied - not shop owner")
                    showPermissionDeniedDialog("manage shops")
                }
            }

            // SHOP
            R.id.nav_shop_home -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_shop_home - hasShop: ${preferenceManager.hasShop()}")
                if (preferenceManager.hasShop()) {
                    Log.d("NavDrawer_DEBUG", "→ Navigating to shopDashboardFragment")
                    navController.navigate(R.id.shopDashboardFragment)
                } else {
                    Log.d("NavDrawer_DEBUG", "⚠️ No shop selected")
                    showSelectShopFirstDialog()
                }
            }

            // BUSINESS OPERATIONS
            R.id.nav_sales -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_sales - Checking permission: VIEW_SALES")
                val hasPermission = permissionManager.hasPermission(PermissionType.VIEW_SALES)
                Log.d("NavDrawer_DEBUG", "   hasPermission: $hasPermission")
                if (hasPermission) {
                    Log.d("NavDrawer_DEBUG", "→ Navigating to salesFragment")
                    navController.navigate(R.id.salesFragment)
                } else {
                    Log.d("NavDrawer_DEBUG", "⚠️ Permission denied - VIEW_SALES")
                    showPermissionDeniedDialog("sales")
                }
            }

            R.id.nav_finance -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_finance - Checking permission: VIEW_FINANCE")
                val hasPermission = permissionManager.hasPermission(PermissionType.VIEW_FINANCE)
                Log.d("NavDrawer_DEBUG", "   hasPermission: $hasPermission")
                if (hasPermission) {
                    Log.d("NavDrawer_DEBUG", "→ Navigating to financeFragment")
                    navController.navigate(R.id.financeFragment)
                } else {
                    Log.d("NavDrawer_DEBUG", "⚠️ Permission denied - VIEW_FINANCE")
                    showPermissionDeniedDialog("finance")
                }
            }

            R.id.nav_inventory -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_inventory - Checking permission: VIEW_INVENTORY")
                val hasPermission = permissionManager.hasPermission(PermissionType.VIEW_INVENTORY)
                Log.d("NavDrawer_DEBUG", "   hasPermission: $hasPermission")
                if (hasPermission) {
                    Log.d("NavDrawer_DEBUG", "→ Navigating to inventoryFragment")
                    navController.navigate(R.id.inventoryFragment)
                } else {
                    Log.d("NavDrawer_DEBUG", "⚠️ Permission denied - VIEW_INVENTORY")
                    showPermissionDeniedDialog("inventory")
                }
            }

            R.id.nav_transfers -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_transfers - Coming soon")
                Toast.makeText(this, "Transfers coming soon", Toast.LENGTH_SHORT).show()
            }

            // MANAGEMENT
            R.id.nav_employees -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_employees - Checking permission: VIEW_EMPLOYEES")
                val hasPermission = permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)
                Log.d("NavDrawer_DEBUG", "   hasPermission: $hasPermission")
                if (hasPermission) {
                    Log.d("NavDrawer_DEBUG", "→ Navigating to employeesFragment")
                    navController.navigate(R.id.employeesFragment)
                } else {
                    Log.d("NavDrawer_DEBUG", "⚠️ Permission denied - VIEW_EMPLOYEES")
                    showPermissionDeniedDialog("employee management")
                }
            }

            R.id.nav_suppliers -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_suppliers - Checking permission: VIEW_INVENTORY")
                val hasPermission = permissionManager.hasPermission(PermissionType.VIEW_INVENTORY)
                Log.d("NavDrawer_DEBUG", "   hasPermission: $hasPermission")
                if (hasPermission) {
                    Log.d("NavDrawer_DEBUG", "→ Navigating to suppliersFragment")
                    navController.navigate(R.id.suppliersFragment)
                } else {
                    Log.d("NavDrawer_DEBUG", "⚠️ Permission denied - VIEW_INVENTORY (suppliers)")
                    showPermissionDeniedDialog("suppliers")
                }
            }

            R.id.nav_customers -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_customers - Checking permission: VIEW_CUSTOMERS")
                val hasPermission = permissionManager.hasPermission(PermissionType.VIEW_CUSTOMERS)
                Log.d("NavDrawer_DEBUG", "   hasPermission: $hasPermission")
                if (hasPermission) {
                    Log.d("NavDrawer_DEBUG", "→ Navigating to customersFragment")
                    navController.navigate(R.id.customersFragment)
                } else {
                    Log.d("NavDrawer_DEBUG", "⚠️ Permission denied - VIEW_CUSTOMERS")
                    showPermissionDeniedDialog("customer management")
                }
            }

            // SETTINGS
            R.id.nav_subscription -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_subscription - isShopOwner: ${permissionManager.isShopOwner()}")
                if (permissionManager.isShopOwner()) {
                    Log.d("NavDrawer_DEBUG", "→ Navigating to subscriptionPackagesFragment")
                    navigateToSubscriptionPackages()
                } else {
                    Log.d("NavDrawer_DEBUG", "⚠️ Permission denied - not shop owner")
                    showPermissionDeniedDialog("subscription management")
                }
            }

            R.id.nav_user_roles -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_user_roles - Navigating to userRolesFragment")
                navController.navigate(R.id.userRolesFragment)
            }

            R.id.nav_account -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_account - Checking permissions")
                val hasViewPermission = permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)
                val isOwner = permissionManager.isShopOwner()
                Log.d("NavDrawer_DEBUG", "   hasViewPermission: $hasViewPermission, isOwner: $isOwner")
                if (hasViewPermission || isOwner) {
                    Log.d("NavDrawer_DEBUG", "→ Navigating to accountFragment")
                    navController.navigate(R.id.accountFragment)
                } else {
                    Log.d("NavDrawer_DEBUG", "⚠️ Permission denied - account settings")
                    showPermissionDeniedDialog("account settings")
                }
            }

            R.id.nav_contact_us -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_contact_us - Navigating to contactUsFragment")
                navController.navigate(R.id.contactUsFragment)
            }

            R.id.nav_logout -> {
                Log.d("NavDrawer_DEBUG", "→ Case: nav_logout - Showing logout dialog")
                logout()
            }

            else -> {
                Log.d("NavDrawer_DEBUG", "⚠️ Unhandled menu item: ${item.itemId}")
                Toast.makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show()
            }
        }

        Log.d("NavDrawer_DEBUG", "🟢 Closing drawer")
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        Log.d("NavDrawer_DEBUG", "🟢 ============ MenuItem handling COMPLETE ============")
        return true
    }

    // -------------------------------------------------------------------------
    // Header updates
    // -------------------------------------------------------------------------

    private fun updateHeaderWithSubscriptionStatus() {
        val headerView = binding.navigationView.getHeaderView(0)
        val tvAppVersion = headerView.findViewById<TextView>(R.id.tv_app_version)

        val hasAccess = hasAccessToBusinessOperations()
        val isOwner = permissionManager.isShopOwner()

        val status = when {
            !preferenceManager.hasShop() -> "NO SHOP SELECTED"
            !isOwner -> "EMPLOYEE ACCOUNT"
            preferenceManager.isTrial() -> "TRIAL (expires: ${preferenceManager.getSubscriptionExpiry()})"
            hasAccess -> "ACTIVE"
            else -> "NO SUBSCRIPTION"
        }

        tvAppVersion.text = "v1.2.0 • $status"
    }

    private fun updateHeaderForShopView(shopName: String) {
        val headerView = binding.navigationView.getHeaderView(0)
        val tvCurrentShop = headerView.findViewById<TextView>(R.id.tv_current_shop)
        val tvViewingMode = headerView.findViewById<TextView>(R.id.tv_viewing_mode)

        tvViewingMode?.text = "Viewing:"
        tvViewingMode?.visibility = View.VISIBLE
        tvCurrentShop.text = shopName
        tvCurrentShop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shop, 0, 0, 0)
    }

    private fun updateHeaderForMainView() {
        val headerView = binding.navigationView.getHeaderView(0)
        val tvCurrentShop = headerView.findViewById<TextView>(R.id.tv_current_shop)
        val tvViewingMode = headerView.findViewById<TextView>(R.id.tv_viewing_mode)

        tvViewingMode?.visibility = View.GONE
        val shopName = preferenceManager.getCurrentShopName()
        tvCurrentShop.text = shopName.ifEmpty { "No Shop Selected" }
        tvCurrentShop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location, 0, 0, 0)
    }

    private fun updateSubscriptionBadge(tvSubscriptionBadge: TextView) {
        val hasAccess = hasAccessToBusinessOperations()
        val isTrial = preferenceManager.isTrial()
        val expiry = preferenceManager.getSubscriptionExpiry()
        val isOwner = permissionManager.isShopOwner()

        val statusText = when {
            !preferenceManager.hasShop() -> "NO SHOP"
            !isOwner -> "EMPLOYEE"
            hasAccess && isTrial -> {
                val daysLeft = calculateDaysUntilExpiry(expiry)
                if (daysLeft > 0) "TRIAL · $daysLeft days left" else "TRIAL ENDED"
            }
            hasAccess && !isTrial -> "ACTIVE"
            preferenceManager.isExpired() -> "EXPIRED"
            else -> "NO SUBSCRIPTION"
        }

        val backgroundDrawable = when {
            !preferenceManager.hasShop() -> R.drawable.bg_subscription_badge_inactive
            !isOwner -> R.drawable.bg_subscription_badge_employee
            hasAccess && isTrial -> R.drawable.bg_subscription_badge_trial
            hasAccess && !isTrial -> R.drawable.bg_subscription_badge_active
            else -> R.drawable.bg_subscription_badge_expired
        }

        tvSubscriptionBadge.text = statusText
        tvSubscriptionBadge.setBackgroundResource(backgroundDrawable)
        tvSubscriptionBadge.visibility = View.VISIBLE
    }

    // -------------------------------------------------------------------------
    // Public API for fragments to call back into MainActivity
    // -------------------------------------------------------------------------

    fun updateHeaderShopInfo(shopName: String) {
        val headerView = binding.navigationView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.tv_current_shop).text = shopName
        preferenceManager.saveCurrentShopName(shopName)
        if (preferenceManager.getCurrentShopId().isNotEmpty()) {
            preferenceManager.saveHasShop(true)
        }
        checkSubscriptionStatus()
    }

    fun updateHeaderUserInfo(userName: String, userEmail: String) {
        val headerView = binding.navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tv_user_name)
        val tvUserEmail = headerView.findViewById<TextView>(R.id.tv_user_email)
        val tvUserInitial = headerView.findViewById<TextView>(R.id.tv_user_initial)
        val llUserAvatar = headerView.findViewById<View>(R.id.ll_user_avatar)

        tvUserName.text = userName
        tvUserEmail.text = userEmail
        val initial = if (userName.isNotEmpty()) userName.first().toString() else "A"
        tvUserInitial.text = initial.uppercase()
        llUserAvatar.setBackgroundColor(getAvatarColor(userName))

        val parts = userName.split(" ", limit = 2)
        val firstName = parts.getOrNull(0) ?: userName
        val lastName = parts.getOrNull(1) ?: ""

        preferenceManager.saveUserFullData(
            userId = preferenceManager.getUserId(),
            email = userEmail,
            firstName = firstName,
            lastName = lastName,
            phone = preferenceManager.getUserPhone()
        )
    }

    fun onSubscriptionUpdated() {
        checkSubscriptionStatus()
        Toast.makeText(this, "Subscription updated successfully!", Toast.LENGTH_SHORT).show()
    }

    // -------------------------------------------------------------------------
    // FAB
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Subscription / access helpers
    // -------------------------------------------------------------------------

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

    private fun calculateDaysUntilExpiry(expiryDate: String?): Int {
        if (expiryDate.isNullOrEmpty()) return 0
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiry = format.parse(expiryDate)
            val diffInMillies = expiry.time - Date().time
            (diffInMillies / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }

    // -------------------------------------------------------------------------
    // Dialogs
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

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

    private fun navigateToProfile() {
        Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSettings() {
        Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
    }

    private fun showDataLoading() {
        binding.progressOverlay?.visibility = View.VISIBLE
    }

    private fun hideDataLoading() {
        binding.progressOverlay?.visibility = View.GONE
    }

    // -------------------------------------------------------------------------
    // Back stack / Up navigation
    // -------------------------------------------------------------------------

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        Log.d("NavDrawer_DEBUG", "📍 onBackPressed() called")
        when {
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                Log.d("NavDrawer_DEBUG", "🔵 Closing drawer via back press")
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            isFabMenuOpen -> {
                Log.d("NavDrawer_DEBUG", "🔵 Closing FAB menu via back press")
                toggleFabMenu()
            }
            else -> {
                Log.d("NavDrawer_DEBUG", "🔵 Standard back press")
                super.onBackPressed()
            }
        }
    }
}