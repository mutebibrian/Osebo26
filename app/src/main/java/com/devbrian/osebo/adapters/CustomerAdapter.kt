package com.devbrian.osebo.adapters


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.models.Customer


class CustomerAdapter(
    private val onItemClick: (Customer) -> Unit,
    private val onMoreOptionsClick: (Customer, View) -> Unit = { _, _ -> }
) : ListAdapter<Customer, CustomerAdapter.CustomerViewHolder>(CustomerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return CustomerViewHolder(view, onItemClick, onMoreOptionsClick)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            // Handle partial updates if needed
            holder.bindPartialUpdate(getItem(position), payloads)
        }
    }

    class CustomerViewHolder(
        itemView: View,
        private val onItemClick: (Customer) -> Unit,
        private val onMoreOptionsClick: (Customer, View) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvCustomerInitial: TextView = itemView.findViewById(R.id.tv_customer_initial)
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tv_customer_name)
        private val tvCustomerEmail: TextView = itemView.findViewById(R.id.tv_customer_email)
        private val tvCustomerPhone: TextView = itemView.findViewById(R.id.tv_customer_phone)
        private val tvTotalSpent: TextView = itemView.findViewById(R.id.tv_total_spent)
        private val tvLastPurchase: TextView = itemView.findViewById(R.id.tv_last_purchase)
        private val tvLoyaltyPoints: TextView = itemView.findViewById(R.id.tv_loyalty_points)
        private val tvPurchaseCount: TextView = itemView.findViewById(R.id.tv_purchase_count)
        private val tvCustomerSince: TextView = itemView.findViewById(R.id.tv_customer_since)
        private val ivMoreOptions: ImageView = itemView.findViewById(R.id.iv_more_options)

        private var currentCustomer: Customer? = null

        init {
            itemView.setOnClickListener {
                currentCustomer?.let { customer ->
                    onItemClick(customer)
                }
            }

            ivMoreOptions.setOnClickListener { view ->
                currentCustomer?.let { customer ->
                    onMoreOptionsClick(customer, view)
                }
            }
        }

        fun bind(customer: Customer) {
            currentCustomer = customer

            // Set customer initial with random color
            val initial = customer.name.first().toString().uppercase()
            tvCustomerInitial.text = initial
            tvCustomerInitial.setBackgroundColor(getAvatarColor(customer.name))

            // Set customer details
            tvCustomerName.text = customer.name
            tvCustomerEmail.text = customer.email
            tvCustomerPhone.text = customer.phone

            // Format currency
            val formattedAmount = formatCurrency(customer.totalSpent)
            tvTotalSpent.text = "UGX $formattedAmount"

            // Set other details
            tvLastPurchase.text = customer.lastPurchase
            tvLoyaltyPoints.text = customer.loyaltyPoints.toString()
            tvPurchaseCount.text = "${customer.totalPurchases} ${if (customer.totalPurchases == 1) "purchase" else "purchases"}"
            tvCustomerSince.text = "Customer since ${customer.customerSince}"

            // Highlight VIP customers (spent more than 500,000)
            if (customer.totalSpent > 500000) {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.vip_customer_background)
                )
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        fun bindPartialUpdate(customer: Customer, payloads: List<Any>) {
            // Handle partial updates here if needed
            // For example, update only specific fields
            currentCustomer = customer

            // Check payloads and update specific views
            payloads.forEach { payload ->
                when (payload) {
                    is CustomerUpdatePayload.TotalSpent -> {
                        val formattedAmount = formatCurrency(payload.newTotalSpent)
                        tvTotalSpent.text = "UGX $formattedAmount"
                    }
                    is CustomerUpdatePayload.LastPurchase -> {
                        tvLastPurchase.text = payload.newLastPurchase
                    }
                    is CustomerUpdatePayload.LoyaltyPoints -> {
                        tvLoyaltyPoints.text = payload.newLoyaltyPoints.toString()
                    }
                }
            }
        }

        private fun getAvatarColor(name: String): Int {
            // Generate consistent color based on customer name
            val colors = listOf(
                Color.parseColor("#FF6B6B"), // Coral Red
                Color.parseColor("#4ECDC4"), // Tiffany Blue
                Color.parseColor("#FFD166"), // Sunglow
                Color.parseColor("#06D6A0"), // Emerald
                Color.parseColor("#118AB2"), // Blue NCS
                Color.parseColor("#EF476F"), // Paradise Pink
                Color.parseColor("#073B4C")  // Midnight Green
            )

            val index = name.hashCode() % colors.size
            return colors[Math.abs(index)]
        }

        private fun formatCurrency(amount: Double): String {
            return when {
                amount >= 1000000 -> String.format("%.1fM", amount / 1000000)
                amount >= 1000 -> String.format("%.1fK", amount / 1000)
                else -> String.format("%.0f", amount)
            }
        }
    }

    private class CustomerDiffCallback : DiffUtil.ItemCallback<Customer>() {
        override fun areItemsTheSame(oldItem: Customer, newItem: Customer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Customer, newItem: Customer): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Customer, newItem: Customer): Any? {
            // Return specific payload for partial updates
            val payloads = mutableListOf<Any>()

            if (oldItem.totalSpent != newItem.totalSpent) {
                payloads.add(CustomerUpdatePayload.TotalSpent(newItem.totalSpent))
            }

            if (oldItem.lastPurchase != newItem.lastPurchase) {
                payloads.add(CustomerUpdatePayload.LastPurchase(newItem.lastPurchase))
            }

            if (oldItem.loyaltyPoints != newItem.loyaltyPoints) {
                payloads.add(CustomerUpdatePayload.LoyaltyPoints(newItem.loyaltyPoints))
            }

            return if (payloads.isNotEmpty()) payloads else null
        }
    }

    // Extension function for easy submission
    fun submitCustomerList(customers: List<Customer>) {
        submitList(customers)
    }

    fun getCustomerAtPosition(position: Int): Customer? {
        return if (position in 0 until itemCount) {
            getItem(position)
        } else {
            null
        }
    }
}

// Payload classes for partial updates
sealed class CustomerUpdatePayload {
    data class TotalSpent(val newTotalSpent: Double) : CustomerUpdatePayload()
    data class LastPurchase(val newLastPurchase: String) : CustomerUpdatePayload()
    data class LoyaltyPoints(val newLoyaltyPoints: Int) : CustomerUpdatePayload()
}