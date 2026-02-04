package com.devbrian.osebo.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.databinding.ItemSubscriptionHistoryBinding
import com.devbrian.osebo.models.SubscriptionHistoryItem

class SubscriptionHistoryAdapter(
    private var historyItems: List<SubscriptionHistoryItem>
) : RecyclerView.Adapter<SubscriptionHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSubscriptionHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SubscriptionHistoryItem) {
            binding.planNameTextView.text = item.planName
            binding.amountTextView.text = "${item.currency} ${item.amount}"
            binding.paymentMethodTextView.text = item.paymentMethod
            binding.dateTextView.text = item.date

            // Set status color
            when (item.status.lowercase()) {
                "completed" -> {
                    binding.statusTextView.text = "Completed"
                    binding.statusTextView.setTextColor(binding.root.context.getColor(android.R.color.holo_green_dark))
                }
                "pending" -> {
                    binding.statusTextView.text = "Pending"
                    binding.statusTextView.setTextColor(binding.root.context.getColor(android.R.color.holo_orange_dark))
                }
                "failed" -> {
                    binding.statusTextView.text = "Failed"
                    binding.statusTextView.setTextColor(binding.root.context.getColor(android.R.color.holo_red_dark))
                }
                else -> {
                    binding.statusTextView.text = item.status
                    binding.statusTextView.setTextColor(binding.root.context.getColor(android.R.color.darker_gray))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubscriptionHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(historyItems[position])
    }

    override fun getItemCount() = historyItems.size

    fun updateHistory(newHistory: List<SubscriptionHistoryItem>) {
        historyItems = newHistory
        notifyDataSetChanged()
    }
}