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
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SubscriptionFragment : Fragment() {

    private var _binding: FragmentSubscriptionBinding? = null
    private val binding get() = _binding!!

    private lateinit var planAdapter: PlanAdapter
    private val viewModel: SubscriptionViewModel by viewModels()

    private var shopId: String = ""
    private var currentSubscription: Subscription? = null

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

        // Get shop ID from arguments or preferences
        shopId = arguments?.getString("shopId") ?: getCurrentShopId()

        if (shopId.isEmpty()) {
            showError("No shop selected")
            return
        }

        setupUI()
        setupObservers()
        setupClickListeners()
        setupViewPager()

        // Load data
        loadSubscriptionData()
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
                        currentSubscription = subscription
                        displaySubscriptionData(subscription)
                        binding.progressBar.visibility = View.GONE
                        binding.errorText.visibility = View.GONE
                    } ?: run {
                        showNoSubscriptionState()
                    }
                }
                is Resource.Error -> {
                    showNoSubscriptionState()
                    if (resource.message != "No active subscription found") {
                        resource.message?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
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

                    binding.progressBar.visibility = View.GONE
                    binding.plansRecyclerView.visibility = View.VISIBLE
                    binding.errorText.visibility = View.GONE
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

                            // Refresh subscription data after payment
                            loadSubscriptionData()
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
                                loadSubscriptionData()
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

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe success messages
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun displaySubscriptionData(subscription: Subscription) {
        // Update UI with real subscription data
        binding.packageValue.text = subscription.displayPackage
        binding.maxValue.text = subscription.durationText

        // Format end date nicely
        val endDate = subscription.formattedEndDate
        binding.expiresValue.text = endDate

        val daysLeft = subscription.daysRemaining
        binding.daysLeftValue.text = daysLeft.toString()

        // Set color based on days remaining
        binding.daysLeftValue.setTextColor(
            resources.getColor(
                if (daysLeft < 3) R.color.error_red
                else if (daysLeft < 7) R.color.warning_yellow
                else R.color.black,
                null
            )
        )

        binding.statusChip.text = subscription.displayStatus

        // Set chip color based on status
        when {
            subscription.isTrial && daysLeft > 0 ->
                binding.statusChip.setChipBackgroundColorResource(R.color.blue_info)
            subscription.isActiveStatus ->
                binding.statusChip.setChipBackgroundColorResource(R.color.success_green)
            subscription.isExpired || subscription.isCancelled ->
                binding.statusChip.setChipBackgroundColorResource(R.color.error_red)
            subscription.isPending ->
                binding.statusChip.setChipBackgroundColorResource(R.color.warning_yellow)
            else ->
                binding.statusChip.setChipBackgroundColorResource(R.color.gray_500)
        }

        // Show/hide remove button based on subscription status
        binding.removeSubscriptionButton.visibility =
            if (subscription.isActiveStatus || subscription.isTrial) View.VISIBLE else View.GONE

        // Show subscription details card
        binding.subscriptionDetails.visibility = View.VISIBLE
    }

    private fun showNoSubscriptionState() {
        binding.packageValue.text = "No Active Plan"
        binding.maxValue.text = "N/A"
        binding.expiresValue.text = "N/A"
        binding.daysLeftValue.text = "0"
        binding.statusChip.text = "Inactive"
        binding.statusChip.setChipBackgroundColorResource(R.color.gray_500)
        binding.removeSubscriptionButton.visibility = View.GONE
        binding.subscriptionDetails.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun loadSubscriptionData() {
        viewModel.getShopActiveSubscription(shopId)
        viewModel.loadSubscriptionPackages()
    }

    private fun setupClickListeners() {
        binding.removeSubscriptionButton.setOnClickListener {
            showRemoveConfirmationDialog()
        }

        binding.contactSalesButton.setOnClickListener {
            openContactSales()
        }

        binding.retryButton?.setOnClickListener {
            loadSubscriptionData()
        }
    }

    private fun setupViewPager() {
        try {
            val tabTitles = arrayOf("Overview", "History")
            val adapter = SubscriptionPagerAdapter(requireActivity(), tabTitles)
            binding.viewPager.adapter = adapter
            TabLayoutMediator(binding.subscriptionTabs, binding.viewPager) { tab, position ->
                tab.text = tabTitles[position]
            }.attach()
        } catch (e: Exception) {
            e.printStackTrace()
            binding.viewPager.visibility = View.GONE
            binding.subscriptionTabs.visibility = View.GONE
        }
    }

    private fun showRemoveConfirmationDialog() {
        if (shopId.isEmpty()) {
            Toast.makeText(requireContext(), "No shop selected", Toast.LENGTH_SHORT).show()
            return
        }

        val subscription = currentSubscription
        if (subscription == null) {
            Toast.makeText(requireContext(), "No active subscription", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Subscription")
            .setMessage("Are you sure you want to cancel your ${subscription.displayPackage}? This action cannot be undone.")
            .setPositiveButton("Cancel Subscription") { _, _ ->
                viewModel.cancelSubscription(shopId, subscription.id)
            }
            .setNegativeButton("Keep Subscription", null)
            .show()
    }

    private fun showPlanConfirmationDialog(plan: SubscriptionPackage) {
        if (shopId.isEmpty()) {
            Toast.makeText(requireContext(), "No shop selected", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if custom plan
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
                append("Shop ID: $shopId\n")
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