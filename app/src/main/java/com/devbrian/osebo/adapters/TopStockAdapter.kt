package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.google.android.material.card.MaterialCardView


class TopStockAdapter(
    private val onItemClick: (TopStockItem) -> Unit
) : RecyclerView.Adapter<TopStockAdapter.ViewHolder>() {

    private var items = listOf<TopStockItem>()

    fun submitList(newItems: List<TopStockItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_stock, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val tvSales: TextView = itemView.findViewById(R.id.tvSales)

        fun bind(item: TopStockItem) {
            tvProductName.text = item.name
            tvQuantity.text = "Qty: ${item.quantity}"

            val salesFormatted = when {
                item.sales >= 1_000_000 -> String.format("UGX %.1fM", item.sales / 1_000_000)
                item.sales >= 1_000 -> String.format("UGX %.1fK", item.sales / 1_000)
                else -> String.format("UGX %,.0f", item.sales)
            }
            tvSales.text = salesFormatted

            val colors = listOf(
                R.color.avatar_blue,
                R.color.avatar_green,
                R.color.avatar_orange,
                R.color.avatar_purple,
                R.color.avatar_red
            )

            val pos = adapterPosition
            val safePos = if (pos != RecyclerView.NO_POSITION) pos else 0
            val colorRes = colors[safePos % colors.size]

            (itemView as? com.google.android.material.card.MaterialCardView)
                ?.setCardBackgroundColor(itemView.context.getColor(colorRes).withAlpha(0.1f))


            (itemView as? MaterialCardView)
                ?.setCardBackgroundColor(itemView.context.getColor(colorRes).withAlpha(0.1f))

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}

data class TopStockItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val sales: Double
)


fun Int.withAlpha(alpha: Float): Int {
    return (this and 0x00FFFFFF) or ((alpha * 255).toInt() shl 24)
}
