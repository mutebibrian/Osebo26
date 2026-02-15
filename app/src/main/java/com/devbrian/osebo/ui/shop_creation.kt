package com.devbrian.osebo

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.devbrian.osebo.data.*
import com.devbrian.osebo.data.ApiClient
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.CreateShopRequest
import com.devbrian.osebo.databinding.ActivityShopCreationBinding
import com.devbrian.osebo.ui.MainActivity
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch

class ShopCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShopCreationBinding
    private lateinit var apiService: ApiService

    private val shopTypes = listOf(
        "Retail",
        "Wholesale",
        "E-commerce",
        "Restaurant",
        "Service",
        "Manufacturing",
        "Online",
        "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize API service
        apiService = ApiClient.createWithAuth(this)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Setup shop type dropdown
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, shopTypes)
        binding.actvShopType.setAdapter(adapter)

        // Set default shop type
        if (binding.actvShopType.text.isEmpty()) {
            binding.actvShopType.setText(shopTypes[0], false)
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
        val address = binding.etAddress.text.toString().trim()
        val shopType = binding.actvShopType.text.toString().trim()
        val registrationNumber = binding.etRegistrationNumber.text.toString().trim()
        val tin = binding.etTin.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (validateInputs(shopName, address, shopType)) {
            // Show loading
            binding.btnCreateShop.isEnabled = false
            binding.btnCreateShop.text = "Creating..."

            // Create shop request object
            val shopRequest = CreateShopRequest(
                name = shopName,
                address = address,
                shop_type = shopType,
                registration_number = if (registrationNumber.isNotEmpty()) registrationNumber else null,
                tax_identification_number = if (tin.isNotEmpty()) tin else null,
                description = if (description.isNotEmpty()) description else null
            )

            // Call API to create shop using coroutine
            createShopApiCall(shopRequest)
        }
    }

    private fun validateInputs(
        shopName: String,
        address: String,
        shopType: String
    ): Boolean {
        var isValid = true

        // Reset errors
        binding.shopNameInputLayout.error = null
        binding.addressInputLayout.error = null
        binding.shopTypeInputLayout.error = null

        // Shop name validation
        if (shopName.isEmpty()) {
            binding.shopNameInputLayout.error = "Shop name is required"
            isValid = false
        }

        // Address validation
        if (address.isEmpty()) {
            binding.addressInputLayout.error = "Address is required"
            isValid = false
        }

        // Shop type validation
        if (shopType.isEmpty()) {
            binding.shopTypeInputLayout.error = "Shop type is required"
            isValid = false
        }

        return isValid
    }

    private fun createShopApiCall(shopRequest: CreateShopRequest) {
        // Use lifecycleScope to launch coroutine
        lifecycleScope.launch {
            try {
                // Call the suspend function
                val response = apiService.createShop(shopRequest)

                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse?.success == true) {
                        apiResponse.data?.let { shop ->
                            // Save shop data
                            ApiClient.saveCurrentShopId(this@ShopCreationActivity, shop.id)
                            ApiClient.saveCurrentShopName(this@ShopCreationActivity, shop.name)

                            // Save additional shop details
                            val sharedPref = getSharedPreferences("OseboPrefs", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("shop_address", shop.address ?: "")
                                putString("shop_type", shop.shop_type ?: "")
                                putString("shop_registration", shop.registration_number ?: "")
                                putString("shop_tin", shop.tax_identification_number ?: "")
                                putString("shop_description", shop.description ?: "")
                                putBoolean("has_shop", true)
                                apply()
                            }

                            showSuccessMessage()
                            navigateToMainActivity()
                        } ?: run {
                            showErrorMessage("No shop data returned")
                        }
                    } else {
                        showErrorMessage(apiResponse?.message ?: "Failed to create shop")
                    }
                } else {
                    // Handle HTTP error
                    when (response.code()) {
                        400 -> showErrorMessage("Invalid shop data")
                        401 -> showErrorMessage("Unauthorized - Please login again")
                        409 -> showErrorMessage("Shop name already exists")
                        422 -> showErrorMessage("Validation error")
                        else -> showErrorMessage("Server error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                // Handle network or other exceptions
                showErrorMessage("Network error: ${e.message}")
            } finally {
                // Reset button state
                binding.btnCreateShop.isEnabled = true
                binding.btnCreateShop.text = "Create Shop"
            }
        }
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

    private fun showErrorMessage(message: String = "Failed to create shop. Please try again.") {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

@HiltAndroidApp
class OseboApplication : Application()