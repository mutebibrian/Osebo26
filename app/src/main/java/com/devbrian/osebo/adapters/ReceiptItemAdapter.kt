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

    
    var isCompactMode: Boolean = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReceiptBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, isCompactMode)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemReceiptBinding,
        private val isCompactMode: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.apply {
                
                val itemName = if (isCompactMode && item.product.name.length > 12) {
                    item.product.name.substring(0, 9) + "..."
                } else {
                    item.product.name
                }
                tvItemName.text = itemName

                
                tvItemQuantity.text = item.quantity.toString()

                
                val unitPrice = if (isCompactMode) {
                    formatCompactCurrencyShort(item.unitPrice)
                } else {
                    CurrencyFormatter.formatShort(item.unitPrice)
                }

                
                tvItemPrice.text = if (item.discount > 0 && !isCompactMode) {
                    "$unitPrice (-${item.discount}%)"
                } else {
                    unitPrice
                }

                
                val itemTotal = item.unitPrice * item.quantity * (1 - item.discount / 100)
                tvItemTotal.text = if (isCompactMode) {
                    formatCompactCurrencyShort(itemTotal)
                } else {
                    CurrencyFormatter.formatFull(itemTotal)
                }

                
                if (isCompactMode) {
                    tvItemPrice.visibility = android.view.View.GONE
                } else {
                    tvItemPrice.visibility = android.view.View.VISIBLE
                }
            }
        }

        private fun formatCompactCurrencyShort(amount: Double): String {
            return when {
                amount >= 1000000 -> String.format("%.1fM", amount / 1000000)
                amount >= 1000 -> String.format("%.1fK", amount / 1000)
                else -> String.format("%.0f", amount)
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

    
    fun getTotalAmount(): Double {
        return currentList.sumOf {
            it.unitPrice * it.quantity * (1 - it.discount / 100)
        }
    }

    
    fun getItemCountValue(): Int {
        return currentList.size
    }

    
    fun getFormattedItemsForPrinting(): List<String> {
        return currentList.map { item ->
            val itemTotal = item.unitPrice * item.quantity * (1 - item.discount / 100)
            val formattedItem = formatItemForPrinting40mm(
                name = item.product.name,
                quantity = item.quantity,
                total = itemTotal,
                discount = item.discount
            )
            formattedItem
        }
    }

    
    fun getDetailedFormattedItemsForPrinting(): List<String> {
        val result = mutableListOf<String>()

        currentList.forEach { item ->
            val name = item.product.name
            val qty = item.quantity
            val discount = item.discount
            val itemTotal = item.unitPrice * qty * (1 - discount / 100)

            
            val itemLine = formatItemForPrinting40mm(name, qty, itemTotal, discount)
            result.add(itemLine)

            
            if (discount > 0) {
                val discountAmount = item.unitPrice * qty * discount / 100
                result.add("  Disc ${discount}%: -${formatCompactCurrencyShort(discountAmount)}")
            }
        }

        return result
    }

    
    private fun formatItemForPrinting40mm(
        name: String,
        quantity: Int,
        total: Double,
        discount: Double
    ): String {
        
        val nameMax = 16
        val qtyMax = 3
        val totalMax = 7

        
        var itemName = name
        if (itemName.length > nameMax) {
            itemName = itemName.substring(0, nameMax - 2) + ".."
        }

        
        val namePadded = itemName.padEnd(nameMax, ' ')
        val qtyPadded = quantity.toString().padStart(qtyMax, ' ')
        val totalFormatted = formatCompactCurrencyShort(total)
        val totalPadded = totalFormatted.padStart(totalMax, ' ')

        return "$namePadded$qtyPadded $totalPadded"
    }

    
    private fun formatItemForPrinting32mm(
        name: String,
        quantity: Int,
        unitPrice: Double,
        total: Double,
        discount: Double
    ): String {
        val nameMax = 16
        val qtyMax = 4
        val priceMax = 7
        val totalMax = 7

        
        val itemName = if (name.length > nameMax) {
            name.substring(0, nameMax - 3) + "..."
        } else {
            name
        }

        
        val namePadded = itemName.padEnd(nameMax, ' ')
        val qtyPadded = quantity.toString().padStart(qtyMax, ' ')
        val priceFormatted = formatCompactCurrencyShort(unitPrice)
        val pricePadded = priceFormatted.padStart(priceMax, ' ')
        val totalFormatted = formatCompactCurrencyShort(total)
        val totalPadded = totalFormatted.padStart(totalMax, ' ')

        return "$namePadded$qtyPadded $pricePadded $totalPadded"
    }

    private fun formatCompactCurrencyShort(amount: Double): String {
        return when {
            amount >= 1000000 -> String.format("%.1fM", amount / 1000000)
            amount >= 1000 -> String.format("%.1fK", amount / 1000)
            else -> String.format("%.0f", amount)
        }
    }

    
    fun getSubtotal(): Double {
        return currentList.sumOf { it.unitPrice * it.quantity }
    }

    
    fun getTotalDiscount(): Double {
        return currentList.sumOf {
            it.unitPrice * it.quantity * it.discount / 100
        }
    }

    
    fun hasDiscounts(): Boolean {
        return currentList.any { it.discount > 0 }
    }

    
    fun getAllItems(): List<CartItem> {
        return currentList
    }

    
    fun getFormattedSummary(
        subtotal: Double,
        discount: Double,
        tax: Double,
        total: Double,
        paid: Double,
        change: Double,
        paymentMethod: String
    ): List<String> {
        val result = mutableListOf<String>()
        result.add("")
        result.add(repeatChar('-', 24))
        result.add(formatTwoColumns("Subtotal:", formatCompactCurrencyShort(subtotal), 24))
        if (discount > 0) {
            result.add(formatTwoColumns("Discount:", "-${formatCompactCurrencyShort(discount)}", 24))
        }
        if (tax > 0) {
            result.add(formatTwoColumns("Tax:", formatCompactCurrencyShort(tax), 24))
        }
        result.add(repeatChar('=', 24))
        result.add(formatTwoColumns("TOTAL:", formatCompactCurrencyShort(total), 24))
        result.add(repeatChar('=', 24))
        result.add(formatTwoColumns("Paid:", formatCompactCurrencyShort(paid), 24))
        result.add(formatTwoColumns("Change:", formatCompactCurrencyShort(change), 24))
        result.add(formatTwoColumns("Pay:", paymentMethod, 24))
        result.add(repeatChar('=', 24))
        return result
    }

    private fun formatTwoColumns(left: String, right: String, width: Int): String {
        val availableWidth = width - left.length
        return left + " ".repeat(availableWidth - right.length) + right
    }

    private fun repeatChar(char: Char, count: Int): String {
        return char.toString().repeat(count)
    }
}
