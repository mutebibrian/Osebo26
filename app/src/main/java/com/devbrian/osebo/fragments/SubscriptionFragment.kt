package com.devbrian.osebo.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.PlanAdapter
import com.devbrian.osebo.adapters.SubscriptionPagerAdapter
import com.devbrian.osebo.databinding.FragmentSubscriptionBinding
import com.devbrian.osebo.domain.model.SubscriptionPlan
import com.devbrian.osebo.ui.SubscriptionViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SubscriptionFragment : Fragment() {

    private lateinit var binding: FragmentSubscriptionBinding
    private lateinit var planAdapter: PlanAdapter
    private val viewModel: SubscriptionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSubscriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
        setupClickListeners()
        setupViewPager()
    }

    private fun setupUI() {
        // Setup horizontal layout manager for plans
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.plansRecyclerView.layoutManager = layoutManager
    }

    private fun setupObservers() {
        viewModel.currentSubscription.observe(viewLifecycleOwner) { subscription ->
            subscription?.let {
                binding.packageValue.text = it.packageName
                binding.maxValue.text = it.maxLimit
                binding.expiresValue.text = it.expires
                binding.daysLeftValue.text = it.daysLeft.toString()
                binding.statusChip.text = it.status

                // Set chip color based on status
                when (it.status.lowercase()) {
                    "active" -> binding.statusChip.setChipBackgroundColorResource(R.color.success_green)
                    "expired" -> binding.statusChip.setChipBackgroundColorResource(R.color.error_red)
                    "pending" -> binding.statusChip.setChipBackgroundColorResource(R.color.warning_yellow)
                }
            }
        }

        viewModel.availablePlans.observe(viewLifecycleOwner) { plans ->
            planAdapter = PlanAdapter(plans) { plan ->
                showPlanConfirmationDialog(plan)
            }
            binding.plansRecyclerView.adapter = planAdapter
        }
    }

    private fun setupClickListeners() {
        binding.removeSubscriptionButton.setOnClickListener {
            showRemoveConfirmationDialog()
        }

        binding.contactSalesButton.setOnClickListener {
            openContactSales()
        }
    }

    private fun setupViewPager() {
        val tabTitles = arrayOf("Overview", "History")
        val adapter = SubscriptionPagerAdapter(requireActivity(), tabTitles)

        // If using ViewPager2
        binding.viewPager.adapter = adapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(binding.subscriptionTabs, binding.viewPager) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()
    }

    private fun showRemoveConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Subscription")
            .setMessage("Are you sure you want to remove this shop subscription?")
            .setPositiveButton("Remove") { _, _ ->
                viewModel.removeSubscription()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPlanConfirmationDialog(plan: SubscriptionPlan) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choose ${plan.name}")
            .setMessage("Are you sure you want to subscribe to ${plan.name} plan for ${plan.price}?")
            .setPositiveButton("Subscribe") { _, _ ->
                viewModel.subscribeToPlan(plan)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openContactSales() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:sales@osebo.ai")
            putExtra(Intent.EXTRA_SUBJECT, "Custom Plan Inquiry")
        }
        startActivity(intent)
    }
}