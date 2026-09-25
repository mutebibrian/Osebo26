package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.ui.screens.AddExpenseScreen
import com.devbrian.osebo.ui.screens.AddExpenseUiState
import com.devbrian.osebo.ui.screens.ExpenseCategoryOption
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.AddExpenseViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddExpenseFragment : Fragment() {
    private val viewModel: AddExpenseViewModel by viewModel()
    private var uiState by mutableStateOf(AddExpenseUiState())
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                AddExpenseScreen(
                    state = uiState,
                    onStateChange = { uiState = it },
                    onBackClick = { findNavController().navigateUp() },
                    onDateClick = ::showDatePicker,
                    onAttachReceiptClick = ::showAttachmentOptions,
                    onSaveClick = ::saveExpense,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val today = Date()
        uiState = uiState.copy(dateLabel = displayDateFormat.format(today))
        viewModel.setDate(apiDateFormat.format(today))
        observeViewModel()
        viewModel.loadExpenseCategories()
    }

    private fun observeViewModel() {
        viewModel.expenseCategories.observe(viewLifecycleOwner) { categories ->
            viewModel.setCategoriesList(categories)
            uiState = uiState.copy(
                categories = categories.map { ExpenseCategoryOption(id = it.id, name = it.name) },
            )
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            uiState = uiState.copy(isLoading = isLoading)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.success.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Expense saved successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun saveExpense() {
        val amount = uiState.amount.toDoubleOrNull()
        val categoryError = if (uiState.selectedCategoryId == null) "Select a category" else null
        val amountError = when {
            uiState.amount.isBlank() -> "Enter an amount"
            amount == null -> "Enter a valid amount"
            amount <= 0 -> "Amount must be greater than 0"
            else -> null
        }
        val descriptionError = if (uiState.description.isBlank()) "Enter a description" else null
        val paymentError = if (uiState.paymentMethod.isBlank()) "Select a payment method" else null

        uiState = uiState.copy(
            categoryError = categoryError,
            amountError = amountError,
            descriptionError = descriptionError,
            paymentError = paymentError,
        )
        if (categoryError != null || amountError != null || descriptionError != null || paymentError != null) return

        viewModel.selectCategory(uiState.selectedCategoryId!!)
        viewModel.setAmount(amount!!)
        viewModel.setDescription(uiState.description.trim())
        viewModel.setPaymentMethod(uiState.paymentMethod)
        viewModel.setReference(uiState.reference)
        viewModel.saveExpense()
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select expense date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val date = Date(selection)
            uiState = uiState.copy(dateLabel = displayDateFormat.format(date))
            viewModel.setDate(apiDateFormat.format(date))
        }
        picker.show(parentFragmentManager, "EXPENSE_DATE_PICKER")
    }

    private fun showAttachmentOptions() {
        val options = arrayOf("Take photo", "Choose from gallery", "View attached")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Attach receipt")
            .setItems(options) { _, which ->
                Toast.makeText(requireContext(), options[which], Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
