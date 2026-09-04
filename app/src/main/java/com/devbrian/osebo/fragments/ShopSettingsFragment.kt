package com.devbrian.osebo.fragments.subpages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentShopSettingsBinding
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.ui.ShopViewModel
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShopSettingsFragment : Fragment() {

    private var _binding: FragmentShopSettingsBinding? = null
    private val binding get() = _binding!!

    private val shopViewModel: ShopViewModel by viewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()

    companion object {
        private const val ARG_SHOP_ID = "shop_id"

        fun newInstance(shopId: String): ShopSettingsFragment {
            val fragment = ShopSettingsFragment()
            val args = Bundle()
            args.putString(ARG_SHOP_ID, shopId)
            fragment.arguments = args
            return fragment
        }
    }

    private var shopId: String = ""
    private var currentShop: Shop? = null
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shopId = arguments?.getString(ARG_SHOP_ID) ?: return

        setupToolbar()
        setupClickListeners()
        setupSwitches()
        loadShopData()
        observeViewModels()
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Shop Settings"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_save -> {
                    if (isEditMode) {
                        saveSettings()
                    }
                    true
                }
                R.id.action_edit -> {
                    enableEditMode()
                    true
                }
                R.id.action_refresh -> {
                    refreshData()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        
        binding.btnEditShopInfo.setOnClickListener {
            expandSection(binding.layoutShopInfoExpanded)
        }

        binding.btnSaveShopInfo.setOnClickListener {
            saveShopInfo()
        }

        binding.btnCancelShopInfo.setOnClickListener {
            collapseSection(binding.layoutShopInfoExpanded)
            loadShopData() 
        }

        
        binding.btnEditBusinessSettings.setOnClickListener {
            expandSection(binding.layoutBusinessSettingsExpanded)
        }

        binding.btnSaveBusinessSettings.setOnClickListener {
            saveBusinessSettings()
        }

        binding.btnCancelBusinessSettings.setOnClickListener {
            collapseSection(binding.layoutBusinessSettingsExpanded)
            loadShopData()
        }

        
        binding.btnEditTaxSettings.setOnClickListener {
            expandSection(binding.layoutTaxSettingsExpanded)
        }

        binding.btnSaveTaxSettings.setOnClickListener {
            saveTaxSettings()
        }

        binding.btnCancelTaxSettings.setOnClickListener {
            collapseSection(binding.layoutTaxSettingsExpanded)
            loadShopData()
        }

        
        binding.btnEditNotificationSettings.setOnClickListener {
            expandSection(binding.layoutNotificationSettingsExpanded)
        }

        binding.btnSaveNotificationSettings.setOnClickListener {
            saveNotificationSettings()
        }

        binding.btnCancelNotificationSettings.setOnClickListener {
            collapseSection(binding.layoutNotificationSettingsExpanded)
            loadNotificationSettings()
        }

        
        binding.btnExportData.setOnClickListener {
            showExportDataDialog()
        }

        binding.btnBackupData.setOnClickListener {
            showBackupDialog()
        }

        binding.btnRestoreData.setOnClickListener {
            showRestoreDialog()
        }

        binding.btnManageUsers.setOnClickListener {
            navigateToUserManagement()
        }

        binding.btnSubscriptionDetails.setOnClickListener {
            navigateToSubscriptionDetails()
        }

        binding.btnDeleteShop.setOnClickListener {
            showDeleteShopDialog()
        }
    }

    private fun setupSwitches() {
        
        binding.switchEmailNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isEditMode) {
                binding.tvEmailNotificationsStatus.text = if (isChecked) "Enabled" else "Disabled"
            }
        }

        binding.switchPushNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isEditMode) {
                binding.tvPushNotificationsStatus.text = if (isChecked) "Enabled" else "Disabled"
            }
        }

        binding.switchSmsNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isEditMode) {
                binding.tvSmsNotificationsStatus.text = if (isChecked) "Enabled" else "Disabled"
            }
        }

        binding.switchLowStockAlerts.setOnCheckedChangeListener { _, isChecked ->
            if (isEditMode) {
                binding.tvLowStockAlertsStatus.text = if (isChecked) "Enabled" else "Disabled"
            }
        }

        binding.switchDailyReports.setOnCheckedChangeListener { _, isChecked ->
            if (isEditMode) {
                binding.tvDailyReportsStatus.text = if (isChecked) "Enabled" else "Disabled"
            }
        }
    }

    private fun expandSection(layout: View) {
        if (layout.visibility == View.GONE) {
            layout.visibility = View.VISIBLE
        } else {
            layout.visibility = View.GONE
        }
    }

    private fun collapseSection(layout: View) {
        layout.visibility = View.GONE
    }

    private fun enableEditMode() {
        isEditMode = true
        binding.toolbar.menu.findItem(R.id.action_edit).isVisible = false
        binding.toolbar.menu.findItem(R.id.action_save).isVisible = true

        
        binding.btnEditShopInfo.visibility = View.VISIBLE
        binding.btnEditBusinessSettings.visibility = View.VISIBLE
        binding.btnEditTaxSettings.visibility = View.VISIBLE
        binding.btnEditNotificationSettings.visibility = View.VISIBLE

        Toast.makeText(requireContext(), "Edit mode enabled", Toast.LENGTH_SHORT).show()
    }

    private fun disableEditMode() {
        isEditMode = false
        binding.toolbar.menu.findItem(R.id.action_edit).isVisible = true
        binding.toolbar.menu.findItem(R.id.action_save).isVisible = false

        
        binding.btnEditShopInfo.visibility = View.GONE
        binding.btnEditBusinessSettings.visibility = View.GONE
        binding.btnEditTaxSettings.visibility = View.GONE
        binding.btnEditNotificationSettings.visibility = View.GONE

        
        binding.layoutShopInfoExpanded.visibility = View.GONE
        binding.layoutBusinessSettingsExpanded.visibility = View.GONE
        binding.layoutTaxSettingsExpanded.visibility = View.GONE
        binding.layoutNotificationSettingsExpanded.visibility = View.GONE
    }

    private fun loadShopData() {
        lifecycleScope.launch {
            shopViewModel.loadShops()
        }
    }

    private fun loadNotificationSettings() {
        
        val prefs = requireContext().getSharedPreferences("shop_prefs_$shopId", android.content.Context.MODE_PRIVATE)

        binding.switchEmailNotifications.isChecked = prefs.getBoolean("email_notifications", true)
        binding.switchPushNotifications.isChecked = prefs.getBoolean("push_notifications", true)
        binding.switchSmsNotifications.isChecked = prefs.getBoolean("sms_notifications", false)
        binding.switchLowStockAlerts.isChecked = prefs.getBoolean("low_stock_alerts", true)
        binding.switchDailyReports.isChecked = prefs.getBoolean("daily_reports", false)

        binding.tvEmailNotificationsStatus.text = if (binding.switchEmailNotifications.isChecked) "Enabled" else "Disabled"
        binding.tvPushNotificationsStatus.text = if (binding.switchPushNotifications.isChecked) "Enabled" else "Disabled"
        binding.tvSmsNotificationsStatus.text = if (binding.switchSmsNotifications.isChecked) "Enabled" else "Disabled"
        binding.tvLowStockAlertsStatus.text = if (binding.switchLowStockAlerts.isChecked) "Enabled" else "Disabled"
        binding.tvDailyReportsStatus.text = if (binding.switchDailyReports.isChecked) "Enabled" else "Disabled"
    }

    private fun observeViewModels() {
        shopViewModel.shops.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    currentShop = resource.data?.find { it.id == shopId }
                    currentShop?.let { shop ->
                        displayShopInfo(shop)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    
                }
            }
        }
    }

    private fun displayShopInfo(shop: Shop) {
        
        binding.tvShopNameValue.text = shop.name
        binding.tvShopAddressValue.text = shop.fullAddress.ifEmpty { "Not set" }
        binding.tvShopPhoneValue.text = shop.phone ?: "Not set"
        binding.tvShopEmailValue.text = shop.email ?: "Not set"
        binding.tvShopWebsiteValue.text = shop.website ?: "Not set"

        
        binding.etShopName.setText(shop.name)
        binding.etShopAddress.setText(shop.address)
        binding.etShopPhone.setText(shop.phone)
        binding.etShopEmail.setText(shop.email)
        binding.etShopWebsite.setText(shop.website)

        
        binding.tvBusinessTypeValue.text = shop.shopTypeDisplay
        binding.tvCurrencyValue.text = "UGX" 
        binding.tvTimeZoneValue.text = "Africa/Kampala" 

        binding.spinnerBusinessType.setSelection(getBusinessTypePosition(shop.shopType))

        
        binding.tvTaxRateValue.text = "${shop.taxIdentificationNumber ?: "0"}%"
        binding.tvTaxNumberValue.text = shop.taxIdentificationNumber ?: "Not set"
        binding.tvRegistrationNumberValue.text = shop.registrationNumber ?: "Not set"

        binding.etTaxRate.setText(shop.taxIdentificationNumber ?: "0")
        binding.etTaxNumber.setText(shop.taxIdentificationNumber)
        binding.etRegistrationNumber.setText(shop.registrationNumber)

        
        val hasSubscription = shop.isSubscriptionActive ||
                shop.subscriptionStatus.equals("active", ignoreCase = true) ||
                shop.subscriptionStatus.equals("trial", ignoreCase = true)

        binding.tvSubscriptionStatusValue.text = if (hasSubscription) "Active" else "Inactive"
        binding.tvSubscriptionStatusValue.setTextColor(
            ContextCompat.getColor(requireContext(),
                if (hasSubscription) R.color.success_green else R.color.error_red
            )
        )
        binding.tvSubscriptionPlanValue.text = shop.subscriptionType ?: "No Plan"
        binding.tvSubscriptionExpiryValue.text = shop.subscriptionExpiry ?: "N/A"
    }

    private fun getBusinessTypePosition(type: String?): Int {
        return when (type?.lowercase()) {
            "retail" -> 0
            "wholesale" -> 1
            "service" -> 2
            "manufacturing" -> 3
            "restaurant" -> 4
            else -> 0
        }
    }

    private fun saveSettings() {
        
        saveShopInfo()
        saveBusinessSettings()
        saveTaxSettings()
        saveNotificationSettings()

        disableEditMode()
        Toast.makeText(requireContext(), "Settings saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun saveShopInfo() {
        
        val updatedName = binding.etShopName.text.toString()
        val updatedAddress = binding.etShopAddress.text.toString()
        val updatedPhone = binding.etShopPhone.text.toString()
        val updatedEmail = binding.etShopEmail.text.toString()
        val updatedWebsite = binding.etShopWebsite.text.toString()

        
        binding.tvShopNameValue.text = updatedName
        binding.tvShopAddressValue.text = updatedAddress.ifEmpty { "Not set" }
        binding.tvShopPhoneValue.text = updatedPhone.ifEmpty { "Not set" }
        binding.tvShopEmailValue.text = updatedEmail.ifEmpty { "Not set" }
        binding.tvShopWebsiteValue.text = updatedWebsite.ifEmpty { "Not set" }

        collapseSection(binding.layoutShopInfoExpanded)
        Toast.makeText(requireContext(), "Shop info saved", Toast.LENGTH_SHORT).show()
    }

    private fun saveBusinessSettings() {
        val businessType = binding.spinnerBusinessType.selectedItem.toString()

        binding.tvBusinessTypeValue.text = businessType

        collapseSection(binding.layoutBusinessSettingsExpanded)
        Toast.makeText(requireContext(), "Business settings saved", Toast.LENGTH_SHORT).show()
    }

    private fun saveTaxSettings() {
        val taxRate = binding.etTaxRate.text.toString()
        val taxNumber = binding.etTaxNumber.text.toString()
        val regNumber = binding.etRegistrationNumber.text.toString()

        binding.tvTaxRateValue.text = "$taxRate%"
        binding.tvTaxNumberValue.text = taxNumber.ifEmpty { "Not set" }
        binding.tvRegistrationNumberValue.text = regNumber.ifEmpty { "Not set" }

        collapseSection(binding.layoutTaxSettingsExpanded)
        Toast.makeText(requireContext(), "Tax settings saved", Toast.LENGTH_SHORT).show()
    }

    private fun saveNotificationSettings() {
        val prefs = requireContext().getSharedPreferences("shop_prefs_$shopId", android.content.Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("email_notifications", binding.switchEmailNotifications.isChecked)
            putBoolean("push_notifications", binding.switchPushNotifications.isChecked)
            putBoolean("sms_notifications", binding.switchSmsNotifications.isChecked)
            putBoolean("low_stock_alerts", binding.switchLowStockAlerts.isChecked)
            putBoolean("daily_reports", binding.switchDailyReports.isChecked)
            apply()
        }

        collapseSection(binding.layoutNotificationSettingsExpanded)
        Toast.makeText(requireContext(), "Notification settings saved", Toast.LENGTH_SHORT).show()
    }

    private fun showExportDataDialog() {
        val options = arrayOf("CSV", "PDF", "Excel")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Export Data")
            .setItems(options) { _, which ->
                val format = options[which]
                Toast.makeText(requireContext(), "Exporting as $format...", Toast.LENGTH_SHORT).show()
                
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBackupDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Backup Data")
            .setMessage("Create a backup of all your shop data?")
            .setPositiveButton("Backup Now") { _, _ ->
                Toast.makeText(requireContext(), "Creating backup...", Toast.LENGTH_SHORT).show()
                
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRestoreDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Restore Data")
            .setMessage("Restore from a previous backup? This will overwrite current data.")
            .setPositiveButton("Restore") { _, _ ->
                Toast.makeText(requireContext(), "Restoring data...", Toast.LENGTH_SHORT).show()
                
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteShopDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Shop")
            .setMessage("Are you sure you want to delete this shop? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteShop()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteShop() {
        lifecycleScope.launch {
            shopViewModel.deleteShop(shopId)
            Toast.makeText(requireContext(), "Shop deleted", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun navigateToUserManagement() {
        Toast.makeText(requireContext(), "User Management", Toast.LENGTH_SHORT).show()
        
    }

    private fun navigateToSubscriptionDetails() {
        Toast.makeText(requireContext(), "Subscription Details", Toast.LENGTH_SHORT).show()
        
    }

    private fun refreshData() {
        loadShopData()
        loadNotificationSettings()
        Toast.makeText(requireContext(), "Refreshing...", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
