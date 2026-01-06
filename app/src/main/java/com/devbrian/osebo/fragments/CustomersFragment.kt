package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.CustomerAdapter
import com.devbrian.osebo.databinding.FragmentCustomersBinding
import com.devbrian.osebo.models.Customer

class CustomersFragment : Fragment() {
    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!
    private lateinit var customerAdapter: CustomerAdapter

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

        setHasOptionsMenu(true)
        setupRecyclerView()
        setupClickListeners()
        loadCustomerData()
    }

    private fun setupRecyclerView() {
        // FIX: Make sure to pass both required parameters
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
                    editCustomer(customer)
                    true
                }
                R.id.action_view_purchases -> {
                    viewCustomerPurchases(customer)
                    true
                }
                R.id.action_send_message -> {
                    sendMessageToCustomer(customer)
                    true
                }
                R.id.action_add_loyalty -> {
                    addLoyaltyPoints(customer)
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

    private fun setupClickListeners() {
        binding.cardAddCustomer.setOnClickListener {
            navigateToAddCustomer()
        }

        binding.cardLoyalty.setOnClickListener {
            navigateToLoyaltyProgram()
        }

        binding.cardGroups.setOnClickListener {
            navigateToCustomerGroups()
        }

        binding.cardCommunication.setOnClickListener {
            navigateToCommunication()
        }

        binding.cardAnalytics.setOnClickListener {
            navigateToCustomerAnalytics()
        }

        binding.tvViewAllCustomers.setOnClickListener {
            navigateToAllCustomers()
        }

        binding.fabAddCustomer.setOnClickListener {
            navigateToAddCustomer()
        }

        binding.etSearchCustomers.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch(binding.etSearchCustomers.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun loadCustomerData() {
        binding.tvTotalCustomers.text = "3"
        binding.tvCustomerSales.text = "1.78M"

        val customers = listOf(
            Customer(
                id = "CUST001",
                name = "John Doe",
                email = "john.doe@email.com",
                phone = "+256 712 345 678",
                totalSpent = 900000.0,
                lastPurchase = "Dec 23, 2025",
                totalPurchases = 5,
                customerSince = "Nov 2025",
                loyaltyPoints = 450
            ),
            Customer(
                id = "CUST002",
                name = "Jane Smith",
                email = "jane.smith@email.com",
                phone = "+256 712 345 679",
                totalSpent = 560000.0,
                lastPurchase = "Dec 20, 2025",
                totalPurchases = 3,
                customerSince = "Oct 2025",
                loyaltyPoints = 280
            ),
            Customer(
                id = "CUST003",
                name = "Robert Johnson",
                email = "robert.j@email.com",
                phone = "+256 712 345 680",
                totalSpent = 320000.0,
                lastPurchase = "Dec 18, 2025",
                totalPurchases = 2,
                customerSince = "Dec 2025",
                loyaltyPoints = 160
            )
        )

        customerAdapter.submitCustomerList(customers)
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            // Filter customers based on search query
            val filteredCustomers = customerAdapter.currentList.filter { customer ->
                customer.name.contains(query, ignoreCase = true) ||
                        customer.email.contains(query, ignoreCase = true) ||
                        customer.phone.contains(query, ignoreCase = true)
            }
            customerAdapter.submitCustomerList(filteredCustomers)

            // Show search result count
            val resultText = if (filteredCustomers.isEmpty()) {
                "No customers found for '$query'"
            } else {
                "Found ${filteredCustomers.size} customers"
            }
            Toast.makeText(requireContext(), resultText, Toast.LENGTH_SHORT).show()
        } else {
            // If query is empty, reload all customers
            loadCustomerData()
        }
    }

    private fun showCustomerDetails(customer: Customer) {
        Toast.makeText(
            requireContext(),
            "Showing details for: ${customer.name}",
            Toast.LENGTH_SHORT
        ).show()
        // Navigate to customer detail screen
    }

    private fun editCustomer(customer: Customer) {
        Toast.makeText(
            requireContext(),
            "Editing customer: ${customer.name}",
            Toast.LENGTH_SHORT
        ).show()
        // Navigate to edit customer screen
    }

    private fun viewCustomerPurchases(customer: Customer) {
        Toast.makeText(
            requireContext(),
            "Viewing purchases for: ${customer.name}",
            Toast.LENGTH_SHORT
        ).show()
        // Navigate to purchase history screen
    }

    private fun sendMessageToCustomer(customer: Customer) {
        Toast.makeText(
            requireContext(),
            "Sending message to: ${customer.name}",
            Toast.LENGTH_SHORT
        ).show()
        // Open messaging dialog
    }

    private fun addLoyaltyPoints(customer: Customer) {
        Toast.makeText(
            requireContext(),
            "Adding loyalty points for: ${customer.name}",
            Toast.LENGTH_SHORT
        ).show()
        // Open add loyalty points dialog
    }

    private fun deleteCustomer(customer: Customer) {
        // Show confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Customer")
            .setMessage("Are you sure you want to delete ${customer.name}?")
            .setPositiveButton("Delete") { _, _ ->
                // Delete customer logic
                Toast.makeText(
                    requireContext(),
                    "Deleted customer: ${customer.name}",
                    Toast.LENGTH_SHORT
                ).show()
                // Remove from adapter
                val updatedList = customerAdapter.currentList.toMutableList()
                updatedList.remove(customer)
                customerAdapter.submitCustomerList(updatedList)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToAddCustomer() {
        Toast.makeText(requireContext(), "Navigate to Add Customer", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLoyaltyProgram() {
        Toast.makeText(requireContext(), "Navigate to Loyalty Program", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToCustomerGroups() {
        Toast.makeText(requireContext(), "Navigate to Customer Groups", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToCommunication() {
        Toast.makeText(requireContext(), "Navigate to Communication", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToCustomerAnalytics() {
        Toast.makeText(requireContext(), "Navigate to Customer Analytics", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAllCustomers() {
        Toast.makeText(requireContext(), "Navigate to All Customers", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_customer_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import_customers -> {
                importCustomers()
                true
            }
            R.id.action_export_customers -> {
                exportCustomers()
                true
            }
            R.id.action_send_bulk_message -> {
                sendBulkMessage()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun importCustomers() {
        Toast.makeText(requireContext(), "Import Customers", Toast.LENGTH_SHORT).show()
    }

    private fun exportCustomers() {
        Toast.makeText(requireContext(), "Export Customers", Toast.LENGTH_SHORT).show()
    }

    private fun sendBulkMessage() {
        Toast.makeText(requireContext(), "Send Bulk Message", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}