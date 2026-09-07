package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.ItemSubscriptionHistoryBinding
import com.devbrian.osebo.models.Subscription

class SubscriptionHistoryAdapter(
    private val onItemClick: (Subscription) -> Unit
) : ListAdapter<Subscription, SubscriptionHistoryAdapter.HistoryViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemSubscriptionHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.hashCode().toLong()
    }

    class HistoryViewHolder(
        private val binding: ItemSubscriptionHistoryBinding,
        private val onItemClick: (Subscription) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Subscription) {
            
            binding.subscription = item

            binding.root.setOnClickListener { onItemClick(item) }

            
            val statusColorRes = when (item.status?.uppercase()) {
                "ACTIVE" -> R.color.success_green
                "TRIAL" -> R.color.blue_500
                "PENDING" -> R.color.warning_orange
                "EXPIRED" -> R.color.error_red
                "CANCELLED" -> R.color.gray_500
                else -> R.color.gray_500
            }
            binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, statusColorRes))

            
            

            binding.executePendingBindings()
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Subscription>() {
            override fun areItemsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
                return oldItem == newItem
            }
        }
    }
}
