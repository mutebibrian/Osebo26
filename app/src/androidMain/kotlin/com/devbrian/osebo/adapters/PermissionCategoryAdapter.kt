package com.devbrian.osebo.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.databinding.ItemPermissionCategoryBinding
import com.devbrian.osebo.databinding.ItemPermissionBinding
import com.devbrian.osebo.models.PermissionCategory
import com.devbrian.osebo.models.PermissionItem

class PermissionCategoryAdapter(
    private val onPermissionChecked: (PermissionItem, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var categories = listOf<PermissionCategory>()
    private var filteredCategories = listOf<PermissionCategory>()

    companion object {
        private const val TYPE_CATEGORY = 0
        private const val TYPE_PERMISSION = 1
    }

    fun submitList(newCategories: List<PermissionCategory>) {
        categories = newCategories
        filteredCategories = newCategories
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        if (query.isEmpty()) {
            filteredCategories = categories
        } else {
            filteredCategories = categories.mapNotNull { category ->
                val filteredPermissions = category.permissions.filter {
                    it.displayName.contains(query, ignoreCase = true) ||
                            it.name.contains(query, ignoreCase = true)
                }
                if (filteredPermissions.isNotEmpty()) {
                    PermissionCategory(category.name, filteredPermissions)
                } else {
                    null
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PermissionCategory -> TYPE_CATEGORY
            is PermissionItem -> TYPE_PERMISSION
            else -> throw IllegalArgumentException("Invalid item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CATEGORY -> {
                val binding = ItemPermissionCategoryBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                CategoryViewHolder(binding)
            }
            TYPE_PERMISSION -> {
                val binding = ItemPermissionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                PermissionViewHolder(binding, onPermissionChecked)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryViewHolder -> holder.bind(getItem(position) as PermissionCategory)
            is PermissionViewHolder -> holder.bind(getItem(position) as PermissionItem)
        }
    }

    override fun getItemCount(): Int = filteredCategories.sumOf { category ->
        1 + category.permissions.size
    }

    private fun getItem(position: Int): Any {
        var currentPos = position
        filteredCategories.forEach { category ->
            if (currentPos == 0) return category
            currentPos--

            if (currentPos < category.permissions.size) {
                return category.permissions[currentPos]
            }
            currentPos -= category.permissions.size
        }
        throw IndexOutOfBoundsException()
    }

    fun getSelectedPermissions(): List<PermissionItem> {
        return categories.flatMap { category ->
            category.permissions.filter { it.isChecked }
        }
    }

    inner class CategoryViewHolder(
        private val binding: ItemPermissionCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: PermissionCategory) {
            binding.tvCategoryName.text = category.name
        }
    }

    inner class PermissionViewHolder(
        private val binding: ItemPermissionBinding,
        private val onPermissionChecked: (PermissionItem, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(permission: PermissionItem) {
            binding.cbPermission.apply {
                text = permission.displayName
                isChecked = permission.isChecked
                setOnCheckedChangeListener { _, isChecked ->
                    permission.isChecked = isChecked
                    onPermissionChecked(permission, isChecked)
                }
            }
        }
    }
}
