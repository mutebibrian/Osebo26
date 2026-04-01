package com.devbrian.osebo.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.ExpenseCategoryAdapter
import com.devbrian.osebo.databinding.DialogAddExpenseCategoryBinding
import com.devbrian.osebo.databinding.FragmentExpenseCategoriesBinding
import com.devbrian.osebo.models.ExpenseCategory
import com.devbrian.osebo.ui.viewmodels.ExpenseCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpenseCategoriesFragment : Fragment() {
    private var _binding: FragmentExpenseCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoryAdapter: ExpenseCategoryAdapter
    private val viewModel: ExpenseCategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseCategoriesBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        observeViewModel()

        viewModel.loadCategories()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add -> {
                    showAddCategoryDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = ExpenseCategoryAdapter(
            onEditClick = { category ->
                showEditCategoryDialog(category)
            },
            onDeleteClick = { category ->
                confirmDeleteCategory(category)
            }
        )

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.filterCategories(s.toString())
            }
        })
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
        // Use categories instead of filteredCategories if filterCategories is not in ViewModel
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            if (categories.isEmpty()) {
                showEmptyState()
            } else {
                showCategoriesState()
                categoryAdapter.submitList(categories)
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

        // Remove successMessage observer if not in ViewModel
    }

    private fun showAddCategoryDialog() {
        val dialogBinding = DialogAddExpenseCategoryBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Expense Category")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.etCategoryName.text.toString().trim()
                val description = dialogBinding.etCategoryDescription.text.toString().trim()

                if (name.isNotEmpty()) {
                    viewModel.createCategory(name, description)
                } else {
                    Toast.makeText(requireContext(), "Category name is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditCategoryDialog(category: ExpenseCategory) {
        val dialogBinding = DialogAddExpenseCategoryBinding.inflate(layoutInflater)
        dialogBinding.etCategoryName.setText(category.name)
        dialogBinding.etCategoryDescription.setText(category.description)
        dialogBinding.tilCategoryName.hint = "Edit Category Name"

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Expense Category")
            .setView(dialogBinding.root)
            .setPositiveButton("Update") { _, _ ->
                val name = dialogBinding.etCategoryName.text.toString().trim()
                val description = dialogBinding.etCategoryDescription.text.toString().trim()

                if (name.isNotEmpty()) {
                    viewModel.updateCategory(category.id, name, description)
                } else {
                    Toast.makeText(requireContext(), "Category name is required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteCategory(category: ExpenseCategory) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.name}'? This may affect existing expenses.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCategory(category.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEmptyState() {
        binding.llEmpty.visibility = View.VISIBLE
        binding.rvCategories.visibility = View.GONE
    }

    private fun showCategoriesState() {
        binding.llEmpty.visibility = View.GONE
        binding.rvCategories.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_categories, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}