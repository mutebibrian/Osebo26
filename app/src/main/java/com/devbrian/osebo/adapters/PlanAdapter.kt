package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.ItemPlanBinding
import com.devbrian.osebo.data.remote.dto.response.PackageDto

class PlanAdapter(
    private val plans: List<PackageDto>,
    private val onPlanSelected: (PackageDto) -> Unit
) : RecyclerView.Adapter<PlanAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemPlanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: PackageDto) {
            // Plan name
            binding.planName.text = plan.displayName

            // Price
            binding.planPrice.text = plan.formattedPrice

            // Plan type badge
            binding.planType.text = when (plan.tier.lowercase()) {
                "basic" -> "BASE"
                "pro" -> "PRO"
                "premium" -> "PREMIUM"
                "enterprise" -> "ENTERPRISE"
                else -> plan.tier.uppercase()
            }

            // Description
            binding.planDescription.text = plan.description

            // Popular badge
            binding.popularBadge.visibility = if (plan.tier.lowercase() == "popular") View.VISIBLE else View.GONE

            // ========== TRIAL BADGE ==========
            // Show if canTry is true, OR if tier is "basic" (fallback)
            val showTrial = plan.canTry || plan.tier.lowercase() == "basic"
            binding.trialBadge.visibility = if (showTrial) View.VISIBLE else View.GONE

            // Features
            binding.planFeatures.text = if (plan.featureNames.isNotEmpty()) {
                plan.featureNames.joinToString("\n") { "✓ $it" }
            } else {
                "✓ All core features included"
            }

            // Additional info
            binding.additionalInfo.text = when (plan.tier.lowercase()) {
                "basic" -> "5 users • 2 shops • Basic analytics"
                "pro" -> "20 users • 10 shops • Advanced analytics"
                "premium" -> "Unlimited users • Unlimited shops • Priority support"
                "enterprise" -> "Custom limits • Dedicated support"
                else -> "Customizable for your business"
            }

            // Button text
            binding.chooseButton.text = if (plan.isCustomPlan) {
                "Contact Sales"
            } else {
                "Choose Plan"
            }

            binding.chooseButton.setOnClickListener {
                onPlanSelected(plan)
            }

            // Card styling
            when (plan.tier.lowercase()) {
                "basic" -> binding.planCard.setCardBackgroundColor(
                    itemView.context.getColor(android.R.color.white)
                )
                "pro" -> binding.planCard.setCardBackgroundColor(
                    itemView.context.getColor(R.color.surface)
                )
                "premium", "enterprise" -> {
                    binding.planCard.setCardBackgroundColor(
                        itemView.context.getColor(R.color.surface)
                    )
                    binding.planCard.cardElevation = 8f
                }
                else -> {
                    binding.planCard.setCardBackgroundColor(
                        itemView.context.getColor(android.R.color.white)
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(plans[position])
    }

    override fun getItemCount() = plans.size
}