package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.databinding.ItemShopBinding

interface OnShopClickListener {
    fun onShopClick(shop: Shop)
    fun onEditClick(shop: Shop)          // not used
    fun onDeleteClick(shop: Shop)        // not used
    fun onSetActiveClick(shop: Shop)     // not used
    fun onSubscribeClick(shop: Shop)
}

class ShopsAdapter(
    private val listener: OnShopClickListener,
    context: android.content.Context
) : ListAdapter<Shop, ShopsAdapter.ShopViewHolder>(ShopDiffCallback()) {

    private val preferenceManager: PreferenceManager = PreferenceManager.getInstance(context)
    private var activeShopId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val binding = ItemShopBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setActiveShopId(shopId: String?) {
        activeShopId = shopId
        notifyDataSetChanged()
    }

    inner class ShopViewHolder(
        private val binding: ItemShopBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // Click on the whole card
            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val shop = getItem(pos)
                    if (shop.isSubscriptionActive) {
                        listener.onShopClick(shop)
                    } else {
                        listener.onSubscribeClick(shop)
                    }
                }
            }

            // Primary action button
            binding.btnPrimaryAction.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val shop = getItem(pos)
                    if (shop.isSubscriptionActive) {
                        listener.onShopClick(shop)
                    } else {
                        listener.onSubscribeClick(shop)
                    }
                }
            }
        }

        fun bind(shop: Shop) {
            binding.apply {
                tvShopName.text = shop.name
                tvShopCategory.text = shop.shopTypeDisplay ?: "Business"
                tvShopLocation.text = shop.fullAddress ?: "Location not set"

                // Logo
                if (!shop.logoUrl.isNullOrEmpty()) {
                    Glide.with(root.context)
                        .load(shop.logoUrl)
                        .placeholder(R.drawable.ic_shop_placeholder)
                        .into(ivShopLogo)
                } else {
                    ivShopLogo.setImageResource(R.drawable.ic_shop_placeholder)
                }

                // ---- GREEN RIBBON ----
                val isActive = shop.id == activeShopId
                val isSubActive = shop.isSubscriptionActive

                if (isActive && isSubActive) {
                    ribbonContainer.visibility = View.VISIBLE
                    tvRibbon.text = if (shop.subscription?.isTrial == true) "TRIAL" else "ACTIVE"
                } else {
                    ribbonContainer.visibility = View.GONE
                }

                // ---- PRIMARY BUTTON ----
                btnPrimaryAction.apply {
                    text = if (isSubActive) "View Shop" else "Activate Shop Subscription"
                    setIconResource(
                        if (isSubActive) R.drawable.ic_arrow_forward
                        else R.drawable.ic_subscribe
                    )
                    // Set background color
                    setBackgroundColor(
                        if (isSubActive) root.context.getColor(R.color.primary)
                        else root.context.getColor(R.color.warning)
                    )
                    setTextColor(android.graphics.Color.WHITE)
                }

                // ---- CARD BACKGROUND HIGHLIGHT ----
                // Use root.setCardBackgroundColor correctly
                if (isActive) {
                    (root as? com.google.android.material.card.MaterialCardView)?.setCardBackgroundColor(
                        root.context.getColor(R.color.surface_highlight)
                    )
                } else {
                    (root as? com.google.android.material.card.MaterialCardView)?.setCardBackgroundColor(
                        android.graphics.Color.WHITE
                    )
                }
            }
        }
    }

    class ShopDiffCallback : DiffUtil.ItemCallback<Shop>() {
        override fun areItemsTheSame(oldItem: Shop, newItem: Shop) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Shop, newItem: Shop) = oldItem == newItem
    }
}