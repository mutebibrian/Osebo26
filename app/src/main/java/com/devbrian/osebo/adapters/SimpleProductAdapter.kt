package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.models.Product

class SimpleProductAdapter(
    private val products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<SimpleProductAdapter.SimpleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_product, parent, false)
        return SimpleViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    class SimpleViewHolder(
        itemView: android.view.View,
        private val onItemClick: (Product) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)

        fun bind(product: Product) {
            tvName.text = product.name
            tvPrice.text = "UGX ${product.price}"
            itemView.setOnClickListener { onItemClick(product) }
        }
    }
}
