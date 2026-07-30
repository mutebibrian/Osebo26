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
    private val onSelectionChanged: (List<SubscriptionPackage>) -> Unit
) : ListAdapter<SubscriptionPackage, SubscriptionPackageAdapter.ViewHolder>(
    PackageDiffCallback()
) {

    private val selectedIds = mutableSetOf<String>()

    inner class ViewHolder(
        private val binding: ItemSubscriptionPackageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(packageItem: SubscriptionPackage) {
            binding.apply {

                tvPackageName.text = packageItem.displayName
                tvPackageDescription.text = packageItem.description

                val featuresText = packageItem.featureList.joinToString(" • ")
                tvFeatures.text = featuresText

                if (packageItem.isPopular) {
                    tvPopularTag.visibility = View.VISIBLE
                    tvPopularTag.text = "POPULAR"
                } else {
                    tvPopularTag.visibility = View.GONE
                }

                if (packageItem.isAddOn) {
                    tvCustomTag.visibility = View.VISIBLE
                    tvCustomTag.text = "CUSTOM"
                } else {
                    tvCustomTag.visibility = View.GONE
                }

                if (packageItem.hasFreeTrial) {
                    tvTrialInfo.visibility = View.VISIBLE
                    tvTrialInfo.text = "Trial Available"
                } else {
                    tvTrialInfo.visibility = View.GONE
                }

                if (packageItem.price > 0) {
                    tvPackagePrice.visibility = View.VISIBLE
                    tvPackagePrice.text = "UGX ${String.format("%,.0f", packageItem.price)}/mo"
                    tvContactSales.visibility = View.GONE
                } else {
                    tvPackagePrice.visibility = View.GONE
                    tvContactSales.visibility = View.VISIBLE
                    tvContactSales.text = "Contact Sales"
                }

                checkboxSelect.setOnCheckedChangeListener(null)
                checkboxSelect.isChecked = selectedIds.contains(packageItem.id)
                checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
                    toggleSelection(packageItem.id, isChecked)
                }

                (binding.cardPackage as? com.google.android.material.card.MaterialCardView)?.isChecked =
                    selectedIds.contains(packageItem.id)

                root.isEnabled = packageItem.isActive
                root.alpha = if (packageItem.isActive) 1.0f else 0.5f

                root.setOnClickListener {
                    if (packageItem.isActive) {
                        checkboxSelect.isChecked = !checkboxSelect.isChecked
                    }
                }
            }
        }
    }

    private fun toggleSelection(id: String, isChecked: Boolean) {
        if (isChecked) selectedIds.add(id) else selectedIds.remove(id)
        notifyDataSetChanged()
        onSelectionChanged(getSelectedPackages())
    }

    fun getSelectedPackages(): List<SubscriptionPackage> {
        return currentList.filter { selectedIds.contains(it.id) }
    }

    fun clearSelection() {
        selectedIds.clear()
        notifyDataSetChanged()
        onSelectionChanged(emptyList())
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
        holder.bind(getItem(position))
    }
}

class PackageDiffCallback : DiffUtil.ItemCallback<SubscriptionPackage>() {
    override fun areItemsTheSame(oldItem: SubscriptionPackage, newItem: SubscriptionPackage) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: SubscriptionPackage, newItem: SubscriptionPackage) =
        oldItem.id == newItem.id &&
                oldItem.name == newItem.name &&
                oldItem.tier == newItem.tier &&
                oldItem.kind == newItem.kind &&
                oldItem.unitMonthlyAmount == newItem.unitMonthlyAmount &&
                oldItem.description == newItem.description &&
                oldItem.isActive == newItem.isActive
}