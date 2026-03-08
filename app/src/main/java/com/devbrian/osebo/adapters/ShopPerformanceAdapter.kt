package com.devbrian.osebo.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.databinding.ItemShopPerformanceBinding
import com.devbrian.osebo.models.ShopPerformance
import java.text.NumberFormat
import java.util.*

class ShopPerformanceAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<ShopPerformance, ShopPerformanceAdapter.ViewHolder>(ShopPerformanceDiffCallback()) {

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance().apply {
        maximumFractionDigits = 0
        currency = Currency.getInstance("UGX")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemShopPerformanceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemShopPerformanceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position).shopId)
                }
            }
        }

        fun bind(performance: ShopPerformance) {
            binding.apply {
                tvShopName.text = performance.shopName
                tvShopLocation.text = performance.location
                tvSalesPercentage.text = "${performance.salesPercentage}%"
                tvExpenses.text = formatCurrency(performance.expenses)

                // Set text color based on subscription status
                when (performance.subscriptionStatus.lowercase(Locale.getDefault())) {
                    "active" -> tvSalesPercentage.setTextColor(Color.parseColor("#4CAF50"))
                    "trial" -> tvSalesPercentage.setTextColor(Color.parseColor("#FF9800"))
                    else -> tvSalesPercentage.setTextColor(Color.parseColor("#9E9E9E"))
                }
            }
        }

        private fun formatCurrency(amount: Double): String {
            return currencyFormatter.format(amount)
        }
    }

    class ShopPerformanceDiffCallback : DiffUtil.ItemCallback<ShopPerformance>() {
        override fun areItemsTheSame(oldItem: ShopPerformance, newItem: ShopPerformance): Boolean {
            return oldItem.shopId == newItem.shopId
        }

        override fun areContentsTheSame(oldItem: ShopPerformance, newItem: ShopPerformance): Boolean {
            return oldItem == newItem
        }
    }
}