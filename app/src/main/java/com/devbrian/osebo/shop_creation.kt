package com.devbrian.osebo

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.databinding.ActivityShopCreationBinding
import com.devbrian.osebo.ui.MainActivity
import dagger.hilt.android.HiltAndroidApp

class ShopCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShopCreationBinding
    private lateinit var preferenceManager: PreferenceManager // Changed name

    private val businessTypes = arrayOf(
        "Retail",
        "Wholesale",
        "E-commerce",
        "Restaurant",
        "Service",
        "Manufacturing",
        "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager.getInstance(this) // Changed initialization

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Setup business type dropdown
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, businessTypes)
        binding.actvBusinessType.setAdapter(adapter)

        // Set default business type
        if (binding.actvBusinessType.text.isEmpty()) {
            binding.actvBusinessType.setText(businessTypes[0], false)
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnCreateShop.setOnClickListener {
            createShop()
        }

        binding.tvSkip.setOnClickListener {
            navigateToMainActivity()
        }
    }

    private fun createShop() {
        val shopName = binding.etShopName.text.toString().trim()
        val businessType = binding.actvBusinessType.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val contact = binding.etContact.text.toString().trim()

        if (validateInputs(shopName, businessType, location, contact)) {
            // Show loading
            binding.btnCreateShop.isEnabled = false
            binding.btnCreateShop.text = "Creating..."

            // Simulate API call to create shop
            simulateShopCreation(shopName, businessType, location, contact)
        }
    }

    private fun validateInputs(
        shopName: String,
        businessType: String,
        location: String,
        contact: String
    ): Boolean {
        var isValid = true

        // Reset errors
        binding.shopNameInputLayout.error = null
        binding.businessTypeInputLayout.error = null
        binding.locationInputLayout.error = null
        binding.contactInputLayout.error = null

        // Shop name validation
        if (shopName.isEmpty()) {
            binding.shopNameInputLayout.error = "Shop name is required"
            isValid = false
        } else if (shopName.length < 2) {
            binding.shopNameInputLayout.error = "Shop name is too short"
            isValid = false
        }

        // Business type validation
        if (businessType.isEmpty()) {
            binding.businessTypeInputLayout.error = "Business type is required"
            isValid = false
        }

        // Location validation
        if (location.isEmpty()) {
            binding.locationInputLayout.error = "Location is required"
            isValid = false
        }

        // Contact validation (optional but recommended)
        if (contact.isNotEmpty() && (contact.length < 10 || !contact.matches(Regex("^[0-9+\\-().\\s]*\$")))) {
            binding.contactInputLayout.error = "Enter a valid phone number"
            isValid = false
        }

        return isValid
    }

    private fun simulateShopCreation(
        shopName: String,
        businessType: String,
        location: String,
        contact: String
    ) {
        // TODO: Replace with actual API call
        binding.root.postDelayed({
            // Mock successful shop creation
            val shopCreated = true

            if (shopCreated) {
                // Generate shop ID
                val shopId = "shop_${System.currentTimeMillis()}"

                // Save shop data using correct method names
                preferenceManager.saveCurrentShopId(shopId)
                preferenceManager.saveCurrentShopName(shopName)

                // Save additional shop details - need to add these methods to PreferenceManager
                // For now, we'll use SharedPreferences directly
                val sharedPref = getSharedPreferences("OseboPrefs", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("business_type", businessType)
                    putString("shop_location", location)
                    putString("shop_contact", contact)
                    putBoolean("has_shop", true)
                    apply()
                }

                showSuccessMessage()
                navigateToMainActivity()
            } else {
                showErrorMessage()
            }

            // Reset button
            binding.btnCreateShop.isEnabled = true
            binding.btnCreateShop.text = "Create Shop"
        }, 1500)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showSuccessMessage() {
        Toast.makeText(
            this,
            "Shop created successfully!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showErrorMessage() {
        Toast.makeText(
            this,
            "Failed to create shop. Please try again.",
            Toast.LENGTH_LONG
        ).show()
    }
}

@HiltAndroidApp
class OseboApplication : Application()