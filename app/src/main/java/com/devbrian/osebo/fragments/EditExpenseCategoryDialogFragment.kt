package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.devbrian.osebo.databinding.DialogEditExpenseCategoryBinding
import com.devbrian.osebo.models.ExpenseCategory

class EditExpenseCategoryDialogFragment : DialogFragment() {

    private var _binding: DialogEditExpenseCategoryBinding? = null
    private val binding get() = _binding!!
    private var category: ExpenseCategory? = null
    private var onCategoryUpdatedListener: ((ExpenseCategory) -> Unit)? = null

    companion object {
        fun newInstance(category: ExpenseCategory): EditExpenseCategoryDialogFragment {
            val fragment = EditExpenseCategoryDialogFragment()
            val args = Bundle()
            args.putString("category_id", category.id)
            args.putString("category_name", category.name)
            args.putString("category_description", category.description)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments?.getString("category_id") ?: ""
        val name = arguments?.getString("category_name") ?: ""
        val description = arguments?.getString("category_description")
        category = ExpenseCategory(id = id, name = name, description = description)
    }

    fun setOnCategoryUpdatedListener(listener: (ExpenseCategory) -> Unit) {
        onCategoryUpdatedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditExpenseCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        category?.let {
            binding.etCategoryName.setText(it.name)
            binding.etDescription.setText(it.description ?: "")
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            val categoryName = binding.etCategoryName.text.toString().trim()
            if (categoryName.isEmpty()) {
                binding.etCategoryName.error = "Category name is required"
                return@setOnClickListener
            }

            val description = binding.etDescription.text.toString().trim()
            category?.let {
                val updatedCategory = it.copy(
                    name = categoryName,
                    description = description.takeIf { desc -> desc.isNotEmpty() }
                )
                onCategoryUpdatedListener?.invoke(updatedCategory)
            }
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
