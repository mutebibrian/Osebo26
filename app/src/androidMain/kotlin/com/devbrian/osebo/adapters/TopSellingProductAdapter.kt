package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.data.local.entity.TopStockItemEntity
import com.devbrian.osebo.utils.CurrencyFormatter

class TopSellingProductAdapter(
    private val onItemClick: (TopStockItemEntity) -> Unit
) : RecyclerView.Adapter<TopSellingProductAdapter.ViewHolder>() {

    private var items = listOf<TopStockItemEntity>()

    fun submitList(newItems: List<TopStockItemEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_selling_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvQuantitySold: TextView = itemView.findViewById(R.id.tvQuantitySold)
        private val tvSalesAmount: TextView = itemView.findViewById(R.id.tvSalesAmount)

        fun bind(item: TopStockItemEntity) {
            tvProductName.text = item.name
            tvQuantitySold.text = "Sold: ${item.quantity} units"
            tvSalesAmount.text = CurrencyFormatter.formatFull(item.sales)

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
