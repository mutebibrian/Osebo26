package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.devbrian.osebo.R
import com.devbrian.osebo.data.ApiClient
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.remote.dto.request.CreateShopRequest
import com.devbrian.osebo.data.ShopType
import com.devbrian.osebo.databinding.ActivityShopCreationBinding
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty
import kotlin.collections.map

class ShopCreationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShopCreationBinding
    private lateinit var apiService: ApiService
    private var shopTypesList: List<ShopType> = emptyList()

    
    private val shopTypeIds = mapOf(
        "Retail" to "3e85af38-a3c9-4167-a3f6-6acf4c9fdc9d",
        "Other" to "9423230e-df5a-4669-8fd8-193e11ce72b0"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        
        apiService = ApiClient.createWithAuth(this)

        setupUI()
        setupListeners()
        loadShopTypes() 
    }

    private fun setupUI() {
        
        val shopTypeNames = listOf("Retail", "Other")  
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, shopTypeNames)
        binding.actvShopType.setAdapter(adapter)

        
        
        if (binding.actvShopType.text.isEmpty()) {
            binding.actvShopType.setText("Retail", false)
        }
    }

    private fun loadShopTypes() {
        
        
    }

    private fun updateShopTypeDropdownFromApi() {
        if (shopTypesList.isNotEmpty()) {
            val shopTypeNames = shopTypesList.map { it.name }
            val adapter = ArrayAdapter(this, R.layout.dropdown_item, shopTypeNames)
            binding.actvShopType.setAdapter(adapter)

            if (binding.actvShopType.text.isEmpty() && shopTypeNames.isNotEmpty()) {
                binding.actvShopType.setText(shopTypeNames[0], false)
            }
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
        val selectedShopTypeName = binding.actvShopType.text.toString().trim()
        val registrationNumber = binding.etRegistrationNumber.text.toString().trim()
        val tin = binding.etTin.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (validateInputs(shopName, address, selectedShopTypeName)) {
            
            val shopTypeId = shopTypeIds[selectedShopTypeName] ?: ""

            if (shopTypeId.isEmpty()) {
                showErrorMessage("Invalid shop type selected")
                return
            }

            
            binding.btnCreateShop.isEnabled = false
            binding.btnCreateShop.text = "Creating..."

            
            val shopRequest = CreateShopRequest(
                name = shopName,
                address = address,
                shopTypeId = shopTypeId, 
                registrationNumber = if (registrationNumber.isNotEmpty()) registrationNumber else null,
                taxIdentificationNumber = if (tin.isNotEmpty()) tin else null,
                description = if (description.isNotEmpty()) description else null
            )

            
            createShopApiCall(shopRequest)
        }
    }

    private fun validateInputs(
        shopName: String,
        address: String,
        shopType: String
    ): Boolean {
        var isValid = true

        
        binding.shopNameInputLayout.error = null
        binding.addressInputLayout.error = null
        binding.shopTypeInputLayout.error = null

        
        if (shopName.isEmpty()) {
            binding.shopNameInputLayout.error = "Shop name is required"
            isValid = false
        } else if (shopName.length < 2) {
            binding.shopNameInputLayout.error = "Shop name must be at least 2 characters"
            isValid = false
        }

        
        if (address.isEmpty()) {
            binding.addressInputLayout.error = "Address is required"
            isValid = false
        }

        
        if (shopType.isEmpty()) {
            binding.shopTypeInputLayout.error = "Shop type is required"
            isValid = false
        } else if (!shopTypeIds.containsKey(shopType)) {
            binding.shopTypeInputLayout.error = "Invalid shop type"
            isValid = false
        }

        return isValid
    }

    private fun createShopApiCall(shopRequest: CreateShopRequest) {
        lifecycleScope.launch {
            try {
                val response = apiService.createShop(shopRequest)

                if (response.isSuccessful) {
                    val apiResponse = response.body()

                    if (apiResponse?.success == true) {
                        apiResponse.data?.let { shop ->
                            // Save basic shop info
                            ApiClient.saveCurrentShopId(this@ShopCreationActivity, shop.id)
                            ApiClient.saveCurrentShopName(this@ShopCreationActivity, shop.name)

                            val sharedPref = getSharedPreferences("OseboPrefs", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("shop_address", shop.address ?: "")
                                putString("shop_type", shop.shopType ?: "")
                                putString("shop_registration", binding.etRegistrationNumber.text.toString().trim())
                                putString("shop_tin", binding.etTin.text.toString().trim())
                                putString("shop_description", shop.description ?: "")
                                putBoolean("has_shop", true)

                                // SET TRIAL STATUS HERE - 15 days free trial
                                putString("subscription_status", "TRIAL")
                                putString("subscription_type", "BASIC")
                                putString("subscription_expiry", calculateTrialEndDate(15))
                                putBoolean("subscription_active", true)

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
                    when (response.code()) {
                        400 -> showErrorMessage("Invalid shop data")
                        401 -> showErrorMessage("Unauthorized - Please login again")
                        409 -> showErrorMessage("Shop name already exists")
                        422 -> showErrorMessage("Validation error: ${response.errorBody()?.string()}")
                        else -> showErrorMessage("Server error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                showErrorMessage("Network error: ${e.message}")
            } finally {
                binding.btnCreateShop.isEnabled = true
                binding.btnCreateShop.text = "Create Shop"
            }
        }
    }

    // Add this helper function
    private fun calculateTrialEndDate(days: Int): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, days)
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(calendar.time)
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


