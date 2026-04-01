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

                // Determine subscription status and colors
                val isActive = shop.subscriptionStatus.equals("active", ignoreCase = true) ||
                        shop.subscriptionStatus.equals("trial", ignoreCase = true)

                val statusInfo = when {
                    isActive && shop.subscriptionStatus.equals("trial", ignoreCase = true) -> {
                        Triple("TRIAL", R.color.blue_500, R.drawable.bg_status_trial)
                    }
                    isActive -> {
                        Triple("ACTIVE", R.color.green_500, R.drawable.bg_status_active)
                    }
                    shop.subscriptionStatus.equals("expired", ignoreCase = true) -> {
                        Triple("EXPIRED", R.color.red_500, R.drawable.bg_status_expired)
                    }
                    else -> {
                        Triple("INACTIVE", R.color.gray_500, R.drawable.bg_status_inactive)
                    }
                }

                // Set status text and colors
                tvShopStatus.text = statusInfo.first
                tvShopStatus.setTextColor(ContextCompat.getColor(root.context, statusInfo.second))

                // Apply background drawable if exists, otherwise just use text color
                try {
                    tvShopStatus.setBackgroundResource(statusInfo.third)
                } catch (e: Exception) {
                    tvShopStatus.setBackgroundColor(Color.TRANSPARENT)
                }

                // Disable click interactions if shop is not active
                val isClickable = isActive
                root.isEnabled = isClickable
                btnViewDetails.isEnabled = isClickable

                // Set alpha to indicate disabled state
                root.alpha = if (isClickable) 1.0f else 0.6f

                // Change button text/appearance based on status
                btnViewDetails.text = when {
                    isActive -> "VIEW SHOP"
                    shop.subscriptionStatus.equals("expired", ignoreCase = true) -> "RENEW"
                    else -> "SUBSCRIBE"
                }

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