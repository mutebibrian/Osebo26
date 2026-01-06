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
import java.text.NumberFormat
import java.util.*

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit = { _ -> },
    private val onMoreOptionsClick: (Transaction, View) -> Unit = { _, _ -> }
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    var showAttachments: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var showNotes: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var currencySymbol: String = "UGX"
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
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

            // Show/hide optional sections
            llAttachments.visibility = if (showAttachments) View.VISIBLE else View.GONE
            tvTransactionNotes.visibility = if (showNotes) View.VISIBLE else View.GONE
        }

        fun bind(transaction: Transaction) {
            currentTransaction = transaction

            // Set transaction type styling
            setTransactionType(transaction.type)

            // Set transaction details
            tvTransactionDescription.text = transaction.description
            tvTransactionCategory.text = transaction.category
            tvTransactionDate.text = transaction.date
            tvPaymentMethod.text = transaction.paymentMethod ?: "Not specified"
            tvTransactionId.text = "#${transaction.id}"

            // Format amount with currency
            tvTransactionAmount.text = formatCurrency(transaction.amount, transaction.type)

            // Set transaction status
            setTransactionStatus(transaction.status)

            // Show attachments if any
            if (showAttachments && transaction.attachmentsCount ?: 0 > 0) {
                llAttachments.visibility = View.VISIBLE
                tvAttachmentCount.text = "${transaction.attachmentsCount} attachment${if ((transaction.attachmentsCount ?: 0) > 1) "s" else ""}"
            }

            // Show notes if any
            if (showNotes && !transaction.notes.isNullOrEmpty()) {
                tvTransactionNotes.visibility = View.VISIBLE
                tvTransactionNotes.text = transaction.notes
            }

            // Highlight large transactions
            if (transaction.amount > 1000000) {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.high_value_transaction)
                )
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }

            // Highlight tax transactions
            if (transaction.category.equals("tax", ignoreCase = true)) {
                tvTransactionCategory.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.red_error)
                )
            }

            // Highlight recurring transactions
            if (transaction.isRecurring == true) {
                itemView.findViewById<View>(R.id.ll_transaction_icon).background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.bg_transaction_recurring)
            }
        }

        private fun setTransactionType(type: String) {
            val context = itemView.context

            when (type.uppercase()) {
                Transaction.TYPE_INCOME -> {
                    // Income - Green background
                    llTransactionIcon.setBackgroundResource(R.drawable.bg_transaction_income)
                    ivTransactionType.setImageResource(R.drawable.ic_income)
                    ivTransactionType.setColorFilter(ContextCompat.getColor(context, R.color.white))
                }
                Transaction.TYPE_EXPENSE -> {
                    // Expense - Red background
                    llTransactionIcon.setBackgroundResource(R.drawable.bg_transaction_expense)
                    ivTransactionType.setImageResource(R.drawable.ic_expense)
                    ivTransactionType.setColorFilter(ContextCompat.getColor(context, R.color.white))
                }
                Transaction.TYPE_TRANSFER -> {
                    // Transfer - Blue background
                    llTransactionIcon.setBackgroundResource(R.drawable.bg_transaction_transfer)
                    ivTransactionType.setImageResource(R.drawable.ic_transfer)
                    ivTransactionType.setColorFilter(ContextCompat.getColor(context, R.color.white))
                }
                else -> {
                    // Default - Grey background
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

            val (backgroundRes, textColorRes) = when (status?.uppercase()) {
                Transaction.STATUS_COMPLETED -> Pair(R.drawable.bg_status_completed, R.color.white)
                Transaction.STATUS_PENDING -> Pair(R.drawable.bg_status_pending, R.color.white)
                Transaction.STATUS_FAILED -> Pair(R.drawable.bg_status_failed, R.color.white)
                Transaction.STATUS_REFUNDED -> Pair(R.drawable.bg_status_refunded, R.color.white)
                Transaction.STATUS_CANCELLED -> Pair(R.drawable.bg_status_cancelled, R.color.white)
                else -> Pair(R.drawable.bg_status_completed, R.color.white)
            }

            tvTransactionStatus.setBackgroundResource(backgroundRes)
            tvTransactionStatus.setTextColor(ContextCompat.getColor(context, textColorRes))
        }

        private fun formatCurrency(amount: Double, type: String): String {
            val formattedAmount = when {
                amount >= 1000000 -> String.format("%s %.1fM", currencySymbol, amount / 1000000)
                amount >= 1000 -> String.format("%s %.1fK", currencySymbol, amount / 1000)
                else -> String.format("%s %,.0f", currencySymbol, amount)
            }

            // Color code based on transaction type
            val colorRes = when (type.uppercase()) {
                Transaction.TYPE_INCOME -> R.color.green_success
                Transaction.TYPE_EXPENSE -> R.color.red_error
                Transaction.TYPE_TRANSFER -> R.color.blue_info
                else -> R.color.text_primary
            }

            tvTransactionAmount.setTextColor(ContextCompat.getColor(itemView.context, colorRes))

            // Add +/- sign
            return when (type.uppercase()) {
                Transaction.TYPE_INCOME -> "+$formattedAmount"
                Transaction.TYPE_EXPENSE -> "-$formattedAmount"
                Transaction.TYPE_TRANSFER -> "↔ $formattedAmount"
                else -> formattedAmount
            }
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }

    // ========== PUBLIC HELPER METHODS ==========

    fun getTransactionAtPosition(position: Int): Transaction? {
        return if (position in 0 until itemCount) {
            getItem(position)
        } else {
            null
        }
    }

    fun filterTransactions(query: String): List<Transaction> {
        return if (query.isEmpty()) {
            currentList
        } else {
            currentList.filter { transaction ->
                transaction.description.contains(query, ignoreCase = true) ||
                        transaction.category.contains(query, ignoreCase = true) ||
                        transaction.paymentMethod?.contains(query, ignoreCase = true) == true ||
                        transaction.id.contains(query, ignoreCase = true)
            }
        }
    }

    fun filterByType(type: String): List<Transaction> {
        return currentList.filter { it.type.equals(type, ignoreCase = true) }
    }

    fun filterByCategory(category: String): List<Transaction> {
        return currentList.filter { it.category.equals(category, ignoreCase = true) }
    }

    fun filterByStatus(status: String): List<Transaction> {
        return currentList.filter { it.status.equals(status, ignoreCase = true) }
    }

    fun getIncomeTransactions(): List<Transaction> {
        return currentList.filter { it.type.equals(Transaction.TYPE_INCOME, ignoreCase = true) }
    }

    fun getExpenseTransactions(): List<Transaction> {
        return currentList.filter { it.type.equals(Transaction.TYPE_EXPENSE, ignoreCase = true) }
    }

    fun getTransferTransactions(): List<Transaction> {
        return currentList.filter { it.type.equals(Transaction.TYPE_TRANSFER, ignoreCase = true) }
    }

    fun getTotalIncome(): Double {
        return getIncomeTransactions().sumOf { it.amount }
    }

    fun getTotalExpenses(): Double {
        return getExpenseTransactions().sumOf { it.amount }
    }

    fun getNetBalance(): Double {
        return getTotalIncome() - getTotalExpenses()
    }

    fun sortByDate(ascending: Boolean = true): List<Transaction> {
        return if (ascending) {
            currentList.sortedBy { it.date }
        } else {
            currentList.sortedByDescending { it.date }
        }
    }

    fun sortByAmount(ascending: Boolean = true): List<Transaction> {
        return if (ascending) {
            currentList.sortedBy { it.amount }
        } else {
            currentList.sortedByDescending { it.amount }
        }
    }

    fun sortByType(): List<Transaction> {
        return currentList.sortedBy { it.type }
    }

    fun getTransactionsByCategory(): Map<String, List<Transaction>> {
        return currentList.groupBy { it.category }
    }

    fun getCategoryTotals(): Map<String, Double> {
        return getTransactionsByCategory().mapValues { (_, transactions) ->
            transactions.sumOf {
                when (it.type) {
                    Transaction.TYPE_INCOME -> it.amount
                    Transaction.TYPE_EXPENSE -> -it.amount
                    else -> 0.0
                }
            }
        }
    }

    fun getMonthlySummary(): Map<String, Double> {
        return currentList.groupBy { it.date.substring(0, 7) } // Group by YYYY-MM
            .mapValues { (_, transactions) ->
                transactions.sumOf {
                    when (it.type) {
                        Transaction.TYPE_INCOME -> it.amount
                        Transaction.TYPE_EXPENSE -> -it.amount
                        else -> 0.0
                    }
                }
            }
    }

    fun submitTransactionList(transactions: List<Transaction>) {
        submitList(transactions)
    }

    fun clearAll() {
        submitList(emptyList())
    }
}