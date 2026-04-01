package com.devbrian.osebo.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.devbrian.osebo.R
import com.devbrian.osebo.models.Transaction
import com.devbrian.osebo.models.TransactionFilter
import java.util.*

class FilterDialogFragment : DialogFragment() {

    private var onFilterAppliedListener: ((TransactionFilter) -> Unit)? = null
    private var startDate: String? = null
    private var endDate: String? = null

    // View references
    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var etMinAmount: EditText
    private lateinit var etMaxAmount: EditText
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var btnClear: Button
    private lateinit var btnApply: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.filter_dialog_fragment, container, false)

        // Initialize views
        btnStartDate = view.findViewById(R.id.btnStartDate)
        btnEndDate = view.findViewById(R.id.btnEndDate)
        etMinAmount = view.findViewById(R.id.etMinAmount)
        etMaxAmount = view.findViewById(R.id.etMaxAmount)
        actvCategory = view.findViewById(R.id.actvCategory)
        btnClear = view.findViewById(R.id.btnClear)
        btnApply = view.findViewById(R.id.btnApply)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategorySpinner()
        setupDatePickers()
        setupClickListeners()

        // Set dialog title
        dialog?.setTitle("Filter Transactions")
    }

    private fun setupCategorySpinner() {
        val categories = listOf(
            "All",
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_SALES),
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_PURCHASE),
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_SALARY),
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_RENT),
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_UTILITIES),
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_TAX),
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_OTHER)
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        actvCategory.setAdapter(adapter)
        actvCategory.setText("All", false)
    }

    private fun setupDatePickers() {
        btnStartDate.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                btnStartDate.text = date
            }
        }

        btnEndDate.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                btnEndDate.text = date
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun setupClickListeners() {
        btnClear.setOnClickListener {
            clearFilters()
        }

        btnApply.setOnClickListener {
            applyFilters()
        }
    }

    private fun clearFilters() {
        etMinAmount.text?.clear()
        etMaxAmount.text?.clear()
        actvCategory.setText("All", false)
        startDate = null
        endDate = null
        btnStartDate.text = "Start Date"
        btnEndDate.text = "End Date"

        onFilterAppliedListener?.invoke(TransactionFilter())
        dismiss()
    }

    private fun applyFilters() {
        val minAmount = etMinAmount.text.toString().toDoubleOrNull()
        val maxAmount = etMaxAmount.text.toString().toDoubleOrNull()
        val categoryText = actvCategory.text.toString()

        val category = when (categoryText) {
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_SALES) -> Transaction.CATEGORY_SALES
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_PURCHASE) -> Transaction.CATEGORY_PURCHASE
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_SALARY) -> Transaction.CATEGORY_SALARY
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_RENT) -> Transaction.CATEGORY_RENT
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_UTILITIES) -> Transaction.CATEGORY_UTILITIES
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_TAX) -> Transaction.CATEGORY_TAX
            Transaction.getCategoryDisplayName(Transaction.CATEGORY_OTHER) -> Transaction.CATEGORY_OTHER
            else -> null
        }

        val filters = TransactionFilter(
            startDate = startDate,
            endDate = endDate,
            minAmount = minAmount,
            maxAmount = maxAmount,
            category = category
        )

        onFilterAppliedListener?.invoke(filters)
        dismiss()
    }

    fun setOnFilterAppliedListener(listener: (TransactionFilter) -> Unit) {
        onFilterAppliedListener = listener
    }
}