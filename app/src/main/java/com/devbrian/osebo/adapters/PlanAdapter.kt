package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.ItemPlanBinding
import com.devbrian.osebo.models.SubscriptionPackage

class PlanAdapter(
    private val plans: List<SubscriptionPackage>,
    private val onPlanSelected: (SubscriptionPackage) -> Unit
) : RecyclerView.Adapter<PlanAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemPlanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plan: SubscriptionPackage) {
            
            binding.planName.text = plan.displayName ?: plan.name

            
            binding.planPrice.text = plan.displayPrice

            
            binding.planType.text = when (plan.name.uppercase()) {
                "BASIC" -> "Basic"
                "PRO" -> "Pro"
                "POPULAR" -> "Enterprise"
                else -> "Custom"
            }

            
            binding.planDescription.text = plan.description ?: getDefaultDescription(plan.name)

            
            binding.popularBadge.visibility = if (plan.isPopular) View.VISIBLE else View.GONE

            
            binding.planFeatures.text = if (plan.features.isNotEmpty()) {
                plan.features.joinToString("\n") { "✓ $it" }
            } else {
                getDefaultFeatures(plan.name)
            }

            
            binding.additionalInfo.text = getAdditionalInfo(plan.name)

            
            binding.chooseButton.text = if (plan.isCustom) {
                "Contact Sales"
            } else {
                "Choose Plan"
            }

            
            binding.chooseButton.setOnClickListener {
                onPlanSelected(plan)
            }

            
            if (plan.isPopular) {
                binding.chooseButton.setBackgroundColor(
                    itemView.context.getColor(R.color.primary_color)
                )
            }
        }

        private fun getDefaultDescription(packageName: String): String {
            return when (packageName.uppercase()) {
                "BASIC" -> "Perfect for small businesses getting started"
                "PRO" -> "Advanced features for growing businesses"
                "POPULAR" -> "Custom enterprise solutions with dedicated support"
                else -> "Tailored solutions for your business needs"
            }
        }

        private fun getDefaultFeatures(packageName: String): String {
            return when (packageName.uppercase()) {
                "BASIC" -> "✓ Inventory\n✓ Reports\n✓ Basic Support"
                "PRO" -> "✓ Inventory\n✓ Reports\n✓ Support\n✓ Analytics"
                "POPULAR" -> "✓ Inventory\n✓ Reports\n✓ Support\n✓ API Access\n✓ Custom Features"
                else -> "✓ Custom features based on your needs"
            }
        }

        private fun getAdditionalInfo(packageName: String): String {
            return when (packageName.uppercase()) {
                "BASIC" -> "5 users • 2 shops • Basic analytics"
                "PRO" -> "20 users • 10 shops • Advanced analytics"
                "POPULAR" -> "Unlimited users • Unlimited shops • Priority support"
                else -> "Custom limits • Dedicated support"
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


