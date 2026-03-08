package com.devbrian.osebo.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.ItemShopDashboardBinding
import com.devbrian.osebo.models.Shop
import java.text.NumberFormat
import java.util.*

class MainShopAdapter(
    private val onShopClick: (Shop) -> Unit,
    private val onViewDetailsClick: (Shop) -> Unit
) : ListAdapter<Shop, MainShopAdapter.ViewHolder>(ShopDiffCallback()) {

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance().apply {
        maximumFractionDigits = 0
        // Set currency to UGX (Ugandan Shilling)
        currency = Currency.getInstance("UGX")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemShopDashboardBinding.inflate(
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
        private val binding: ItemShopDashboardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onShopClick(getItem(position))
                }
            }

            binding.btnViewDetails.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onViewDetailsClick(getItem(position))
                }
            }
        }

        fun bind(shop: Shop) {
            binding.apply {
                tvShopName.text = shop.name
                tvShopLocation.text = shop.address ?: "Location not set"
                tvShopType.text = shop.shopTypeDisplay
                tvTodaySales.text = formatCurrency(shop.totalRevenue)
                tvProductsCount.text = shop.totalProducts.toString()
                tvEmployeesCount.text = shop.totalEmployees.toString()

                // Set shop status based on subscription
                val (statusText, statusColor) = when (shop.subscriptionStatus.lowercase(Locale.getDefault())) {
                    "active" -> Pair("Active", R.color.success_green)
                    "trial" -> Pair("Trial", R.color.warning_orange)
                    else -> Pair("Inactive", R.color.error_red)
                }
                tvShopStatus.text = statusText
                tvShopStatus.setTextColor(ContextCompat.getColor(root.context, statusColor))

                // Set background color for status
                tvShopStatus.setBackgroundColor(Color.TRANSPARENT) // Remove this if using bg_status drawable

                // Set shop initial
                val initials = getInitials(shop.name)
                tvShopInitial.text = initials
                tvShopInitial.setBackgroundColor(Color.parseColor(getColorForShop(shop.name)))
            }
        }

        private fun getInitials(name: String): String {
            val words = name.split(" ")
            return when (words.size) {
                1 -> words[0].take(2).uppercase(Locale.getDefault())
                else -> (words[0].first().toString() + words[1].first().toString()).uppercase(Locale.getDefault())
            }
        }

        private fun getColorForShop(name: String): String {
            val colors = listOf(
                "#FF6B6B", "#4ECDC4", "#FFD166", "#06D6A0",
                "#118AB2", "#EF476F", "#073B4C", "#FF9F1C"
            )
            val index = name.hashCode() % colors.size
            return colors[Math.abs(index)]
        }

        private fun formatCurrency(amount: Double): String {
            return currencyFormatter.format(amount)
        }
    }

    class ShopDiffCallback : DiffUtil.ItemCallback<Shop>() {
        override fun areItemsTheSame(oldItem: Shop, newItem: Shop): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Shop, newItem: Shop): Boolean {
            return oldItem == newItem
        }
    }
}