package com.devbrian.osebo.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var preferenceManager: PreferenceManager

    private var isFabMenuOpen = false






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

        // Check subscription status on create
        checkSubscriptionStatus()
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
            createNewSale()
            toggleFabMenu()
        }

        binding.fabProduct.setOnClickListener {
            addNewProduct()
            toggleFabMenu()
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
        if (!hasActiveShopAndSubscription()) {
            return
        }

        Toast.makeText(this, "Add New Product", Toast.LENGTH_SHORT).show()
        navController.navigate(R.id.inventoryFragment)
    }

    private fun createNewSale() {
        if (!hasActiveShopAndSubscription()) {
            return
        }

        Toast.makeText(this, "Create New Sale", Toast.LENGTH_SHORT).show()
        navController.navigate(R.id.salesFragment)
    }

    private fun hasActiveShopAndSubscription(): Boolean {
        // Check if user has a shop
        if (!preferenceManager.hasShop()) {
            Toast.makeText(this, "Please select a shop first", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.shopsFragment)
            return false
        }

        // Check if shop has active subscription
        if (!preferenceManager.hasActiveSubscription()) {
            Toast.makeText(this, "Active subscription required. Please subscribe to a plan.", Toast.LENGTH_LONG).show()
            navController.navigate(R.id.subscriptionPackagesFragment)
            return false
        }

        return true
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

        val userName = preferenceManager.getUserName()
        val userEmail = preferenceManager.getUserEmail()
        val shopName = preferenceManager.getCurrentShopName()

        tvUserName.text = userName.ifEmpty { "Admin User" }
        tvUserEmail.text = userEmail.ifEmpty { "admin@example.com" }
        tvCurrentShop.text = shopName.ifEmpty { "No Shop Selected" }

        // Show subscription status in header
        val subscriptionStatus = preferenceManager.getSubscriptionStatus()
        if (subscriptionStatus.isNotEmpty()) {
            tvAppVersion.text = "v1.2.0 • $subscriptionStatus"
        } else {
            tvAppVersion.text = "v1.2.0 (Build 305)"
        }

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

    private fun setupNavigationMenu() {
        val menu = binding.navigationView.menu

        val hasShops = preferenceManager.hasShop()
        val hasActiveSubscription = preferenceManager.hasActiveSubscription()
        val subscriptionStatus = preferenceManager.getSubscriptionStatus()

        println("🔍 ===== NAVIGATION MENU SETUP =====")
        println("🔍 hasShops: $hasShops")
        println("🔍 hasActiveSubscription: $hasActiveSubscription")
        println("🔍 subscriptionStatus: $subscriptionStatus")
        println("🔍 currentShopId: ${preferenceManager.getCurrentShopId()}")
        println("🔍 currentShopName: ${preferenceManager.getCurrentShopName()}")

        // Shop home is visible if user has a shop
        menu.findItem(R.id.nav_shop_home).isVisible = hasShops

        // Business operations are visible only if shop has active subscription
        val showBusinessItems = hasShops && hasActiveSubscription
        println("🔍 showBusinessItems: $showBusinessItems")

        menu.findItem(R.id.nav_sales).isVisible = showBusinessItems
        menu.findItem(R.id.nav_finance).isVisible = showBusinessItems
        menu.findItem(R.id.nav_inventory).isVisible = showBusinessItems
        menu.findItem(R.id.nav_transfers).isVisible = showBusinessItems
        menu.findItem(R.id.nav_employees).isVisible = showBusinessItems
        menu.findItem(R.id.nav_customers).isVisible = showBusinessItems
    }
    fun refreshNavigationMenu() {
        setupNavigationMenu()
        binding.navigationView.invalidate()
    }

    private fun checkSubscriptionStatus() {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isNotEmpty()) {
            // Refresh navigation menu based on subscription status
            setupNavigationMenu()
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
            R.id.nav_shop_home -> {
                navController.navigate(R.id.dashboardFragment)
            }
            R.id.nav_sales -> {
                if (hasActiveShopAndSubscription()) {
                    navController.navigate(R.id.salesFragment)
                }
            }
            R.id.nav_finance -> {
                if (hasActiveShopAndSubscription()) {
                    navController.navigate(R.id.financeFragment)
                }
            }
            R.id.nav_inventory -> {
                if (hasActiveShopAndSubscription()) {
                    navController.navigate(R.id.inventoryFragment)
                }
            }
            R.id.nav_transfers -> {
                if (hasActiveShopAndSubscription()) {
                    Toast.makeText(this, "Transfers Coming Soon", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.nav_employees -> {
                if (hasActiveShopAndSubscription()) {
                    navController.navigate(R.id.employeesFragment)
                }
            }
            R.id.nav_customers -> {
                if (hasActiveShopAndSubscription()) {
                    navController.navigate(R.id.customersFragment)
                }
            }
            R.id.nav_account -> {
                navigateToAccount()
            }
            R.id.nav_contact_us -> {
                navigateToContactUs()
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
        Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSettings() {
        Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAccount() {
        Toast.makeText(this, "Account", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToContactUs() {
        Toast.makeText(this, "Contact Us", Toast.LENGTH_SHORT).show()
    }

    private fun logout() {
        androidx.appcompat.app.AlertDialog.Builder(this)
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

        // Re-check subscription when shop changes
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

        preferenceManager.saveUserData(
            userId = preferenceManager.getUserId(),
            email = userEmail,
            name = userName
        )
    }

















}