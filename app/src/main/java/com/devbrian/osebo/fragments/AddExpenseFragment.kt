package com.devbrian.osebo.fragments

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.databinding.FragmentAddExpenseBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddExpenseFragment : Fragment() {
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private lateinit var dateFormat: SimpleDateFormat

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        setupToolbar()
        setupCategorySpinner()
        setupPaymentMethodSpinner()
        setupDatePicker()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf(
            "Rent",
            "Utilities",
            "Salaries",
            "Supplies",
            "Marketing",
            "Transport",
            "Maintenance",
            "Equipment",
            "Insurance",
            "Taxes",
            "Other"
        )

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.actvCategory.setAdapter(adapter)

        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
            binding.tilCategory.error = null
        }
    }

    private fun setupPaymentMethodSpinner() {
        val methods = arrayOf(
            "Cash",
            "Bank Transfer",
            "Mobile Money",
            "Credit Card",
            "Cheque"
        )

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, methods)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.actvPaymentMethod.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        binding.etDate.setText(dateFormat.format(Date()))

        binding.etDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Date(selection)
            binding.etDate.setText(dateFormat.format(date))
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveExpense()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAttachReceipt.setOnClickListener {
            showAttachmentOptions()
        }
    }

    private fun saveExpense() {
        // Validate fields
        val category = binding.actvCategory.text.toString()
        val amount = binding.etAmount.text.toString()
        val description = binding.etDescription.text.toString()
        val date = binding.etDate.text.toString()

        var isValid = true

        if (category.isEmpty()) {
            binding.tilCategory.error = "Select a category"
            isValid = false
        }

        if (amount.isEmpty()) {
            binding.tilAmount.error = "Enter amount"
            isValid = false
        } else {
            try {
                amount.toDouble()
                binding.tilAmount.error = null
            } catch (e: NumberFormatException) {
                binding.tilAmount.error = "Invalid amount"
                isValid = false
            }
        }

        if (description.isEmpty()) {
            binding.tilDescription.error = "Enter description"
            isValid = false
        }

        if (!isValid) return

        // TODO: Save to API
        Toast.makeText(requireContext(), "Expense saved successfully", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    private fun showAttachmentOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "View Attached")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Attach Receipt")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(requireContext(), "Take Photo", Toast.LENGTH_SHORT).show()
                    1 -> Toast.makeText(requireContext(), "Choose from Gallery", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(requireContext(), "View Attached", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}