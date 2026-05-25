package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.DialogAddExpenseCategoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddExpenseCategoryDialogFragment : DialogFragment() {

    private var _binding: DialogAddExpenseCategoryBinding? = null
    private val binding get() = _binding!!

    private var onCategoryAddedListener: ((String, String?) -> Unit)? = null

    fun setOnCategoryAddedListener(listener: (String, String?) -> Unit) {
        onCategoryAddedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddExpenseCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            onCategoryAddedListener?.invoke(categoryName, description.takeIf { it.isNotEmpty() })
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
