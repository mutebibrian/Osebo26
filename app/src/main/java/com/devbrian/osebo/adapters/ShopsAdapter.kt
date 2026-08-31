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
import com.devbrian.osebo.databinding.ItemShopBinding
import com.devbrian.osebo.models.Shop

interface OnShopClickListener {
    fun onShopClick(shop: Shop)
    fun onEditClick(shop: Shop)
    fun onDeleteClick(shop: Shop)
    fun onSetActiveClick(shop: Shop)
    fun onSubscribeClick(shop: Shop)  // fired when subscription is needed
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
        val shop = getItem(position)
        holder.bind(shop)
    }

    fun setActiveShopId(shopId: String?) {
        activeShopId = shopId
        notifyDataSetChanged()
    }

    inner class ShopViewHolder(
        private val binding: ItemShopBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onShopClick(getItem(position))
                }
            }

            binding.btnEdit.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(getItem(position))
                }
            }

            binding.btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(getItem(position))
                }
            }

            binding.btnSetActive.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onSetActiveClick(getItem(position))
                }
            }

            binding.btnSubscriptionAction.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val shop = getItem(position)
                    if (shop.isSubscriptionActive) {
                        listener.onShopClick(shop)  // navigate to shop dashboard
                    } else {
                        listener.onSubscribeClick(shop)  // navigate to subscription packages
                    }
                }
            }
        }

        fun bind(shop: Shop) {
            binding.apply {
                // Basic info
                tvShopName.text = shop.name
                tvShopDescription.text = shop.description ?: ""
                tvShopLocation.text = shop.location ?: ""
                tvShopCategory.text = shop.category ?: ""

                // Logo
                if (!shop.logoUrl.isNullOrEmpty()) {
                    Glide.with(root.context)
                        .load(shop.logoUrl)
                        .placeholder(R.drawable.ic_shop_placeholder)
                        .into(ivShopLogo)
                } else {
                    ivShopLogo.setImageResource(R.drawable.ic_shop_placeholder)
                }

                // Active badge and Set Active button
                val isActive = shop.id == activeShopId
                tvActiveBadge.visibility = if (isActive) View.VISIBLE else View.GONE
                btnSetActive.visibility = if (isActive) View.GONE else View.VISIBLE

                // Edit/Delete/SetActive are only for the current shop
                val currentShopId = preferenceManager.getCurrentShopId()
                val isCurrentShop = currentShopId == shop.id
                layoutActions.visibility = if (isCurrentShop) View.VISIBLE else View.GONE

                // ========== SUBSCRIPTION BUTTON – ALWAYS VISIBLE ==========
                btnSubscriptionAction.visibility = View.VISIBLE
                if (shop.isSubscriptionActive) {
                    btnSubscriptionAction.text = "View Shop"
                    // Optionally: make it green or primary color
                    btnSubscriptionAction.setBackgroundColor(
                        root.context.getColor(android.R.color.holo_green_light)
                    )
                } else {
                    btnSubscriptionAction.text = "Activate Shop Subscription"
                    btnSubscriptionAction.setBackgroundColor(
                        root.context.getColor(android.R.color.holo_orange_light)
                    )
                }

                // Visual cue: background color for inactive shops (optional)
                if (shop.isSubscriptionActive) {
                    root.setCardBackgroundColor(root.context.getColor(android.R.color.white))
                } else {
                    root.setCardBackgroundColor(root.context.getColor(android.R.color.darker_gray))
                }
            }
        }
    }

    class ShopDiffCallback : DiffUtil.ItemCallback<Shop>() {
        override fun areItemsTheSame(oldItem: Shop, newItem: Shop): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Shop, newItem: Shop): Boolean {
            return oldItem == newItem
        }
    }
}