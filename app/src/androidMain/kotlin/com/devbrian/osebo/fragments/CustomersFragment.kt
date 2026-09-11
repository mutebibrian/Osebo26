package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.CustomerAdapter
import com.devbrian.osebo.databinding.FragmentCustomersBinding
import com.devbrian.osebo.databinding.DialogAddCustomerBinding
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.ui.viewmodels.CustomerViewModel
import java.text.NumberFormat
import java.util.*

class CustomersFragment : Fragment() {

    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomerViewModel by viewModel()
    private lateinit var customerAdapter: CustomerAdapter
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency = Currency.getInstance("UGX")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        setupSearchListener()
    }

    private fun setupRecyclerView() {
        customerAdapter = CustomerAdapter(
            onItemClick = { customer ->
                showCustomerDetails(customer)
            },
            onMoreOptionsClick = { customer, anchorView ->
                showCustomerOptionsMenu(customer, anchorView)
            }
        )

        binding.rvCustomers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = customerAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.customers.observe(viewLifecycleOwner) { customers ->
            customerAdapter.submitCustomerList(customers)
            binding.tvTotalCustomers.text = customers.size.toString()

            val totalSales = customers.sumOf { it.totalSpent }
            binding.tvCustomerSales.text = formatCurrency(totalSales)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }
    }

    private fun setupSearchListener() {
        binding.etSearchCustomers.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearchCustomers.text.toString()
                if (query.isNotEmpty()) {
                    viewModel.searchCustomers(query)
                } else {
                    viewModel.refreshCustomers()
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupClickListeners() {
        binding.cardAddCustomer.setOnClickListener {
            showAddCustomerDialog()
        }

        binding.fabAddCustomer.setOnClickListener {
            showAddCustomerDialog()
        }

        binding.tvViewAllCustomers.setOnClickListener {
            Toast.makeText(requireContext(), "View all customers", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddCustomerDialog() {
        val dialogBinding = DialogAddCustomerBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Customer")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.etName.text.toString()
                val phone = dialogBinding.etPhone.text.toString()
                val email = dialogBinding.etEmail.text.toString()
                val location = dialogBinding.etLocation.text.toString()

                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    viewModel.createCustomer(
                        name = name,
                        phone = phone,
                        email = email.ifEmpty { null },
                        location = location.ifEmpty { null }
                    )
                } else {
                    Toast.makeText(requireContext(), "Name and phone are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomerOptionsMenu(customer: Customer, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.menu_customer_item, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_view_details -> {
                    showCustomerDetails(customer)
                    true
                }
                R.id.action_edit_customer -> {
                    showEditCustomerDialog(customer)
                    true
                }
                R.id.action_delete_customer -> {
                    deleteCustomer(customer)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showCustomerDetails(customer: Customer) {
        Toast.makeText(requireContext(), "Name: ${customer.name}\nPhone: ${customer.phone}", Toast.LENGTH_LONG).show()
    }

    private fun showEditCustomerDialog(customer: Customer) {
        Toast.makeText(requireContext(), "Edit feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun deleteCustomer(customer: Customer) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Customer")
            .setMessage("Delete ${customer.name}?")
            .setPositiveButton("Delete") { _, _ ->
                
                Toast.makeText(requireContext(), "Delete feature coming soon", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatCurrency(amount: Double): String {
        return when {
            amount >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", amount / 1_000_000)
            amount >= 1_000 -> String.format(Locale.getDefault(), "%.1fK", amount / 1_000)
            else -> String.format(Locale.getDefault(), "%.0f", amount)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


