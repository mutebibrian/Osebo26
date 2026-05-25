package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentPaymentStatusBinding
import com.devbrian.osebo.fragments.subscription.PaymentStatusFragmentArgs
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import com.devbrian.osebo.data.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PaymentStatusFragment : Fragment() {

    private var _binding: FragmentPaymentStatusBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SubscriptionViewModel by viewModels()
    private val args: PaymentStatusFragmentArgs by navArgs()

    private lateinit var preferenceManager: PreferenceManager
    private var isPollingActive = true
    private var pollCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager.getInstance(requireContext())

        setupUI()
        setupClickListeners()
        observeViewModel()
        startPolling()
    }

    private fun setupUI() {
        binding.toolbar.title = "Payment Status"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvTransactionId.text = "Transaction ID: ${args.transactionId.take(12)}..."
        binding.tvAmount.text = "Amount: ${args.currency} ${String.format("%,.0f", args.amount)}"

        // Show initial status
        updateStatus("Payment Pending", R.color.yellow_500)
        binding.tvStatusMessage.text = "Please check your phone and enter your PIN when prompted"
        binding.tvPollingMessage.text = "Waiting for payment confirmation..."

        println("🔍 PaymentStatusFragment: Started with transactionId=${args.transactionId}, shopId=${args.shopId}")
    }

    private fun setupClickListeners() {
        binding.btnTryAgain.setOnClickListener {
            navigateBackToPackages()
        }

        binding.btnViewSubscription.setOnClickListener {
            navigateToSubscriptionDetails()
        }
    }

    private fun observeViewModel() {
        // Observe payment polling status
        viewModel.paymentPollingStatus.observe(viewLifecycleOwner) { resource ->
            println("🔔 PaymentStatus: paymentPollingStatus = $resource")

            when (resource) {
                is Resource.Loading -> {
                    updateStatus("Processing...", R.color.yellow_500)
                }
                is Resource.Success -> {
                    val response = resource.data
                    println("🔔 PaymentStatus: Success response = $response")
                    println("🔔 PaymentStatus: status = ${response?.status}")
                    println("🔔 PaymentStatus: success = ${response?.success}")

                    if (response?.success == true) {
                        when (response.status.lowercase()) {
                            "completed", "success" -> {
                                println("✅ PaymentStatus: Payment COMPLETED!")
                                updateStatus("Payment Completed!", R.color.green_500)
                                binding.ivStatusIcon.setImageResource(R.drawable.ic_check_circle)
                                binding.tvStatusMessage.text = "Your subscription has been activated successfully!"
                                binding.tvPollingMessage.text = "Subscription activated!"
                                showSuccessButtons()
                                stopPolling()

                                // Refresh subscription status
                                refreshShopSubscription(args.shopId)
                            }
                            "pending" -> {
                                println("⏳ PaymentStatus: Payment PENDING (poll #$pollCount)")
                                updateStatus("Payment Pending", R.color.yellow_500)
                                binding.tvStatusMessage.text = response.message ?: "Please complete payment on your phone"
                                val nextSeconds = response.nextPollSeconds
                                binding.tvNextCheck.text = "Next check in $nextSeconds seconds"
                                binding.tvPollingMessage.text = "Waiting for payment confirmation..."
                            }
                            "failed", "cancelled" -> {
                                println("❌ PaymentStatus: Payment ${response.status}")
                                updateStatus("Payment ${response.status.replaceFirstChar { it.uppercase() }}", R.color.red_500)
                                binding.ivStatusIcon.setImageResource(R.drawable.ic_error)
                                binding.tvStatusMessage.text = response.message ?: "Payment failed. Please try again."
                                showRetryButton()
                                stopPolling()
                            }
                            "timeout" -> {
                                println("⏰ PaymentStatus: Payment TIMEOUT")
                                updateStatus("Payment Timeout", R.color.red_500)
                                binding.tvStatusMessage.text = "Payment took too long. Please try again."
                                showRetryButton()
                                stopPolling()
                            }
                            else -> {
                                println("❓ PaymentStatus: Unknown status = ${response.status}")
                                updateStatus("Unknown Status", R.color.gray_500)
                                binding.tvStatusMessage.text = response.message ?: "Unknown payment status"
                                showRetryButton()
                                stopPolling()
                            }
                        }
                    } else {
                        println("❌ PaymentStatus: API returned success=false - ${response?.message}")
                        updateStatus("Error", R.color.red_500)
                        binding.tvStatusMessage.text = response?.message ?: "Payment verification failed"
                        showRetryButton()
                        stopPolling()
                    }
                }
                is Resource.Error -> {
                    println("❌ PaymentStatus: Error - ${resource.message}")
                    updateStatus("Error", R.color.red_500)
                    binding.tvStatusMessage.text = resource.message ?: "Network error. Please check your connection."
                    showRetryButton()
                    stopPolling()
                }
            }
        }

        // Observe shop subscription status
        viewModel.shopSubscriptionStatus.observe(viewLifecycleOwner) { resource ->
            println("🔔 PaymentStatus: shopSubscriptionStatus = $resource")

            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { statusResponse ->
                        println("🔍 ShopSubscriptionStatus Response:")
                        println("   - success: ${statusResponse.success}")
                        println("   - status: ${statusResponse.status}")
                        println("   - isActive: ${statusResponse.isActive}")
                        println("   - subscriptionId: ${statusResponse.subscriptionId}")
                        println("   - type: ${statusResponse.type}")
                        println("   - expiryDate: ${statusResponse.expiryDate}")

                        if (statusResponse.isActive == true) {
                            binding.tvStatusMessage.text = "Subscription activated! Redirecting..."

                            // Save subscription details to preferences
                            val subscriptionStatus = when {
                                statusResponse.type.equals("trial", ignoreCase = true) -> "TRIAL"
                                statusResponse.isActive -> "ACTIVE"
                                else -> statusResponse.status.uppercase()
                            }

                            preferenceManager.saveSubscriptionStatus(subscriptionStatus)
                            statusResponse.subscriptionId?.let {
                                preferenceManager.saveSubscriptionId(it)
                                preferenceManager.saveCurrentShopUuid(it)
                            }
                            statusResponse.type?.let { preferenceManager.saveSubscriptionType(it) }
                            statusResponse.expiryDate?.let { preferenceManager.saveSubscriptionExpiry(it) }
                            statusResponse.shopId?.let { preferenceManager.saveCurrentShopId(it) }

                            println("✅ Saved subscription to preferences: $subscriptionStatus")
                            preferenceManager.debugSubscriptionInfo()

                            // Navigate to dashboard after delay
                            binding.btnViewSubscription.postDelayed({
                                navigateToDashboard()
                            }, 1500)
                        } else {
                            println("⚠️ Subscription not active yet. Status: ${statusResponse.status}")
                            when (statusResponse.status.lowercase()) {
                                "trial" -> {
                                    binding.tvStatusMessage.text = "Trial activated! Redirecting..."
                                    binding.btnViewSubscription.postDelayed({
                                        navigateToDashboard()
                                    }, 1500)
                                }
                                else -> {
                                    binding.tvStatusMessage.text = "Subscription status: ${statusResponse.status}"
                                }
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    println("⚠️ Error refreshing subscription: ${resource.message}")
                }
                else -> {}
            }
        }

        // Observe current subscription as fallback
        viewModel.currentSubscription.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { subscription ->
                        println("🔍 Active subscription loaded:")
                        println("   - id: ${subscription.id}")
                        println("   - status: ${subscription.status}")
                        println("   - package: ${subscription.packageType}")
                        println("   - isActiveStatus: ${subscription.isActiveStatus}")

                        if (subscription.isActiveStatus) {
                            binding.tvStatusMessage.text = "Subscription activated! Redirecting..."

                            preferenceManager.saveSubscriptionStatus(
                                if (subscription.isTrial) "TRIAL" else "ACTIVE"
                            )
                            preferenceManager.saveSubscriptionId(subscription.id)
                            preferenceManager.saveCurrentShopUuid(subscription.id)
                            preferenceManager.saveSubscriptionType(subscription.packageType)
                            subscription.endDate?.let { preferenceManager.saveSubscriptionExpiry(it) }

                            binding.btnViewSubscription.postDelayed({
                                navigateToDashboard()
                            }, 1500)
                        }
                    }
                }
                is Resource.Error -> {
                    println("⚠️ Error loading active subscription: ${resource.message}")
                }
                else -> {}
            }
        }
    }

    private fun refreshShopSubscription(shopId: String) {
        println("🔄 Refreshing shop subscription for shop: $shopId")

        // Check subscription status
        viewModel.checkShopSubscription(shopId)

        // Also get active subscription
        viewModel.getShopActiveSubscription(shopId)
    }

    private fun navigateToDashboard() {
        println("🔍 NAVIGATION: Attempting to navigate to dashboard")

        try {
            val action = PaymentStatusFragmentDirections.actionPaymentStatusFragmentToMainDashboard()
            println("🔍 NAVIGATION: Action created, executing...")
            findNavController().navigate(action)
            println("✅ NAVIGATION: Navigation successful")
        } catch (e: Exception) {
            println("❌ NAVIGATION: Error - ${e.message}")
            e.printStackTrace()

            // Fallback: pop back to main
            try {
                findNavController().popBackStack(R.id.mainDashboardFragment, false)
            } catch (e2: Exception) {
                findNavController().navigateUp()
            }
        }
    }

    private fun updateStatus(title: String, colorRes: Int) {
        binding.tvStatusTitle.text = title
        binding.tvStatusTitle.setTextColor(requireContext().getColor(colorRes))
        binding.ivStatusIcon.setColorFilter(requireContext().getColor(colorRes))
    }

    private fun startPolling() {
        println("🚀 PaymentStatus: Starting payment polling for transaction: ${args.transactionId}")
        isPollingActive = true
        pollCount = 0

        // Start ViewModel polling
        viewModel.startPaymentPolling(args.transactionId, args.shopId)

        // Also start manual polling as backup
        startManualPolling()
    }

    private fun startManualPolling() {
        CoroutineScope(Dispatchers.Main).launch {
            while (isPollingActive && pollCount < 60) { // Max 60 attempts (about 5 minutes)
                delay(5000) // Check every 5 seconds
                pollCount++

                if (!isPollingActive) break

                binding.tvNextCheck.text = "Checking... ($pollCount/60)"
                println("🔄 Manual poll #$pollCount for transaction: ${args.transactionId}")

                // Call API to check payment status
                viewModel.checkPaymentStatus(args.transactionId)
            }

            if (pollCount >= 60 && isPollingActive) {
                println("⏰ Manual polling timeout after 60 attempts")
                updateStatus("Payment Timeout", R.color.red_500)
                binding.tvStatusMessage.text = "Payment confirmation timed out. Please check your subscription status later."
                showRetryButton()
                isPollingActive = false
            }
        }
    }

    private fun stopPolling() {
        println("🛑 PaymentStatus: Stopping payment polling")
        isPollingActive = false
        viewModel.stopPaymentPolling()
    }

    private fun showSuccessButtons() {
        binding.btnViewSubscription.visibility = View.VISIBLE
        binding.btnTryAgain.visibility = View.GONE
        binding.pbPolling.visibility = View.GONE
        binding.tvPollingMessage.visibility = View.GONE
        binding.tvNextCheck.visibility = View.GONE
    }

    private fun showRetryButton() {
        binding.btnTryAgain.visibility = View.VISIBLE
        binding.btnViewSubscription.visibility = View.GONE
        binding.pbPolling.visibility = View.GONE
        binding.tvPollingMessage.visibility = View.GONE
        binding.tvNextCheck.visibility = View.GONE
    }

    private fun navigateBackToPackages() {
        try {
            val action = PaymentStatusFragmentDirections.actionPaymentStatusFragmentToSubscriptionPackagesFragment()
            findNavController().navigate(action)
        } catch (e: Exception) {
            findNavController().popBackStack()
        }
    }

    private fun navigateToSubscriptionDetails() {
        try {
            val action = PaymentStatusFragmentDirections
                .actionPaymentStatusFragmentToSubscriptionDetailsFragment()
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Could not load subscription details", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPolling()
        _binding = null
    }
}