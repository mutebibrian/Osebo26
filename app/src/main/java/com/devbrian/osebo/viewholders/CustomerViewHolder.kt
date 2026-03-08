package com.devbrian.osebo.viewholders

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.CustomerUpdatePayload
import com.devbrian.osebo.models.Customer
import java.text.SimpleDateFormat
import java.util.Locale

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
            currentCustomer?.let(onItemClick)
        }

        ivMoreOptions.setOnClickListener { view ->
            currentCustomer?.let { onMoreOptionsClick(it, view) }
        }
    }

    fun bind(customer: Customer) {
        currentCustomer = customer

        
        tvCustomerInitial.text = getInitials(customer.name)
        tvCustomerInitial.setBackgroundColor(getAvatarColor(customer.name))

        
        tvCustomerName.text = customer.name
        tvCustomerEmail.text = customer.email ?: "No email"
        tvCustomerPhone.text = customer.phone ?: "No phone"

        
        tvTotalSpent.text = "UGX ${formatCurrency(customer.totalSpent)}"

        
        tvLastPurchase.text = "Last: ${formatDate(customer.lastPurchase)}"
        tvCustomerSince.text = "Since ${formatDate(customer.customerSince)}"

        
        tvLoyaltyPoints.text = "${customer.loyaltyPoints} pts"

        val purchaseCount = customer.totalPurchases
        tvPurchaseCount.text =
            "$purchaseCount ${if (purchaseCount == 1) "purchase" else "purchases"}"

        
        val isVip = customer.customerType.equals("vip", ignoreCase = true) || customer.totalSpent > 500000
        if (isVip) {
            itemView.setBackgroundColor(
                ContextCompat.getColor(itemView.context, R.color.vip_customer_background)
            )
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    fun bindPartialUpdate(customer: Customer, payloads: List<Any>) {
        currentCustomer = customer

        payloads.forEach { payload ->
            when (payload) {

                is CustomerUpdatePayload.TotalSpent -> {
                    tvTotalSpent.text = "UGX ${formatCurrency(payload.newTotalSpent)}"

                    val isVip = customer.customerType.equals("vip", ignoreCase = true) || payload.newTotalSpent > 500000
                    if (isVip) {
                        itemView.setBackgroundColor(
                            ContextCompat.getColor(itemView.context, R.color.vip_customer_background)
                        )
                    } else {
                        itemView.setBackgroundColor(Color.TRANSPARENT)
                    }
                }

                is CustomerUpdatePayload.LastPurchase -> {
                    tvLastPurchase.text = "Last: ${formatDate(payload.newLastPurchase)}"
                }

                is CustomerUpdatePayload.LoyaltyPoints -> {
                    tvLoyaltyPoints.text = "${payload.newLoyaltyPoints} pts"
                }

                is CustomerUpdatePayload.Status -> {
                    
                }
            }
        }
    }

    

    private fun getInitials(name: String): String {
        if (name.isBlank()) return "?"

        val parts = name.trim().split("\\s+".toRegex())
        return when {
            parts.size >= 2 -> "${parts[0][0]}${parts[1][0]}".uppercase()
            else -> parts[0][0].toString().uppercase()
        }
    }

    private fun getAvatarColor(name: String): Int {
        val colors = listOf(
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#FFD166"),
            Color.parseColor("#06D6A0"),
            Color.parseColor("#118AB2"),
            Color.parseColor("#EF476F"),
            Color.parseColor("#073B4C")
        )

        val index = kotlin.math.abs(name.hashCode()) % colors.size
        return colors[index]
    }

    private fun formatCurrency(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format("%.1fK", amount / 1_000)
            else -> String.format("%.0f", amount)
        }
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "Never"

        return try {
            
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val output = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val date = input.parse(dateString)
            if (date != null) output.format(date) else dateString.take(10)
        } catch (e: Exception) {
            dateString.take(10)
        }
    }
}

