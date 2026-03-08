package com.devbrian.osebo.ui

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.databinding.ActivityMainBinding
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.models.ShopSubscription
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var preferenceManager: PreferenceManager
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 1001

    // Business operation destinations that require active subscription
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

    // Management destinations that require subscription
    private val managementRequiredDestinations = setOf(
        R.id.accountFragment,
        R.id.sessionsFragment,
        R.id.userRolesTitle,
        R.id.subscriptionOverviewFragment,
        R.id.subscriptionsFragment ,
        R.id.subscriptionDetailsFragment
    )

    private var isFabMenuOpen = false
    private var subscriptionCheckInProgress = false
    private var currentShop: Shop? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager.getInstance(this)

        setupToolbar()
        setupNavigation()
        setupHeaderView()
        setupNavigationMenu()
        setupFloatingActionButtons()
        setupNavControllerListener()

        loadCurrentShopData()
        checkSubscriptionStatus()
        checkForExpiringSubscription()

        // Check if we should open ShopsFragment
        if (intent.getBooleanExtra("OPEN_SHOPS_FRAGMENT", false)) {
            navController.navigate(R.id.shopsFragment)
        }
    }

    private fun setupNavControllerListener() {
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            // Show FAB only on DashboardFragment
            if (destination.id == R.id.dashboardFragment) {
                showFab()
            } else {
                hideFab()
            }

            // Check if destination requires subscription
            when {
                destination.id in subscriptionRequiredDestinations -> {
                    if (!hasAccessToBusinessOperations()) {
                        navController.popBackStack()
                        showSubscriptionRequiredDialog("business operations")
                    }
                }
                destination.id in managementRequiredDestinations -> {
                    if (!hasAccessToManagement()) {
                        navController.popBackStack()
                        showSubscriptionRequiredDialog("management features")
                    }
                }
            }

            // Handle special cases with arguments
            handleDestinationArguments(destination.id, arguments)
        }
    }

    private fun handleDestinationArguments(destinationId: Int, arguments: Bundle?) {
        when (destinationId) {
            R.id.subscriptionPackagesFragment -> {
                // This fragment is allowed even without subscription as it's for purchasing
                // No action needed
            }
        }
    }

    private fun checkShopAccess(shop: Shop) {
        if (!hasActiveSubscriptionForShop(shop)) {
            showSubscriptionRequiredDialog("access this shop's features")
        }
    }

    private fun hasActiveSubscriptionForShop(shop: Shop): Boolean {
        return when {
            shop.subscription?.isActive == true -> true
            shop.subscription?.isTrial == true && !isSubscriptionExpired(shop.subscription) -> true
            shop.subscriptionStatus.equals("active", ignoreCase = true) -> true
            shop.subscriptionStatus.equals("trial", ignoreCase = true) && !isShopSubscriptionExpired(shop) -> true
            else -> false
        }
    }

    private fun isSubscriptionExpired(subscription: ShopSubscription?): Boolean {
        if (subscription?.endsAt == null) return false
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiryDate = format.parse(subscription.endsAt)
            Date().after(expiryDate)
        } catch (e: Exception) {
            false
        }
    }

    private fun isShopSubscriptionExpired(shop: Shop): Boolean {
        val expiry = shop.subscriptionExpiry ?: return false
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiryDate = format.parse(expiry)
            Date().after(expiryDate)
        } catch (e: Exception) {
            false
        }
    }

    private fun showFab() {
        binding.mainFab.visibility = View.VISIBLE
        binding.mainFab.alpha = 0f
        binding.mainFab.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun hideFab() {
        if (isFabMenuOpen) {
            toggleFabMenu()
        }

        binding.mainFab.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                binding.mainFab.visibility = View.GONE
                binding.fabSale.visibility = View.GONE
                binding.fabProduct.visibility = View.GONE
            }
            .start()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment,
                R.id.shopsFragment,
                R.id.salesFragment,
                R.id.financeFragment,
                R.id.inventoryFragment,
                R.id.employeesFragment,
                R.id.customersFragment
            ),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navigationView.setupWithNavController(navController)
        binding.navigationView.setNavigationItemSelectedListener(this)

        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupFloatingActionButtons() {
        binding.mainFab.setOnClickListener {
            toggleFabMenu()
        }

        binding.fabSale.setOnClickListener {
            if (hasAccessToBusinessOperations()) {
                createNewSale()
                toggleFabMenu()
            } else {
                showSubscriptionRequiredDialog("create sales")
                toggleFabMenu()
            }
        }

        binding.fabProduct.setOnClickListener {
            if (hasAccessToBusinessOperations()) {
                addNewProduct()
                toggleFabMenu()
            } else {
                showSubscriptionRequiredDialog("add products")
                toggleFabMenu()
            }
        }
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
        if (!preferenceManager.hasShop()) {
            showSelectShopFirstDialog()
            return
        }
        navController.navigate(R.id.addProductFragment)
    }

    private fun createNewSale() {
        if (!preferenceManager.hasShop()) {
            showSelectShopFirstDialog()
            return
        }
        navController.navigate(R.id.newSaleFragment)
    }

    private fun showSelectShopFirstDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Shop Selected")
            .setMessage("Please select or create a shop first to continue.")
            .setPositiveButton("Go to Shops") { _, _ ->
                navController.navigate(R.id.shopsFragment)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSubscriptionRequiredDialog(feature: String = "business operations") {
        AlertDialog.Builder(this)
            .setTitle("Subscription Required")
            .setMessage("You need an active subscription to access $feature. Please subscribe to continue.")
            .setPositiveButton("Subscribe Now") { _, _ ->
                navigateToSubscriptionPackages()
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Learn More") { _, _ ->
                showSubscriptionInfoDialog()
            }
            .show()
    }

    private fun showSubscriptionInfoDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Subscriptions")
            .setMessage("With an active subscription, you can:\n\n" +
                    "✓ Process sales transactions\n" +
                    "✓ Track finances and expenses\n" +
                    "✓ Manage inventory and stock\n" +
                    "✓ Handle employee management\n" +
                    "✓ Generate reports and analytics\n" +
                    "✓ Access premium features\n\n" +
                    "Choose a plan that fits your business needs!")
            .setPositiveButton("View Plans") { _, _ ->
                navigateToSubscriptionPackages()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showSubscriptionExpiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Subscription Expired")
            .setMessage("Your subscription has expired. Please renew to continue accessing business features.")
            .setPositiveButton("Renew Now") { _, _ ->
                navigateToSubscriptionPackages()
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun showTrialExpiringDialog(daysLeft: Int) {
        AlertDialog.Builder(this)
            .setTitle("Trial Ending Soon")
            .setMessage("Your free trial will expire in $daysLeft days. Subscribe now to continue using all features.")
            .setPositiveButton("Subscribe Now") { _, _ ->
                navigateToSubscriptionPackages()
            }
            .setNegativeButton("Remind Later", null)
            .show()
    }

    private fun navigateToSubscriptionPackages() {
        if (!preferenceManager.hasShop()) {
            showSelectShopFirstDialog()
            return
        }

        val currentShop = getCurrentShopFromPrefs()
        if (currentShop != null) {
            val bundle = Bundle().apply {
                putSerializable("shop", currentShop)
                putString("currentPackage", preferenceManager.getSubscriptionType())
            }
            navController.navigate(R.id.subscriptionPackagesFragment, bundle)
        } else {
            navController.navigate(R.id.shopsFragment)
        }
    }

    private fun getCurrentShopFromPrefs(): Shop? {
        val shopId = preferenceManager.getCurrentShopId()
        val shopName = preferenceManager.getCurrentShopName()

        return if (shopId.isNotEmpty()) {
            Shop(
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
        } else {
            null
        }
    }

    private fun loadCurrentShopData() {
        currentShop = getCurrentShopFromPrefs()
    }

    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnectPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val hasScanPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

            if (!hasConnectPermission || !hasScanPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    ),
                    BLUETOOTH_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    /**
     * Main access control method for business operations
     * Returns true if user can access business features
     */
    private fun hasAccessToBusinessOperations(): Boolean {
        if (subscriptionCheckInProgress) return false

        subscriptionCheckInProgress = true

        try {
            println("🔍 ===== CHECKING BUSINESS OPERATIONS ACCESS =====")

            // Check if shop exists
            if (!preferenceManager.hasShop()) {
                println("❌ No shop selected")
                return false
            }

            // Check subscription from preferences
            val hasActiveSub = preferenceManager.hasActiveSubscription()
            val isInTrial = preferenceManager.isTrial()
            val status = preferenceManager.getSubscriptionStatus()
            val expiry = preferenceManager.getSubscriptionExpiry()

            println("🔍 Subscription from Prefs:")
            println("🔍   status: $status")
            println("🔍   hasActiveSub: $hasActiveSub")
            println("🔍   isTrial: $isInTrial")
            println("🔍   expiry: $expiry")

            // Check if trial is expired
            if (isInTrial && isTrialExpired(expiry)) {
                println("❌ Trial expired")
                preferenceManager.saveSubscriptionStatus("expired")
                return false
            }

            val hasAccess = hasActiveSub || (isInTrial && !isTrialExpired(expiry))
            println("🔍 Access granted: $hasAccess")

            return hasAccess
        } finally {
            subscriptionCheckInProgress = false
        }
    }

    /**
     * Access control for management features
     */
    private fun hasAccessToManagement(): Boolean {
        return hasAccessToBusinessOperations()
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

    private fun checkForExpiringSubscription() {
        if (!preferenceManager.hasShop()) return

        val expiryDateStr = preferenceManager.getSubscriptionExpiry()
        if (expiryDateStr.isEmpty()) return

        try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiryDate = format.parse(expiryDateStr)
            val today = Date()

            val diffInMillies = expiryDate.time - today.time
            val diffInDays = diffInMillies / (1000 * 60 * 60 * 24)

            when {
                diffInDays < 0 -> {
                    // Subscription expired
                    if (preferenceManager.hasActiveSubscription()) {
                        preferenceManager.saveSubscriptionStatus("expired")
                        showSubscriptionExpiredDialog()
                    }
                }
                diffInDays <= 7 && preferenceManager.isTrial() -> {
                    // Trial expiring soon
                    showTrialExpiringDialog(diffInDays.toInt())
                }
                diffInDays <= 3 && preferenceManager.hasActiveSubscription() -> {
                    // Active subscription expiring soon
                    showSubscriptionExpiringDialog(diffInDays.toInt())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showSubscriptionExpiringDialog(daysLeft: Int) {
        AlertDialog.Builder(this)
            .setTitle("Subscription Expiring Soon")
            .setMessage("Your subscription will expire in $daysLeft days. Renew now to avoid interruption.")
            .setPositiveButton("Renew Now") { _, _ ->
                navigateToSubscriptionPackages()
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun setupHeaderView() {
        val headerView = binding.navigationView.getHeaderView(0)

        val tvUserName = headerView.findViewById<TextView>(R.id.tv_user_name)
        val tvUserEmail = headerView.findViewById<TextView>(R.id.tv_user_email)
        val tvUserInitial = headerView.findViewById<TextView>(R.id.tv_user_initial)
        val tvCurrentShop = headerView.findViewById<TextView>(R.id.tv_current_shop)
        val tvAppVersion = headerView.findViewById<TextView>(R.id.tv_app_version)
        val ivSettings = headerView.findViewById<ImageView>(R.id.iv_settings)
        val llUserAvatar = headerView.findViewById<View>(R.id.ll_user_avatar)
        val tvSubscriptionBadge = headerView.findViewById<TextView>(R.id.tv_subscription_badge)

        val userName = preferenceManager.getUserName()
        val userEmail = preferenceManager.getUserEmail()
        val shopName = preferenceManager.getCurrentShopName()

        tvUserName.text = userName.ifEmpty { "Admin User" }
        tvUserEmail.text = userEmail.ifEmpty { "admin@powerlipay.com" }
        tvCurrentShop.text = shopName.ifEmpty { "No Shop Selected" }

        // Update subscription badge
        updateSubscriptionBadge(tvSubscriptionBadge)

        val initial = if (userName.isNotEmpty()) userName.first().toString() else "A"
        tvUserInitial.text = initial.uppercase()
        llUserAvatar.setBackgroundColor(getAvatarColor(userName))

        llUserAvatar.setOnClickListener {
            navigateToProfile()
        }

        ivSettings.setOnClickListener {
            navigateToSettings()
        }
    }

    private fun updateSubscriptionBadge(tvSubscriptionBadge: TextView) {
        val hasAccess = hasAccessToBusinessOperations()
        val isTrial = preferenceManager.isTrial()
        val expiry = preferenceManager.getSubscriptionExpiry()

        val statusText = when {
            !preferenceManager.hasShop() -> "NO SHOP"
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
            hasAccess && isTrial -> R.drawable.bg_subscription_badge_trial
            hasAccess && !isTrial -> R.drawable.bg_subscription_badge_active
            else -> R.drawable.bg_subscription_badge_expired
        }

        tvSubscriptionBadge.text = statusText
        tvSubscriptionBadge.setBackgroundResource(backgroundDrawable)
        tvSubscriptionBadge.visibility = View.VISIBLE
    }

    private fun calculateDaysUntilExpiry(expiryDate: String?): Int {
        if (expiryDate.isNullOrEmpty()) return 0

        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiry = format.parse(expiryDate)
            val today = Date()
            val diffInMillies = expiry.time - today.time
            (diffInMillies / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }

    private fun setupNavigationMenu() {
        val menu = binding.navigationView.menu
        val hasShops = preferenceManager.hasShop()
        val hasSubscription = hasAccessToBusinessOperations()

        println("🔍 Navigation Menu Setup:")
        println("🔍   hasShops: $hasShops")
        println("🔍   hasSubscription: $hasSubscription")

        // Always show these items
        menu.findItem(R.id.nav_dashboard).isVisible = true
        menu.findItem(R.id.nav_shops).isVisible = true

        // Show/hide business operation items based on subscription
        val businessItems = mapOf(
            R.id.nav_sales to "Sales",
            R.id.nav_finance to "Finance",
            R.id.nav_inventory to "Inventory",
            R.id.nav_employees to "Employees",
            R.id.nav_customers to "Customers"
        )

        businessItems.forEach { (itemId, name) ->
            val item = menu.findItem(itemId)
            item.isVisible = hasShops && hasSubscription
            println("🔍   $name visible: ${item.isVisible}")
        }

        // Handle subscription menu item
        val subscriptionItem = menu.findItem(R.id.nav_subscription)
        subscriptionItem.isVisible = hasShops

        if (hasShops && !hasSubscription) {
            subscriptionItem.title = "UPGRADE NOW"
            subscriptionItem.icon = ContextCompat.getDrawable(this, R.drawable.ic_upgrade)
        } else if (hasShops && hasSubscription) {
            subscriptionItem.title = "Subscription"
            subscriptionItem.icon = ContextCompat.getDrawable(this, R.drawable.ic_subscription)
        }

        updateHeaderWithSubscriptionStatus()
    }

    private fun updateHeaderWithSubscriptionStatus() {
        val headerView = binding.navigationView.getHeaderView(0)
        val tvAppVersion = headerView.findViewById<TextView>(R.id.tv_app_version)

        val hasAccess = hasAccessToBusinessOperations()
        val status = when {
            !preferenceManager.hasShop() -> "NO SHOP SELECTED"
            preferenceManager.isTrial() -> {
                val expiry = preferenceManager.getSubscriptionExpiry()
                "TRIAL (expires: $expiry)"
            }
            hasAccess -> "ACTIVE"
            else -> "NO SUBSCRIPTION"
        }

        tvAppVersion.text = "v1.2.0 • $status"

        // Update text color based on status
        val textColor = when {
            !preferenceManager.hasShop() -> R.color.gray
            preferenceManager.isTrial() -> R.color.warning_orange
            hasAccess -> R.color.success_green
            else -> R.color.error_red
        }
        tvAppVersion.setTextColor(ContextCompat.getColor(this, textColor))
    }

    fun refreshNavigationMenu() {
        setupNavigationMenu()
        binding.navigationView.invalidate()
    }

    private fun checkSubscriptionStatus() {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isNotEmpty()) {
            setupNavigationMenu()
            checkForExpiringSubscription()

            // Debug output
            preferenceManager.debugSubscriptionInfo()
            println("🔍 hasAccessToBusinessOperations: ${hasAccessToBusinessOperations()}")
        }
    }

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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                navController.navigate(R.id.dashboardFragment)
            }
            R.id.nav_shops -> {
                navController.navigate(R.id.shopsFragment)
            }
            R.id.nav_sales -> {
                if (hasAccessToBusinessOperations()) {
                    navController.navigate(R.id.salesFragment)
                } else {
                    showSubscriptionRequiredDialog("sales")
                }
            }
            R.id.nav_finance -> {
                if (hasAccessToBusinessOperations()) {
                    navController.navigate(R.id.financeFragment)
                } else {
                    showSubscriptionRequiredDialog("finance")
                }
            }
            R.id.nav_inventory -> {
                if (hasAccessToBusinessOperations()) {
                    navController.navigate(R.id.inventoryFragment)
                } else {
                    showSubscriptionRequiredDialog("inventory")
                }
            }
            R.id.nav_employees -> {
                if (hasAccessToBusinessOperations()) {
                    navController.navigate(R.id.employeesFragment)
                } else {
                    showSubscriptionRequiredDialog("employee management")
                }
            }
            R.id.nav_customers -> {
                if (hasAccessToBusinessOperations()) {
                    navController.navigate(R.id.customersFragment)
                } else {
                    showSubscriptionRequiredDialog("customer management")
                }
            }
            R.id.nav_subscription -> {
                if (hasAccessToBusinessOperations()) {
                    navigateToSubscriptionDetails()
                } else {
                    navigateToSubscriptionPackages()
                }
            }
            R.id.nav_account -> {
                if (hasAccessToManagement()) {
                    navController.navigate(R.id.accountFragment)
                } else {
                    showSubscriptionRequiredDialog("account settings")
                }
            }
            R.id.nav_contact_us -> {
                navController.navigate(R.id.contactUsFragment)
            }
            R.id.nav_logout -> {
                logout()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else if (isFabMenuOpen) {
            toggleFabMenu()
        } else {
            super.onBackPressed()
        }
    }

    private fun navigateToProfile() {
        // Navigate to profile fragment if needed
        Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
    }
    private fun navigateToSubscriptionDetails() {
        if (!preferenceManager.hasShop()) {
            showSelectShopFirstDialog()
            return
        }

        val shopId = preferenceManager.getCurrentShopId()
        val subscriptionId = preferenceManager.getSubscriptionId()

        if (subscriptionId.isNotEmpty()) {
            val bundle = Bundle().apply {
                putString("shopId", shopId)
                putString("subscriptionId", subscriptionId)
            }
            navController.navigate(R.id.subscriptionDetailsFragment, bundle)
        } else {
            Toast.makeText(this, "Loading subscription details...", Toast.LENGTH_SHORT).show()

            val bundle = Bundle().apply {
                putString("shopId", shopId)
            }
            navController.navigate(R.id.subscriptionOverviewFragment, bundle)
        }
    }

    private fun navigateToSettings() {
        // Navigate to settings fragment if needed
        Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
    }

    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        preferenceManager.clearAll()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    fun updateHeaderShopInfo(shopName: String) {
        val headerView = binding.navigationView.getHeaderView(0)
        val tvCurrentShop = headerView.findViewById<TextView>(R.id.tv_current_shop)
        tvCurrentShop.text = shopName
        preferenceManager.saveCurrentShopName(shopName)

        if (preferenceManager.getCurrentShopId().isNotEmpty()) {
            preferenceManager.saveHasShop(true)
        }

        checkSubscriptionStatus()
        refreshNavigationMenu()
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

        preferenceManager.saveUserData(
            userId = preferenceManager.getUserId(),
            email = userEmail,
            name = userName
        )
    }

    /**
     * Call this method after successful subscription purchase
     */
    fun onSubscriptionUpdated() {
        refreshNavigationMenu()
        checkSubscriptionStatus()
        Toast.makeText(this, "Subscription updated successfully!", Toast.LENGTH_SHORT).show()
    }
}