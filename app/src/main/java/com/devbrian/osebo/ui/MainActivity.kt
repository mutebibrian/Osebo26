package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        setupToolbar()
        setupNavigation()
        setupHeaderView()
        setupNavigationMenu()
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

        // Define top-level destinations
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

        // Set custom listener
        binding.navigationView.setNavigationItemSelectedListener(this)

        // Set navigation icon click listener
        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupHeaderView() {
        val headerView = binding.navigationView.getHeaderView(0)

        // Get views from header
        val tvUserName = headerView.findViewById<TextView>(R.id.tv_user_name)
        val tvUserEmail = headerView.findViewById<TextView>(R.id.tv_user_email)
        val tvUserInitial = headerView.findViewById<TextView>(R.id.tv_user_initial)
        val tvCurrentShop = headerView.findViewById<TextView>(R.id.tv_current_shop)
        val tvAppVersion = headerView.findViewById<TextView>(R.id.tv_app_version)
        val ivSettings = headerView.findViewById<ImageView>(R.id.iv_settings)
        val llUserAvatar = headerView.findViewById<View>(R.id.ll_user_avatar)

        // Set user data
        val userName = preferencesManager.getUserName() ?: "Admin User"
        val userEmail = preferencesManager.getUserEmail() ?: "admin@example.com"
        val shopName = preferencesManager.getShopName() ?: "No Shop Selected"

        tvUserName.text = userName
        tvUserEmail.text = userEmail
        tvCurrentShop.text = shopName
        tvAppVersion.text = "v1.2.0 (Build 305)"

        // Set user initial
        val initial = userName.firstOrNull()?.toString() ?: "A"
        tvUserInitial.text = initial.uppercase()

        // Set avatar background color based on name
        llUserAvatar.setBackgroundColor(getAvatarColor(userName))

        // Set click listeners
        llUserAvatar.setOnClickListener {
            navigateToProfile()
        }

        ivSettings.setOnClickListener {
            navigateToSettings()
        }
    }

    private fun setupNavigationMenu() {
        // Customize menu items based on user role/permissions
        val menu = binding.navigationView.menu

        // Check if user has shops
        val hasShops = preferencesManager.hasShop()

        // Enable/disable shop-related items
        menu.findItem(R.id.nav_shop_home).isVisible = hasShops
        menu.findItem(R.id.nav_sales).isVisible = hasShops
        menu.findItem(R.id.nav_finance).isVisible = hasShops
        menu.findItem(R.id.nav_inventory).isVisible = hasShops
        menu.findItem(R.id.nav_transfers).isVisible = hasShops
        menu.findItem(R.id.nav_employees).isVisible = hasShops
        menu.findItem(R.id.nav_customers).isVisible = hasShops

        // If no shop, show message
        if (!hasShops) {
            // You could add a message item here
        }
    }

    private fun getAvatarColor(name: String): Int {
        val colors = listOf(
            android.graphics.Color.parseColor("#FF6B6B"), // Coral Red
            android.graphics.Color.parseColor("#4ECDC4"), // Tiffany Blue
            android.graphics.Color.parseColor("#FFD166"), // Sunglow
            android.graphics.Color.parseColor("#06D6A0"), // Emerald
            android.graphics.Color.parseColor("#118AB2"), // Blue NCS
            android.graphics.Color.parseColor("#EF476F"), // Paradise Pink
            android.graphics.Color.parseColor("#073B4C")  // Midnight Green
        )

        val index = name.hashCode() % colors.size
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
                // Navigate to shop home/dashboard
                navController.navigate(R.id.dashboardFragment)
            }
            R.id.nav_sales -> {
                navController.navigate(R.id.salesFragment)
            }
            R.id.nav_finance -> {
                navController.navigate(R.id.financeFragment)
            }
            R.id.nav_inventory -> {
                navController.navigate(R.id.inventoryFragment)
            }
            R.id.nav_transfers -> {
                // Navigate to transfers fragment (create this if needed)
                Toast.makeText(this, "Transfers Coming Soon", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_employees -> {
                navController.navigate(R.id.employeesFragment)
            }
            R.id.nav_customers -> {
                navController.navigate(R.id.customersFragment)
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

        // Close drawer
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun navigateToProfile() {
        Toast.makeText(this, "Navigate to Profile", Toast.LENGTH_SHORT).show()
        // Navigate to profile activity/fragment
    }

    private fun navigateToSettings() {
        Toast.makeText(this, "Navigate to Settings", Toast.LENGTH_SHORT).show()
        // Navigate to settings activity/fragment
    }

    private fun navigateToAccount() {
        Toast.makeText(this, "Navigate to Account", Toast.LENGTH_SHORT).show()
        // Navigate to account activity/fragment
    }

    private fun navigateToContactUs() {
        Toast.makeText(this, "Navigate to Contact Us", Toast.LENGTH_SHORT).show()
        // Navigate to contact us activity/fragment
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
        // Clear user data
        preferencesManager.clearUserData()

        // Navigate to login activity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    // Update header when shop changes
    fun updateHeaderShopInfo(shopName: String) {
        val headerView = binding.navigationView.getHeaderView(0)
        val tvCurrentShop = headerView.findViewById<TextView>(R.id.tv_current_shop)
        tvCurrentShop.text = shopName
    }

    // Update header when user info changes
    fun updateHeaderUserInfo(userName: String, userEmail: String) {
        val headerView = binding.navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tv_user_name)
        val tvUserEmail = headerView.findViewById<TextView>(R.id.tv_user_email)
        val tvUserInitial = headerView.findViewById<TextView>(R.id.tv_user_initial)
        val llUserAvatar = headerView.findViewById<View>(R.id.ll_user_avatar)

        tvUserName.text = userName
        tvUserEmail.text = userEmail

        val initial = userName.firstOrNull()?.toString() ?: "A"
        tvUserInitial.text = initial.uppercase()
        llUserAvatar.setBackgroundColor(getAvatarColor(userName))
    }
}