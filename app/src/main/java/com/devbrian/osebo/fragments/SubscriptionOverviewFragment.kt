package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devbrian.osebo.databinding.FragmentSubscriptionOverviewBinding
import com.devbrian.osebo.ui.SubscriptionViewModel
import com.google.android.material.snackbar.Snackbar

class SubscriptionOverviewFragment : Fragment() {

    private lateinit var binding: FragmentSubscriptionOverviewBinding
    private val viewModel: SubscriptionViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSubscriptionOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.currentSubscription.observe(viewLifecycleOwner) { subscription ->
            subscription?.let {
                // Show subscription details and hide no subscription layout
                binding.noSubscriptionLayout.visibility = View.GONE
                binding.subscriptionDetailsLayout.visibility = View.VISIBLE

                // Update subscription details
                binding.planNameTextView.text = it.planName
                binding.planTypeTextView.text = it.planType.uppercase()
                binding.priceTextView.text = "${it.currency} ${it.price}/month"
                binding.startDateTextView.text = it.startDate
                binding.endDateTextView.text = it.endDate
                binding.daysLeftTextView.text = "${it.daysLeft} days"
                binding.statusTextView.text = it.status.capitalize()
                binding.autoRenewTextView.text = if (it.autoRenew) "Yes" else "No"
                binding.paymentMethodTextView.text = it.paymentMethod ?: "Not set"

                // Set status color
                when (it.status.lowercase()) {
                    "active" -> binding.statusTextView.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
                    "expired" -> binding.statusTextView.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                    "pending" -> binding.statusTextView.setTextColor(requireContext().getColor(android.R.color.holo_orange_dark))
                    else -> binding.statusTextView.setTextColor(requireContext().getColor(android.R.color.darker_gray))
                }

                // Set days left color (red if less than 7 days)
                if (it.daysLeft < 7) {
                    binding.daysLeftTextView.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                } else {
                    binding.daysLeftTextView.setTextColor(requireContext().getColor(android.R.color.black))
                }
            } ?: run {
                // No subscription - show empty state
                binding.noSubscriptionLayout.visibility = View.VISIBLE
                binding.subscriptionDetailsLayout.visibility = View.GONE
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.upgradePlanButton.setOnClickListener {
            // Navigate to plans or show upgrade dialog
            showSnackbar("Upgrade feature coming soon")
        }

        binding.renewButton.setOnClickListener {
            viewModel.currentSubscription.value?.let { subscription ->
                // Auto-renew toggle
                showSnackbar("Renewal settings coming soon")
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}