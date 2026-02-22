package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.databinding.ItemReceiptBinding
import com.devbrian.osebo.models.CartItem
import com.devbrian.osebo.utils.CurrencyFormatter

class ReceiptItemAdapter : ListAdapter<CartItem, ReceiptItemAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReceiptBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemReceiptBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.apply {
                tvItemName.text = item.product.name
                tvItemQuantity.text = item.quantity.toString()

                // Calculate item total
                val itemTotal = item.unitPrice * item.quantity * (1 - item.discount / 100)
                tvItemPrice.text = CurrencyFormatter.formatFull(itemTotal)

                // If there's a discount, show it
                if (item.discount > 0) {

                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.product.id == newItem.product.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }

    // Helper method to get total amount
    fun getTotalAmount(): Double {
        return currentList.sumOf { it.unitPrice * it.quantity * (1 - it.discount / 100) }
    }

    fun getItemCountValue(): Int {
        return currentList.size
    }

    // Helper method to get formatted item for printing
    fun getFormattedItemsForPrinting(): List<String> {
        return currentList.map { item ->
            val total = item.unitPrice * item.quantity * (1 - item.discount / 100)
            "${item.product.name} x${item.quantity} - ${CurrencyFormatter.formatFull(total)}"
        }
    }
}