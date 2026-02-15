package com.devbrian.osebo.fragments.subscription

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.PaymentHistoryAdapter
import com.devbrian.osebo.databinding.FragmentSubscriptionDetailsBinding
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.models.subscription.Subscription
import com.devbrian.osebo.utils.Resource
import com.devbrian.osebo.viewmodels.SubscriptionViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubscriptionDetailsFragment : Fragment() {

    private var _binding: FragmentSubscriptionDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubscriptionViewModel by viewModels()
    private lateinit var paymentHistoryAdapter: PaymentHistoryAdapter

    private var shop: Shop? = null
    private var subscription: Subscription? = null
    private var subscriptionId: String? = null

    private val args: SubscriptionDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get data from navigation arguments
        shop = args.shop
        subscription = args.subscription
        subscriptionId = args.subscriptionId

        setupUI()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadSubscriptionDetails()
    }

    private fun setupUI() {
        binding.toolbar.title = "Subscription Details"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Set shop info if available
        shop?.let {
            binding.toolbar.subtitle = it.name
        }
    }

    private fun setupRecyclerView() {
        paymentHistoryAdapter = PaymentHistoryAdapter()
        binding.rvPaymentHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = paymentHistoryAdapter
            setHasFixedSize(true)
        }

        paymentHistoryAdapter.setOnItemClickListener { payment ->
            showPaymentDetailsDialog(payment)
        }
    }

    private fun setupClickListeners() {
        binding.btnUpgrade.setOnClickListener {
            navigateToUpgrade()
        }

        binding.btnCancel.setOnClickListener {
            showCancelConfirmationDialog()
        }

        binding.btnViewAllPayments.setOnClickListener {
            navigateToPaymentHistory()
        }

        binding.btnManageSubscription.setOnClickListener {
            showManageOptionsDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.subscriptionDetails.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                    binding.layoutContent.visibility = View.GONE
                }
                is Resource.Success -> {
                    showLoading(false)
                    resource.data?.let { subscription ->
                        this.subscription = subscription
                        binding.layoutContent.visibility = View.VISIBLE
                        updateUI(subscription)
                        // Load payment history
                        loadPaymentHistory(subscription.id)
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Failed to load subscription details",
                        Toast.LENGTH_SHORT
                    ).show()
                    showErrorState()
                }
            }
        }

        viewModel.paymentHistory.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.rvPaymentHistory.visibility = View.GONE
                    binding.progressBarPayments.visibility = View.VISIBLE
                    binding.tvNoPayments.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBarPayments.visibility = View.GONE
                    resource.data?.let { payments ->
                        if (payments.isEmpty()) {
                            binding.rvPaymentHistory.visibility = View.GONE
                            binding.tvNoPayments.visibility = View.VISIBLE
                            binding.btnViewAllPayments.visibility = View.GONE
                        } else {
                            binding.rvPaymentHistory.visibility = View.VISIBLE
                            binding.tvNoPayments.visibility = View.GONE
                            paymentHistoryAdapter.submitList(payments.take(3)) // Show last 3
                            if (payments.size > 3) {
                                binding.btnViewAllPayments.visibility = View.VISIBLE
                            } else {
                                binding.btnViewAllPayments.visibility = View.GONE
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    binding.progressBarPayments.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Failed to load payment history",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        viewModel.cancelSubscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showCancelLoading(true)
                }
                is Resource.Success -> {
                    showCancelLoading(false)
                    Toast.makeText(requireContext(), "Subscription cancelled successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    showCancelLoading(false)
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Failed to cancel subscription",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        viewModel.renewSubscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show renew loading
                }
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Subscription renewed successfully", Toast.LENGTH_SHORT).show()
                    // Refresh subscription details
                    loadSubscriptionDetails()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Failed to renew subscription",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadSubscriptionDetails() {
        // If we have subscription object directly, use it
        subscription?.let {
            updateUI(it)
            loadPaymentHistory(it.id)
            return
        }

        // Otherwise load from API
        subscriptionId?.let { id ->
            lifecycleScope.launch {
                viewModel.getSubscriptionDetails(id)
            }
        } ?: run {
            shop?.let {
                // Load active subscription for this shop
                lifecycleScope.launch {
                    viewModel.getShopActiveSubscription(it.id)
                }
            }
        }
    }

    private fun updateUI(subscription: Subscription) {
        this.subscription = subscription

        // Plan name and status
        binding.tvPlanName.text = subscription.displayPackage
        binding.tvPlanStatus.text = subscription.displayStatus

        // Status color
        val statusColor = when (subscription.status.uppercase()) {
            "ACTIVE" -> R.color.green_500
            "EXPIRED" -> R.color.red_500
            "PENDING" -> R.color.yellow_500
            "TRIAL" -> R.color.blue_500
            else -> R.color.gray_500
        }
        binding.tvPlanStatus.setTextColor(ContextCompat.getColor(requireContext(), statusColor))
        binding.tvPlanStatus.background = ContextCompat.getDrawable(
            requireContext(),
            when (subscription.status.uppercase()) {
                "ACTIVE" -> R.drawable.bg_status_active
                "TRIAL" -> R.drawable.bg_status_trial
                "EXPIRED" -> R.drawable.bg_status_expired
                "PENDING" -> R.drawable.bg_status_pending
                else -> R.drawable.bg_status_inactive
            }
        )

        // Plan icon based on package type
        val (iconRes, iconColor) = when (subscription.packageType) {
            "BASIC" -> Pair(R.drawable.ic_package_basic, R.color.blue_500)
            "PRO" -> Pair(R.drawable.ic_package_pro, R.color.purple_500)
            "POPULAR", "ENTERPRISE" -> Pair(R.drawable.ic_package_enterprise, R.color.orange_500)
            else -> Pair(R.drawable.ic_package_basic, R.color.blue_500)
        }
        binding.ivPlanIcon.setImageResource(iconRes)
        binding.ivPlanIcon.imageTintList = android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), iconColor)
        )

        // Description
        val description = when (subscription.packageType) {
            "BASIC" -> "Perfect for small businesses getting started with inventory management. Includes 1 month free trial."
            "PRO" -> "Advanced features for growing businesses with analytics, reporting, and priority support."
            "POPULAR", "ENTERPRISE" -> "Enterprise solution with custom features, API access, and dedicated support."
            else -> "Subscription plan for your business"
        }
        binding.tvPlanDescription.text = description

        // Price
        binding.tvPlanPrice.text = if (subscription.isTrial) {
            "Free Trial"
        } else {
            "${subscription.currency} ${subscription.amount.toInt()}"
        }

        // Dates
        subscription.startDate?.let { startDate ->
            binding.tvStartDate.text = formatDate(startDate)
        } ?: run {
            binding.layoutStartDate.visibility = View.GONE
        }

        subscription.endDate?.let { endDate ->
            binding.tvRenewalDate.text = formatDate(endDate)
            binding.tvNextBilling.text = formatDate(endDate)
        } ?: run {
            binding.layoutRenewalDate.visibility = View.GONE
            binding.layoutNextBilling.visibility = View.GONE
        }

        // Trial info
        if (subscription.isTrial && subscription.trialEndsAt != null) {
            binding.layoutTrialInfo.visibility = View.VISIBLE
            binding.tvTrialEndsAt.text = formatDate(subscription.trialEndsAt)
        } else {
            binding.layoutTrialInfo.visibility = View.GONE
        }

        // Payment method
        subscription.paymentMethod?.let { method ->
            binding.tvPaymentMethod.text = when (method.uppercase()) {
                "MOBILE_MONEY" -> "Mobile Money"
                "CREDIT_CARD" -> "Credit Card"
                "BANK_TRANSFER" -> "Bank Transfer"
                else -> method
            }
        } ?: run {
            binding.layoutPaymentMethod.visibility = View.GONE
        }

        // Phone number
        subscription.phoneNumber?.let { phone ->
            binding.tvBillingPhone.text = phone
        } ?: run {
            binding.layoutBillingPhone.visibility = View.GONE
        }

        // Update button states based on status
        updateButtonStates(subscription)
    }

    private fun updateButtonStates(subscription: Subscription) {
        when (subscription.status.uppercase()) {
            "ACTIVE" -> {
                binding.btnUpgrade.text = "Upgrade Plan"
                binding.btnUpgrade.visibility = View.VISIBLE
                binding.btnCancel.text = "Cancel Subscription"
                binding.btnCancel.visibility = View.VISIBLE
                binding.btnManageSubscription.visibility = View.VISIBLE
            }
            "TRIAL" -> {
                binding.btnUpgrade.text = "Upgrade Now"
                binding.btnUpgrade.visibility = View.VISIBLE
                binding.btnCancel.text = "End Trial"
                binding.btnCancel.visibility = View.VISIBLE
                binding.btnManageSubscription.visibility = View.VISIBLE
            }
            "EXPIRED" -> {
                binding.btnUpgrade.text = "Renew Now"
                binding.btnUpgrade.visibility = View.VISIBLE
                binding.btnCancel.visibility = View.GONE
                binding.btnManageSubscription.visibility = View.GONE
            }
            "PENDING" -> {
                binding.btnUpgrade.text = "Complete Payment"
                binding.btnUpgrade.visibility = View.VISIBLE
                binding.btnCancel.text = "Cancel Payment"
                binding.btnCancel.visibility = View.VISIBLE
                binding.btnManageSubscription.visibility = View.GONE
            }
            else -> {
                binding.btnUpgrade.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
                binding.btnManageSubscription.visibility = View.GONE
            }
        }
    }

    private fun loadPaymentHistory(subscriptionId: String) {
        lifecycleScope.launch {
            viewModel.getPaymentHistory(subscriptionId)
        }
    }

    private fun navigateToUpgrade() {
        subscription?.let { currentSubscription ->
            when (currentSubscription.status.uppercase()) {
                "EXPIRED", "INACTIVE" -> {
                    // Navigate to package selection for renewal
                    val action = SubscriptionDetailsFragmentDirections
                        .actionSubscriptionDetailsFragmentToSubscriptionPackagesFragment(
                            shop = shop
                        )
                    findNavController().navigate(action)
                }
                "PENDING" -> {
                    // Navigate to payment completion
                    val action = SubscriptionDetailsFragmentDirections
                        .actionSubscriptionDetailsFragmentToPaymentStatusFragment(
                            transactionId = currentSubscription.transactionId ?: "",
                            shopId = currentSubscription.shopId,
                            amount = currentSubscription.amount,
                            currency = currentSubscription.currency
                        )
                    findNavController().navigate(action)
                }
                else -> {
                    // Navigate to package selection for upgrade
                    val action = SubscriptionDetailsFragmentDirections
                        .actionSubscriptionDetailsFragmentToSubscriptionPackagesFragment(
                            shop = shop,
                            currentPackage = currentSubscription.packageType
                        )
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun showCancelConfirmationDialog() {
        subscription?.let { sub ->
            val title = when (sub.status.uppercase()) {
                "TRIAL" -> "End Free Trial"
                "PENDING" -> "Cancel Payment"
                else -> "Cancel Subscription"
            }

            val message = when (sub.status.uppercase()) {
                "TRIAL" -> "Are you sure you want to end your free trial? You'll lose access to premium features immediately."
                "PENDING" -> "Are you sure you want to cancel this pending payment? Your subscription will not be activated."
                else -> "Are you sure you want to cancel your subscription? You'll continue to have access until the end of your current billing period (${formatDate(sub.endDate ?: "")})."
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes, Cancel") { _, _ ->
                    cancelSubscription()
                }
                .setNegativeButton("Keep It", null)
                .show()
        }
    }

    private fun cancelSubscription() {
        subscription?.let { sub ->
            lifecycleScope.launch {
                viewModel.cancelSubscription(sub.id)
            }
        }
    }

    private fun showManageOptionsDialog() {
        subscription?.let { sub ->
            val options = arrayOf(
                "Change Payment Method",
                "Update Billing Information",
                "Download Invoice",
                "View Usage Statistics"
            )

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Manage Subscription")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> showChangePaymentMethod()
                        1 -> updateBillingInfo()
                        2 -> downloadInvoice()
                        3 -> viewUsageStatistics()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showChangePaymentMethod() {
        Toast.makeText(requireContext(), "Change payment method feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun updateBillingInfo() {
        Toast.makeText(requireContext(), "Update billing info feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun downloadInvoice() {
        subscription?.let { sub ->
            // In a real app, this would download a PDF invoice
            Toast.makeText(requireContext(), "Downloading invoice...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun viewUsageStatistics() {
        shop?.let {
            val action = SubscriptionDetailsFragmentDirections
                .actionSubscriptionDetailsFragmentToUsageStatisticsFragment(
                    shopId = it.id
                )
            findNavController().navigate(action)
        }
    }

    private fun showPaymentDetailsDialog(payment: com.devbrian.osebo.models.subscription.Payment) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_payment_details, null)

        dialogView.findViewById<android.widget.TextView>(R.id.tvPaymentAmount).text =
            "${payment.currency} ${payment.amount}"
        dialogView.findViewById<android.widget.TextView>(R.id.tvPaymentStatus).text =
            payment.displayStatus
        dialogView.findViewById<android.widget.TextView>(R.id.tvPaymentDate).text =
            payment.paidAt ?: payment.createdAt
        dialogView.findViewById<android.widget.TextView>(R.id.tvPaymentMethod).text =
            payment.displayMethod
        dialogView.findViewById<android.widget.TextView>(R.id.tvTransactionId).text =
            payment.transactionId ?: "N/A"

        // Set status color
        val statusColor = when (payment.status) {
            "COMPLETED" -> R.color.green_500
            "PENDING" -> R.color.yellow_500
            "FAILED" -> R.color.red_500
            else -> R.color.gray_500
        }
        dialogView.findViewById<android.widget.TextView>(R.id.tvPaymentStatus)
            .setTextColor(ContextCompat.getColor(requireContext(), statusColor))

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Payment Details")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun navigateToPaymentHistory() {
        subscription?.let { sub ->
            val action = SubscriptionDetailsFragmentDirections
                .actionSubscriptionDetailsFragmentToPaymentHistoryFragment(
                    subscriptionId = sub.id
                )
            findNavController().navigate(action)
        }
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A"

        return try {
            // Simple formatting - in production, use SimpleDateFormat or LocalDateTime
            if (dateString.length >= 10) {
                val parts = dateString.substring(0, 10).split("-")
                if (parts.size == 3) {
                    "${parts[2]}/${parts[1]}/${parts[0]}" // DD/MM/YYYY format
                } else {
                    dateString
                }
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.layoutError.visibility = View.GONE
    }

    private fun showCancelLoading(show: Boolean) {
        // You can add a progress bar for cancel operation
        if (show) {
            Toast.makeText(requireContext(), "Cancelling subscription...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showErrorState() {
        binding.layoutContent.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.btnRetry.setOnClickListener {
            loadSubscriptionDetails()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}