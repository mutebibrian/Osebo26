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

@AndroidEntryPoint
class PaymentStatusFragment : Fragment() {

    private var _binding: FragmentPaymentStatusBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SubscriptionViewModel by viewModels()
    private val args: PaymentStatusFragmentArgs by navArgs()

    private lateinit var preferenceManager: PreferenceManager

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
            when (resource) {
                is Resource.Loading -> {
                    updateStatus("Checking...", R.color.yellow_500)
                }
                is Resource.Success -> {
                    val response = resource.data

                    if (response?.success == true) {
                        when (response.status.lowercase()) {
                            "completed", "success" -> {
                                updateStatus("Payment Completed!", R.color.green_500)
                                binding.ivStatusIcon.setImageResource(R.drawable.ic_check_circle)
                                binding.tvStatusMessage.text = "Your subscription has been activated successfully!"
                                showSuccessButtons()
                                stopPolling()

                                // CRITICAL: Refresh shop subscription status
                                refreshShopSubscription(args.shopId)
                            }
                            "pending" -> {
                                updateStatus("Payment Pending", R.color.yellow_500)
                                binding.tvStatusMessage.text = response.message ?: "Please complete payment on your phone"
                                binding.tvNextCheck.text = "Next check in ${response.nextPollSeconds ?: 5} seconds"
                            }
                            "failed", "cancelled" -> {
                                updateStatus("Payment ${response.status.replaceFirstChar { it.uppercase() }}", R.color.red_500)
                                binding.ivStatusIcon.setImageResource(R.drawable.ic_error)
                                binding.tvStatusMessage.text = response.message ?: "Payment failed. Please try again."
                                showRetryButton()
                                stopPolling()
                            }
                            else -> {
                                updateStatus("Unknown Status", R.color.gray_500)
                                binding.tvStatusMessage.text = response.message ?: "Unknown payment status"
                                showRetryButton()
                                stopPolling()
                            }
                        }
                    } else {
                        updateStatus("Error", R.color.red_500)
                        binding.tvStatusMessage.text = response?.message ?: "Payment verification failed"
                        showRetryButton()
                        stopPolling()
                    }
                }
                is Resource.Error -> {
                    updateStatus("Error", R.color.red_500)
                    binding.tvStatusMessage.text = resource.message ?: "Network error"
                    showRetryButton()
                    stopPolling()
                }
            }
        }

        // Observe shop subscription status - Using actual fields from response
        viewModel.shopSubscriptionStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { statusResponse ->
                        println("🔍 ShopSubscriptionStatus Response:")
                        println("   - success: ${statusResponse.success}")
                        println("   - status: ${statusResponse.status}")
                        println("   - isActive: ${statusResponse.isActive}")
                        println("   - subscriptionId: ${statusResponse.subscriptionId}")
                        println("   - daysRemaining: ${statusResponse.daysRemaining}")

                        // Check if subscription is active using the isActive field
                        if (statusResponse.isActive) {
                            binding.tvStatusMessage.text = "Subscription activated! Redirecting..."

                            // Update local preferences with subscription data
                            preferenceManager.saveSubscriptionStatus(
                                statusResponse.status.uppercase()
                            )

                            statusResponse.subscriptionId?.let {
                                preferenceManager.saveSubscriptionId(it)
                                preferenceManager.saveCurrentShopUuid(it)
                            }

                            statusResponse.subscription?.packageType?.let {
                                preferenceManager.saveSubscriptionType(it)
                            }

                            statusResponse.expiryDate?.let {
                                preferenceManager.saveSubscriptionExpiry(it)
                            }

                            // Save shop info
                            statusResponse.shopId?.let {
                                preferenceManager.saveCurrentShopId(it)
                            }

                            statusResponse.shopName?.let {
                                preferenceManager.saveCurrentShopName(it)
                            }

                            println("✅ Saved subscription to preferences")
                            preferenceManager.debugSubscriptionInfo()

                            // Navigate to dashboard after short delay
                            binding.btnViewSubscription.postDelayed({
                                navigateToDashboard()
                            }, 1500)
                        } else {
                            // Subscription not active yet
                            println("⚠️ Subscription not active yet. Status: ${statusResponse.status}")

                            // Show appropriate message based on status
                            when (statusResponse.status.lowercase()) {
                                "pending" -> {
                                    binding.tvStatusMessage.text = "Payment is still processing..."
                                }
                                "trial" -> {
                                    binding.tvStatusMessage.text = "Trial activated! Redirecting..."
                                    binding.btnViewSubscription.postDelayed({
                                        navigateToDashboard()
                                    }, 1500)
                                }
                                else -> {
                                    binding.tvStatusMessage.text = "Subscription is not active. Please try again."
                                }
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    println("⚠️ Error refreshing subscription: ${resource.message}")
                    binding.tvStatusMessage.text = "Could not verify subscription. Please check later."
                }
                else -> {}
            }
        }

        // Observe current active subscription as backup
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

                            // Update local preferences
                            preferenceManager.saveSubscriptionStatus(
                                if (subscription.isTrial) "TRIAL" else "ACTIVE"
                            )
                            preferenceManager.saveSubscriptionId(subscription.id)
                            preferenceManager.saveCurrentShopUuid(subscription.id)
                            preferenceManager.saveSubscriptionType(subscription.packageType)
                            subscription.endDate?.let { preferenceManager.saveSubscriptionExpiry(it) }

                            println("✅ Saved subscription to preferences from currentSubscription")
                            preferenceManager.debugSubscriptionInfo()

                            // Navigate to dashboard
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

        // First check shop subscription status
        viewModel.checkShopSubscription(shopId)

        // Also get the active subscription to ensure it's updated in local storage
        viewModel.getShopActiveSubscription(shopId)
    }

    private fun navigateToDashboard() {
        try {
            // Try to navigate to main dashboard
            val action = PaymentStatusFragmentDirections.actionPaymentStatusFragmentToMainDashboard()
            findNavController().navigate(action)
        } catch (e: Exception) {
            println("⚠️ Navigation error: ${e.message}")

            // Fallback: Try to navigate to shop dashboard or pop back stack
            try {
                findNavController().popBackStack(R.id.mainDashboardFragment, false)
            } catch (e2: Exception) {
                // Last resort: just go back
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
        viewModel.startPaymentPolling(args.transactionId, args.shopId)
    }

    private fun stopPolling() {
        viewModel.stopPaymentPolling()
    }

    private fun showSuccessButtons() {
        binding.btnViewSubscription.visibility = View.VISIBLE
        binding.btnTryAgain.visibility = View.GONE

        // FIX: Use pbPolling instead of progressBar
        binding.pbPolling.visibility = View.GONE
        binding.tvPollingMessage.visibility = View.GONE
        binding.tvNextCheck.visibility = View.GONE
    }

    private fun showRetryButton() {
        binding.btnTryAgain.visibility = View.VISIBLE
        binding.btnViewSubscription.visibility = View.GONE

        // FIX: Use pbPolling instead of progressBar
        binding.pbPolling.visibility = View.GONE
        binding.tvPollingMessage.visibility = View.GONE
        binding.tvNextCheck.visibility = View.GONE
    }
    private fun navigateBackToPackages() {
        try {
            findNavController().navigate(
                R.id.action_paymentStatusFragment_to_subscriptionPackagesFragment
            )
        } catch (e: Exception) {
            findNavController().popBackStack()
        }
    }

    private fun navigateToSubscriptionDetails() {
        try {
            // Navigate to subscription details with the shopId
            val action = PaymentStatusFragmentDirections
                .actionPaymentStatusFragmentToSubscriptionDetailsFragment(
                    shopId = args.shopId,
                    subscriptionId = null,
                    subscription = null
                )
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