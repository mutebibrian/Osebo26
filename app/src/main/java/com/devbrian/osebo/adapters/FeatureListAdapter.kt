package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.ItemFeatureBinding
import com.devbrian.osebo.models.Feature

class FeatureListAdapter : ListAdapter<Feature, FeatureListAdapter.FeatureViewHolder>(FeatureDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val binding = ItemFeatureBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeatureViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    class FeatureViewHolder(
        private val binding: ItemFeatureBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(feature: Feature) {
            binding.apply {
                tvFeatureName.text = feature.name

                if (feature.included) {
                    ivCheckmark.visibility = View.VISIBLE
                    ivCheckmark.setImageResource(R.drawable.ic_check_circle)
                    ivCheckmark.imageTintList = ContextCompat.getColorStateList(
                        root.context,
                        R.color.green_500
                    )
                    tvFeatureName.alpha = 1.0f
                    tvFeatureName.setTextColor(
                        ContextCompat.getColor(
                            root.context,
                            R.color.on_surface
                        )
                    )
                } else {
                    ivCheckmark.visibility = View.GONE
                    tvFeatureName.alpha = 0.6f
                    tvFeatureName.setTextColor(
                        ContextCompat.getColor(
                            root.context,
                            R.color.on_surface_variant
                        )
                    )
                }
            }
        }
    }

    class FeatureDiffCallback : DiffUtil.ItemCallback<Feature>() {
        override fun areItemsTheSame(oldItem: Feature, newItem: Feature): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Feature, newItem: Feature): Boolean {
            return oldItem.name == newItem.name &&
                    oldItem.included == newItem.included
        }
    }
}