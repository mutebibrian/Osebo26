package com.devbrian.osebo.fragments.subpages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.FeatureListAdapter
import com.devbrian.osebo.databinding.FragmentShopSubscriptionBinding
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.models.Subscription
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ShopSubscriptionFragment : Fragment() {

    private var _binding: FragmentShopSubscriptionBinding? = null
    private val binding get() = _binding!!

    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private lateinit var featureAdapter: FeatureListAdapter
    private lateinit var shop: Shop
    private var currentSubscription: Subscription? = null

    companion object {
        private const val ARG_SHOP = "shop"

        fun newInstance(shop: Shop): ShopSubscriptionFragment {
            val fragment = ShopSubscriptionFragment()
            val args = Bundle()
            args.putSerializable(ARG_SHOP, shop)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopSubscriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shop = arguments?.getSerializable(ARG_SHOP) as? Shop ?: run {
            Toast.makeText(requireContext(), "Shop data not found", Toast.LENGTH_SHORT).show()
            return
        }

        setupRecyclerView()
        setupClickListeners()
        loadSubscriptionDetails()
        displaySubscriptionInfo()
        observeCancelResult()
    }

    private fun setupRecyclerView() {
        featureAdapter = FeatureListAdapter()
        binding.rvFeatures.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = featureAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnManageSubscription.setOnClickListener {
            // Navigate to subscription packages (this is the Subscribe Now button in no-subscription layout)
            Toast.makeText(requireContext(), "Subscribe Now", Toast.LENGTH_SHORT).show()
        }

        binding.btnRenewSubscription.setOnClickListener {
            // Handle renew
            Toast.makeText(requireContext(), "Renew Subscription", Toast.LENGTH_SHORT).show()
        }

        binding.btnUpgradeSubscription.setOnClickListener {
            // Handle upgrade
            Toast.makeText(requireContext(), "Upgrade Subscription", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displaySubscriptionInfo() {
        val hasSubscription = shop.isSubscriptionActive ||
                shop.subscriptionStatus.equals("active", ignoreCase = true) ||
                shop.subscriptionStatus.equals("trial", ignoreCase = true)

        if (hasSubscription) {
            binding.cardSubscription.visibility = View.VISIBLE
            binding.layoutNoSubscription.visibility = View.GONE

            // Plan info
            binding.tvSubscriptionPlan.text = when (shop.subscriptionType?.uppercase()) {
                "BASIC" -> "Basic Plan"
                "PRO" -> "Pro Plan"
                "POPULAR", "ENTERPRISE" -> "Enterprise Plan"
                else -> shop.subscriptionType ?: "Unknown Plan"
            }

            // Status
            val isTrial = shop.subscriptionStatus.equals("trial", ignoreCase = true)
            binding.tvSubscriptionStatus.text = if (isTrial) "TRIAL" else "ACTIVE"
            binding.tvSubscriptionStatus.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isTrial) R.color.warning_orange else R.color.success_green
                )
            )

            // Expiry
            binding.tvSubscriptionExpiry.text = formatDate(shop.subscriptionExpiry)

            // Show/hide renew/upgrade buttons based on subscription status
            if (isSubscriptionExpiringSoon(shop.subscriptionExpiry)) {
                binding.btnRenewSubscription.visibility = View.VISIBLE
                binding.btnUpgradeSubscription.visibility = View.GONE
            } else {
                binding.btnRenewSubscription.visibility = View.GONE
                binding.btnUpgradeSubscription.visibility = View.VISIBLE
            }

        } else {
            binding.cardSubscription.visibility = View.GONE
            binding.layoutNoSubscription.visibility = View.VISIBLE
        }
    }

    private fun loadSubscriptionDetails() {
        lifecycleScope.launch {
            subscriptionViewModel.getShopActiveSubscription(shop.id)

            subscriptionViewModel.currentSubscription.observe(viewLifecycleOwner) { resource ->
                when (resource) {
                    is Resource.Success -> {
                        binding.progressBar?.visibility = View.GONE
                        resource.data?.let { subscription ->
                            currentSubscription = subscription

                            // Update detailed subscription info card
                            binding.tvCurrentPlanDetail.text = subscription.displayPackage
                            binding.tvSubscriptionStatusDetail.text = subscription.displayStatus
                            binding.tvStartDate.text = subscription.formattedStartDate
                            binding.tvEndDate.text = subscription.formattedEndDate
                            binding.tvAmountPaid.text = subscription.formattedAmount
                            binding.tvPaymentMethod.text = subscription.displayPaymentMethod
                            binding.tvDaysRemaining.text = subscription.displayDaysRemaining

                            // Set days remaining color
                            val daysRemainingColor = when {
                                subscription.daysRemaining < 0 -> R.color.error_red
                                subscription.daysRemaining <= 3 -> R.color.warning_orange
                                else -> R.color.success_green
                            }
                            binding.tvDaysRemaining.setTextColor(
                                ContextCompat.getColor(requireContext(), daysRemainingColor)
                            )

                            // Load features from package
                            subscription.packageDetails?.features?.let { features ->
                                featureAdapter.submitList(features)
                            }
                        }
                    }
                    is Resource.Error -> {
                        binding.progressBar?.visibility = View.GONE
                        // Don't show error for now, just use the shop data
                    }
                    is Resource.Loading -> {
                        binding.progressBar?.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun loadFeatures() {
        // Load features based on plan type
        val features = when (shop.subscriptionType?.uppercase()) {
            "BASIC" -> listOf(
                "Up to 500 products",
                "Basic inventory management",
                "Sales tracking",
                "Customer management",
                "Basic reports"
            )
            "PRO" -> listOf(
                "Unlimited products",
                "Advanced inventory management",
                "Sales analytics",
                "Customer loyalty program",
                "Employee management",
                "Advanced reports",
                "Multi-store support"
            )
            "POPULAR", "ENTERPRISE" -> listOf(
                "Everything in Pro",
                "Custom integrations",
                "Dedicated support",
                "API access",
                "Advanced security",
                "Bulk operations",
                "Custom reports"
            )
            else -> emptyList()
        }

        featureAdapter.submitList(features.map { feature ->
            com.devbrian.osebo.models.Feature(
                name = feature,
                included = true,
                description = ""
            )
        })
    }

    private fun showCancelConfirmationDialog() {
        if (currentSubscription == null) {
            Toast.makeText(requireContext(), "No active subscription to cancel", Toast.LENGTH_SHORT).show()
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
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
                subscriptionViewModel.cancelSubscription(shop.id, subscription.id)
            }
        } ?: run {
            Toast.makeText(requireContext(), "No active subscription to cancel", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeCancelResult() {
        subscriptionViewModel.cancelSubscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Subscription cancelled successfully", Toast.LENGTH_SHORT).show()
                    loadSubscriptionDetails()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message ?: "Failed to cancel subscription", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Optionally show loading
                }
            }
        }
    }

    private fun isSubscriptionExpiringSoon(expiryDate: String?): Boolean {
        if (expiryDate.isNullOrEmpty()) return false

        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiry = format.parse(expiryDate)
            val today = Date()
            val diffInMillies = expiry.time - today.time
            val diffInDays = diffInMillies / (1000 * 60 * 60 * 24)
            diffInDays <= 7 && diffInDays > 0
        } catch (e: Exception) {
            false
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