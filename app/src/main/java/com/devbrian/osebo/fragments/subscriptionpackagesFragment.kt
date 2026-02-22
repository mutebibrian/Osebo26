package com.devbrian.osebo.fragments

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.SubscriptionPackageAdapter
import com.devbrian.osebo.databinding.FragmentSubscriptionPackagesBinding
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.models.SubscriptionPackage
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.view.ViewTreeObserver
import com.devbrian.osebo.ui.MainActivity

@AndroidEntryPoint
class SubscriptionPackagesFragment : Fragment() {

    private var _binding: FragmentSubscriptionPackagesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubscriptionViewModel by viewModels()
    private lateinit var subscriptionPackageAdapter: SubscriptionPackageAdapter

    private var shop: Shop? = null
    private var currentPackage: String? = null

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

        // Get data from navigation arguments
        shop = args.shop
        currentPackage = args.currentPackage

        setupUI()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadSubscriptionPackages()

        // Check RecyclerView visibility after layout
        checkRecyclerViewVisibility()
    }

    private fun setupUI() {
        binding.toolbar.title = "Choose a Plan"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Set shop info as toolbar subtitle
        shop?.let {
            binding.toolbar.subtitle = it.name
        }

        // Show current plan if available
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
        subscriptionPackageAdapter = SubscriptionPackageAdapter { packageItem ->
            onPackageSelected(packageItem)
        }

        binding.rvPackages.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = subscriptionPackageAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }

        println("🔄 RECYCLERVIEW: Setup completed")
    }

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            val selectedPackage = subscriptionPackageAdapter.getSelectedPackage()
            selectedPackage?.let { packageItem ->
                proceedWithPackage(packageItem)
            } ?: run {
                Toast.makeText(
                    requireContext(),
                    "Please select a package to continue",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnSkip.setOnClickListener {
            findNavController().navigateUp()
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
                            binding.rvPackages.postDelayed({
                                checkRecyclerViewState()
                            }, 500)
                        }
                    }
                }
                is Resource.Error -> {
                    showErrorState(resource.message ?: "Failed to load packages")
                }
                is Resource.Loading -> {
                    // No progress bar to show
                }
            }
        }

        // Observe subscription creation result
        viewModel.subscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    // ✅ Get paymentId from response.data
                    val paymentId = resource.data?.data?.paymentId
                    if (!paymentId.isNullOrBlank()) {
                        navigateToPayment(paymentId)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Subscription created successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Failed to create subscription",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Loading -> {
                    // No progress bar to show
                }
            }
        }
        // Observe trial activation result
        viewModel.activateTrialResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Free trial activated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Failed to activate trial",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is Resource.Loading -> {}
            }
        }
    }

    // Check RecyclerView visibility and dimensions
    private fun checkRecyclerViewVisibility() {
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val rect = Rect()
                binding.rvPackages.getGlobalVisibleRect(rect)

                println("📏 RECYCLERVIEW DEBUG - LAYOUT COMPLETE")
                println("📏 RecyclerView width: ${binding.rvPackages.width}")
                println("📏 RecyclerView height: ${binding.rvPackages.height}")
                println("📏 RecyclerView visible rect: $rect")

                binding.rvPackages.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        binding.rvPackages.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    // Check RecyclerView state after data is loaded
    private fun checkRecyclerViewState() {
        println("📏 RECYCLERVIEW STATE DEBUG")
        println("📏 RecyclerView child count: ${binding.rvPackages.childCount}")
        println("📏 RecyclerView adapter item count: ${binding.rvPackages.adapter?.itemCount}")
    }

    private fun loadSubscriptionPackages() {
        lifecycleScope.launch {
            viewModel.loadSubscriptionPackages()
        }
    }

    private fun onPackageSelected(packageItem: SubscriptionPackage) {
        updateSelectedPackageUI(packageItem)
    }

    private fun updateSelectedPackageUI(packageItem: SubscriptionPackage) {
        // Show the selected package details card
        binding.cardSelectedPackage.visibility = View.VISIBLE

        binding.tvSelectedPackageName.text = packageItem.displayName
        binding.tvSelectedPackagePrice.text = packageItem.displayPrice
        binding.tvSelectedPackageDescription.text = packageItem.description

        // Update Continue button
        binding.btnContinue.isEnabled = true

        binding.btnContinue.text = when {
            packageItem.hasFreeTrial && currentPackage == null -> "START FREE TRIAL"
            packageItem.isCustom -> "CONTACT SALES"
            else -> "CONTINUE WITH ${packageItem.displayName.uppercase()}"
        }

        // Show trial info if available
        if (packageItem.hasFreeTrial && currentPackage == null) {
            binding.layoutTrialInfo.visibility = View.VISIBLE
            binding.tvTrialDays.text = "${packageItem.freeTrialDays} days free trial"
        } else {
            binding.layoutTrialInfo.visibility = View.GONE
        }

        // Update features list
        updateFeaturesList(packageItem.featureList)
    }

    private fun updateFeaturesList(features: List<String>) {
        binding.layoutFeatures.removeAllViews()

        if (features.isEmpty()) {
            val textView = TextView(requireContext()).apply {
                text = "✓ No specific features listed"
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface_variant))
                setPadding(0, 8.dpToPx(), 0, 8.dpToPx())
            }
            binding.layoutFeatures.addView(textView)
        } else {
            features.forEach { feature ->
                val textView = TextView(requireContext()).apply {
                    text = "✓ $feature"
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface_variant))
                    setPadding(0, 8.dpToPx(), 0, 8.dpToPx())
                }
                binding.layoutFeatures.addView(textView)
            }
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun proceedWithPackage(packageItem: SubscriptionPackage) {
        shop?.let { shop ->
            when {
                packageItem.hasFreeTrial && currentPackage == null -> {
                    // Activate free trial
                    showTrialConfirmationDialog(packageItem, shop)
                }
                packageItem.isCustom -> {
                    // Custom/Enterprise plan
                    showContactDialog(packageItem)
                }
                else -> {
                    // ✅ PAID SUBSCRIPTION - Show Payment Dialog Fragment
                    showPaymentDialog(packageItem, shop)
                }
            }
        } ?: run {
            Toast.makeText(
                requireContext(),
                "Shop information is required",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ✅ NEW METHOD: Show PaymentDialogFragment
    private fun showPaymentDialog(packageItem: SubscriptionPackage, shop: Shop) {
        val dialog = PaymentDialogFragment.newInstance(
            shopId = shop.id,
            packageId = packageItem.id,
            packageName = packageItem.displayName,
            amount = packageItem.price
        )

        dialog.setPaymentListener { phoneNumber, packageId, months ->
            createSubscription(shop.id, packageId, phoneNumber)
        }

        dialog.show(parentFragmentManager, PaymentDialogFragment.TAG)
    }

    private fun showTrialConfirmationDialog(packageItem: SubscriptionPackage, shop: Shop) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Start Free Trial")
            .setMessage("Start your ${packageItem.freeTrialDays}-day free trial of ${packageItem.displayName}? You can upgrade anytime.")
            .setPositiveButton("Start Trial") { _, _ ->
                activateFreeTrial(shop.id, packageItem.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showContactDialog(packageItem: SubscriptionPackage) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Contact Sales")
            .setMessage("${packageItem.displayName} requires custom pricing. Please contact our sales team for more information.")
            .setPositiveButton("Contact Sales") { _, _ ->
                openContactSales(packageItem)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openContactSales(packageItem: SubscriptionPackage) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:sales@osebo.ai".toUri()
            putExtra(Intent.EXTRA_SUBJECT, "Custom Plan Inquiry - ${packageItem.displayName}")
            putExtra(Intent.EXTRA_TEXT, buildString {
                appendLine("Hello,")
                appendLine("\nI'm interested in the ${packageItem.displayName} plan.")
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

    // ✅ REMOVED: showPaymentMethodDialog and showPhoneNumberDialog - replaced by PaymentDialogFragment

    // After successfully activating subscription or trial
    private fun activateFreeTrial(shopId: String, packageId: String) {
        lifecycleScope.launch {
            viewModel.activateFreeTrial(shopId, packageId)

            // After trial is activated, refresh the navigation menu in MainActivity
            (requireActivity() as? MainActivity)?.refreshNavigationMenu()
        }
    }

    private fun createSubscription(
        shopId: String,
        packageId: String,
        phoneNumber: String
    ) {
        lifecycleScope.launch {
            viewModel.createSubscription(shopId, packageId, phoneNumber, 1)

            // Observe the result and refresh menu when successful
            viewModel.subscriptionResult.observe(viewLifecycleOwner) { resource ->
                when (resource) {
                    is Resource.Success -> {
                        if (resource.data?.success == true) {
                            // Refresh navigation menu
                            (requireActivity() as? MainActivity)?.refreshNavigationMenu()
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun navigateToPayment(transactionId: String) {
        try {
            shop?.let { shop ->
                val selectedPackage = subscriptionPackageAdapter.getSelectedPackage()
                val amount = selectedPackage?.price?.toFloat() ?: 0.0f
                val currency = selectedPackage?.currency ?: "UGX"

                val action = SubscriptionPackagesFragmentDirections
                    .actionSubscriptionPackagesFragmentToPaymentStatusFragment(
                        transactionId = transactionId,
                        shopId = shop.id,
                        amount = amount,
                        currency = currency
                    )
                findNavController().navigate(action)
            }
        } catch (e: IllegalArgumentException) {
            Toast.makeText(
                requireContext(),
                "Payment initiated. Please check your phone to complete the transaction.",
                Toast.LENGTH_LONG
            ).show()
            findNavController().popBackStack()
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