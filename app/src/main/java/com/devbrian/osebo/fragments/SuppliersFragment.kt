package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentSuppliersBinding
import com.devbrian.osebo.databinding.ItemSupplierBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SuppliersFragment : Fragment() {
    private var _binding: FragmentSuppliersBinding? = null
    private val binding get() = _binding!!
    private lateinit var suppliersAdapter: SuppliersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuppliersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        loadSuppliers()
    }

    private fun setupRecyclerView() {
        suppliersAdapter = SuppliersAdapter { supplier ->
            showSupplierOptions(supplier)
        }

        binding.rvSuppliers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = suppliersAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.fabAddSupplier.setOnClickListener {
            showAddSupplierDialog()
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadSuppliers()
        }

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterSuppliers(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterSuppliers(newText)
                return true
            }
        })
    }

    private fun loadSuppliers() {
        binding.swipeRefresh.isRefreshing = true

        // TODO: Load from API
        // For now, show sample data
        val sampleSuppliers = listOf(
            Supplier(
                id = "1",
                name = "ABC Distributors",
                contactPerson = "John Doe",
                phone = "+256 700 123456",
                email = "john@abcdist.com",
                address = "Kampala Road, Kampala",
                products = 45,
                totalPurchases = 15000000.0
            ),
            Supplier(
                id = "2",
                name = "XYZ Wholesale",
                contactPerson = "Jane Smith",
                phone = "+256 712 987654",
                email = "jane@xyzwholesale.com",
                address = "Jinja Road, Kampala",
                products = 32,
                totalPurchases = 8750000.0
            ),
            Supplier(
                id = "3",
                name = "Global Imports Ltd",
                contactPerson = "Peter Mwangi",
                phone = "+256 701 456789",
                email = "peter@globalimports.com",
                address = "Industrial Area, Nairobi",
                products = 78,
                totalPurchases = 23400000.0
            )
        )

        binding.swipeRefresh.isRefreshing = false
        suppliersAdapter.submitList(sampleSuppliers)

        // Show empty state if no suppliers
        if (sampleSuppliers.isEmpty()) {
            binding.rvSuppliers.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.rvSuppliers.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
        }
    }

    private fun filterSuppliers(query: String?) {
    }

    private fun showAddSupplierDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_supplier, null)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add New Supplier")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = dialogView.findViewById<TextInputEditText>(R.id.et_supplier_name).text.toString()
                val contact = dialogView.findViewById<TextInputEditText>(R.id.et_contact_person).text.toString()
                val phone = dialogView.findViewById<TextInputEditText>(R.id.et_phone).text.toString()
                val email = dialogView.findViewById<TextInputEditText>(R.id.et_email).text.toString()
                val address = dialogView.findViewById<TextInputEditText>(R.id.et_address).text.toString()

                Toast.makeText(requireContext(), "Supplier added: $name", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSupplierOptions(supplier: Supplier) {
        val options = arrayOf("View Details", "Edit", "View Products", "Purchase History", "Delete")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(supplier.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSupplierDetails(supplier)
                    1 -> showEditSupplierDialog(supplier)
                    2 -> viewSupplierProducts(supplier)
                    3 -> viewPurchaseHistory(supplier)
                    4 -> confirmDeleteSupplier(supplier)
                }
            }
            .show()
    }

    private fun showSupplierDetails(supplier: Supplier) {
        val details = """
            Name: ${supplier.name}
            Contact: ${supplier.contactPerson}
            Phone: ${supplier.phone}
            Email: ${supplier.email}
            Address: ${supplier.address}
            Products Supplied: ${supplier.products}
            Total Purchases: ${formatCurrency(supplier.totalPurchases)}
        """.trimIndent()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supplier Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showEditSupplierDialog(supplier: Supplier) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_supplier, null).apply {
            findViewById<TextInputEditText>(R.id.et_supplier_name).setText(supplier.name)
            findViewById<TextInputEditText>(R.id.et_contact_person).setText(supplier.contactPerson)
            findViewById<TextInputEditText>(R.id.et_phone).setText(supplier.phone)
            findViewById<TextInputEditText>(R.id.et_email).setText(supplier.email)
            findViewById<TextInputEditText>(R.id.et_address).setText(supplier.address)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Supplier")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                Toast.makeText(requireContext(), "Supplier updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun viewSupplierProducts(supplier: Supplier) {
        Toast.makeText(requireContext(), "Viewing products from ${supplier.name}", Toast.LENGTH_SHORT).show()
        // Navigate to products filtered by supplier
    }

    private fun viewPurchaseHistory(supplier: Supplier) {
        Toast.makeText(requireContext(), "Purchase history for ${supplier.name}", Toast.LENGTH_SHORT).show()
        // Navigate to purchase history
    }

    private fun confirmDeleteSupplier(supplier: Supplier) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Supplier")
            .setMessage("Are you sure you want to delete ${supplier.name}?")
            .setPositiveButton("Delete") { _, _ ->
                Toast.makeText(requireContext(), "Supplier deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatCurrency(amount: Double): String {
        return String.format("UGX %,.0f", amount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class Supplier(
        val id: String,
        val name: String,
        val contactPerson: String,
        val phone: String,
        val email: String,
        val address: String,
        val products: Int,
        val totalPurchases: Double
    )
}

class SuppliersAdapter(
    private val onItemClick: (SuppliersFragment.Supplier) -> Unit
) : RecyclerView.Adapter<SuppliersAdapter.SupplierViewHolder>() {

    private var suppliers = listOf<SuppliersFragment.Supplier>()

    fun submitList(list: List<SuppliersFragment.Supplier>) {
        suppliers = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val binding = ItemSupplierBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SupplierViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        holder.bind(suppliers[position])
    }

    override fun getItemCount() = suppliers.size

    inner class SupplierViewHolder(
        private val binding: ItemSupplierBinding
    ) : RecyclerView.ViewHolder(binding.getRoot()) {  // FIXED: Changed binding.root to binding.getRoot()

        fun bind(supplier: SuppliersFragment.Supplier) {
            binding.tvSupplierName.text = supplier.name
            binding.tvContactPerson.text = supplier.contactPerson
            binding.tvPhone.text = supplier.phone
            binding.tvProductsCount.text = "${supplier.products} products"

            val formattedAmount = String.format("UGX %,.0f", supplier.totalPurchases)
            binding.tvTotalPurchases.text = formattedAmount

            binding.tvInitial.text = supplier.name.first().toString().uppercase()

            // Set random background color for avatar
            val colors = arrayOf(
                R.color.avatar_blue,
                R.color.avatar_green,
                R.color.avatar_orange,
                R.color.avatar_purple,
                R.color.avatar_red
            )
            val colorRes = colors[adapterPosition % colors.size]
            binding.avatarLayout.setBackgroundResource(colorRes)

            binding.root.setOnClickListener {
                onItemClick(supplier)
            }
        }
    }
}