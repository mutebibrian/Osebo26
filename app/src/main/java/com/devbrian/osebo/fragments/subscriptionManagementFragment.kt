package com.devbrian.osebo.fragments

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
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentSubscriptionManagementBinding
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.models.Subscription
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SubscriptionManagementFragment : Fragment() {

    private var _binding: FragmentSubscriptionManagementBinding? = null
    private val binding get() = _binding!!

    private val args: SubscriptionManagementFragmentArgs by navArgs()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()

    private var currentSubscription: Subscription? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupClickListeners()
        loadSubscriptionDetails()
        observeViewModels()
    }

    private fun setupToolbar() {
        // Check if toolbar exists in layout
        binding.toolbar.apply {
            title = "Manage Subscription"
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnRenewSubscription.setOnClickListener {
            showRenewDialog()
        }

        binding.btnUpgradeSubscription.setOnClickListener {
            navigateToUpgrade()
        }

        binding.btnCancelSubscription.setOnClickListener {
            showCancelConfirmationDialog()
        }

        binding.btnChangePaymentMethod.setOnClickListener {
            showChangePaymentDialog()
        }

        binding.btnViewBillingHistory.setOnClickListener {
            navigateToBillingHistory()
        }

        binding.btnEnableAutoRenew.setOnClickListener {
            toggleAutoRenew(true)
        }

        binding.btnDisableAutoRenew.setOnClickListener {
            toggleAutoRenew(false)
        }

        binding.btnSubscribeNow.setOnClickListener {
            navigateToSubscriptionPackages()
        }
    }

    private fun loadSubscriptionDetails() {
        lifecycleScope.launch {
            subscriptionViewModel.getShopActiveSubscription(args.shopId)

            subscriptionViewModel.currentSubscription.observe(viewLifecycleOwner) { resource ->
                when (resource) {
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        resource.data?.let { subscription ->
                            currentSubscription = subscription
                            displaySubscriptionDetails(subscription)
                        } ?: run {
                            showNoSubscriptionState()
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvError.text = resource.message ?: "Failed to load subscription details"
                        binding.tvError.visibility = View.VISIBLE
                        binding.layoutContent.visibility = View.GONE
                        binding.layoutNoSubscription.visibility = View.GONE
                    }
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvError.visibility = View.GONE
                        binding.layoutContent.visibility = View.GONE
                        binding.layoutNoSubscription.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun displaySubscriptionDetails(subscription: Subscription) {
        binding.layoutContent.visibility = View.VISIBLE
        binding.layoutNoSubscription.visibility = View.GONE
        binding.tvError.visibility = View.GONE

        // Basic Info
        binding.tvPlanName.text = subscription.displayPackage
        binding.tvPlanNameDetail.text = subscription.displayPackage
        binding.tvStatus.text = subscription.displayStatus.uppercase()
        binding.tvAmount.text = subscription.formattedAmount

        // Status color
        val statusColor = when {
            subscription.isActiveStatus -> R.color.success_green
            subscription.isExpired -> R.color.error_red
            subscription.isTrialActive -> R.color.warning_orange
            else -> R.color.gray
        }
        binding.tvStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), statusColor))
        binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

        // Dates
        binding.tvStartDate.text = formatDate(subscription.startDate)
        binding.tvEndDate.text = formatDate(subscription.endDate ?: subscription.trialEndsAt)
        binding.tvDaysRemaining.text = subscription.displayDaysRemaining

        // Days remaining color
        val daysColor = when {
            subscription.daysRemaining < 0 -> R.color.error_red
            subscription.daysRemaining <= 3 -> R.color.warning_orange
            else -> R.color.success_green
        }
        binding.tvDaysRemaining.setTextColor(ContextCompat.getColor(requireContext(), daysColor))

        // Payment Info
        binding.tvPaymentMethod.text = subscription.displayPaymentMethod
        binding.tvBillingCycle.text = subscription.durationText
        binding.tvLastPayment.text = formatDate(subscription.updatedAt)
        binding.tvNextBilling.text = formatDate(subscription.endDate)

        // Auto Renew
        if (subscription.autoRenew) {
            binding.layoutAutoRenewEnabled.visibility = View.VISIBLE
            binding.layoutAutoRenewDisabled.visibility = View.GONE
            binding.tvAutoRenewDate.text = "Next billing: ${formatDate(subscription.endDate)}"
        } else {
            binding.layoutAutoRenewEnabled.visibility = View.GONE
            binding.layoutAutoRenewDisabled.visibility = View.VISIBLE
        }
    }

    private fun showNoSubscriptionState() {
        binding.layoutContent.visibility = View.GONE
        binding.layoutNoSubscription.visibility = View.VISIBLE
        binding.tvError.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun showRenewDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Renew Subscription")
            .setMessage("Do you want to renew your subscription?")
            .setPositiveButton("Renew Now") { _, _ ->
                renewSubscription()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun renewSubscription() {
        currentSubscription?.let { subscription ->
            lifecycleScope.launch {
                Toast.makeText(requireContext(), "Processing renewal...", Toast.LENGTH_SHORT).show()
                // TODO: Implement renewal logic with subscriptionViewModel.renewSubscription()
            }
        } ?: run {
            Toast.makeText(requireContext(), "No active subscription to renew", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCancelConfirmationDialog() {
        if (currentSubscription == null) {
            Toast.makeText(requireContext(), "No active subscription to cancel", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Subscription")
            .setMessage("Are you sure you want to cancel your subscription? You will lose access to premium features at the end of your billing period.")
            .setPositiveButton("Cancel Subscription") { _, _ ->
                cancelSubscription()
            }
            .setNegativeButton("Keep Subscription", null)
            .show()
    }

    private fun cancelSubscription() {
        currentSubscription?.let { subscription ->
            lifecycleScope.launch {
                subscriptionViewModel.cancelSubscription(args.shopId, subscription.id)

                subscriptionViewModel.cancelSubscriptionResult.observe(viewLifecycleOwner) { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            Toast.makeText(requireContext(), "Subscription cancelled successfully", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        is Resource.Error -> {
                            Toast.makeText(requireContext(), resource.message ?: "Failed to cancel", Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun showChangePaymentDialog() {
        Toast.makeText(requireContext(), "Change Payment Method", Toast.LENGTH_SHORT).show()
        // TODO: Implement payment method change dialog
    }

    private fun toggleAutoRenew(enable: Boolean) {
        currentSubscription?.let { subscription ->
            lifecycleScope.launch {
                // TODO: Implement auto-renew toggle in ViewModel
                val message = if (enable) "Auto-renew enabled" else "Auto-renew disabled"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                loadSubscriptionDetails() // Refresh
            }
        } ?: run {
            Toast.makeText(requireContext(), "No active subscription", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToUpgrade() {
        try {
            if (currentSubscription != null) {
                // Create a shop object with current subscription info
                val shop = Shop(
                    id = args.shopId,
                    name = "",
                    subscriptionStatus = currentSubscription?.status ?: "inactive",
                    subscriptionType = currentSubscription?.packageType
                )

                val action = SubscriptionManagementFragmentDirections
                    .actionSubscriptionManagementFragmentToSubscriptionPackagesFragment(
                        shop,
                        currentSubscription?.packageType ?: ""
                    )
                findNavController().navigate(action)
            } else {
                navigateToSubscriptionPackages()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToBillingHistory() {
        Toast.makeText(requireContext(), "View Billing History", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to billing history fragment
    }

    private fun navigateToSubscriptionPackages() {
        // Create a shop object with the shopId - Shop class has default values
        val shop = Shop(
            id = args.shopId,
            name = "",  // Will be loaded in the packages fragment
            subscriptionStatus = "inactive"
        )

        try {
            val action = SubscriptionManagementFragmentDirections
                .actionSubscriptionManagementFragmentToSubscriptionPackagesFragment(shop, "")
            findNavController().navigate(action)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModels() {
        // Observe cancel subscription result if needed
        subscriptionViewModel.cancelSubscriptionResult.observe(viewLifecycleOwner) { resource ->
            // Already handled in cancelSubscription()
        }
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A"

        return try {
            val inputFormat = if (dateString.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            }

            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}