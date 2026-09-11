package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.SubscriptionPackageAdapter
import com.devbrian.osebo.databinding.FragmentSubscriptionOverviewBinding
import com.devbrian.osebo.models.RenewSubscriptionRequest
import com.devbrian.osebo.models.Subscription
import com.devbrian.osebo.models.SubscriptionPackage
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionOverviewFragment : Fragment() {

    private var _binding: FragmentSubscriptionOverviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubscriptionViewModel by viewModel()
    private lateinit var packageAdapter: SubscriptionPackageAdapter
    private var shopId: String = ""
    private var currentSubscription: Subscription? = null
    private var selectedPackages: List<SubscriptionPackage> = emptyList()
    private var canActivateTrial: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shopId = getCurrentShopId()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        loadSubscriptionData()
    }

    private fun setupRecyclerView() {
        packageAdapter = SubscriptionPackageAdapter { selected ->
            selectedPackages = selected
        }

        binding.plansRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = packageAdapter
        }
    }

    private fun setupObservers() {
        // Drives the "Active Bundles" overview card and the "Current Bundle Access" table —
        // matches web's Shop Billing screen
        viewModel.shopSubscriptionStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val data = resource.data
                    binding.progressBar.visibility = View.GONE

                    canActivateTrial = data?.canActivate ?: true

                    val activeBundleCount = data?.subscription?.let { 1 } ?: 0
                    binding.tvActiveBundles.text = activeBundleCount.toString()

                    if (data?.isActive == true && data.subscription != null) {
                        currentSubscription = data.subscription
                        binding.noSubscriptionLayout.visibility = View.GONE
                        binding.subscriptionDetailsLayout.visibility = View.VISIBLE
                        binding.bundleAccessCard.visibility = View.VISIBLE
                        binding.topActionButtonsRow.visibility = View.VISIBLE
                        displaySubscriptionData(data.subscription)
                    } else {
                        currentSubscription = null
                        binding.noSubscriptionLayout.visibility = View.VISIBLE
                        binding.subscriptionDetailsLayout.visibility = View.GONE
                        binding.bundleAccessCard.visibility = View.GONE
                        binding.topActionButtonsRow.visibility = View.GONE
                        binding.subscribeNowButton.text =
                            if (canActivateTrial) "Start Free Trial" else "Manage Bundles"
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showNoSubscriptionState()
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

        viewModel.subscriptionPackages.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val packages = resource.data ?: emptyList()
                    packageAdapter.submitList(packages)
                    binding.errorText.visibility = View.GONE
                    binding.plansRecyclerView.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    binding.errorText.text = resource.message ?: "Failed to load plans"
                    binding.errorText.visibility = View.VISIBLE
                    binding.plansRecyclerView.visibility = View.GONE
                }
                is Resource.Loading -> {}
            }
        }

        viewModel.subscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val response = resource.data
                    if (!response?.paymentId.isNullOrBlank()) {
                        showSnackbar("Payment initiated! Please check your phone.")
                    } else {
                        showSnackbar(response?.message ?: "Subscription created successfully")
                    }
                    loadSubscriptionData()
                }
                is Resource.Error -> {
                    showSnackbar(resource.message ?: "Failed to create subscription")
                }
                is Resource.Loading -> {}
            }
        }

        viewModel.renewSubscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    showSnackbar("Renewal payment initiated! Please check your phone.")
                    loadSubscriptionData()
                }
                is Resource.Error -> {
                    showSnackbar(resource.message ?: "Failed to renew subscription")
                }
                is Resource.Loading -> {}
            }
        }

        viewModel.cancelSubscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    showSnackbar("Renewal cancelled")
                    // cancelSubscription() refreshes a different LiveData internally
                    // (currentSubscription, not shopSubscriptionStatus) - reload the one
                    // this screen actually observes so the table/overview reflect it.
                    loadSubscriptionData()
                }
                is Resource.Error -> {
                    showSnackbar(resource.message ?: "Failed to cancel renewal")
                }
                is Resource.Loading -> {}
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let { showSnackbar(it) }
        }
    }

    private fun displaySubscriptionData(subscription: Subscription) {
        binding.tvShopName.text = getCurrentShopName()
        binding.planNameTextView.text = subscription.displayPackage ?: subscription.packageType
        binding.priceTextView.text = if (subscription.isTrial) "Free Trial" else subscription.monthlyPrice

        val expiryDate = formatDateForDisplay(subscription.endDate)
        binding.expiresValue.text = expiryDate

        val daysLeft = subscription.daysRemaining
        val daysColor = when {
            daysLeft < 3 -> R.color.error_red
            daysLeft < 7 -> R.color.warning_orange
            else -> R.color.success_green
        }
        binding.daysLeftValue.text = subscription.displayDaysRemaining
        binding.daysLeftValue.setTextColor(ContextCompat.getColor(requireContext(), daysColor))

        binding.statusChip.text = subscription.displayStatus
        val statusColor = when {
            subscription.isTrialActive -> R.color.info_blue
            subscription.isActiveStatus -> R.color.success_green
            subscription.isExpired -> R.color.error_red
            subscription.isPending -> R.color.warning_orange
            else -> R.color.gray
        }
        binding.statusChip.setChipBackgroundColorResource(statusColor)

        // Current Bundle Access table row
        binding.tvBundleName.text = subscription.displayPackage
        binding.tvBundleType.text = subscription.packageTier.uppercase().ifEmpty { "BASE" }
        binding.tvBundleEnds.text = expiryDate
        binding.tvBundleDaysLeft.text = subscription.displayDaysRemaining
        binding.tvBundleMonthlyRate.text = if (subscription.isTrial) "Free Trial" else subscription.formattedAmount
        binding.tvMonthlyTotalCost.text = if (subscription.isTrial) "Free Trial" else subscription.formattedAmount

        binding.btnCancelRenewal.visibility =
            if (subscription.isActiveStatus || subscription.isTrialActive) View.VISIBLE else View.GONE
    }

    private fun showNoSubscriptionState() {
        binding.noSubscriptionLayout.visibility = View.VISIBLE
        binding.subscriptionDetailsLayout.visibility = View.GONE
        binding.bundleAccessCard.visibility = View.GONE
        binding.topActionButtonsRow.visibility = View.GONE
        binding.tvShopName.text = getCurrentShopName()
        binding.subscribeNowButton.text = if (canActivateTrial) "Start Free Trial" else "Manage Bundles"
    }

    private fun loadSubscriptionData() {
        if (shopId.isNotEmpty()) {
            viewModel.checkShopSubscription(shopId)
        }
        viewModel.loadSubscriptionPackages()
    }

    private fun setupClickListeners() {
        binding.tabOverviewButton.setOnClickListener {
            // Already on Overview - no-op, kept as a visible active tab
        }

        binding.tabHistoryButton.setOnClickListener {
            findNavController().navigate(
                R.id.paymentHistoryFragment,
                bundleOf("subscriptionId" to currentSubscription?.id)
            )
        }

        binding.btnManageBundles.setOnClickListener {
            binding.plansRecyclerView.smoothScrollToPosition(0)
        }

        binding.btnRenewBundlesActive.setOnClickListener {
            currentSubscription?.let { showRenewPaymentDialog(it) } ?: showSnackbar("No active subscription to renew")
        }

        binding.btnCancelRenewal.setOnClickListener {
            currentSubscription?.let { showCancelRenewalDialog(it) }
        }

        // "Start Free Trial" / "Manage Bundles" — both scroll to bundle picker,
        // matching the web app's flow into Choose Bundles
        binding.subscribeNowButton.setOnClickListener {
            binding.plansRecyclerView.smoothScrollToPosition(0)
        }

        binding.contactSalesButton.setOnClickListener {
            openContactSales()
        }

        binding.retryButton?.setOnClickListener {
            loadSubscriptionData()
        }

        binding.btnCheckoutSelected?.setOnClickListener {
            proceedWithSelection()
        }
    }

    private fun proceedWithSelection() {
        if (shopId.isEmpty()) {
            Toast.makeText(requireContext(), "No shop selected", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedPackages.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one bundle", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedPackages.any { it.isCustom }) {
            openContactSales()
            return
        }
        showPaymentDialog(selectedPackages)
    }

    private fun showRenewPaymentDialog(subscription: Subscription) {
        if (shopId.isEmpty()) {
            showSnackbar("No shop selected")
            return
        }
        val dialog = PaymentDialogFragment.newInstance(
            shopId = shopId,
            packageId = subscription.actualPackageId,
            packageName = subscription.displayPackage,
            amount = subscription.amount
        )
        dialog.setPaymentListener { phoneNumber, _, months, _ ->
            viewModel.renewSubscription(
                shopId,
                RenewSubscriptionRequest(phoneNumber = phoneNumber, months = months, autoRenew = true)
            )
        }
        dialog.show(parentFragmentManager, "RenewPaymentDialog")
    }

    private fun showCancelRenewalDialog(subscription: Subscription) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cancel Renewal")
            .setMessage("This stops ${subscription.displayPackage} from renewing automatically. You'll keep access until ${formatDateForDisplay(subscription.endDate)}.")
            .setPositiveButton("Cancel Renewal") { _, _ ->
                if (shopId.isNotEmpty() && subscription.id.isNotEmpty()) {
                    viewModel.cancelSubscription(shopId, subscription.id)
                }
            }
            .setNegativeButton("Keep Renewal", null)
            .show()
    }

    private fun showPaymentDialog(packages: List<SubscriptionPackage>) {
        val totalAmount = packages.sumOf { it.price }
        val dialog = PaymentDialogFragment.newInstance(
            shopId = shopId,
            packageId = packages.joinToString(",") { it.id },
            packageName = packages.joinToString(", ") { it.displayName },
            amount = totalAmount
        )
        dialog.setPaymentListener { phoneNumber, _, months, _ ->
            viewModel.createSubscription(
                shopId = shopId,
                packageIds = packages.map { it.id },
                phoneNumber = phoneNumber,
                months = months
            )
        }
        dialog.show(parentFragmentManager, "PaymentDialog")
    }

    private fun openContactSales() {
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = "mailto:support@osebo.ai".toUri()
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Custom Plan Inquiry")
            putExtra(android.content.Intent.EXTRA_TEXT, buildString {
                append("Hello,\n\nI'm interested in a custom subscription plan.\n\nShop ID: $shopId\nPlease contact me with more information.")
            })
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatDateForDisplay(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A"
        return try {
            val inputFormat = if (dateString.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") }
            } else SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(inputFormat.parse(dateString)!!)
        } catch (e: Exception) { dateString }
    }

    private fun getCurrentShopId(): String {
        val prefs = requireContext().getSharedPreferences("OseboPrefs", android.content.Context.MODE_PRIVATE)
        return prefs.getString("current_shop_id", "") ?: ""
    }

    private fun getCurrentShopName(): String {
        val prefs = requireContext().getSharedPreferences("OseboPrefs", android.content.Context.MODE_PRIVATE)
        return prefs.getString("current_shop_name", "") ?: ""
    }

    private fun showSnackbar(message: String) = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
