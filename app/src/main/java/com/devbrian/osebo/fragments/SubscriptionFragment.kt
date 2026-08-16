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

        shopId = arguments?.getString("shopId") ?: getCurrentShopId()

        if (shopId.isEmpty()) {
            showError("No shop selected")
            return
        }

        setupUI()
        setupObservers()
        setupClickListeners()
        setupViewPager()

        loadSubscriptionData()
    }

    private fun setupUI() {
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.plansRecyclerView.layoutManager = layoutManager
    }

    private fun setupObservers() {

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

        viewModel.subscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show loading if needed
                }
                is Resource.Success -> {
                    val response = resource.data
                    if (response?.paymentId != null) {
                        Toast.makeText(requireContext(),
                            "Payment initiated! Please check your phone.",
                            Toast.LENGTH_LONG).show()
                        loadSubscriptionData()
                    } else {
                        Toast.makeText(requireContext(),
                            response?.message ?: "Subscription created!",
                            Toast.LENGTH_SHORT).show()
                        loadSubscriptionData()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(),
                        resource.message ?: "Failed to create subscription",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        // paymentPollingStatus is now Resource<Subscription?> — polls the subscription
        // record itself by paymentId, since there's no separate payment-status endpoint.
        viewModel.paymentPollingStatus.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { checkResponse ->
                        when {
                            checkResponse.isActive -> {
                                Toast.makeText(requireContext(),
                                    "Payment successful! Your subscription is now active.",
                                    Toast.LENGTH_LONG
                                ).show()
                                loadSubscriptionData()
                            }
                            checkResponse.isFailed || checkResponse.isCancelled -> {
                                Toast.makeText(requireContext(),
                                    "Payment ${checkResponse.payment?.status ?: "failed"}. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                // still pending — no action needed, polling continues
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun displaySubscriptionData(subscription: Subscription) {
        binding.packageValue.text = subscription.displayPackage
        binding.maxValue.text = subscription.durationText

        val endDate = subscription.formattedEndDate
        binding.expiresValue.text = endDate

        val daysLeft = subscription.daysRemaining
        binding.daysLeftValue.text = daysLeft.toString()

        binding.daysLeftValue.setTextColor(
            resources.getColor(
                if (daysLeft < 3) R.color.error_red
                else if (daysLeft < 7) R.color.warning_yellow
                else R.color.black,
                null
            )
        )

        binding.statusChip.text = subscription.displayStatus

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

        binding.removeSubscriptionButton.visibility =
            if (subscription.isActiveStatus || subscription.isTrial) View.VISIBLE else View.GONE

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

        if (plan.isCustom) {
            openContactSales()
            return
        }

        showPaymentDialog(shopId, plan)
    }

    private fun showPaymentDialog(shopId: String, plan: SubscriptionPackage) {
        val dialog = PaymentDialogFragment.newInstance(
            shopId = shopId,
            packageId = plan.id,
            packageName = plan.displayName,
            amount = plan.price
        )

        dialog.setPaymentListener { phoneNumber, packageId, months, amount ->
            viewModel.createSubscription(
                shopId = shopId,
                packageIds = listOf(packageId),
                phoneNumber = phoneNumber,
                months = months
            )
        }

        dialog.show(parentFragmentManager, PaymentDialogFragment.TAG)
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