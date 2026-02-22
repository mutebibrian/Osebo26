package com.devbrian.osebo.fragments

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.PlanAdapter
import com.devbrian.osebo.adapters.SubscriptionPagerAdapter
import com.devbrian.osebo.databinding.FragmentSubscriptionBinding
import com.devbrian.osebo.models.Subscription
import com.devbrian.osebo.models.SubscriptionPackage
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SubscriptionFragment : Fragment() {

    private var _binding: FragmentSubscriptionBinding? = null
    private val binding get() = _binding!!

    private lateinit var planAdapter: PlanAdapter
    private val viewModel: SubscriptionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
        setupClickListeners()
        setupViewPager()

        // Load initial data
        val shopId = getCurrentShopId()
        if (shopId.isNotEmpty()) {
            viewModel.getShopActiveSubscription(shopId)
        }

        // Load subscription packages (plans)
        viewModel.loadSubscriptionPackages()
    }

    private fun setupUI() {
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.plansRecyclerView.layoutManager = layoutManager
    }

    private fun setupObservers() {
        // Observe current subscription
        viewModel.currentSubscription.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { subscription ->
                        // Show subscription details
                        binding.packageValue.text = subscription.displayPackage
                        binding.maxValue.text = resources.getQuantityString(
                            R.plurals.months_count,
                            subscription.months,
                            subscription.months
                        )
                        binding.expiresValue.text = subscription.endDate ?: "N/A"

                        val daysLeft = if (subscription.endDate != null) {
                            calculateDaysLeft(subscription.endDate)
                        } else {
                            0
                        }
                        binding.daysLeftValue.text = daysLeft.toString()

                        binding.statusChip.text = subscription.displayStatus

                        when (subscription.status?.lowercase()) {
                            "active" -> binding.statusChip.setChipBackgroundColorResource(R.color.success_green)
                            "expired", "cancelled" -> binding.statusChip.setChipBackgroundColorResource(R.color.error_red)
                            "pending" -> binding.statusChip.setChipBackgroundColorResource(R.color.warning_yellow)
                            "trial" -> binding.statusChip.setChipBackgroundColorResource(R.color.blue_info)
                            else -> binding.statusChip.setChipBackgroundColorResource(R.color.gray_500)
                        }
                    } ?: run {
                        // No active subscription
                        binding.packageValue.text = "No Active Plan"
                        binding.maxValue.text = "N/A"
                        binding.expiresValue.text = "N/A"
                        binding.daysLeftValue.text = "0"
                        binding.statusChip.text = "Inactive"
                        binding.statusChip.setChipBackgroundColorResource(R.color.gray_500)
                    }
                }
                is Resource.Error -> {
                    binding.packageValue.text = "No Active Plan"
                    binding.maxValue.text = "N/A"
                    binding.expiresValue.text = "N/A"
                    binding.daysLeftValue.text = "0"
                    binding.statusChip.text = "Inactive"
                    binding.statusChip.setChipBackgroundColorResource(R.color.gray_500)

                    resource.message?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Loading -> {
                    // Show loading state
                    binding.packageValue.text = "Loading..."
                    binding.maxValue.text = "Loading..."
                    binding.expiresValue.text = "Loading..."
                    binding.daysLeftValue.text = "-"
                    binding.statusChip.text = "Loading..."
                }
            }
        }

        // Observe subscription packages
        viewModel.subscriptionPackages.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val plans = resource.data ?: emptyList()
                    planAdapter = PlanAdapter(plans) { plan ->
                        showPlanConfirmationDialog(plan)
                    }
                    binding.plansRecyclerView.adapter = planAdapter

                    // Hide loading, show content
                    binding.progressBar.visibility = View.GONE
                    binding.plansRecyclerView.visibility = View.VISIBLE
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(),
                        "Failed to load plans: ${resource.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = resource.message ?: "Failed to load plans"
                }
                is Resource.Loading -> {
                    // Show loading indicator
                    binding.progressBar.visibility = View.VISIBLE
                    binding.plansRecyclerView.visibility = View.GONE
                    binding.errorText.visibility = View.GONE
                }
            }
        }

        // Observe create subscription result
        viewModel.createSubscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { response ->
                        if (response.success) {
                            Toast.makeText(requireContext(),
                                "Subscription initiated. Check your phone for payment prompt.",
                                Toast.LENGTH_LONG
                            ).show()

                            // Refresh subscription data
                            val shopId = getCurrentShopId()
                            if (shopId.isNotEmpty()) {
                                viewModel.getShopActiveSubscription(shopId)
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(),
                        resource.message ?: "Failed to create subscription",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }

        // Observe payment polling status
        viewModel.paymentPollingStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { response ->
                        when (response.status.lowercase()) {
                            "completed", "success" -> {
                                Toast.makeText(requireContext(),
                                    "Payment successful! Your subscription is now active.",
                                    Toast.LENGTH_LONG
                                ).show()

                                // Refresh subscription data
                                val shopId = getCurrentShopId()
                                if (shopId.isNotEmpty()) {
                                    viewModel.getShopActiveSubscription(shopId)
                                }
                            }
                            "failed", "cancelled" -> {
                                Toast.makeText(requireContext(),
                                    "Payment ${response.status}. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        // Error messages observer
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Success messages observer
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Loading state observer
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun calculateDaysLeft(endDate: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val end = try {
            dateFormat.parse(endDate)
        } catch (_: Exception) {
            null
        }
        val today = Calendar.getInstance().time
        val diff = (end?.time ?: 0L) - today.time
        val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        return days.toInt().coerceAtLeast(0)
    }

    private fun setupClickListeners() {
        binding.removeSubscriptionButton.setOnClickListener {
            showRemoveConfirmationDialog()
        }

        binding.contactSalesButton.setOnClickListener {
            openContactSales()
        }

        binding.retryButton?.setOnClickListener {
            viewModel.loadSubscriptionPackages()
            val shopId = getCurrentShopId()
            if (shopId.isNotEmpty()) {
                viewModel.getShopActiveSubscription(shopId)
            }
        }
    }

    private fun setupViewPager() {
        val tabTitles = arrayOf("Overview", "History")
        val adapter = SubscriptionPagerAdapter(requireActivity(), tabTitles)

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.subscriptionTabs, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun showRemoveConfirmationDialog() {
        val shopId = getCurrentShopId()
        if (shopId.isEmpty()) {
            Toast.makeText(requireContext(), "No shop selected", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Subscription")
            .setMessage("Are you sure you want to cancel this subscription?")
            .setPositiveButton("Cancel Subscription") { _, _ ->
                viewModel.currentSubscription.value?.let { resource ->
                    if (resource is Resource.Success) {
                        resource.data?.let { subscription ->
                            viewModel.cancelSubscription(shopId, subscription.id)
                        }
                    }
                }
            }
            .setNegativeButton("Keep Subscription", null)
            .show()
    }

    private fun showPlanConfirmationDialog(plan: SubscriptionPackage) {
        val shopId = getCurrentShopId()
        if (shopId.isEmpty()) {
            Toast.makeText(requireContext(), "No shop selected", Toast.LENGTH_SHORT).show()
            return
        }

        // For custom/enterprise plans, redirect to contact sales
        if (plan.isCustom) {
            openContactSales()
            return
        }

        // Show payment dialog
        showPaymentDialog(shopId, plan)
    }

    private fun showPaymentDialog(shopId: String, plan: SubscriptionPackage) {
        val dialog = PaymentDialogFragment().apply {
            arguments = Bundle().apply {
                putString("shopId", shopId)
                putString("packageId", plan.id)
                putString("packageName", plan.displayName)
                putDouble("amount", plan.price)
            }
        }

        dialog.setPaymentListener { phoneNumber, packageId, months ->
            viewModel.createSubscription(
                shopId = shopId,
                packageId = packageId,
                phoneNumber = phoneNumber,
                months = months
            )
        }

        dialog.show(parentFragmentManager, "PaymentDialog")
    }

    private fun openContactSales() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:sales@osebo.ai".toUri()
            putExtra(Intent.EXTRA_SUBJECT, "Custom Plan Inquiry")
            putExtra(Intent.EXTRA_TEXT, buildString {
                append("Hello,\n\n")
                append("I'm interested in a custom subscription plan for my shop.\n\n")
                append("Shop ID: ${getCurrentShopId()}\n")
                append("Please contact me with more information.")
            })
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentShopId(): String {
        val prefs = requireContext().getSharedPreferences("OseboPrefs", Context.MODE_PRIVATE)
        return prefs.getString("current_shop_id", "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}