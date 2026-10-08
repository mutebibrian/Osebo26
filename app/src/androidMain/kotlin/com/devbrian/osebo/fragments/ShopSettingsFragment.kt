package com.devbrian.osebo.fragments.subpages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.ui.ShopViewModel
import com.devbrian.osebo.ui.screens.ShopInfoDraft
import com.devbrian.osebo.ui.screens.ShopSettingsScreen
import com.devbrian.osebo.ui.screens.ShopSettingsUiState
import com.devbrian.osebo.ui.screens.ShopTaxDraft
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ShopSettingsFragment : Fragment() {
    private val shopViewModel: ShopViewModel by viewModel()
    private var shopId: String = ""
    private var currentShop: Shop? = null
    private var uiState by mutableStateOf(ShopSettingsUiState())

    private val businessTypes = listOf(
        "Retail Store",
        "Wholesale",
        "Service Business",
        "Manufacturing",
        "Restaurant",
        "Salon & Spa",
        "Grocery Store",
        "Pharmacy",
        "Hardware Store",
        "Fashion & Clothing",
        "Electronics Store",
        "Online Store",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shopId = arguments?.getString(ARG_SHOP_ID).orEmpty()
        loadNotificationSettings()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                ShopSettingsScreen(
                    state = uiState,
                    businessTypes = businessTypes,
                    onStateChange = { uiState = it },
                    onBackClick = { findNavController().navigateUp() },
                    onRefreshClick = ::refreshData,
                    onSaveShopInfo = ::saveShopInfo,
                    onSaveBusinessType = ::saveBusinessType,
                    onSaveTax = ::saveTaxSettings,
                    onSaveNotifications = ::saveNotificationSettings,
                    onSubscriptionClick = { showMessage("Subscription details") },
                    onExportClick = ::showExportDataDialog,
                    onBackupClick = ::showBackupDialog,
                    onRestoreClick = ::showRestoreDialog,
                    onManageUsersClick = { showMessage("User management") },
                    onDeleteClick = ::showDeleteShopDialog,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeShopData()
        loadShopData()
    }

    private fun observeShopData() {
        shopViewModel.shops.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> uiState = uiState.copy(isLoading = currentShop == null, error = null)
                is Resource.Success -> {
                    currentShop = resource.data?.firstOrNull { it.id == shopId }
                    currentShop?.let(::displayShopInfo)
                    uiState = uiState.copy(isLoading = false, error = if (currentShop == null) "Shop details are unavailable." else null)
                }
                is Resource.Error -> uiState = uiState.copy(isLoading = false, error = resource.message)
            }
        }
    }

    private fun loadShopData() {
        lifecycleScope.launch { shopViewModel.loadShops() }
    }

    private fun displayShopInfo(shop: Shop) {
        val hasSubscription = shop.isSubscriptionActive ||
            shop.subscriptionStatus.equals("active", true) ||
            shop.subscriptionStatus.equals("trial", true)
        uiState = uiState.copy(
            shopName = shop.name,
            address = shop.fullAddress,
            phone = shop.phone.orEmpty(),
            email = shop.email.orEmpty(),
            website = shop.website.orEmpty(),
            businessType = shop.shopTypeDisplay,
            taxNumber = shop.taxIdentificationNumber.orEmpty(),
            registrationNumber = shop.registrationNumber.orEmpty(),
            subscriptionActive = hasSubscription,
            subscriptionPlan = shop.subscriptionType ?: "No plan",
            subscriptionExpiry = shop.subscriptionExpiry ?: "N/A",
            isLoading = false,
            error = null,
        )
    }

    private fun saveShopInfo(draft: ShopInfoDraft) {
        uiState = uiState.copy(
            shopName = draft.name,
            address = draft.address,
            phone = draft.phone,
            email = draft.email,
            website = draft.website,
        )
        showMessage("Shop information saved")
    }

    private fun saveBusinessType(type: String) {
        uiState = uiState.copy(businessType = type)
        showMessage("Business settings saved")
    }

    private fun saveTaxSettings(draft: ShopTaxDraft) {
        uiState = uiState.copy(
            taxRate = draft.rate.ifBlank { "0" },
            taxNumber = draft.number,
            registrationNumber = draft.registrationNumber,
        )
        showMessage("Tax settings saved")
    }

    private fun loadNotificationSettings() {
        if (!isAdded && context == null) return
        val prefs = requireContext().getSharedPreferences("shop_prefs_$shopId", Context.MODE_PRIVATE)
        uiState = uiState.copy(
            emailNotifications = prefs.getBoolean("email_notifications", true),
            pushNotifications = prefs.getBoolean("push_notifications", true),
            smsNotifications = prefs.getBoolean("sms_notifications", false),
            lowStockAlerts = prefs.getBoolean("low_stock_alerts", true),
            dailyReports = prefs.getBoolean("daily_reports", false),
        )
    }

    private fun saveNotificationSettings() {
        requireContext().getSharedPreferences("shop_prefs_$shopId", Context.MODE_PRIVATE).edit().apply {
            putBoolean("email_notifications", uiState.emailNotifications)
            putBoolean("push_notifications", uiState.pushNotifications)
            putBoolean("sms_notifications", uiState.smsNotifications)
            putBoolean("low_stock_alerts", uiState.lowStockAlerts)
            putBoolean("daily_reports", uiState.dailyReports)
            apply()
        }
        showMessage("Notification settings saved")
    }

    private fun showExportDataDialog() {
        val options = arrayOf("CSV", "PDF", "Excel")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Export data")
            .setItems(options) { _, index -> showMessage("Exporting as ${options[index]}...") }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBackupDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Back up data")
            .setMessage("Create a backup of all shop data?")
            .setPositiveButton("Back up now") { _, _ -> showMessage("Creating backup...") }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRestoreDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Restore data")
            .setMessage("Restore a previous backup? This replaces the current data.")
            .setPositiveButton("Restore") { _, _ -> showMessage("Restoring data...") }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteShopDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete shop")
            .setMessage("Delete ${uiState.shopName}? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteShop() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteShop() {
        lifecycleScope.launch {
            shopViewModel.deleteShop(shopId)
            showMessage("Shop deleted")
            findNavController().navigateUp()
        }
    }

    private fun refreshData() {
        uiState = uiState.copy(isLoading = currentShop == null, error = null)
        loadNotificationSettings()
        loadShopData()
    }

    private fun showMessage(message: String) {
        if (isAdded) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val ARG_SHOP_ID = "shop_id"

        fun newInstance(shopId: String) = ShopSettingsFragment().apply {
            arguments = Bundle().apply { putString(ARG_SHOP_ID, shopId) }
        }
    }
}
