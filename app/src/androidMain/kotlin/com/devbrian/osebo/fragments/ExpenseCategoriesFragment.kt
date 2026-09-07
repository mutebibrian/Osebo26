package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.ExpenseCategoryAdapter
import com.devbrian.osebo.databinding.FragmentExpenseCategoriesBinding
import com.devbrian.osebo.models.ExpenseCategory
import com.devbrian.osebo.ui.viewmodels.ExpenseCategoriesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpenseCategoriesFragment : Fragment() {
    private var _binding: FragmentExpenseCategoriesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseCategoriesViewModel by viewModels()
    private lateinit var adapter: ExpenseCategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        viewModel.loadCategories()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = ExpenseCategoryAdapter(
            onEditClick = { category ->
                showEditCategoryDialog(category)
            },
            onDeleteClick = { category ->
                viewModel.deleteCategory(category.id)
            }
        )

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ExpenseCategoriesFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }

        binding.btnAddFirstCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.submitList(categories)

            if (categories.isEmpty()) {
                binding.llEmpty.visibility = View.VISIBLE
                binding.rvCategories.visibility = View.GONE
            } else {
                binding.llEmpty.visibility = View.GONE
                binding.rvCategories.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccessMessage()
            }
        }
    }

    private fun showAddCategoryDialog() {
        val dialog = AddExpenseCategoryDialogFragment()
        dialog.setOnCategoryAddedListener { categoryName, description ->
            viewModel.createCategory(categoryName, description)
        }
        dialog.show(parentFragmentManager, "AddCategoryDialog")
    }

    private fun showEditCategoryDialog(category: ExpenseCategory) {
        val dialog = EditExpenseCategoryDialogFragment.newInstance(category)
        dialog.setOnCategoryUpdatedListener { updatedCategory ->
            viewModel.updateCategory(updatedCategory)
        }
        dialog.show(parentFragmentManager, "EditCategoryDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
