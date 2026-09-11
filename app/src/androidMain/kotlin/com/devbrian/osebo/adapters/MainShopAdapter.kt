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
import com.devbrian.osebo.data.models.Shop
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
                // Basic shop info with null safety
                tvShopName.text = shop.name ?: "Unnamed Shop"
                tvShopLocation.text = shop.address?.takeIf { it.isNotEmpty() } ?: "Location not set"
                tvShopType.text = shop.shopTypeDisplay?.takeIf { it.isNotEmpty() } ?: "Shop"

                // Safe number formatting
                tvTodaySales.text = formatCurrency(shop.totalRevenue ?: 0.0)
                tvProductsCount.text = (shop.totalProducts ?: 0).toString()
                tvEmployeesCount.text = (shop.totalEmployees ?: 0).toString()

                // Status determination with null safety
                val status = shop.subscriptionStatus ?: "inactive"
                val isActive = status.equals("active", ignoreCase = true)
                val isTrial = status.equals("trial", ignoreCase = true)
                val isExpired = status.equals("expired", ignoreCase = true)
                val isClickable = isActive || isTrial

                // Status text and background based on subscription status
                val (statusText, statusBgRes, statusTextColor) = when {
                    isTrial -> Triple("TRIAL", R.drawable.bg_status_trial, R.color.white)
                    isActive -> Triple("ACTIVE", R.drawable.bg_status_active, R.color.white)
                    isExpired -> Triple("EXPIRED", R.drawable.bg_status_expired, R.color.white)
                    else -> Triple("INACTIVE", R.drawable.bg_status_inactive, R.color.white)
                }

                // Set status text and background
                tvShopStatus.text = statusText
                tvShopStatus.setTextColor(ContextCompat.getColor(root.context, statusTextColor))

                try {
                    tvShopStatus.setBackgroundResource(statusBgRes)
                } catch (e: Exception) {
                    // Fallback to colored background if drawable not found
                    val bgColor = when {
                        isTrial -> R.color.orange_warning
                        isActive -> R.color.green_success
                        isExpired -> R.color.red_error
                        else -> R.color.gray
                    }
                    tvShopStatus.setBackgroundColor(ContextCompat.getColor(root.context, bgColor))
                }

                // Shop initials - FIXED: Added null safety and empty string check
                val shopName = shop.name ?: "Shop"
                val initials = getInitials(shopName)
                tvShopInitial.text = initials

                // Set gradient background for shop initial
                try {
                    tvShopInitial.setBackgroundResource(R.drawable.bg_shop_initial_gradient)
                } catch (e: Exception) {
                    // Fallback to solid color
                    tvShopInitial.setBackgroundColor(Color.parseColor(getColorForShop(shopName)))
                }

                // Enable/disable based on subscription status
                root.isEnabled = isClickable
                btnViewDetails.isEnabled = isClickable
                root.alpha = if (isClickable) 1.0f else 0.6f

                // Change button text based on status
                btnViewDetails.text = when {
                    isTrial -> "VIEW SHOP"
                    isActive -> "VIEW SHOP"
                    isExpired -> "RENEW"
                    else -> "SUBSCRIBE"
                }
            }
        }

        /**
         * FIXED: Get initials from shop name with proper null/empty handling
         * This was causing the NoSuchElementException crash
         */
        private fun getInitials(name: String): String {
            // SAFETY CHECK: Handle empty or null names
            if (name.isEmpty()) {
                return "S"  // Default for "Shop"
            }

            val trimmed = name.trim()
            if (trimmed.isEmpty()) {
                return "S"
            }

            val words = trimmed.split(" ")

            return when (words.size) {
                1 -> {
                    val firstWord = words[0]
                    if (firstWord.isNotEmpty()) {
                        // Take first two letters if available, otherwise just first
                        when {
                            firstWord.length >= 2 -> firstWord.take(2).uppercase(Locale.getDefault())
                            firstWord.length == 1 -> firstWord.uppercase(Locale.getDefault())
                            else -> "S"
                        }
                    } else {
                        "S"
                    }
                }
                else -> {
                    // Get first letter of first and second word
                    val first = words[0].firstOrNull()?.toString() ?: ""
                    val second = words[1].firstOrNull()?.toString() ?: ""

                    if (first.isNotEmpty() && second.isNotEmpty()) {
                        (first + second).uppercase(Locale.getDefault())
                    } else if (first.isNotEmpty()) {
                        first.uppercase(Locale.getDefault())
                    } else {
                        "S"
                    }
                }
            }
        }

        /**
         * Generate a consistent color for a shop based on its name
         */
        private fun getColorForShop(name: String): String {
            val colors = listOf(
                "#FF6B6B", "#4ECDC4", "#FFD166", "#06D6A0",
                "#118AB2", "#EF476F", "#073B4C", "#FF9F1C"
            )
            val safeName = if (name.isEmpty()) "Shop" else name
            val index = safeName.hashCode() % colors.size
            return colors[Math.abs(index)]
        }

        /**
         * Format currency amount safely
         */
        private fun formatCurrency(amount: Double): String {
            return try {
                currencyFormatter.format(amount)
            } catch (e: Exception) {
                "UGX 0"
            }
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