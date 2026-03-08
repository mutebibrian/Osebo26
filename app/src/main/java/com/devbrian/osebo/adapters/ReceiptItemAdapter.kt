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
                // Item name
                tvItemName.text = item.product.name

                // Quantity
                tvItemQuantity.text = item.quantity.toString()

                // Unit price - show original and discounted if applicable
                val unitPrice = CurrencyFormatter.formatShort(item.unitPrice)
                tvItemPrice.text = if (item.discount > 0) {
                    "$unitPrice (-${item.discount}%)"
                } else {
                    unitPrice
                }

                // Item total - calculate with discount
                val itemTotal = item.unitPrice * item.quantity * (1 - item.discount / 100)
                tvItemTotal.text = CurrencyFormatter.formatFull(itemTotal)

                // If discount exists, show original price crossed out (optional - uncomment if you want this)
                // if (item.discount > 0) {
                //     val originalTotal = item.unitPrice * item.quantity
                //     tvItemOriginalTotal.text = CurrencyFormatter.formatFull(originalTotal)
                //     tvItemOriginalTotal.visibility = View.VISIBLE
                // } else {
                //     tvItemOriginalTotal.visibility = View.GONE
                // }
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

    /**
     * Get the total amount for all items in the receipt
     */
    fun getTotalAmount(): Double {
        return currentList.sumOf {
            it.unitPrice * it.quantity * (1 - it.discount / 100)
        }
    }

    /**
     * Get the count of items in the receipt
     */
    fun getItemCountValue(): Int {
        return currentList.size
    }

    /**
     * Get formatted items for printing with proper columns
     */
    fun getFormattedItemsForPrinting(): List<String> {
        return currentList.map { item ->
            val itemTotal = item.unitPrice * item.quantity * (1 - item.discount / 100)
            val formattedItem = formatItemForPrinting(
                name = item.product.name,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                total = itemTotal,
                discount = item.discount
            )
            formattedItem
        }
    }

    /**
     * Get detailed formatted items for printing with discount info
     */
    fun getDetailedFormattedItemsForPrinting(): List<String> {
        val result = mutableListOf<String>()

        currentList.forEach { item ->
            val name = item.product.name
            val qty = item.quantity
            val unitPrice = item.unitPrice
            val discount = item.discount
            val itemTotal = unitPrice * qty * (1 - discount / 100)

            // Main item line
            val itemLine = formatItemForPrinting(name, qty, unitPrice, itemTotal, discount)
            result.add(itemLine)

            // If discount exists, add discount line
            if (discount > 0) {
                val discountAmount = unitPrice * qty * discount / 100
                result.add("  Discount (${discount}%): -${CurrencyFormatter.formatFull(discountAmount)}")
            }
        }

        return result
    }

    /**
     * Format a single item for thermal printer with proper column alignment
     */
    private fun formatItemForPrinting(
        name: String,
        quantity: Int,
        unitPrice: Double,
        total: Double,
        discount: Double
    ): String {
        val nameMax = 16
        val qtyMax = 4
        val priceMax = 8
        val totalMax = 8

        // Truncate name if too long
        val itemName = if (name.length > nameMax) {
            name.substring(0, nameMax - 3) + "..."
        } else {
            name
        }

        // Format with proper spacing
        val namePadded = itemName.padEnd(nameMax, ' ')
        val qtyPadded = quantity.toString().padStart(qtyMax, ' ')
        val priceFormatted = CurrencyFormatter.formatShort(unitPrice)
        val pricePadded = priceFormatted.padStart(priceMax, ' ')
        val totalFormatted = CurrencyFormatter.formatShort(total)
        val totalPadded = totalFormatted.padStart(totalMax, ' ')

        return namePadded + qtyPadded + " " + pricePadded + " " + totalPadded
    }

    /**
     * Get subtotal before discounts
     */
    fun getSubtotal(): Double {
        return currentList.sumOf { it.unitPrice * it.quantity }
    }

    /**
     * Get total discount amount
     */
    fun getTotalDiscount(): Double {
        return currentList.sumOf {
            it.unitPrice * it.quantity * it.discount / 100
        }
    }

    /**
     * Check if any items have discounts
     */
    fun hasDiscounts(): Boolean {
        return currentList.any { it.discount > 0 }
    }

    /**
     * Get all items in the receipt
     */
    fun getAllItems(): List<CartItem> {
        return currentList
    }
}