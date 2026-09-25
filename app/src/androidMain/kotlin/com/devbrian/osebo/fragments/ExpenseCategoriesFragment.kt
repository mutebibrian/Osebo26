package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.models.ExpenseCategory
import com.devbrian.osebo.ui.screens.ExpenseCategoriesScreen
import com.devbrian.osebo.ui.screens.ExpenseCategoryUi
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.ExpenseCategoriesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExpenseCategoriesFragment : Fragment() {
    private val viewModel: ExpenseCategoriesViewModel by viewModel()
    private var categories by mutableStateOf<List<ExpenseCategoryUi>>(emptyList())
    private var isLoading by mutableStateOf(false)
    private var sourceCategories: List<ExpenseCategory> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                ExpenseCategoriesScreen(
                    categories = categories,
                    isLoading = isLoading,
                    onBackClick = { findNavController().navigateUp() },
                    onCreateCategory = viewModel::createCategory,
                    onUpdateCategory = ::updateCategory,
                    onDeleteCategory = { viewModel.deleteCategory(it.id) },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        viewModel.loadCategories()
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { result ->
            sourceCategories = result
            categories = result.map { category ->
                ExpenseCategoryUi(
                    id = category.id,
                    name = category.name,
                    description = category.description.orEmpty(),
                    color = parseColor(category.color),
                    isDefault = category.isDefault,
                )
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading = it }
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

    private fun updateCategory(category: ExpenseCategoryUi, name: String, description: String?) {
        val original = sourceCategories.firstOrNull { it.id == category.id } ?: return
        viewModel.updateCategory(original.copy(name = name, description = description))
    }

    private fun parseColor(value: String?): Color = try {
        Color(android.graphics.Color.parseColor(value ?: "#176BFF"))
    } catch (_: IllegalArgumentException) {
        Color(0xFF176BFF)
    }
}
