package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.models.CartItem
import java.text.NumberFormat
import java.util.*

class CartAdapter(
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit,
    private val onDiscountApplied: (CartItem, Double) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val items = mutableListOf<CartItem>()

    fun submitList(newItems: List<CartItem>) {
        println("📦 CartAdapter - submitList called with ${newItems.size} items")
        val previousSize = items.size
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
        println("📦 CartAdapter - Now has ${items.size} items (was $previousSize)")
    }

    fun updateItem(item: CartItem) {
        val index = items.indexOfFirst { it.product.id == item.product.id }
        if (index != -1) {
            println("📦 CartAdapter - Updating item at index $index: ${item.product.name} (qty: ${item.quantity})")
            items[index] = item
            notifyItemChanged(index)
        } else {
            println("📦 CartAdapter - Item not found for update: ${item.product.name}")
        }
    }

    fun removeItem(item: CartItem) {
        val index = items.indexOfFirst { it.product.id == item.product.id }
        if (index != -1) {
            println("📦 CartAdapter - Removing item at index $index: ${item.product.name}")
            items.removeAt(index)
            notifyItemRemoved(index)
        } else {
            println("📦 CartAdapter - Item not found for removal: ${item.product.name}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view, onQuantityChanged, onRemoveItem, onDiscountApplied)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        println("📦 CartAdapter - Binding item at position $position: ${item.product.name} (qty: ${item.quantity})")
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class CartViewHolder(
        itemView: View,
        private val onQuantityChanged: (CartItem, Int) -> Unit,
        private val onRemoveItem: (CartItem) -> Unit,
        private val onDiscountApplied: (CartItem, Double) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val tvProductSku: TextView = itemView.findViewById(R.id.tv_product_sku)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_price)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tv_quantity)
        private val tvSubtotal: TextView = itemView.findViewById(R.id.tv_subtotal)
        private val tvDiscount: TextView = itemView.findViewById(R.id.tv_discount)
        private val btnDecrease: View = itemView.findViewById(R.id.btn_decrease)
        private val btnIncrease: View = itemView.findViewById(R.id.btn_increase)
        private val btnRemove: View = itemView.findViewById(R.id.btn_remove)
        private val btnDiscount: View = itemView.findViewById(R.id.btn_discount)

        private var currentItem: CartItem? = null
        private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
            currency = Currency.getInstance("UGX")
        }

        init {
            btnDecrease.setOnClickListener {
                currentItem?.let { item ->
                    println("📦 CartAdapter - Decrease button clicked for ${item.product.name}")
                    if (item.quantity > 1) {
                        onQuantityChanged(item, item.quantity - 1)
                    } else {
                        // If quantity is 1, removing is better than going to 0
                        onRemoveItem(item)
                    }
                }
            }

            btnIncrease.setOnClickListener {
                currentItem?.let { item ->
                    println("📦 CartAdapter - Increase button clicked for ${item.product.name}")
                    onQuantityChanged(item, item.quantity + 1)
                }
            }

            btnRemove.setOnClickListener {
                currentItem?.let { item ->
                    println("📦 CartAdapter - Remove button clicked for ${item.product.name}")
                    onRemoveItem(item)
                }
            }

            btnDiscount.setOnClickListener {
                currentItem?.let { item ->
                    println("📦 CartAdapter - Discount button clicked for ${item.product.name}")
                    showDiscountDialog(item)
                }
            }
        }

        fun bind(item: CartItem) {
            currentItem = item

            tvProductName.text = item.product.name
            tvProductSku.text = "SKU: ${item.product.sku}"
            tvPrice.text = currencyFormat.format(item.unitPrice)
            tvQuantity.text = item.quantity.toString()
            tvSubtotal.text = currencyFormat.format(item.subtotal)

            if (item.discount > 0) {
                tvDiscount.visibility = View.VISIBLE
                tvDiscount.text = "${item.discount}% off"
            } else {
                tvDiscount.visibility = View.GONE
            }

            // Log binding for debugging
            println("📦 CartViewHolder - Bound ${item.product.name}: qty=${item.quantity}, price=${item.unitPrice}, subtotal=${item.subtotal}")
        }

        private fun showDiscountDialog(item: CartItem) {
            // For now, just apply a sample discount
            // In a real app, you'd show a dialog for user input
            println("📦 CartAdapter - Applying 10% discount to ${item.product.name}")
            onDiscountApplied(item, 10.0)
        }
    }
}