package com.devbrian.osebo.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.models.Customer
import java.text.SimpleDateFormat
import java.util.Locale

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

            
            tvCustomerInitial.text = getInitials(customer.name)
            tvCustomerInitial.setBackgroundColor(getAvatarColor(customer.name))

            
            tvCustomerName.text = customer.name
            tvCustomerEmail.text = customer.email ?: "No email"
            tvCustomerPhone.text = customer.phone ?: "No phone"

            
            val totalAmount = if (customer.totalSpent > 0) customer.totalSpent else customer.totalPurchases
            tvTotalSpent.text = "UGX ${formatCurrency(totalAmount)}"

            
            val lastPurchaseFormatted = formatDate(customer.lastPurchase)
            val customerSinceFormatted = formatDate(customer.customerSince)

            tvLastPurchase.text = "Last: $lastPurchaseFormatted"
            tvCustomerSince.text = "Since $customerSinceFormatted"

            
            tvLoyaltyPoints.text = "${customer.loyaltyPoints} pts"

            val purchaseCount = customer.totalPurchases.toInt()
            tvPurchaseCount.text =
                "$purchaseCount ${if (purchaseCount == 1) "purchase" else "purchases"}"

            
            
            val totalAmountDouble = totalAmount.toDouble()
            val isVip = customer.customerType.lowercase() == "vip" || totalAmountDouble > 500000.0
            if (isVip) {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.vip_customer_background)
                )
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
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

        fun bindPartialUpdate(customer: Customer, payloads: List<Any>) {
            currentCustomer = customer

            payloads.forEach { payload ->
                when (payload) {
                    is CustomerUpdatePayload.TotalSpent -> {
                        val formattedAmount = formatCurrency(payload.newTotalSpent)
                        tvTotalSpent.text = "UGX $formattedAmount"

                        
                        
                        val isVip = customer.customerType.lowercase() == "vip" || payload.newTotalSpent > 500000.0
                        if (isVip) {
                            itemView.setBackgroundColor(
                                ContextCompat.getColor(itemView.context, R.color.vip_customer_background)
                            )
                        } else {
                            itemView.setBackgroundColor(Color.TRANSPARENT)
                        }
                    }
                    is CustomerUpdatePayload.LastPurchase -> {
                        val formattedDate = formatDate(payload.newLastPurchase)
                        tvLastPurchase.text = "Last: $formattedDate"
                    }
                    is CustomerUpdatePayload.LoyaltyPoints -> {
                        tvLoyaltyPoints.text = "${payload.newLoyaltyPoints} pts"
                    }
                    is CustomerUpdatePayload.Status -> {
                        
                    }
                }
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

            val index = name.hashCode() % colors.size
            return colors[Math.abs(index)]
        }

        private fun formatCurrency(amount: Any): String {
            return try {
                
                val amountDouble = when (amount) {
                    is Int -> amount.toDouble()
                    is Long -> amount.toDouble()
                    is Float -> amount.toDouble()
                    is Double -> amount
                    is String -> amount.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }

                when {
                    amountDouble >= 1000000.0 -> String.format(Locale.getDefault(), "%.1fM", amountDouble / 1000000.0)
                    amountDouble >= 1000.0 -> String.format(Locale.getDefault(), "%.1fK", amountDouble / 1000.0)
                    else -> String.format(Locale.getDefault(), "%.0f", amountDouble)
                }
            } catch (e: Exception) {
                "0"
            }
        }

        private fun formatDate(dateString: String?): String {
            if (dateString.isNullOrEmpty()) return "Never"

            return try {
                
                
                val dateFormats = listOf(
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                    SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                )

                var parsedDate: java.util.Date? = null
                for (format in dateFormats) {
                    try {
                        parsedDate = format.parse(dateString)
                        if (parsedDate != null) break
                    } catch (e: Exception) {
                        
                    }
                }

                if (parsedDate != null) {
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    outputFormat.format(parsedDate)
                } else {
                    
                    if (dateString.length > 10) dateString.substring(0, 10) else dateString
                }
            } catch (e: Exception) {
                
                if (dateString.length > 10) dateString.substring(0, 10) else dateString
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
            val payloads = mutableListOf<Any>()

            if (oldItem.totalSpent != newItem.totalSpent || oldItem.totalPurchases != newItem.totalPurchases) {
                payloads.add(CustomerUpdatePayload.TotalSpent(newItem.totalSpent))
            }

            if (oldItem.lastPurchase != newItem.lastPurchase) {
                payloads.add(CustomerUpdatePayload.LastPurchase(newItem.lastPurchase ?: ""))
            }

            if (oldItem.loyaltyPoints != newItem.loyaltyPoints) {
                payloads.add(CustomerUpdatePayload.LoyaltyPoints(newItem.loyaltyPoints))
            }

            if (oldItem.status != newItem.status) {
                payloads.add(CustomerUpdatePayload.Status(newItem.status))
            }

            return if (payloads.isNotEmpty()) payloads else null
        }
    }

    
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

    
    fun filterCustomers(query: String, originalList: List<Customer>): List<Customer> {
        return if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter { customer ->
                customer.name.contains(query, ignoreCase = true) ||
                        customer.email?.contains(query, ignoreCase = true) == true ||
                        customer.phone?.contains(query, ignoreCase = true) == true
            }
        }
    }

    
    fun sortCustomers(customers: List<Customer>, sortBy: String, ascending: Boolean = true): List<Customer> {
        return when (sortBy.lowercase()) {
            "name" -> {
                if (ascending) customers.sortedBy { it.name }
                else customers.sortedByDescending { it.name }
            }
            "totalspent" -> {
                if (ascending) {
                    customers.sortedBy { customer ->
                        
                        if (customer.totalSpent > 0) customer.totalSpent else customer.totalPurchases.toDouble()
                    }
                } else {
                    customers.sortedByDescending { customer ->
                        
                        if (customer.totalSpent > 0) customer.totalSpent else customer.totalPurchases.toDouble()
                    }
                }
            }
            "lastpurchase" -> {
                
                if (ascending) {
                    customers.sortedWith(compareBy(nullsLast()) { it.lastPurchase })
                } else {
                    customers.sortedWith(compareByDescending(nullsLast()) { it.lastPurchase })
                }
            }
            "customersince" -> {
                
                if (ascending) {
                    customers.sortedWith(compareBy(nullsLast()) { it.customerSince })
                } else {
                    customers.sortedWith(compareByDescending(nullsLast()) { it.customerSince })
                }
            }
            else -> customers
        }
    }
}



sealed class CustomerUpdatePayload {
    data class TotalSpent(val newTotalSpent: Double) : CustomerUpdatePayload()
    data class LastPurchase(val newLastPurchase: String) : CustomerUpdatePayload()
    data class LoyaltyPoints(val newLoyaltyPoints: Int) : CustomerUpdatePayload()
    data class Status(val newStatus: String) : CustomerUpdatePayload()
}

