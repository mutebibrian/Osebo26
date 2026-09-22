package com.devbrian.osebo.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.repository.ShopRepositoryImpl
import com.devbrian.osebo.databinding.ActivityMainBinding
import com.devbrian.osebo.fragments.MainDashboardFragment
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.models.PermissionType
import com.devbrian.osebo.ui.components.MoreMenuItem
import com.devbrian.osebo.ui.components.MoreMenuSection
import com.devbrian.osebo.ui.components.MoreMenuSheet
import com.devbrian.osebo.ui.components.UserBottomNavigation
import com.devbrian.osebo.ui.components.UserNavigationItem
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.utils.PermissionManager
import com.devbrian.osebo.utils.Resource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var permissionManager: PermissionManager

    private val shopRepository: ShopRepositoryImpl by inject()

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

    private val inventoryBottomNavigationDestinations = setOf(
        R.id.inventoryFragment,
        R.id.addProductFragment,
        R.id.productDetailsFragment,
        R.id.restockFragment,
    )

    private val salesBottomNavigationDestinations = setOf(
        R.id.salesFragment,
        R.id.newSaleFragment,
        R.id.paymentFragment,
        R.id.receiptFragment,
    )

    private var isFabMenuOpen = false
    private var subscriptionCheckInProgress = false
    private var currentShop: Shop? = null
    private var isDataLoading = false
    private val selectedBottomNavigationItem: MutableState<UserNavigationItem> =
        mutableStateOf(UserNavigationItem.Home)
    private val isMoreMenuVisible: MutableState<Boolean> = mutableStateOf(false)
    private val moreMenuSections: MutableState<List<MoreMenuSection>> = mutableStateOf(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupImmersiveWindow()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager.getInstance(this)
        permissionManager = PermissionManager(this)

        setupNavigation()
        setupFloatingActionButtons()
        setupNavControllerListener()
        setupUserBottomNavigation()

        loadInitialData()
    }

    private fun setupImmersiveWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun setupUserBottomNavigation() {
        val bottomNavigation = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OseboTheme {
                    UserBottomNavigation(
                        selectedItem = selectedBottomNavigationItem.value,
                        onItemSelected = ::handleBottomNavigationSelection,
                        onOseboAiClick = {
                            Toast.makeText(
                                this@MainActivity,
                                "Osebo AI is coming next",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                    )

                    if (isMoreMenuVisible.value) {
                        MoreMenuSheet(
                            sections = moreMenuSections.value,
                            onDismiss = ::dismissMoreMenu,
                            onItemSelected = ::handleMoreMenuSelection,
                        )
                    }
                }
            }
        }

        binding.contentOverlay.addView(
            bottomNavigation,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM,
            ),
        )

        binding.mainFab.visibility = View.GONE
        binding.fabSale.visibility = View.GONE
        binding.fabProduct.visibility = View.GONE
    }

    private fun handleBottomNavigationSelection(item: UserNavigationItem) {
        when (item) {
            UserNavigationItem.Home -> navigateFromBottomBar(R.id.mainDashboardFragment)
            UserNavigationItem.Inventory -> {
                if (permissionManager.hasPermission(PermissionType.VIEW_INVENTORY)) {
                    navigateFromBottomBar(R.id.inventoryFragment)
                } else {
                    showPermissionDeniedDialog("inventory")
                }
            }
            UserNavigationItem.Sales -> {
                if (permissionManager.hasPermission(PermissionType.VIEW_SALES)) {
                    navigateFromBottomBar(R.id.salesFragment)
                } else {
                    showPermissionDeniedDialog("sales")
                }
            }
            UserNavigationItem.More -> {
                selectedBottomNavigationItem.value = UserNavigationItem.More
                setupNavigationMenu()
                isMoreMenuVisible.value = true
            }
        }
    }

    private fun dismissMoreMenu() {
        isMoreMenuVisible.value = false
        syncBottomNavigation(navController.currentDestination?.id)
    }

    private fun handleMoreMenuSelection(item: MoreMenuItem) {
        isMoreMenuVisible.value = false
        handleMoreNavigationItem(item.menuItemId)
    }

    private fun refreshMoreMenuSections() {
        val hasShops = preferenceManager.hasShop()
        val isOwner = permissionManager.isShopOwner()

        fun menuItem(
            menuItemId: Int,
            label: String,
            iconRes: Int,
            visible: Boolean = true,
            isDestructive: Boolean = false,
        ): MoreMenuItem? {
            if (!visible) return null
            return MoreMenuItem(
                menuItemId = menuItemId,
                label = label,
                iconRes = iconRes,
                isDestructive = isDestructive,
            )
        }

        val subscriptionLabel = if (hasAccessToBusinessOperations()) "Subscription" else "UPGRADE NOW"

        moreMenuSections.value = listOf(
            MoreMenuSection(
                title = "Shop",
                items = listOfNotNull(
                    menuItem(
                        R.id.nav_shops,
                        "Shops",
                        R.drawable.ic_iconsax_shop,
                        visible = isOwner,
                    ),
                    menuItem(
                        R.id.nav_shop_home,
                        "Shop Home",
                        R.drawable.ic_iconsax_home,
                        visible = hasShops,
                    ),
                ),
            ),
            MoreMenuSection(
                title = "Business",
                items = listOfNotNull(
                    menuItem(
                        R.id.nav_finance,
                        "Finance",
                        R.drawable.ic_iconsax_finance,
                        visible = hasShops && permissionManager.hasPermission(PermissionType.VIEW_FINANCE),
                    ),
                    menuItem(
                        R.id.nav_transfers,
                        "Transfers",
                        R.drawable.ic_iconsax_transfer,
                        visible = hasShops,
                    ),
                ),
            ),
            MoreMenuSection(
                title = "People",
                items = listOfNotNull(
                    menuItem(
                        R.id.nav_employees,
                        "Employees",
                        R.drawable.ic_iconsax_employees,
                        visible = hasShops && permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES),
                    ),
                    menuItem(
                        R.id.nav_suppliers,
                        "Suppliers",
                        R.drawable.ic_iconsax_suppliers,
                        visible = hasShops && permissionManager.hasPermission(PermissionType.VIEW_INVENTORY),
                    ),
                    menuItem(
                        R.id.nav_customers,
                        "Customers",
                        R.drawable.ic_iconsax_customers,
                        visible = hasShops && permissionManager.hasPermission(PermissionType.VIEW_CUSTOMERS),
                    ),
                ),
            ),
            MoreMenuSection(
                title = "Account & Support",
                items = listOfNotNull(
                    menuItem(
                        R.id.nav_subscription,
                        subscriptionLabel,
                        R.drawable.ic_iconsax_subscription,
                        visible = hasShops && isOwner,
                    ),
                    menuItem(
                        R.id.nav_user_roles,
                        "User Roles",
                        R.drawable.ic_iconsax_roles,
                        visible = hasShops && isOwner,
                    ),
                    menuItem(
                        R.id.nav_account,
                        "Account",
                        R.drawable.ic_iconsax_account,
                        visible = hasShops &&
                            (isOwner || permissionManager.hasPermission(PermissionType.VIEW_EMPLOYEES)),
                    ),
                    menuItem(R.id.nav_contact_us, "Contact Us", R.drawable.ic_iconsax_contact),
                    menuItem(
                        R.id.nav_logout,
                        "Logout",
                        R.drawable.ic_iconsax_logout,
                        isDestructive = true,
                    ),
                ),
            ),
        ).filter { it.items.isNotEmpty() }
    }

    private fun navigateFromBottomBar(destinationId: Int) {
        if (navController.currentDestination?.id != destinationId) {
            navController.navigate(destinationId)
        }
    }

    private fun syncBottomNavigation(destinationId: Int?) {
        selectedBottomNavigationItem.value = when {
            destinationId == null || destinationId == R.id.mainDashboardFragment -> {
                UserNavigationItem.Home
            }
            destinationId in inventoryBottomNavigationDestinations -> {
                UserNavigationItem.Inventory
            }
            destinationId in salesBottomNavigationDestinations -> {
                UserNavigationItem.Sales
            }
            else -> UserNavigationItem.More
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupNavControllerListener() {
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            Log.d("NavDrawer_DEBUG", "📍 Destination changed to: ${destination.label} (ID: ${destination.id})")
            syncBottomNavigation(destination.id)

            if (destination.id == R.id.mainDashboardFragment) {
                showFab()
            } else {
                hideFab()
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

    // ===== PROFILE AND SHOP UPDATE METHODS =====
    fun updateHeaderUserInfo(userName: String, userEmail: String) {
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
        if (preferenceManager.getCurrentShopId().isNotEmpty()) {
            preferenceManager.saveHasShop(true)
        }
        checkSubscriptionStatus()
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

    // ===== MORE MENU =====
    private fun setupNavigationMenu() {
        refreshMoreMenuSections()
    }

    fun refreshNavigationMenu() {
        setupNavigationMenu()
    }

    private fun handleMoreNavigationItem(itemId: Int) {
        when (itemId) {
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
                else showPermissionDeniedDialog("user roles management")
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
    }

    // ===== FAB METHODS =====
    private fun showFab() {
        binding.mainFab.visibility = View.GONE
        binding.fabSale.visibility = View.GONE
        binding.fabProduct.visibility = View.GONE
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
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        when {
            isFabMenuOpen -> {
                toggleFabMenu()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}
