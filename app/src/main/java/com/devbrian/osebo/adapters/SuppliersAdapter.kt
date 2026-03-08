package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.ItemSupplierBinding
import com.devbrian.osebo.fragments.SuppliersFragment

class SuppliersAdapter(
    private val onItemClick: (SuppliersFragment.Supplier) -> Unit
) : RecyclerView.Adapter<SuppliersAdapter.SupplierViewHolder>() {

    private var suppliers = listOf<SuppliersFragment.Supplier>()

    fun submitList(list: List<SuppliersFragment.Supplier>) {
        suppliers = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val binding = ItemSupplierBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SupplierViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        holder.bind(suppliers[position])
    }

    override fun getItemCount() = suppliers.size

    inner class SupplierViewHolder(
        private val binding: ItemSupplierBinding
    ) : RecyclerView.ViewHolder(binding.getRoot()) {  // Change binding.root to binding.getRoot()

        fun bind(supplier: SuppliersFragment.Supplier) {
            binding.tvSupplierName.text = supplier.name
            binding.tvContactPerson.text = supplier.contactPerson
            binding.tvPhone.text = supplier.phone
            binding.tvProductsCount.text = "${supplier.products} products"

            val formattedAmount = String.format("UGX %,.0f", supplier.totalPurchases)
            binding.tvTotalPurchases.text = formattedAmount

            binding.tvInitial.text = supplier.name.first().toString().uppercase()

            // Set random background color for avatar
            val colors = arrayOf(
                R.color.avatar_blue,
                R.color.avatar_green,
                R.color.avatar_orange,
                R.color.avatar_purple,
                R.color.avatar_red
            )
            val colorRes = colors[adapterPosition % colors.size]
            binding.avatarLayout.setBackgroundResource(colorRes)

            binding.root.setOnClickListener {  // This binding.root is for the click listener
                onItemClick(supplier)
            }
        }
    }
}