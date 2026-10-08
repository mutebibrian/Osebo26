package com.devbrian.osebo.fragments

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import com.devbrian.osebo.models.Transaction
import com.devbrian.osebo.models.TransactionFilter
import com.devbrian.osebo.ui.screens.TransactionFilterScreen
import com.devbrian.osebo.ui.screens.TransactionFilterUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import java.util.Calendar

class FilterDialogFragment : DialogFragment() {
    private var onFilterAppliedListener: ((TransactionFilter) -> Unit)? = null
    private var uiState by mutableStateOf(TransactionFilterUiState())

    private val categories by lazy {
        listOf(
            "All",
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_SALES),
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_PURCHASE),
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_SALARY),
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_RENT),
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_UTILITIES),
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_TAX),
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_OTHER),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                TransactionFilterScreen(
                    state = uiState,
                    categories = categories,
                    onStateChange = { uiState = it },
                    onStartDateClick = { showDatePicker { uiState = uiState.copy(startDate = it) } },
                    onEndDateClick = { showDatePicker { uiState = uiState.copy(endDate = it) } },
                    onClear = ::clearFilters,
                    onApply = ::applyFilters,
                    onDismiss = ::dismiss,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val width = (resources.displayMetrics.widthPixels * 0.94f).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day -> onDateSelected(String.format("%04d-%02d-%02d", year, month + 1, day)) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    private fun clearFilters() {
        uiState = TransactionFilterUiState()
        onFilterAppliedListener?.invoke(TransactionFilter())
        dismiss()
    }

    private fun applyFilters() {
        val category = when (uiState.category) {
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_SALES) -> Transaction.CATEGORY_SALES
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_PURCHASE) -> Transaction.CATEGORY_PURCHASE
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_SALARY) -> Transaction.CATEGORY_SALARY
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_RENT) -> Transaction.CATEGORY_RENT
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_UTILITIES) -> Transaction.CATEGORY_UTILITIES
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_TAX) -> Transaction.CATEGORY_TAX
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_OTHER) -> Transaction.CATEGORY_OTHER
            else -> null
        }
        onFilterAppliedListener?.invoke(
            TransactionFilter(
                startDate = uiState.startDate,
                endDate = uiState.endDate,
                minAmount = uiState.minAmount.toDoubleOrNull(),
                maxAmount = uiState.maxAmount.toDoubleOrNull(),
                category = category,
            ),
        )
        dismiss()
    }

    fun setOnFilterAppliedListener(listener: (TransactionFilter) -> Unit) {
        onFilterAppliedListener = listener
    }
}
