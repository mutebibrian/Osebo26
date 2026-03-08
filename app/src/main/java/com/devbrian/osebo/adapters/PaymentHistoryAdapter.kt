package com.devbrian.osebo.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.databinding.ItemPaymentHistoryBinding
import com.devbrian.osebo.models.PaymentHistory

class PaymentHistoryAdapter(
    private val onItemClick: (PaymentHistory) -> Unit
) : ListAdapter<PaymentHistory, PaymentHistoryAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaymentViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PaymentViewHolder(
        private val binding: ItemPaymentHistoryBinding,
        private val onItemClick: (PaymentHistory) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: PaymentHistory) {
            binding.apply {
                tvAmount.text = payment.displayAmount
                tvStatus.text = payment.displayStatus
                tvDate.text = payment.displayDate
                tvMethod.text = payment.displayMethod
                tvReference.text = "Ref: ${payment.reference ?: payment.transactionId ?: "N/A"}"

                // Set status color
                tvStatus.setTextColor(
                    root.context.getColor(
                        when {
                            payment.isSuccessful -> android.R.color.holo_green_dark
                            payment.isPending -> android.R.color.holo_orange_dark
                            payment.isFailed -> android.R.color.holo_red_dark
                            else -> android.R.color.darker_gray
                        }
                    )
                )

                root.setOnClickListener {
                    onItemClick(payment)
                }
            }
        }
    }

    class PaymentDiffCallback : DiffUtil.ItemCallback<PaymentHistory>() {
        override fun areItemsTheSame(oldItem: PaymentHistory, newItem: PaymentHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PaymentHistory, newItem: PaymentHistory): Boolean {
            return oldItem == newItem
        }
    }
}