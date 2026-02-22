package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.databinding.ItemSubscriptionPackageBinding
import com.devbrian.osebo.models.SubscriptionPackage

class SubscriptionPackageAdapter(
    private val onPackageClick: (SubscriptionPackage) -> Unit
) : ListAdapter<SubscriptionPackage, SubscriptionPackageAdapter.ViewHolder>(
    PackageDiffCallback()
) {

    private var selectedPosition = -1
    private var selectedPackage: SubscriptionPackage? = null

    inner class ViewHolder(
        private val binding: ItemSubscriptionPackageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(packageItem: SubscriptionPackage, position: Int) {
            binding.apply {
                // Package name and tier
                tvPackageName.text = when (packageItem.tier.lowercase()) {
                    "basic" -> "Basic Plan"
                    "pro" -> "Pro Plan"
                    "custom" -> "Enterprise Plan"
                    else -> packageItem.name
                }

                // Price formatting based on your API response
                tvPackagePrice.text = if (packageItem.price > 0) {
                    "UGX ${String.format("%,.0f", packageItem.price)}/month"
                } else {
                    "Contact Sales"
                }

                // Description from API
                tvPackageDescription.text = packageItem.description

                // Features - format as bullet points or comma separated
                val featuresText = packageItem.featureList.joinToString(" • ")
                tvFeatures.text = featuresText

                // Popular tag - only show for Pro plan (tier = "pro")
                if (packageItem.tier.lowercase() == "pro") {
                    tvPopularTag.visibility = View.VISIBLE
                    tvPopularTag.text = "POPULAR"
                } else {
                    tvPopularTag.visibility = View.GONE
                }

                // Custom tag for Enterprise plan
                if (packageItem.tier.lowercase() == "custom") {
                    tvCustomTag.visibility = View.VISIBLE
                    tvCustomTag.text = "CUSTOM"
                } else {
                    tvCustomTag.visibility = View.GONE
                }

                // Trial info - API doesn't show trial, so hide it
                tvTrialInfo.visibility = View.GONE

                // Set selection state
                (binding.cardPackage as? com.google.android.material.card.MaterialCardView)?.isChecked =
                    position == selectedPosition

                // Disable click for inactive packages
                root.isEnabled = packageItem.isActive
                root.alpha = if (packageItem.isActive) 1.0f else 0.5f

                // Set click listener
                root.setOnClickListener {
                    if (packageItem.isActive) {
                        selectPackage(packageItem)
                        onPackageClick(packageItem)
                    }
                }

                // Hide price for custom plan
                if (packageItem.tier.lowercase() == "custom") {
                    tvPackagePrice.visibility = View.GONE
                    tvContactSales.visibility = View.VISIBLE
                    tvContactSales.text = "Contact Sales"
                } else {
                    tvPackagePrice.visibility = View.VISIBLE
                    tvContactSales.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubscriptionPackageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    fun selectPackage(packageItem: SubscriptionPackage) {
        val position = currentList.indexOfFirst { it.id == packageItem.id }
        if (position != -1) {
            val previousPosition = selectedPosition
            selectedPosition = position
            selectedPackage = packageItem

            if (previousPosition != -1) {
                notifyItemChanged(previousPosition)
            }
            notifyItemChanged(position)
        }
    }

    fun getSelectedPackage(): SubscriptionPackage? {
        return selectedPackage
    }

    fun clearSelection() {
        val previousPosition = selectedPosition
        selectedPosition = -1
        selectedPackage = null
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition)
        }
    }
}

class PackageDiffCallback : DiffUtil.ItemCallback<SubscriptionPackage>() {
    override fun areItemsTheSame(
        oldItem: SubscriptionPackage,
        newItem: SubscriptionPackage
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: SubscriptionPackage,
        newItem: SubscriptionPackage
    ): Boolean {
        return oldItem.id == newItem.id &&
                oldItem.name == newItem.name &&
                oldItem.tier == newItem.tier &&
                oldItem.unitMonthlyAmount == newItem.unitMonthlyAmount &&
                oldItem.description == newItem.description &&
                oldItem.isActive == newItem.isActive
    }
}