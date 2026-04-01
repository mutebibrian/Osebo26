package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.models.ExpenseCategory

// Remove any import of ItemExpenseCategoryBinding

class ExpenseCategoryAdapter(
    private val onEditClick: (ExpenseCategory) -> Unit,
    private val onDeleteClick: (ExpenseCategory) -> Unit
) : RecyclerView.Adapter<ExpenseCategoryAdapter.CategoryViewHolder>() {

    private var categories = listOf<ExpenseCategory>()

    fun submitList(list: List<ExpenseCategory>) {
        categories = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val viewColorIndicator: View = itemView.findViewById(R.id.view_color_indicator)
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        private val tvCategoryDescription: TextView = itemView.findViewById(R.id.tv_category_description)
        private val ivEdit: ImageView = itemView.findViewById(R.id.iv_edit)
        private val ivDelete: ImageView = itemView.findViewById(R.id.iv_delete)

        fun bind(category: ExpenseCategory) {
            tvCategoryName.text = category.name
            tvCategoryDescription.text = category.description.ifEmpty { "No description" }

            // Set color indicator
            try {
                val color = android.graphics.Color.parseColor(category.color ?: "#6200EE")
                viewColorIndicator.setBackgroundColor(color)
            } catch (e: Exception) {
                viewColorIndicator.setBackgroundColor(
                    androidx.core.content.ContextCompat.getColor(itemView.context, R.color.colorPrimary)
                )
            }

            ivEdit.setOnClickListener {
                onEditClick(category)
            }

            ivDelete.setOnClickListener {
                onDeleteClick(category)
            }
        }
    }
}