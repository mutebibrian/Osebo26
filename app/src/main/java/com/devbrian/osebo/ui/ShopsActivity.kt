package com.devbrian.osebo.ui


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.databinding.ActivityShopsBinding
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ShopsActivity : AppCompatActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private lateinit var binding: ActivityShopsBinding
    private lateinit var shopViewModel: ShopViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        shopViewModel = ViewModelProvider(this)[ShopViewModel::class.java]

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
                        // No shops, navigate to shop creation
                        navigateToShopCreation()
                    } else {
                        // Try to find and select a shop with active subscription
                        selectBestShop(shops)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, "Error loading shops: ${resource.message}", Toast.LENGTH_SHORT).show()
                    // If error, still try to navigate to shops fragment
                    navigateToMainActivity()
                }
            }
        }
    }

    private fun selectBestShop(shops: List<Shop>) {
        // First, try to find a shop with active subscription
        val shopWithSubscription = shops.find { shop ->
            shop.subscription?.isActive == true ||
                    shop.subscriptionStatus.equals("active", ignoreCase = true) ||
                    shop.subscriptionStatus.equals("trial", ignoreCase = true)
        }

        if (shopWithSubscription != null) {
            // Found a shop with active subscription, select it automatically
            selectShopAndNavigate(shopWithSubscription)
        } else {
            // No shop with active subscription, check if any shops exist
            if (shops.isNotEmpty()) {
                // Navigate to ShopsFragment to let user choose
                navigateToShopsFragment()
            } else {
                // No shops at all, navigate to shop creation
                navigateToShopCreation()
            }
        }
    }

    private fun selectShopAndNavigate(shop: Shop) {
        // Save the selected shop
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
                packageId = subscription.subscriptionPackage?.id
            )
        }

        Toast.makeText(
            this,
            "Automatically selected ${shop.name} as your active shop",
            Toast.LENGTH_LONG
        ).show()

        // Navigate to MainActivity
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