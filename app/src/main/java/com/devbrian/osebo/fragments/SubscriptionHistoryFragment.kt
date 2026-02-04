package com.devbrian.osebo.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.adapters.SubscriptionHistoryAdapter
import com.devbrian.osebo.databinding.FragmentSubscriptionHistoryBinding
import com.devbrian.osebo.models.SubscriptionHistoryItem

class SubscriptionHistoryFragment : Fragment() {

    private lateinit var binding: FragmentSubscriptionHistoryBinding
    private lateinit var historyAdapter: SubscriptionHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSubscriptionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadHistoryData()
    }

    private fun setupRecyclerView() {
        historyAdapter = SubscriptionHistoryAdapter(emptyList())

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadHistoryData() {
        // Mock data - replace with API call
        val historyItems = listOf(
            SubscriptionHistoryItem(
                id = "1",
                planName = "Pro Plan",
                amount = 150000.0,
                currency = "UGX",
                paymentMethod = "Mobile Money",
                status = "Completed",
                date = "Dec 10, 2025"
            ),
            SubscriptionHistoryItem(
                id = "2",
                planName = "Basic Plan",
                amount = 50000.0,
                currency = "UGX",
                paymentMethod = "Mobile Money",
                status = "Completed",
                date = "Nov 10, 2025"
            ),
            SubscriptionHistoryItem(
                id = "3",
                planName = "Pro Plan",
                amount = 150000.0,
                currency = "UGX",
                paymentMethod = "Credit Card",
                status = "Failed",
                date = "Oct 10, 2025"
            ),
            SubscriptionHistoryItem(
                id = "4",
                planName = "Basic Plan",
                amount = 50000.0,
                currency = "UGX",
                paymentMethod = "Bank Transfer",
                status = "Completed",
                date = "Sep 10, 2025"
            )
        )

        historyAdapter.updateHistory(historyItems)

        // Show empty state if no data
        if (historyItems.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.historyRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.historyRecyclerView.visibility = View.VISIBLE
        }
    }
}