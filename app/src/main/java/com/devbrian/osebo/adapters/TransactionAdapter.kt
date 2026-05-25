package com.devbrian.osebo.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.models.Transaction
import java.util.*



class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit = { _ -> },
    private val onMoreOptionsClick: (Transaction, View) -> Unit = { _, _ -> }
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    var showAttachments: Boolean = false
    var showNotes: Boolean = false
    var currencySymbol: String = "UGX"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transactions, parent, false)
        return TransactionViewHolder(view, onItemClick, onMoreOptionsClick, showAttachments, showNotes, currencySymbol)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TransactionViewHolder(
        itemView: View,
        private val onItemClick: (Transaction) -> Unit,
        private val onMoreOptionsClick: (Transaction, View) -> Unit,
        private val showAttachments: Boolean,
        private val showNotes: Boolean,
        private val currencySymbol: String
    ) : RecyclerView.ViewHolder(itemView) {

        
        private val llTransactionIcon: LinearLayout = itemView.findViewById(R.id.ll_transaction_icon)
        private val ivTransactionType: ImageView = itemView.findViewById(R.id.iv_transaction_type)
        private val tvTransactionDescription: TextView = itemView.findViewById(R.id.tv_transaction_description)
        private val tvTransactionCategory: TextView = itemView.findViewById(R.id.tv_transaction_category)
        private val tvTransactionAmount: TextView = itemView.findViewById(R.id.tv_transaction_amount)
        private val tvTransactionDate: TextView = itemView.findViewById(R.id.tv_transaction_date)
        private val tvPaymentMethod: TextView = itemView.findViewById(R.id.tv_payment_method)
        private val tvTransactionId: TextView = itemView.findViewById(R.id.tv_transaction_id)
        private val tvTransactionStatus: TextView = itemView.findViewById(R.id.tv_transaction_status)
        private val ivMoreOptions: ImageView = itemView.findViewById(R.id.iv_more_options)
        private val llAttachments: LinearLayout = itemView.findViewById(R.id.ll_attachments)
        private val tvAttachmentCount: TextView = itemView.findViewById(R.id.tv_attachment_count)
        private val tvTransactionNotes: TextView = itemView.findViewById(R.id.tv_transaction_notes)

        private var currentTransaction: Transaction? = null

        init {
            itemView.setOnClickListener {
                currentTransaction?.let { transaction ->
                    onItemClick(transaction)
                }
            }

            ivMoreOptions.setOnClickListener { view ->
                currentTransaction?.let { transaction ->
                    onMoreOptionsClick(transaction, view)
                }
            }

            llAttachments.visibility = if (showAttachments) View.VISIBLE else View.GONE
            tvTransactionNotes.visibility = if (showNotes) View.VISIBLE else View.GONE
        }

        fun bind(transaction: Transaction) {
            currentTransaction = transaction
            setTransactionType(transaction.type)

            tvTransactionDescription.text = transaction.description
            tvTransactionCategory.text = transaction.category
            tvTransactionDate.text = transaction.date
            tvPaymentMethod.text = transaction.paymentMethod ?: "Not specified"
            tvTransactionId.text = "#${transaction.id}"
            tvTransactionAmount.text = formatCurrency(transaction.amount, transaction.type)
            setTransactionStatus(transaction.status)

            if (showAttachments && transaction.attachmentsCount ?: 0 > 0) {
                llAttachments.visibility = View.VISIBLE
                tvAttachmentCount.text = "${transaction.attachmentsCount} attachment${if ((transaction.attachmentsCount ?: 0) > 1) "s" else ""}"
            } else {
                llAttachments.visibility = View.GONE
            }

            if (showNotes && !transaction.notes.isNullOrEmpty()) {
                tvTransactionNotes.visibility = View.VISIBLE
                tvTransactionNotes.text = transaction.notes
            } else {
                tvTransactionNotes.visibility = View.GONE
            }

            if (transaction.amount > 1000000) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.high_value_transaction))
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }

            if (transaction.category.equals("tax", ignoreCase = true)) {
                tvTransactionCategory.setTextColor(ContextCompat.getColor(itemView.context, R.color.red_error))
            } else {
                tvTransactionCategory.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
            }

            if (transaction.isRecurring == true) {
                llTransactionIcon.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_transaction_recurring)
            } else {
                llTransactionIcon.background = null
            }
        }

        private fun setTransactionType(type: String) {
            val context = itemView.context
            when (type.uppercase()) {
                Transaction.TYPE_INCOME -> {
                    llTransactionIcon.setBackgroundResource(R.drawable.bg_transaction_income)
                    ivTransactionType.setImageResource(R.drawable.ic_income)
                    ivTransactionType.setColorFilter(ContextCompat.getColor(context, R.color.white))
                }
                Transaction.TYPE_EXPENSE -> {
                    llTransactionIcon.setBackgroundResource(R.drawable.bg_transaction_expense)
                    ivTransactionType.setImageResource(R.drawable.ic_expense)
                    ivTransactionType.setColorFilter(ContextCompat.getColor(context, R.color.white))
                }
                Transaction.TYPE_TRANSFER -> {
                    llTransactionIcon.setBackgroundResource(R.drawable.bg_transaction_transfer)
                    ivTransactionType.setImageResource(R.drawable.ic_transfer)
                    ivTransactionType.setColorFilter(ContextCompat.getColor(context, R.color.white))
                }
                else -> {
                    llTransactionIcon.setBackgroundResource(R.drawable.bg_transaction_default)
                    ivTransactionType.setImageResource(R.drawable.ic_transaction)
                    ivTransactionType.setColorFilter(ContextCompat.getColor(context, R.color.white))
                }
            }
        }

        private fun setTransactionStatus(status: String?) {
            val context = itemView.context
            val statusText = status ?: "Completed"
            tvTransactionStatus.text = statusText

            val backgroundRes = when (status?.uppercase()) {
                Transaction.STATUS_COMPLETED -> R.drawable.bg_status_completed
                Transaction.STATUS_PENDING -> R.drawable.bg_status_pending
                Transaction.STATUS_FAILED -> R.drawable.bg_status_failed
                Transaction.STATUS_REFUNDED -> R.drawable.bg_status_refunded
                Transaction.STATUS_CANCELLED -> R.drawable.bg_status_cancelled
                else -> R.drawable.bg_status_completed
            }

            tvTransactionStatus.setBackgroundResource(backgroundRes)
            tvTransactionStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
        }

        private fun formatCurrency(amount: Double, type: String): String {
            val formattedAmount = when {
                amount >= 1000000 -> String.format("%s %.1fM", currencySymbol, amount / 1000000)
                amount >= 1000 -> String.format("%s %.1fK", currencySymbol, amount / 1000)
                else -> String.format("%s %,.0f", currencySymbol, amount)
            }

            val colorRes = when (type.uppercase()) {
                Transaction.TYPE_INCOME -> R.color.green_success
                Transaction.TYPE_EXPENSE -> R.color.red_error
                Transaction.TYPE_TRANSFER -> R.color.blue_info
                else -> R.color.text_primary
            }

            tvTransactionAmount.setTextColor(ContextCompat.getColor(itemView.context, colorRes))

            return when (type.uppercase()) {
                Transaction.TYPE_INCOME -> "+$formattedAmount"
                Transaction.TYPE_EXPENSE -> "-$formattedAmount"
                Transaction.TYPE_TRANSFER -> "↔ $formattedAmount"
                else -> formattedAmount
            }
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean =
            oldItem == newItem
    }

    fun submitTransactionList(transactions: List<Transaction>) {
        submitList(transactions)
    }

    fun getTotalIncome(): Double =
        currentList.filter { it.type == Transaction.TYPE_INCOME }.sumOf { it.amount }

    fun getTotalExpenses(): Double =
        currentList.filter { it.type == Transaction.TYPE_EXPENSE }.sumOf { it.amount }

    fun getNetBalance(): Double = getTotalIncome() - getTotalExpenses()
}
