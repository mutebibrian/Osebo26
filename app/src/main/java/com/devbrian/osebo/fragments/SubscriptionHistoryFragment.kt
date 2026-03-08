package com.devbrian.osebo.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.adapters.SubscriptionHistoryAdapter
import com.devbrian.osebo.databinding.FragmentSubscriptionHistoryBinding
import com.devbrian.osebo.models.Payment
import com.devbrian.osebo.models.Subscription
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubscriptionHistoryFragment : Fragment() {

    private var _binding: FragmentSubscriptionHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var subscriptionHistoryAdapter: SubscriptionHistoryAdapter
    private val viewModel: SubscriptionViewModel by viewModels()

    private var shopId: String = ""
    private var subscriptionId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shopId = getCurrentShopId()

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        if (shopId.isNotEmpty()) {
            viewModel.getShopActiveSubscription(shopId)
        } else {
            showError("No shop selected")
        }
    }

    private fun setupRecyclerView() {
        subscriptionHistoryAdapter = SubscriptionHistoryAdapter { subscription ->
            Toast.makeText(
                requireContext(),
                "${subscription.displayPackage}: ${subscription.formattedAmount}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = subscriptionHistoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.currentSubscription.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val sub = resource.data
                    if (sub == null) {
                        showEmptyState()
                        return@observe
                    }

                    subscriptionId = sub.id

                    if (shopId.isNotEmpty() && subscriptionId.isNotEmpty()) {
                        viewModel.getPaymentHistory(shopId, subscriptionId)
                    } else {
                        showEmptyState()
                    }
                }

                is Resource.Error -> showEmptyState()

                is Resource.Loading -> showLoading()
            }
        }

        viewModel.paymentHistory.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val payments = resource.data.orEmpty()
                    if (payments.isEmpty()) showEmptyState() else updateHistoryData(payments)
                }

                is Resource.Error -> showError(resource.message ?: "Failed to load payment history")

                is Resource.Loading -> showLoading()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnRetry.setOnClickListener {
            if (shopId.isNotEmpty()) {
                viewModel.getShopActiveSubscription(shopId)
            }
        }
    }

    private fun updateHistoryData(payments: List<Payment>) {
        // Convert Payments -> Subscription rows (because your item layout + adapter are Subscription-based)
        val subscriptions: List<Subscription> = payments.map { payment ->
            Subscription(
                id = payment.id,
                shopId = shopId,
                packageType = payment.description ?: "Unknown",
                amount = payment.amount,
                currency = payment.currency,
                status = payment.status,
                paymentMethod = payment.paymentMethod,
                createdAt = payment.paymentDate ?: payment.paidAt ?: payment.createdAt,
                isActive = payment.status.equals("completed", ignoreCase = true)
                // NOTE: if your Subscription constructor requires more params, add defaults here
            )
        }

        // FIX: ListAdapter uses submitList(...)
        subscriptionHistoryAdapter.submitList(subscriptions)

        binding.progressBar.visibility = View.GONE
        binding.historyRecyclerView.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
        binding.errorText.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.historyRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
        binding.emptyStateText.text = "No payment history found"
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.historyRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        binding.errorText.visibility = View.GONE
        binding.btnRetry.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.historyRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        binding.errorText.visibility = View.VISIBLE
        binding.btnRetry.visibility = View.VISIBLE
        binding.errorText.text = message
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