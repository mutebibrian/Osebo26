package com.devbrian.osebo

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// MainActivity is in the same package (com.devbrian.osebo), so no import is needed.
// If you prefer keeping an import, use: import com.devbrian.osebo.MainActivity

// Correct the databinding import to match your namespace:
import com.devbrian.osebo.databinding.ActivityShopCreationBinding



class ShopCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShopCreationBinding
    private lateinit var preferencesManager: PreferencesManager

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

        preferencesManager = PreferencesManager(this)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Setup business type dropdown
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, businessTypes)
        binding.actvBusinessType.setAdapter(adapter)

        // Set default business type
        binding.actvBusinessType.setText(businessTypes[0], false)
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

        // Shop name validation
        if (shopName.isEmpty()) {
            binding.shopNameInputLayout.error = "Shop name is required"
            isValid = false
        } else if (shopName.length < 3) {
            binding.shopNameInputLayout.error = "Shop name is too short"
            isValid = false
        } else {
            binding.shopNameInputLayout.error = null
        }

        // Business type validation
        if (businessType.isEmpty()) {
            binding.businessTypeInputLayout.error = "Business type is required"
            isValid = false
        } else {
            binding.businessTypeInputLayout.error = null
        }

        // Location validation
        if (location.isEmpty()) {
            binding.locationInputLayout.error = "Location is required"
            isValid = false
        } else {
            binding.locationInputLayout.error = null
        }

        // Contact validation (optional but recommended)
        if (contact.isNotEmpty() && contact.length < 10) {
            binding.contactInputLayout.error = "Enter a valid phone number"
            isValid = false
        } else {
            binding.contactInputLayout.error = null
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
            val shopCreated = true // Change based on API response

            if (shopCreated) {
                // Save shop info to preferences
                preferencesManager.saveShopInfo(
                    shopName = shopName,
                    businessType = businessType,
                    location = location,
                    contact = contact,
                    shopId = "shop_${System.currentTimeMillis()}"
                )

                // Set hasShop flag
                preferencesManager.setHasShop(true)

                showSuccessMessage()
                navigateToMainActivity()
            } else {
                showErrorMessage()
            }

            // Reset button
            binding.btnCreateShop.isEnabled = true
            binding.btnCreateShop.text = "Create Shop"
        }, 2000)
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