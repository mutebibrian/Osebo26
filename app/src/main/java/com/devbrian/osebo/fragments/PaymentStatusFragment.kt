package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentPaymentStatusBinding
import com.devbrian.osebo.fragments.subscription.PaymentStatusFragmentArgs
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentStatusFragment : Fragment() {

    private var _binding: FragmentPaymentStatusBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SubscriptionViewModel by viewModels()
    private val args: PaymentStatusFragmentArgs by navArgs()

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
        binding.tvAmount.text = "Amount: ${args.currency} ${args.amount}"
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
                            }
                            "pending" -> {
                                updateStatus("Payment Pending", R.color.yellow_500)
                                binding.tvStatusMessage.text = response.message ?: "Please complete payment on your phone"
                                binding.tvNextCheck.text = "Next check in ${response.nextPollSeconds} seconds"
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

        viewModel.isPolling.observe(viewLifecycleOwner) { isPolling ->
            binding.pbPolling.visibility = if (isPolling) View.VISIBLE else View.GONE
            binding.tvPollingMessage.visibility = if (isPolling) View.VISIBLE else View.GONE
            binding.tvNextCheck.visibility = if (isPolling) View.VISIBLE else View.GONE
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
    }

    private fun showRetryButton() {
        binding.btnTryAgain.visibility = View.VISIBLE
        binding.btnViewSubscription.visibility = View.GONE
    }

    private fun navigateBackToPackages() {
        findNavController().navigate(
            R.id.action_paymentStatusFragment_to_subscriptionPackagesFragment
        )
    }

    private fun navigateToSubscriptionDetails() {
        // Navigate to subscription details with the shopId
        val action = PaymentStatusFragmentDirections
            .actionPaymentStatusFragmentToSubscriptionDetailsFragment(
                shopId = args.shopId,
                subscriptionId = null,
                subscription = null
            )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPolling()
        _binding = null
    }
}

