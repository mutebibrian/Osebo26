package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferencesManager
import com.devbrian.osebo.databinding.ItemShopBinding
import com.devbrian.osebo.models.Shop

interface OnShopClickListener {
    fun onShopClick(shop: Shop)
    fun onEditClick(shop: Shop)
    fun onDeleteClick(shop: Shop)
    fun onSetActiveClick(shop: Shop)
    fun onSubscribeClick(shop: Shop) // NEW — fired when subscription is needed
}

class ShopsAdapter(
    private val listener: OnShopClickListener
) : ListAdapter<Shop, ShopsAdapter.ShopViewHolder>(ShopDiffCallback()) {

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

            // NEW — toggles between "View Shop" and "Activate Shop Subscription"
            binding.btnSubscriptionAction.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val shop = getItem(position)
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
                tvShopDescription.text = shop.description
                tvShopLocation.text = shop.location
                tvShopCategory.text = shop.category

                if (!shop.logoUrl.isNullOrEmpty()) {
                    Glide.with(root.context)
                        .load(shop.logoUrl)
                        .placeholder(R.drawable.ic_shop_placeholder)
                        .into(ivShopLogo)
                } else {
                    ivShopLogo.setImageResource(R.drawable.ic_shop_placeholder)
                }

                val isActive = shop.id == activeShopId
                if (isActive) {
                    root.setBackgroundResource(R.drawable.bg_active_shop)
                    tvActiveBadge.visibility = View.VISIBLE
                    btnSetActive.visibility = View.GONE
                } else {
                    root.setBackgroundResource(R.drawable.bg_shop_item)
                    tvActiveBadge.visibility = View.GONE
                    btnSetActive.visibility = View.VISIBLE
                }

                val showActions = PreferencesManager(root.context).getShopId() == shop.id
                layoutActions.visibility = if (showActions) View.VISIBLE else View.GONE

                // NEW — toggle button text based on real subscription status
                if (shop.isSubscriptionActive) {
                    btnSubscriptionAction.text = "View Shop"
                } else {
                    btnSubscriptionAction.text = "Activate Shop Subscription"
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