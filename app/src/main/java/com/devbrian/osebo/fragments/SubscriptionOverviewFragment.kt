package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentSubscriptionOverviewBinding
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionOverviewFragment : Fragment() {

    private var _binding: FragmentSubscriptionOverviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubscriptionViewModel by viewModels(ownerProducer = { requireParentFragment() })

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

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.currentSubscription.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { subscription ->
                        // Show subscription details and hide no subscription layout
                        binding.noSubscriptionLayout.visibility = View.GONE
                        binding.subscriptionDetailsLayout.visibility = View.VISIBLE

                        // Update subscription details using correct property names
                        binding.planNameTextView.text = subscription.displayPackage ?: subscription.packageType
                        binding.planTypeTextView.text = subscription.packageType.uppercase(Locale.getDefault())
                        binding.priceTextView.text = "${subscription.currency} ${subscription.amount.toInt()}/month"
                        binding.startDateTextView.text = subscription.startDate ?: "N/A"
                        binding.endDateTextView.text = subscription.endDate ?: "N/A"

                        // Calculate days left
                        val daysLeft = if (subscription.endDate != null) {
                            calculateDaysLeft(subscription.endDate!!)
                        } else {
                            0
                        }
                        binding.daysLeftTextView.text = "$daysLeft days"

                        binding.statusTextView.text = subscription.displayStatus
                        binding.autoRenewTextView.text = if (subscription.autoRenew) "Yes" else "No"
                        binding.paymentMethodTextView.text = subscription.paymentMethod ?: "Not set"

                        // Set status color
                        when (subscription.status?.lowercase(Locale.getDefault())) {
                            "active" -> binding.statusTextView.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.success_green)
                            )
                            "expired", "cancelled" -> binding.statusTextView.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.error_red)
                            )
                            "pending" -> binding.statusTextView.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.warning_yellow)
                            )
                            "trial" -> binding.statusTextView.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.info_blue)
                            )
                            else -> binding.statusTextView.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.gray)
                            )
                        }

                        // Set days left color (red if less than 7 days)
                        if (daysLeft < 7) {
                            binding.daysLeftTextView.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.error_red)
                            )
                        } else {
                            binding.daysLeftTextView.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.black)
                            )
                        }
                    } ?: run {
                        // No subscription - show empty state
                        binding.noSubscriptionLayout.visibility = View.VISIBLE
                        binding.subscriptionDetailsLayout.visibility = View.GONE
                    }
                }
                is Resource.Error -> {
                    // Show error state
                    binding.noSubscriptionLayout.visibility = View.VISIBLE
                    binding.subscriptionDetailsLayout.visibility = View.GONE
                    showSnackbar("Failed to load subscription: ${resource.message ?: "Unknown error"}")
                }
                is Resource.Loading -> {
                    // Show loading state
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun calculateDaysLeft(endDate: String): Int {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val end = dateFormat.parse(endDate)
            val today = Calendar.getInstance().time
            val diff = end.time - today.time
            val days = java.util.concurrent.TimeUnit.DAYS.convert(diff, java.util.concurrent.TimeUnit.MILLISECONDS)
            days.toInt().coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }

    private fun setupClickListeners() {
        binding.upgradePlanButton.setOnClickListener {
            // Navigate to plans or show upgrade dialog
            showSnackbar("Upgrade feature coming soon")
        }

        binding.renewButton.setOnClickListener {
            viewModel.currentSubscription.value?.let { resource ->
                if (resource is Resource.Success) {
                    resource.data?.let { subscription ->
                        // Show renew dialog
                        showSnackbar("Renewal feature coming soon")
                    }
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}