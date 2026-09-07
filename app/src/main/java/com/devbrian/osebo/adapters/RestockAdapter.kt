package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.databinding.ItemRestockProductBinding
import com.devbrian.osebo.models.Product

class RestockAdapter(
    private val onProductSelected: (Product, Double, Boolean) -> Unit
) : RecyclerView.Adapter<RestockAdapter.RestockViewHolder>() {

    private var products: List<Product> = emptyList()
    private val selectedQuantities = mutableMapOf<String, Double>()
    private val selectedProducts = mutableSetOf<String>()

    fun submitList(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    fun getSelectedProducts(): Map<Product, Double> {
        val result = mutableMapOf<Product, Double>()
        selectedProducts.forEach { productId ->
            val product = products.find { it.id == productId }
            val quantity = selectedQuantities[productId] ?: 0.0
            if (product != null && quantity > 0) {
                result[product] = quantity
            }
        }
        return result
    }

    fun getSelectedCount(): Int = selectedProducts.size

    fun clearSelection() {
        selectedProducts.clear()
        selectedQuantities.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestockViewHolder {
        val binding = ItemRestockProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RestockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RestockViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class RestockViewHolder(
        private val binding: ItemRestockProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.apply {
                tvProductName.text = product.name
                tvProductSku.text = "SKU: ${product.sku}"
                tvCurrentStock.text = product.displayStock
                tvProductInitial.text = product.name.take(2).uppercase()

                // Set low stock warning
                if (product.needsRestock) {
                    tvLowStockWarning.visibility = View.VISIBLE
                    tvLowStockWarning.text = "⚠️ Low Stock (Below ${product.lowStockThreshold})"
                } else {
                    tvLowStockWarning.visibility = View.GONE
                }

                // Set checkbox state
                cbSelect.isChecked = selectedProducts.contains(product.id)

                // Set quantity
                val savedQuantity = selectedQuantities[product.id]
                if (savedQuantity != null && savedQuantity > 0) {
                    etQuantity.setText(savedQuantity.toString())
                } else {
                    etQuantity.setText("0")
                }

                // Checkbox listener
                cbSelect.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedProducts.add(product.id)
                        val quantity = etQuantity.text.toString().toDoubleOrNull() ?: 0.0
                        if (quantity > 0) {
                            selectedQuantities[product.id] = quantity
                            onProductSelected(product, quantity, true)
                        }
                    } else {
                        selectedProducts.remove(product.id)
                        selectedQuantities.remove(product.id)
                        onProductSelected(product, 0.0, false)
                    }
                }

                // Quantity change listener
                etQuantity.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val quantity = etQuantity.text.toString().toDoubleOrNull() ?: 0.0
                        if (quantity > 0 && selectedProducts.contains(product.id)) {
                            selectedQuantities[product.id] = quantity
                            onProductSelected(product, quantity, true)
                        } else if (quantity <= 0 && selectedProducts.contains(product.id)) {
                            selectedProducts.remove(product.id)
                            selectedQuantities.remove(product.id)
                            cbSelect.isChecked = false
                            onProductSelected(product, 0.0, false)
                        }
                    }
                }
            }
        }
    }
}