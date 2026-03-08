package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.ItemTopProductBinding
import com.devbrian.osebo.models.TopProduct

class TopProductsAdapter(
    private val onItemClick: (TopProduct) -> Unit
) : ListAdapter<TopProduct, TopProductsAdapter.TopProductViewHolder>(TopProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopProductViewHolder {
        val binding = ItemTopProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TopProductViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: TopProductViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class TopProductViewHolder(
        private val binding: ItemTopProductBinding,
        private val onItemClick: (TopProduct) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentProduct: TopProduct? = null

        init {
            binding.root.setOnClickListener {
                currentProduct?.let { product ->
                    onItemClick.invoke(product)
                }
            }
        }

        fun bind(product: TopProduct) {
            currentProduct = product

            binding.apply {
                tvProductName.text = product.name
                tvProductSku.text = "SKU: ${product.sku}"
                tvQuantity.text = product.formattedQuantity
                tvRevenue.text = product.formattedRevenue
                tvTrend.text = product.trendText

                // Set trend color
                val trendColor = when {
                    product.trend > 0 -> R.color.success_green
                    product.trend < 0 -> R.color.error_red
                    else -> R.color.gray
                }
                tvTrend.setTextColor(
                    ContextCompat.getColor(root.context, trendColor)
                )

                // Set trend icon
                val trendIcon = when {
                    product.trend > 0 -> R.drawable.ic_trend_up
                    product.trend < 0 -> R.drawable.ic_trend_down
                    else -> R.drawable.ic_trend_flat
                }
                ivTrend.setImageResource(trendIcon)
                ivTrend.setColorFilter(
                    ContextCompat.getColor(root.context, trendColor)
                )

                // Load product image
                loadProductImage(product.imageUrl)
            }
        }

        private fun loadProductImage(imageUrl: String?) {
            if (imageUrl.isNullOrEmpty()) {
                binding.ivProductImage.setImageResource(R.drawable.ic_product_placeholder)
                binding.ivProductImage.setColorFilter(
                    ContextCompat.getColor(binding.root.context, R.color.gray_400)
                )
            } else {
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .apply(RequestOptions().transform(CircleCrop()))
                    .placeholder(R.drawable.ic_product_placeholder)
                    .error(R.drawable.ic_product_placeholder)
                    .into(binding.ivProductImage)
            }
        }
    }

    class TopProductDiffCallback : DiffUtil.ItemCallback<TopProduct>() {
        override fun areItemsTheSame(oldItem: TopProduct, newItem: TopProduct): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TopProduct, newItem: TopProduct): Boolean {
            return oldItem == newItem
        }
    }
}