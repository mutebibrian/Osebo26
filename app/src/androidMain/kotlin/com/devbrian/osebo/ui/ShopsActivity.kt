package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.databinding.ActivityShopsBinding
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.utils.Resource
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ShopsActivity : AppCompatActivity() {

    private val preferenceManager: PreferenceManager by inject()
    private val shopViewModel: ShopViewModel by viewModel()

    private lateinit var binding: ActivityShopsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        loadShops()
    }

    private fun setupObservers() {
        shopViewModel.shops.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    val shops = resource.data ?: emptyList()

                    if (shops.isEmpty()) {
                        navigateToShopCreation()
                    } else {
                        selectBestShop(shops)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Error loading shops: ${resource.message}", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                }
            }
        }
    }

    private fun selectBestShop(shops: List<Shop>) {
        // Find a shop with an active subscription
        val shopWithSubscription = shops.find { shop ->
            shop.subscription?.isActive == true ||
                    shop.subscriptionStatus.equals("active", ignoreCase = true) ||
                    shop.subscriptionStatus.equals("trial", ignoreCase = true)
        }

        if (shopWithSubscription != null) {
            selectShopAndNavigate(shopWithSubscription)
        } else {
            if (shops.isNotEmpty()) {
                navigateToShopsFragment()
            } else {
                navigateToShopCreation()
            }
        }
    }

    private fun selectShopAndNavigate(shop: Shop) {
        // Save shop info
        preferenceManager.saveCurrentShopId(shop.id)
        preferenceManager.saveCurrentShopName(shop.name)
        preferenceManager.saveHasShop(true)

        // Save subscription info if available
        shop.subscription?.let { subscription ->
            preferenceManager.saveSubscriptionInfo(
                subscriptionId = subscription.id,
                status = if (subscription.isTrial) "TRIAL" else "ACTIVE",
                type = subscription.packageType,
                expiry = subscription.endsAt,
                packageId = shop.planId   // ✅ FIXED: use shop.planId instead of subscription.subscriptionPackage?.id
            )
        }

        Toast.makeText(
            this,
            "Automatically selected ${shop.name} as your active shop",
            Toast.LENGTH_LONG
        ).show()

        navigateToMainActivity()
    }

    private fun navigateToShopsFragment() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("OPEN_SHOPS_FRAGMENT", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToShopCreation() {
        val intent = Intent(this, ShopCreationActivity::class.java).apply {
            putExtra("USER_ID", preferenceManager.getUserId())
            putExtra("EMAIL", preferenceManager.getUserEmail())
            putExtra("PHONE", preferenceManager.getUserPhone())
            putExtra("FIRST_NAME", preferenceManager.getFirstName())
            putExtra("LAST_NAME", preferenceManager.getLastName())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun loadShops() {
        shopViewModel.loadShops()
    }
}