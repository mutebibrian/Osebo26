package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.domain.model.SubscriptionPlan
import com.devbrian.osebo.databinding.ItemPlanBinding

class PlanAdapter(
    private val plans: List<SubscriptionPlan>,
    private val onPlanSelected: (SubscriptionPlan) -> Unit
) : RecyclerView.Adapter<PlanAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemPlanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: SubscriptionPlan) {
            // Domain model properties are non-null by design
            binding.planName.text = plan.name
            binding.planPrice.text = "${plan.currency} ${plan.price}"  // Already formatted string
            binding.planType.text = plan.type.replaceFirstChar { it.uppercase() }
            binding.planFeatures.text = plan.features.joinToString("\n") { "✓ $it" }
            binding.planDescription.text = plan.description

            // Show popular badge for popular plans
            binding.popularBadge.visibility = if (plan.isPopular) View.VISIBLE else View.GONE

            // Show additional info if needed
            binding.additionalInfo.text =
                "${plan.maxUsers} users • ${plan.maxShops} shops • ${plan.maxStorage} storage"

            binding.chooseButton.setOnClickListener {
                onPlanSelected(plan)
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