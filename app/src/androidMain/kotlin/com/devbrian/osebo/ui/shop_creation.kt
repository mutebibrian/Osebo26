package com.devbrian.osebo.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.devbrian.osebo.data.ApiClient
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.ShopType
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.data.remote.dto.request.CreateShopRequest
import com.devbrian.osebo.ui.screens.ShopFormMode
import com.devbrian.osebo.ui.screens.ShopFormScreen
import com.devbrian.osebo.ui.screens.ShopFormUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ShopCreationActivity : AppCompatActivity() {
    private lateinit var apiService: ApiService
    private var shopTypes by mutableStateOf<List<ShopType>>(emptyList())
    private var formState by mutableStateOf(
        ShopFormUiState(mode = ShopFormMode.Create, isTypeLoading = true),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
        apiService = ApiClient.createWithAuth(this)

        setContentView(
            ComposeView(this).apply {
                setContent {
                    OseboTheme {
                        ShopFormScreen(
                            state = formState,
                            shopTypes = shopTypes.map { it.name },
                            onStateChange = { formState = it },
                            onBackClick = { onBackPressedDispatcher.onBackPressed() },
                            onSkipClick = ::navigateToMainActivity,
                            onContinue = ::validatePrimaryStep,
                            onSave = ::createShop,
                        )
                    }
                }
            },
        )
        loadShopTypes()
    }

    private fun loadShopTypes() {
        lifecycleScope.launch {
            try {
                val response = apiService.getShopTypes()
                val loaded = response.body()?.data.orEmpty()
                if (response.isSuccessful && response.body()?.success == true && loaded.isNotEmpty()) {
                    shopTypes = loaded
                    formState = formState.copy(type = formState.type.ifBlank { loaded.first().name }, isTypeLoading = false)
                } else {
                    useFallbackShopTypes()
                }
            } catch (_: Exception) {
                useFallbackShopTypes()
            }
        }
    }

    private fun useFallbackShopTypes() {
        shopTypes = listOf(
            ShopType(id = "a5613dae-6297-448c-a022-40f7ef1f57f4", name = "Retail"),
            ShopType(id = "9423230e-df5a-4669-8fd8-193e11ce72b0", name = "Other"),
        )
        formState = formState.copy(
            type = formState.type.ifBlank { shopTypes.first().name },
            isTypeLoading = false,
            submitError = "Default shop types are available while the full list is offline.",
        )
    }

    private fun validatePrimaryStep() {
        val state = formState
        val nameError = when {
            state.name.trim().isEmpty() -> "Shop name is required"
            state.name.trim().length < 2 -> "Use at least 2 characters"
            else -> null
        }
        val addressError = "Address is required".takeIf { state.address.trim().isEmpty() }
        val typeError = "Choose a shop type".takeIf { shopTypes.none { it.name.equals(state.type, true) } }
        val hasError = listOf(nameError, addressError, typeError).any { it != null }
        formState = state.copy(
            nameError = nameError,
            addressError = addressError,
            typeError = typeError,
            currentStep = if (hasError) 0 else 1,
            submitError = if (hasError) "Check the highlighted shop details." else null,
        )
    }

    private fun createShop() {
        validatePrimaryStep()
        if (formState.currentStep == 0 || formState.isLoading) return

        val accountId = getSharedPreferences("OseboPrefs", MODE_PRIVATE).getString("account_id", "").orEmpty()
        if (accountId.isEmpty()) {
            showError("Session expired. Please log in again.")
            navigateToLogin()
            return
        }
        val selectedType = shopTypes.firstOrNull { it.name.equals(formState.type, true) }
        if (selectedType == null) {
            formState = formState.copy(currentStep = 0, typeError = "Choose a valid shop type")
            return
        }

        val request = CreateShopRequest(
            name = formState.name.trim(),
            address = formState.address.trim(),
            shopTypeId = selectedType.id,
            registrationNumber = formState.registrationNumber.trim().ifEmpty { null },
            taxIdentificationNumber = formState.tin.trim().ifEmpty { null },
            description = formState.description.trim().ifEmpty { " " },
            accountId = accountId,
        )
        performCreateShop(request)
    }

    private fun performCreateShop(request: CreateShopRequest) {
        lifecycleScope.launch {
            formState = formState.copy(isLoading = true, submitError = null)
            try {
                val response = apiService.createShop(request)
                val body = response.body()
                if (response.isSuccessful && body?.success == true && body.data != null) {
                    handleShopCreationSuccess(body.data)
                } else {
                    val message = when (response.code()) {
                        400 -> "Invalid shop data. Check your entries."
                        401 -> "Session expired. Please log in again."
                        409 -> "A shop with this name already exists."
                        422 -> "Some shop details were rejected."
                        else -> body?.message ?: "Unable to create shop."
                    }
                    showError(message)
                }
            } catch (error: HttpException) {
                showError("Server error: ${error.code()}")
            } catch (error: Exception) {
                showError("Network error: ${error.message}")
            } finally {
                formState = formState.copy(isLoading = false)
            }
        }
    }

    private fun handleShopCreationSuccess(shop: Shop) {
        ApiClient.saveCurrentShopId(this, shop.id)
        ApiClient.saveCurrentShopName(this, shop.name)
        with(getSharedPreferences("OseboPrefs", MODE_PRIVATE).edit()) {
            putString("shop_address", shop.address.orEmpty())
            putString("shop_type", shop.shopType.orEmpty())
            putString("shop_registration", formState.registrationNumber.trim())
            putString("shop_tin", formState.tin.trim())
            putString("shop_description", shop.description.orEmpty())
            putBoolean("has_shop", true)
            putString("subscription_status", "TRIAL")
            putString("subscription_type", "BASIC")
            putString("subscription_expiry", calculateTrialEndDate(15))
            putBoolean("subscription_active", true)
            apply()
        }
        Toast.makeText(this, "Shop created successfully", Toast.LENGTH_SHORT).show()
        navigateToMainActivity()
    }

    private fun calculateTrialEndDate(days: Int): String {
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, days) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    private fun showError(message: String) {
        formState = formState.copy(submitError = message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
