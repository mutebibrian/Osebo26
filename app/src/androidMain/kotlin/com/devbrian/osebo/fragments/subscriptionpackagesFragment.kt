package com.devbrian.osebo.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.adapters.SubscriptionPackageAdapter
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.databinding.FragmentSubscriptionPackagesBinding
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.models.SubscriptionPackage
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubscriptionPackagesFragment : Fragment() {

    private var _binding: FragmentSubscriptionPackagesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubscriptionViewModel by viewModels()
    private lateinit var subscriptionPackageAdapter: SubscriptionPackageAdapter

    private var shop: Shop? = null
    private var currentPackage: String? = null
    private var selectedPackages: List<SubscriptionPackage> = emptyList()
    private var shopId: String = ""

    private val args: SubscriptionPackagesFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionPackagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            shop = args.shop
            currentPackage = args.currentPackage
        } catch (e: Exception) {
            println("⚠️ Error getting arguments: ${e.message}")
            shop = null
            currentPackage = null
        }

        if (shop == null) {
            val preferenceManager = PreferenceManager.getInstance(requireContext())
            shopId = preferenceManager.getCurrentShopId()
            val shopName = preferenceManager.getCurrentShopName()

            if (shopId.isNotEmpty()) {
                shop = Shop(
                    id = shopId,
                    name = shopName,
                    description = preferenceManager.getShopLocation(),
                    address = preferenceManager.getShopLocation(),
                    shopType = preferenceManager.getBusinessType(),
                    totalRevenue = 0.0,
                    totalExpenses = 0.0,
                    profit = 0.0,
                    totalProducts = 0,
                    totalEmployees = 0,
                    logoUrl = null,
                    subscription = null,
                    phone = preferenceManager.getShopContact()
                )
                println("✅ Created shop from preferences: $shopName")
            } else {
                Toast.makeText(
                    requireContext(),
                    "No shop selected. Please select a shop first.",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigateUp()
                return
            }
        } else {
            shopId = shop?.id ?: ""
        }

        setupUI()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadSubscriptionPackages()
    }

    private fun setupUI() {
        binding.toolbar.title = "Choose Bundles"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        shop?.let {
            binding.toolbar.subtitle = it.name
        }

        currentPackage?.let { packageType ->
            binding.tvCurrentPlan.text = "Current: ${getPackageDisplayName(packageType)}"
            binding.tvCurrentPlan.visibility = View.VISIBLE
        } ?: run {
            binding.tvCurrentPlan.visibility = View.GONE
        }
    }

    private fun getPackageDisplayName(packageType: String): String {
        return when (packageType.uppercase()) {
            "BASIC" -> "Basic"
            "PRO" -> "Pro"
            "POPULAR", "ENTERPRISE" -> "Enterprise"
            else -> packageType
        }
    }

    private fun setupRecyclerView() {
        subscriptionPackageAdapter = SubscriptionPackageAdapter { selected ->
            onSelectionChanged(selected)
        }

        binding.rvPackages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = subscriptionPackageAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }
    }

    private fun onSelectionChanged(selected: List<SubscriptionPackage>) {
        selectedPackages = selected

        if (selected.isEmpty()) {
            binding.cardCheckoutSummary.visibility = View.GONE
            binding.btnCheckout.isEnabled = false
            binding.btnCheckout.text = "SELECT BUNDLES TO CONTINUE"
            binding.tvSelectionCount.text = "0 selected"
            return
        }

        binding.cardCheckoutSummary.visibility = View.VISIBLE
        binding.tvSelectedBundlesList.text = selected.joinToString(", ") { it.displayName }

        val total = selected.sumOf { it.price }
        // Use hasFreeTrial – no fallback
        val hasTrial = selected.any { it.hasFreeTrial }
        val preferenceManager = PreferenceManager.getInstance(requireContext())
        val hasActiveSub = preferenceManager.hasActiveSubscription()
        val isTrialEligible = hasTrial && !hasActiveSub

        binding.tvEstimatedTotal.text = if (isTrialEligible) {
            "UGX 0"
        } else {
            "UGX ${String.format("%,.0f", total)}"
        }
        binding.tvSelectionCount.text = "${selected.size} selected"

        binding.btnCheckout.isEnabled = true
        binding.btnCheckout.text = if (isTrialEligible) {
            "START FREE TRIAL (${selected.size} BUNDLE${if (selected.size > 1) "S" else ""})"
        } else {
            "CONTINUE WITH ${selected.size} BUNDLE${if (selected.size > 1) "S" else ""}"
        }
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnCheckout.setOnClickListener {
            if (selectedPackages.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please select at least one bundle",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                proceedWithPackages(selectedPackages)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.subscriptionPackages.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { packages ->
                        if (packages.isEmpty()) {
                            showErrorState("No subscription packages available")
                        } else {
                            binding.layoutContent.visibility = View.VISIBLE
                            binding.layoutError.visibility = View.GONE
                            subscriptionPackageAdapter.submitList(packages)
                        }
                    }
                }
                is Resource.Error -> {
                    showErrorState(resource.message ?: "Failed to load packages")
                }
                is Resource.Loading -> {}
            }
        }

        viewModel.subscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* optional loading */ }
                is Resource.Success -> {
                    val response = resource.data
                    val paymentId = response?.paymentId

                    if (paymentId.isNullOrBlank()) {
                        Toast.makeText(
                            requireContext(),
                            response?.message ?: "Free trial started! 15 days free.",
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().navigateUp()
                    } else {
                        try {
                            val totalAmount = selectedPackages.sumOf { it.price }.toFloat()
                            val action = SubscriptionPackagesFragmentDirections
                                .actionSubscriptionPackagesFragmentToPaymentStatusFragment(
                                    transactionId = paymentId,
                                    shopId = shopId,
                                    amount = totalAmount,
                                    currency = "UGX",
                                    phoneNumber = shop?.phone ?: ""
                                )
                            findNavController().navigate(action)
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Payment initiated. Please check your phone.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Failed to create subscription",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadSubscriptionPackages() {
        lifecycleScope.launch {
            viewModel.loadSubscriptionPackages()
        }
    }

    private fun proceedWithPackages(packages: List<SubscriptionPackage>) {
        shop?.let { shop ->
            val customOnly = packages.any { it.isCustom }
            if (customOnly) {
                showContactDialog(packages)
                return
            }

            // Use hasFreeTrial – no fallback
            val hasTrial = packages.any { it.hasFreeTrial }
            val preferenceManager = PreferenceManager.getInstance(requireContext())
            val hasActiveSub = preferenceManager.hasActiveSubscription()
            val isTrialEligible = hasTrial && !hasActiveSub

            if (isTrialEligible) {
                val phone = shop.phone?.takeIf { it.isNotEmpty() }
                if (phone != null) {
                    showFreeTrialDialog(shop, packages, phone)
                } else {
                    showPhoneNumberDialog(shop, packages)
                }
            } else {
                showPaymentDialog(shop, packages)
            }
        } ?: run {
            Toast.makeText(
                requireContext(),
                "Shop information is required",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showPhoneNumberDialog(shop: Shop, packages: List<SubscriptionPackage>) {
        val phoneInput = EditText(requireContext()).apply {
            hint = "Enter phone number (e.g., 256700000000)"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Phone Number Required")
            .setMessage("Please enter your phone number to start the free trial.")
            .setView(phoneInput)
            .setPositiveButton("Start Trial") { _, _ ->
                val phone = phoneInput.text.toString().trim()
                if (phone.isNotEmpty()) {
                    // Pass the entered phone directly, no need to modify shop object
                    showFreeTrialDialog(shop, packages, phone)
                } else {
                    Toast.makeText(requireContext(), "Phone number is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFreeTrialDialog(shop: Shop, packages: List<SubscriptionPackage>, phoneNumber: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Start Free Trial")
            .setMessage(
                "You are eligible for a 15‑day free trial. " +
                        "Your card will not be charged until the trial ends.\n\n" +
                        "Bundles: ${packages.joinToString(", ") { it.displayName }}\n" +
                        "Due today: UGX 0"
            )
            .setPositiveButton("Start Trial") { _, _ ->
                startFreeTrial(shop.id, packages.map { it.id }, phoneNumber)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startFreeTrial(shopId: String, packageIds: List<String>, phoneNumber: String) {
        if (!isAdded) return
        viewModel.createSubscription(shopId, packageIds, phoneNumber, months = 1, isTrial = true)
    }

    private fun showPaymentDialog(shop: Shop, packages: List<SubscriptionPackage>) {
        val totalAmount = packages.sumOf { it.price }

        val dialog = PaymentDialogFragment.newInstance(
            shopId = shop.id,
            packageId = packages.joinToString(",") { it.id },
            packageName = packages.joinToString(", ") { it.displayName },
            amount = totalAmount
        )

        dialog.setPaymentListener { phoneNumber, _, months, _ ->
            createSubscription(shop.id, packages.map { it.id }, phoneNumber, months)
        }

        dialog.show(parentFragmentManager, "PaymentDialog")
    }

    private fun createSubscription(
        shopId: String,
        packageIds: List<String>,
        phoneNumber: String,
        months: Int,
        isTrial: Boolean = false
    ) {
        if (!isAdded) return
        viewModel.createSubscription(shopId, packageIds, phoneNumber, months, isTrial)
    }

    private fun showContactDialog(packages: List<SubscriptionPackage>) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Contact Sales")
            .setMessage(
                "${packages.joinToString(", ") { it.displayName }} requires custom pricing. " +
                        "Please contact our sales team for more information."
            )
            .setPositiveButton("Contact Sales") { _, _ ->
                openContactSales(packages)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openContactSales(packages: List<SubscriptionPackage>) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:sales@osebo.ai".toUri()
            putExtra(
                Intent.EXTRA_SUBJECT,
                "Custom Plan Inquiry - ${packages.joinToString(", ") { it.displayName }}"
            )
            putExtra(Intent.EXTRA_TEXT, buildString {
                appendLine("Hello,")
                appendLine("\nI'm interested in: ${packages.joinToString(", ") { it.displayName }}")
                appendLine("\nShop: ${shop?.name ?: "N/A"}")
                appendLine("Shop ID: ${shop?.id ?: "N/A"}")
                appendLine("\nPlease contact me with more information.")
            })
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showErrorState(message: String? = null) {
        binding.layoutContent.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE

        message?.let {
            binding.tvErrorMessage.text = it
        }

        binding.btnRetry.setOnClickListener {
            loadSubscriptionPackages()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}