package com.devbrian.osebo.fragments

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.databinding.FragmentAddExpenseBinding
import com.devbrian.osebo.ui.viewmodels.AddExpenseViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddExpenseFragment : Fragment() {
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private lateinit var dateFormat: SimpleDateFormat
    private val viewModel: AddExpenseViewModel by viewModel()

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
        observeViewModel()

        viewModel.loadExpenseCategories()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCategorySpinner() {
        viewModel.expenseCategories.observe(viewLifecycleOwner) { categories ->
            val categoryNames = categories.map { it.name }.toTypedArray()
            val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, categoryNames)
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            binding.actvCategory.setAdapter(adapter)

            viewModel.setCategoriesList(categories)
        }

        binding.actvCategory.setOnItemClickListener { _, _, position, _ ->
            binding.tilCategory.error = null
            viewModel.selectCategory(position)
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

        binding.actvPaymentMethod.setOnItemClickListener { _, _, position, _ ->
            viewModel.setPaymentMethod(methods[position])
        }
    }

    private fun setupDatePicker() {
        binding.etDate.setText(dateFormat.format(Date()))
        viewModel.setDate(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

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
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            viewModel.setDate(formattedDate)
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

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Remove progressBar reference if it doesn't exist
            binding.btnSave.isEnabled = !isLoading
            binding.btnSave.text = if (isLoading) "Saving..." else "Save Expense"
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
        val category = binding.actvCategory.text.toString()
        val amountStr = binding.etAmount.text.toString()
        val description = binding.etDescription.text.toString()
        val paymentMethod = binding.actvPaymentMethod.text.toString()

        var isValid = true

        if (category.isEmpty()) {
            binding.tilCategory.error = "Select a category"
            isValid = false
        }

        if (amountStr.isEmpty()) {
            binding.tilAmount.error = "Enter amount"
            isValid = false
        } else {
            try {
                val amount = amountStr.toDouble()
                if (amount <= 0) {
                    binding.tilAmount.error = "Amount must be greater than 0"
                    isValid = false
                } else {
                    viewModel.setAmount(amount)
                    binding.tilAmount.error = null
                }
            } catch (e: NumberFormatException) {
                binding.tilAmount.error = "Invalid amount"
                isValid = false
            }
        }

        if (description.isEmpty()) {
            binding.tilDescription.error = "Enter description"
            isValid = false
        } else {
            viewModel.setDescription(description)
            binding.tilDescription.error = null
        }

        if (paymentMethod.isEmpty()) {
            binding.tilPaymentMethod.error = "Select payment method"
            isValid = false
        }

        if (!isValid) return

        viewModel.saveExpense()
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