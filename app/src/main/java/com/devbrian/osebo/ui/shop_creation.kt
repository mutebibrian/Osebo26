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
import com.devbrian.osebo.data.ShopType
import com.devbrian.osebo.data.remote.dto.request.CreateShopRequest
import com.devbrian.osebo.databinding.ActivityShopCreationBinding
import com.devbrian.osebo.models.Shop
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

class ShopCreationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopCreationBinding
    private lateinit var apiService: ApiService
    private var shopTypesList: List<ShopType> = emptyList()
    private var isCreatingShop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiClient.createWithAuth(this)

        setupListeners()
        loadShopTypes()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.btnCreateShop.setOnClickListener { createShop() }
        binding.tvSkip.setOnClickListener { navigateToMainActivity() }
    }

    /**
     * Fetch available shop types from the backend.
     * If the API fails, fall back to hardcoded types and always enable the form.
     */
    private fun loadShopTypes() {
        lifecycleScope.launch {
            try {
                val response = apiService.getShopTypes()
                if (response.isSuccessful && response.body()?.success == true) {
                    shopTypesList = response.body()?.data ?: emptyList()
                    if (shopTypesList.isNotEmpty()) {
                        updateShopTypeDropdown()
                    } else {
                        useHardcodedShopTypes()
                    }
                } else {
                    useHardcodedShopTypes()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                useHardcodedShopTypes()
            }
            // Always enable the form regardless of API success/failure
            enableShopCreation(true)
        }
    }

    private fun useHardcodedShopTypes() {
        // Fallback shop types – replace IDs with valid ones from your backend
        shopTypesList = listOf(
            ShopType(id = "a5613dae-6297-448c-a022-40f7ef1f57f4", name = "Retail"),
            ShopType(id = "9423230e-df5a-4669-8fd8-193e11ce72b0", name = "Other")
        )
        updateShopTypeDropdown()
        showErrorMessage("Using default shop types. Some features may be limited.")
    }

    private fun updateShopTypeDropdown() {
        if (shopTypesList.isEmpty()) {
            binding.shopTypeInputLayout.error = "No shop types available"
            return
        }

        val shopTypeNames = shopTypesList.map { it.name }
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, shopTypeNames)
        binding.actvShopType.setAdapter(adapter)

        if (binding.actvShopType.text.isNullOrEmpty() && shopTypeNames.isNotEmpty()) {
            binding.actvShopType.setText(shopTypeNames[0], false)
        }
    }

    private fun enableShopCreation(enable: Boolean) {
        binding.btnCreateShop.isEnabled = enable
        binding.actvShopType.isEnabled = enable
        binding.etShopName.isEnabled = enable
        binding.etAddress.isEnabled = enable
        binding.etRegistrationNumber.isEnabled = enable
        binding.etTin.isEnabled = enable
        binding.etDescription.isEnabled = enable
    }

    /**
     * Retrieve the current account ID from SharedPreferences.
     * This should have been saved after login.
     */
    private fun getCurrentAccountId(): String {
        val sharedPref = getSharedPreferences("OseboPrefs", MODE_PRIVATE)
        return sharedPref.getString("account_id", "") ?: ""
    }

    private fun createShop() {
        if (isCreatingShop) return

        val shopName = binding.etShopName.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val selectedShopTypeName = binding.actvShopType.text.toString().trim()
        val registrationNumber = binding.etRegistrationNumber.text.toString().trim()
        val tin = binding.etTin.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val accountId = getCurrentAccountId()

        // Validate required fields
        if (!validateRequiredFields(shopName, address, selectedShopTypeName)) {
            return
        }

        // Validate account ID
        if (accountId.isEmpty()) {
            showErrorMessage("Session expired. Please log in again.")
            navigateToLogin()
            return
        }

        // Find the selected shop type ID
        val selectedShopType = shopTypesList.find { it.name.equals(selectedShopTypeName, ignoreCase = true) }
        if (selectedShopType == null) {
            binding.shopTypeInputLayout.error = "Invalid shop type selected"
            return
        }

        // Backend requires 'description' field – send a space if empty
        val finalDescription = if (description.isEmpty()) " " else description

        val shopRequest = CreateShopRequest(
            name = shopName,
            address = address,
            shopTypeId = selectedShopType.id,
            registrationNumber = registrationNumber.takeIf { it.isNotEmpty() },
            taxIdentificationNumber = tin.takeIf { it.isNotEmpty() },
            description = finalDescription,
            accountId = accountId
        )

        performCreateShop(shopRequest)
    }

    private fun validateRequiredFields(shopName: String, address: String, shopType: String): Boolean {
        var isValid = true

        binding.shopNameInputLayout.error = null
        binding.addressInputLayout.error = null
        binding.shopTypeInputLayout.error = null

        if (shopName.isEmpty()) {
            binding.shopNameInputLayout.error = "Shop name is required"
            isValid = false
        } else if (shopName.length < 2) {
            binding.shopNameInputLayout.error = "Name must be at least 2 characters"
            isValid = false
        }

        if (address.isEmpty()) {
            binding.addressInputLayout.error = "Address is required"
            isValid = false
        }

        if (shopType.isEmpty()) {
            binding.shopTypeInputLayout.error = "Please select a shop type"
            isValid = false
        }

        return isValid
    }

    private fun performCreateShop(shopRequest: CreateShopRequest) {
        isCreatingShop = true
        binding.btnCreateShop.isEnabled = false
        binding.btnCreateShop.text = "Creating..."

        lifecycleScope.launch {
            try {
                val response = apiService.createShop(shopRequest)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        handleShopCreationSuccess(apiResponse.data)
                    } else {
                        showErrorMessage(apiResponse?.message ?: "Creation failed")
                    }
                } else {
                    handleErrorResponse(response.code(), response.errorBody()?.string())
                }
            } catch (e: HttpException) {
                showErrorMessage("Server error: ${e.code()}")
            } catch (e: Exception) {
                showErrorMessage("Network error: ${e.message}")
            } finally {
                isCreatingShop = false
                binding.btnCreateShop.isEnabled = true
                binding.btnCreateShop.text = "Create Shop"
            }
        }
    }

    private fun handleErrorResponse(code: Int, errorBody: String?) {
        val message = when (code) {
            400 -> "Invalid shop data: ${errorBody ?: "Please check your inputs"}"
            401 -> "Unauthorised – please log in again"
            409 -> "A shop with this name already exists"
            422 -> "Validation error: $errorBody"
            else -> "Server error (HTTP $code)"
        }
        showErrorMessage(message)
    }

    private fun handleShopCreationSuccess(shop: Shop) {
        // Save shop information
        ApiClient.saveCurrentShopId(this, shop.id)
        ApiClient.saveCurrentShopName(this, shop.name)

        val sharedPref = getSharedPreferences("OseboPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("shop_address", shop.address ?: "")
            putString("shop_type", shop.shopType ?: "")
            putString("shop_registration", binding.etRegistrationNumber.text.toString().trim())
            putString("shop_tin", binding.etTin.text.toString().trim())
            putString("shop_description", shop.description ?: "")
            putBoolean("has_shop", true)
            // Trial info
            putString("subscription_status", "TRIAL")
            putString("subscription_type", "BASIC")
            putString("subscription_expiry", calculateTrialEndDate(15))
            putBoolean("subscription_active", true)
            apply()
        }

        Toast.makeText(this, "Shop created successfully!", Toast.LENGTH_SHORT).show()
        navigateToMainActivity()
    }

    private fun calculateTrialEndDate(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}