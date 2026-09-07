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
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.FeatureListAdapter
import com.devbrian.osebo.adapters.SubscriptionHistoryAdapter
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.databinding.FragmentSubscriptionDetailsBinding
import com.devbrian.osebo.models.Feature
import com.devbrian.osebo.models.Subscription
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class SubscriptionDetailsFragment : Fragment() {

    private var _binding: FragmentSubscriptionDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubscriptionViewModel by viewModels()

    private lateinit var subscriptionHistoryAdapter: SubscriptionHistoryAdapter
    private lateinit var featureAdapter: FeatureListAdapter

    private var currentSubscription: Subscription? = null

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

        setupUI()
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()
        loadSubscriptionDetails()
    }

    private fun setupUI() {
        binding.toolbar.title = "Subscription Details"
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerViews() {
        subscriptionHistoryAdapter = SubscriptionHistoryAdapter { subscription ->
            Toast.makeText(
                requireContext(),
                "${subscription.displayPackage}: ${subscription.formattedAmount}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.rvPaymentHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = subscriptionHistoryAdapter
            setHasFixedSize(true)
        }

        featureAdapter = FeatureListAdapter()
        binding.rvFeatures.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = featureAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnUpgrade.setOnClickListener { navigateToUpgrade() }
        binding.btnCancel.setOnClickListener { showCancelConfirmationDialog() }
        binding.btnViewAllPayments.setOnClickListener { navigateToPaymentHistory() }
        binding.btnManageSubscription.setOnClickListener { showManageOptionsDialog() }
        binding.btnRetry.setOnClickListener { loadSubscriptionDetails() }
    }

    private fun observeViewModel() {
        viewModel.subscriptionDetails.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                    binding.layoutContent.visibility = View.GONE
                    binding.layoutError.visibility = View.GONE
                }

                is Resource.Success -> {
                    showLoading(false)
                    val sub = resource.data
                    if (sub == null) {
                        showErrorState("Subscription data not found")
                        return@observe
                    }

                    currentSubscription = sub

                    binding.layoutContent.visibility = View.VISIBLE
                    binding.layoutError.visibility = View.GONE

                    updateUI(sub)
                    subscriptionHistoryAdapter.submitList(listOf(sub))
                    loadFeatures(sub)
                }

                is Resource.Error -> {
                    showLoading(false)
                    showErrorState(resource.message ?: "Failed to load subscription details")
                }
            }
        }

        viewModel.cancelSubscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showCancelLoading(true)

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
    }

    private fun loadSubscriptionDetails() {
        currentSubscription?.let { sub ->
            binding.layoutContent.visibility = View.VISIBLE
            binding.layoutError.visibility = View.GONE
            updateUI(sub)
            subscriptionHistoryAdapter.submitList(listOf(sub))
            loadFeatures(sub)
            return
        }

        lifecycleScope.launch {

            val shopId = args.shopId
            if (shopId.isNullOrEmpty()) {
                showErrorState("Missing shop id")
                return@launch
            }

            val subscriptionId = args.subscriptionId
            if (!subscriptionId.isNullOrEmpty()) {
                viewModel.getSubscriptionDetails(subscriptionId)
            } else {
                viewModel.getShopActiveSubscription(shopId)
            }
        }
    }

    private fun loadFeatures(subscription: Subscription) {
        val features = subscription.packageDetails?.features
        if (!features.isNullOrEmpty()) {
            binding.featuresSection.visibility = View.VISIBLE
            featureAdapter.submitList(features)
        } else {
            val defaultFeatures = getDefaultFeaturesForPlan(subscription.packageType)
            if (defaultFeatures.isNotEmpty()) {
                binding.featuresSection.visibility = View.VISIBLE
                featureAdapter.submitList(defaultFeatures)
            } else {
                binding.featuresSection.visibility = View.GONE
            }
        }
    }

    private fun getDefaultFeaturesForPlan(packageType: String?): List<Feature> {
        return when (packageType?.uppercase()) {
            "BASIC" -> listOf(
                Feature(name = "Up to 500 products", included = true, description = ""),
                Feature(name = "Basic inventory management", included = true, description = ""),
                Feature(name = "Sales tracking", included = true, description = ""),
                Feature(name = "Customer management", included = true, description = ""),
                Feature(name = "Basic reports", included = true, description = "")
            )

            "PRO" -> listOf(
                Feature(name = "Unlimited products", included = true, description = ""),
                Feature(name = "Advanced inventory management", included = true, description = ""),
                Feature(name = "Sales analytics", included = true, description = ""),
                Feature(name = "Customer loyalty program", included = true, description = ""),
                Feature(name = "Employee management", included = true, description = ""),
                Feature(name = "Advanced reports", included = true, description = "")
            )

            "POPULAR", "ENTERPRISE" -> listOf(
                Feature(name = "Unlimited products", included = true, description = ""),
                Feature(name = "Advanced inventory management", included = true, description = ""),
                Feature(name = "Sales analytics", included = true, description = ""),
                Feature(name = "Customer loyalty program", included = true, description = ""),
                Feature(name = "Employee management", included = true, description = ""),
                Feature(name = "Advanced reports", included = true, description = ""),
                Feature(name = "Multi-store support", included = true, description = ""),
                Feature(name = "API access", included = true, description = "")
            )

            else -> emptyList()
        }
    }

    private fun updateUI(subscription: Subscription) {
        binding.tvPlanName.text = subscription.displayPackage
        binding.tvPlanStatus.text = subscription.displayStatus.uppercase()

        val statusColor = when (subscription.status?.uppercase()) {
            "ACTIVE" -> R.color.green_500
            "EXPIRED" -> R.color.red_500
            "PENDING" -> R.color.yellow_500
            "TRIAL" -> R.color.blue_500
            else -> R.color.gray_500
        }
        binding.tvPlanStatus.setTextColor(ContextCompat.getColor(requireContext(), statusColor))

        binding.tvPlanPrice.text = if (subscription.isTrial) "Free Trial" else subscription.formattedAmount

        subscription.startDate?.let {
            binding.tvStartDate.text = formatDateForDisplay(it)
            binding.layoutStartDate.visibility = View.VISIBLE
        } ?: run { binding.layoutStartDate.visibility = View.GONE }

        subscription.endDate?.let {
            binding.tvRenewalDate.text = formatDateForDisplay(it)
            binding.tvNextBilling.text = formatDateForDisplay(it)
            binding.layoutRenewalDate.visibility = View.VISIBLE
            binding.layoutNextBilling.visibility = View.VISIBLE
        } ?: run {
            binding.layoutRenewalDate.visibility = View.GONE
            binding.layoutNextBilling.visibility = View.GONE
        }

        if (subscription.isTrial && subscription.trialEndsAt != null) {
            binding.layoutTrialInfo.visibility = View.VISIBLE
            binding.tvTrialEndsAt.text = formatDateForDisplay(subscription.trialEndsAt)
        } else {
            binding.layoutTrialInfo.visibility = View.GONE
        }
    }

    private fun navigateToUpgrade() {
        val currentPackage: String = currentSubscription?.packageType ?: ""

        
        val shopPlaceholder = Shop(
            id = args.shopId ?: "",
            name = "",
            subscriptionStatus = "inactive"
        )

        val action = SubscriptionDetailsFragmentDirections
            .actionSubscriptionDetailsFragmentToSubscriptionPackagesFragment(
                shop = shopPlaceholder,
                currentPackage = currentPackage
            )
        findNavController().navigate(action)
    }

    private fun navigateToPaymentHistory() {
        val subscriptionId: String = currentSubscription?.id
            ?: args.subscriptionId
            ?: run {
                Toast.makeText(requireContext(), "Missing subscription ID", Toast.LENGTH_SHORT).show()
                return
            }

        val action = SubscriptionDetailsFragmentDirections
            .actionSubscriptionDetailsFragmentToPaymentHistoryFragment(subscriptionId)

        findNavController().navigate(action)
    }

    private fun showCancelConfirmationDialog() {
        val sub = currentSubscription ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Subscription")
            .setMessage("Are you sure you want to cancel your subscription?")
            .setPositiveButton("Yes, Cancel") { _, _ -> cancelSubscription(sub.id) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelSubscription(subscriptionId: String) {
        val shopId: String = args.shopId
            ?: run {
                Toast.makeText(requireContext(), "Missing shop ID", Toast.LENGTH_SHORT).show()
                return
            }

        lifecycleScope.launch { viewModel.cancelSubscription(shopId, subscriptionId) }
    }

    private fun showManageOptionsDialog() {
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
                    0 -> Toast.makeText(requireContext(), "Change payment method", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(requireContext(), "Update billing info", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(requireContext(), "Downloading invoice...", Toast.LENGTH_SHORT).show()
                    3 -> viewUsageStatistics()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun viewUsageStatistics() {
        val shopId: String = args.shopId
            ?: run {
                Toast.makeText(requireContext(), "Missing shop ID", Toast.LENGTH_SHORT).show()
                return
            }

        val action = SubscriptionDetailsFragmentDirections
            .actionSubscriptionDetailsFragmentToUsageStatisticsFragment(shopId)
        findNavController().navigate(action)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showCancelLoading(show: Boolean) {
        if (show) Toast.makeText(requireContext(), "Cancelling...", Toast.LENGTH_SHORT).show()
    }

    private fun showErrorState(message: String) {
        binding.layoutContent.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
        binding.progressBar.visibility = View.GONE
    }

    private fun formatDateForDisplay(dateString: String?): String {
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
            if (date == null) "N/A" else outputFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
