package com.devbrian.osebo.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.models.Product

class ProductAdapter(
    private val onItemClick: (Product) -> Unit = { _ -> },
    private val onMoreOptionsClick: (Product, View) -> Unit = { _, _ -> },
    private val onViewDetailsClick: (Product) -> Unit = { _ -> },
    private val onRestockClick: (Product) -> Unit = { _ -> }
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    var showQuickActions: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var lowStockThreshold: Int = 5
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var currencySymbol: String = "UGX"
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)

        println("📱 Adapter - Creating ViewHolder")

        return ProductViewHolder(
            view,
            onItemClick,
            onMoreOptionsClick,
            onViewDetailsClick,
            onRestockClick,
            showQuickActions,
            lowStockThreshold,
            currencySymbol
        )
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        println("📱 ProductAdapter - Binding product at position $position: ${product.name}")
        holder.bind(product)
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        if (count > 0) {
            println("📱 ProductAdapter - ItemCount: $count")
        }
        return count
    }

    class ProductViewHolder(
        itemView: View,
        private val onItemClick: (Product) -> Unit,
        private val onMoreOptionsClick: (Product, View) -> Unit,
        private val onViewDetailsClick: (Product) -> Unit,
        private val onRestockClick: (Product) -> Unit,
        private val showQuickActions: Boolean,
        private val lowStockThreshold: Int,
        private val currencySymbol: String
    ) : RecyclerView.ViewHolder(itemView) {

        // Main UI Elements
        private val llProductImage: LinearLayout = itemView.findViewById(R.id.ll_product_image)
        private val ivProduct: ImageView = itemView.findViewById(R.id.iv_product)
        private val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val tvProductSku: TextView = itemView.findViewById(R.id.tv_product_sku)
        private val tvProductCategory: TextView = itemView.findViewById(R.id.tv_product_category)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        private val tvStockQuantity: TextView = itemView.findViewById(R.id.tv_stock_quantity)
        private val tvLowStockWarning: TextView = itemView.findViewById(R.id.tv_low_stock_warning)
        private val tvCostPrice: TextView = itemView.findViewById(R.id.tv_cost_price)
        private val pbStockLevel: ProgressBar = itemView.findViewById(R.id.pb_stock_level)
        private val tvBarcode: TextView = itemView.findViewById(R.id.tv_barcode)
        private val ivMoreOptions: ImageView = itemView.findViewById(R.id.iv_more_options)
        private val llQuickActions: LinearLayout = itemView.findViewById(R.id.ll_quick_actions)
        private val tvViewDetails: TextView = itemView.findViewById(R.id.tv_view_details)
        private val tvRestock: TextView = itemView.findViewById(R.id.tv_restock)

        private var currentProduct: Product? = null
        private var isExpanded: Boolean = false

        init {
            itemView.setOnClickListener {
                currentProduct?.let { product ->
                    println("📱 ProductViewHolder - Main item clicked: ${product.name}")
                    if (showQuickActions) {
                        toggleExpansion()
                    }
                    onItemClick(product)
                }
            }

            // Set up quick action click listeners
            tvViewDetails.setOnClickListener {
                currentProduct?.let { product ->
                    onViewDetailsClick(product)
                }
            }

            tvRestock.setOnClickListener {
                currentProduct?.let { product ->
                    onRestockClick(product)
                }
            }

            ivMoreOptions.setOnClickListener {
                currentProduct?.let { product ->
                    onMoreOptionsClick(product, it)
                }
            }
        }

        fun bind(product: Product) {
            currentProduct = product

            println("📱 ProductViewHolder - Binding product: ${product.name}")
            println("   - ID: ${product.id}")
            println("   - SKU: ${product.sku}")
            println("   - Price: ${product.price}")
            println("   - Stock: ${product.stock}")

            // Set product details
            tvProductName.text = product.name ?: "Unknown Product"
            tvProductSku.text = "SKU: ${product.sku ?: "N/A"}"
            tvProductCategory.text = product.category ?: "Uncategorized"

            // Format prices
            tvProductPrice.text = formatCurrency(product.price)
            tvCostPrice.text = if (product.cost != null && product.cost > 0) {
                "Cost: ${formatCurrency(product.cost)}"
            } else {
                "Cost: N/A"
            }

            // Set stock information
            tvStockQuantity.text = "${product.stock} ${if (product.stock == 1) "unit" else "units"}"

            // Show low stock warning if applicable
            val threshold = product.lowStockThreshold ?: lowStockThreshold
            if (product.stock <= threshold) {
                tvLowStockWarning.visibility = View.VISIBLE
                val warningText = when {
                    product.stock == 0 -> "Out of stock"
                    product.stock == 1 -> "Only 1 left"
                    else -> "Low stock"
                }
                tvLowStockWarning.text = warningText

                val warningColor = when {
                    product.stock == 0 -> R.color.red_error
                    product.stock == 1 -> R.color.orange_warning
                    else -> R.color.yellow_warning
                }
                tvLowStockWarning.setTextColor(
                    ContextCompat.getColor(itemView.context, warningColor)
                )
            } else {
                tvLowStockWarning.visibility = View.GONE
            }

            // Set stock progress bar
            val maxStockForProgress = (threshold * 3).coerceAtLeast(30)
            val progress = if (maxStockForProgress > 0) {
                ((product.stock.toFloat() * 100) / maxStockForProgress).toInt().coerceIn(0, 100)
            } else {
                0
            }
            pbStockLevel.progress = progress

            // Set progress bar color
            val progressColor = when {
                product.stock == 0 -> R.color.red_error
                product.stock <= threshold -> R.color.orange_warning
                product.stock <= threshold * 2 -> R.color.yellow_warning
                else -> R.color.green_success
            }
            pbStockLevel.progressTintList = ContextCompat.getColorStateList(
                itemView.context,
                progressColor
            )

            // Set barcode if available
            tvBarcode.text = product.barcode?.takeIf { it.isNotEmpty() } ?: "No barcode"

            // Set product image background and icon based on category
            setProductImageBackground(product.category ?: "OTHER")
            setProductIcon(product.category ?: "OTHER")

            // Highlight out of stock products
            if (product.stock == 0) {
                itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.context, R.color.stock_out_background)
                )
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }

            // Show/hide quick actions
            llQuickActions.visibility = if (showQuickActions && isExpanded) View.VISIBLE else View.GONE
            ivMoreOptions.visibility = if (showQuickActions) View.VISIBLE else View.GONE
        }

        private fun toggleExpansion() {
            isExpanded = !isExpanded
            llQuickActions.visibility = if (showQuickActions && isExpanded) View.VISIBLE else View.GONE

            // Animate the expansion
            if (isExpanded) {
                llQuickActions.alpha = 0f
                llQuickActions.animate().alpha(1f).setDuration(200).start()
            }
        }

        private fun setProductImageBackground(category: String) {
            val colorRes = when (category.uppercase()) {
                "ELECTRONICS" -> R.color.category_electronics
                "CLOTHING" -> R.color.category_clothing
                "FOOD" -> R.color.category_food
                "BEVERAGES" -> R.color.category_beverages
                "OFFICE_SUPPLIES" -> R.color.category_office
                "FURNITURE" -> R.color.category_furniture
                else -> R.color.category_default
            }

            llProductImage.setBackgroundColor(
                ContextCompat.getColor(itemView.context, colorRes)
            )
        }

        private fun setProductIcon(category: String) {
            val iconRes = when (category.uppercase()) {
                "ELECTRONICS" -> R.drawable.ic_electronics
                "CLOTHING" -> R.drawable.ic_clothing
                "FOOD" -> R.drawable.ic_food
                "BEVERAGES" -> R.drawable.ic_beverage
                "OFFICE_SUPPLIES" -> R.drawable.ic_office
                "FURNITURE" -> R.drawable.ic_furniture
                else -> R.drawable.ic_product
            }

            ivProduct.setImageResource(iconRes)
        }

        private fun formatCurrency(amount: Double): String {
            return when {
                amount >= 1000000 -> String.format("%s %.1fM", currencySymbol, amount / 1000000)
                amount >= 1000 -> String.format("%s %.1fK", currencySymbol, amount / 1000)
                else -> String.format("%s %,.0f", currencySymbol, amount)
            }
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Product, newItem: Product): Any? {
            val payloads = mutableListOf<String>()

            if (oldItem.stock != newItem.stock) payloads.add("stock")
            if (oldItem.price != newItem.price) payloads.add("price")
            if (oldItem.name != newItem.name) payloads.add("name")

            return if (payloads.isNotEmpty()) payloads else null
        }
    }

    // ========== PUBLIC HELPER METHODS ==========

    fun getProductAtPosition(position: Int): Product? {
        return if (position in 0 until itemCount) {
            getItem(position)
        } else {
            null
        }
    }

    fun filterProducts(query: String): List<Product> {
        return if (query.isEmpty()) {
            currentList
        } else {
            currentList.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        (product.sku?.contains(query, ignoreCase = true) == true) ||
                        product.category?.contains(query, ignoreCase = true) == true ||
                        product.barcode?.contains(query, ignoreCase = true) == true
            }
        }
    }

    fun filterByCategory(category: String): List<Product> {
        return currentList.filter { it.category.equals(category, ignoreCase = true) }
    }

    fun getLowStockProducts(): List<Product> {
        return currentList.filter { it.stock <= (it.lowStockThreshold ?: lowStockThreshold) }
    }

    fun getOutOfStockProducts(): List<Product> {
        return currentList.filter { it.stock == 0 }
    }

    fun getHighStockProducts(): List<Product> {
        return currentList.filter { it.stock > (it.lowStockThreshold ?: lowStockThreshold) * 3 }
    }

    fun sortByName(ascending: Boolean = true): List<Product> {
        return if (ascending) {
            currentList.sortedBy { it.name }
        } else {
            currentList.sortedByDescending { it.name }
        }
    }

    fun sortByStock(ascending: Boolean = true): List<Product> {
        return if (ascending) {
            currentList.sortedBy { it.stock }
        } else {
            currentList.sortedByDescending { it.stock }
        }
    }

    fun sortByPrice(ascending: Boolean = true): List<Product> {
        return if (ascending) {
            currentList.sortedBy { it.price }
        } else {
            currentList.sortedByDescending { it.price }
        }
    }

    fun sortByCategory(): List<Product> {
        return currentList.sortedBy { it.category }
    }

    fun getTotalInventoryValue(): Double {
        return currentList.sumOf { it.price * it.stock }
    }

    fun getTotalCostValue(): Double {
        return currentList.sumOf { (it.cost ?: 0.0) * it.stock }
    }

    fun getTotalProfitPotential(): Double {
        return currentList.sumOf { (it.price - (it.cost ?: 0.0)) * it.stock }
    }

    fun getCategoryCount(): Map<String, Int> {
        return currentList.groupingBy { it.category }.eachCount() as Map<String, Int>
    }

    fun getStockValueByCategory(): Map<String, Double> {
        return currentList.groupBy { it.category }
            .mapValues { entry ->
                entry.value.sumOf { it.price * it.stock }
            } as Map<String, Double>
    }

    fun getProductsNeedingRestock(): List<Product> {
        return currentList.filter { it.stock <= (it.lowStockThreshold ?: lowStockThreshold) }
    }

    fun getTopSellingProducts(limit: Int = 5): List<Product> {
        return currentList.sortedByDescending { it.price * it.stock }
            .take(limit)
    }

    fun getAverageStockLevel(): Double {
        return if (currentList.isEmpty()) 0.0 else {
            currentList.sumOf { it.stock } / currentList.size.toDouble()
        }
    }

    fun submitProductList(products: List<Product>) {
        submitList(products)
    }

    fun clearAll() {
        submitList(emptyList())
    }
}