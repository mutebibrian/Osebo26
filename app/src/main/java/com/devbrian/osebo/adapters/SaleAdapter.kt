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
import com.devbrian.osebo.models.Sale


class SaleAdapter(
    private val onItemClick: (Sale) -> Unit,
    private val onViewDetailsClick: (Sale) -> Unit = { _ -> },
    private val onPrintReceiptClick: (Sale) -> Unit = { _ -> }
) : ListAdapter<Sale, SaleAdapter.SaleViewHolder>(SaleDiffCallback()) {

    var showFooterActions: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sale, parent, false)
        return SaleViewHolder(view, onItemClick, onViewDetailsClick, onPrintReceiptClick, showFooterActions)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SaleViewHolder(
        itemView: View,
        private val onItemClick: (Sale) -> Unit,
        private val onViewDetailsClick: (Sale) -> Unit,
        private val onPrintReceiptClick: (Sale) -> Unit,
        private val showFooterActions: Boolean
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvSaleId: TextView = itemView.findViewById(R.id.tv_sale_id)
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tv_customer_name)
        private val tvSaleAmount: TextView = itemView.findViewById(R.id.tv_sale_amount)
        private val tvSaleDate: TextView = itemView.findViewById(R.id.tv_sale_date)
        private val tvSaleTime: TextView = itemView.findViewById(R.id.tv_sale_time)
        private val tvItemsCount: TextView = itemView.findViewById(R.id.tv_items_count)
        private val tvPaymentMethod: TextView = itemView.findViewById(R.id.tv_payment_method)
        private val tvSaleStatus: TextView = itemView.findViewById(R.id.tv_sale_status)
        private val ivSaleType: ImageView = itemView.findViewById(R.id.iv_sale_type)
        private val llSaleFooter: View = itemView.findViewById(R.id.ll_sale_footer)
        private val tvViewDetails: TextView = itemView.findViewById(R.id.tv_view_details)
        private val tvPrintReceipt: TextView = itemView.findViewById(R.id.tv_print_receipt)

        private var currentSale: Sale? = null

        init {
            itemView.setOnClickListener {
                currentSale?.let { sale ->
                    onItemClick(sale)
                }
            }

            tvViewDetails.setOnClickListener {
                currentSale?.let { sale ->
                    onViewDetailsClick(sale)
                }
            }

            tvPrintReceipt.setOnClickListener {
                currentSale?.let { sale ->
                    onPrintReceiptClick(sale)
                }
            }

            // Show/hide footer based on configuration
            llSaleFooter.visibility = if (showFooterActions) View.VISIBLE else View.GONE
        }

        fun bind(sale: Sale) {
            currentSale = sale

            // Set basic sale info
            tvSaleId.text = sale.id.takeLast(8)
            tvCustomerName.text = sale.customerName

            // Handle zero amounts specially
            if (sale.amount == 0.0) {
                tvSaleAmount.text = "Quotation"
                tvSaleAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.orange_warning))
                // Optionally show a badge or different background
            } else {
                tvSaleAmount.text = formatCurrency(sale.amount)
                tvSaleAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
            }

            // Use formatted date and time
            tvSaleDate.text = sale.getFormattedDate()
            tvSaleTime.text = sale.getFormattedTime()

            tvItemsCount.text = "${sale.itemsCount} ${if (sale.itemsCount == 1) "item" else "items"}"
            tvPaymentMethod.text = sale.paymentMethod ?: "Cash"

            // Set sale status with appropriate color
            tvSaleStatus.text = sale.status
            setStatusBackground(sale.status)

            // Set sale type icon based on payment method
            setSaleTypeIcon(sale.paymentMethod)

            // Style based on amount
            if (sale.amount == 0.0) {
                itemView.alpha = 0.7f
            } else {
                itemView.alpha = 1.0f
            }

            // Highlight high-value sales
            if (sale.amount > 100000) {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.high_value_sale_background)
                )
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        private fun setStatusBackground(status: String) {
            val context = itemView.context
            val normalizedStatus = status.uppercase()

            val backgroundRes = when (normalizedStatus) {
                "COMPLETED" -> R.drawable.bg_status_completed
                "PENDING" -> R.drawable.bg_status_pending
                "CANCELLED" -> R.drawable.bg_status_cancelled
                "REFUNDED" -> R.drawable.bg_status_refunded
                "PARTIAL" -> R.drawable.bg_status_partial
                else -> R.drawable.bg_status_pending
            }

            tvSaleStatus.setBackgroundResource(backgroundRes)
            tvSaleStatus.text = normalizedStatus
        }

        private fun setSaleTypeIcon(paymentMethod: String?) {
            val iconRes = when (paymentMethod?.uppercase()) {
                "CASH" -> R.drawable.ic_cash
                "CARD" -> R.drawable.ic_credit_card
                "MOBILE MONEY" -> R.drawable.ic_mobile_money
                "BANK TRANSFER" -> R.drawable.ic_bank_transfer
                "CREDIT" -> R.drawable.ic_credit
                else -> R.drawable.ic_sale
            }

            val iconColor = when (paymentMethod?.uppercase()) {
                "CASH" -> Color.parseColor("#4CAF50")
                "CARD" -> Color.parseColor("#2196F3")
                "MOBILE MONEY" -> Color.parseColor("#FF9800")
                "BANK TRANSFER" -> Color.parseColor("#9C27B0")
                "CREDIT" -> Color.parseColor("#F44336")
                else -> Color.parseColor("#2196F3")
            }

            ivSaleType.setImageResource(iconRes)
            ivSaleType.setColorFilter(iconColor)
        }

        private fun formatCurrency(amount: Double): String {
            return when {
                amount >= 1000000 -> String.format("UGX %.1fM", amount / 1000000)
                amount >= 1000 -> String.format("UGX %.1fK", amount / 1000)
                else -> String.format("UGX %.0f", amount)
            }
        }
    }

    private class SaleDiffCallback : DiffUtil.ItemCallback<Sale>() {
        override fun areItemsTheSame(oldItem: Sale, newItem: Sale): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Sale, newItem: Sale): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Sale, newItem: Sale): Any? {
            val payloads = mutableListOf<String>()

            if (oldItem.status != newItem.status) {
                payloads.add("status")
            }

            if (oldItem.amount != newItem.amount) {
                payloads.add("amount")
            }

            return if (payloads.isNotEmpty()) payloads else null
        }
    }

    // Helper methods
    fun getSaleAtPosition(position: Int): Sale? {
        return if (position in 0 until itemCount) {
            getItem(position)
        } else {
            null
        }
    }

    fun filterSales(query: String, sales: List<Sale>): List<Sale> {
        return if (query.isEmpty()) {
            sales
        } else {
            sales.filter { sale ->
                sale.id.contains(query, ignoreCase = true) ||
                        sale.customerName.contains(query, ignoreCase = true) ||
                        sale.status.contains(query, ignoreCase = true) ||
                        (sale.paymentMethod?.contains(query, ignoreCase = true) ?: false)
            }
        }
    }

    fun sortSalesByDate(sales: List<Sale>, ascending: Boolean = false): List<Sale> {
        return if (ascending) {
            sales.sortedBy { it.date }
        } else {
            sales.sortedByDescending { it.date }
        }
    }

    fun sortSalesByAmount(sales: List<Sale>, ascending: Boolean = false): List<Sale> {
        return if (ascending) {
            sales.sortedBy { it.amount }
        } else {
            sales.sortedByDescending { it.amount }
        }
    }
}