package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.RestockAdapter
import com.devbrian.osebo.databinding.FragmentRestockBinding
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.ui.viewmodels.InventoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class RestockFragment : Fragment() {

    private var _binding: FragmentRestockBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var adapter: RestockAdapter
    private var allProducts: List<Product> = emptyList()
    private var isRestocking = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRestockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup toolbar back button
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        setupSearchView()
        setupObservers()

        // Load products once
        viewModel.refreshProducts()
    }

    private fun setupRecyclerView() {
        adapter = RestockAdapter { _, _, _ ->
            updateSelectedSummary()
        }

        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RestockFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterProducts(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterProducts(newText)
                return true
            }
        })
    }

    private fun filterProducts(query: String) {
        if (query.isEmpty()) {
            adapter.submitList(allProducts)
        } else {
            val filtered = allProducts.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.sku.contains(query, ignoreCase = true) ||
                        product.barcode?.contains(query, ignoreCase = true) == true
            }
            adapter.submitList(filtered)
        }
    }

    private fun setupObservers() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            adapter.submitList(products)
            updateSelectedSummary()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // IMPORTANT: Only handle success messages from restock operations, not from refresh
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                // Only navigate back if we're in the middle of a restock operation
                if (isRestocking) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearMessages()
                    findNavController().navigateUp()
                    isRestocking = false
                }
                // If it's just a refresh success message, ignore it
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearMessages()
                isRestocking = false
            }
        }
    }

    private fun updateSelectedSummary() {
        val selectedCount = adapter.getSelectedCount()
        if (selectedCount > 0) {
            binding.cardSelectedSummary.visibility = View.VISIBLE
            binding.tvSelectedCount.text = "$selectedCount item(s) selected"

            binding.btnProcessRestock.setOnClickListener {
                processRestock()
            }
        } else {
            binding.cardSelectedSummary.visibility = View.GONE
        }
    }

    private fun processRestock() {
        val selectedProducts = adapter.getSelectedProducts()

        if (selectedProducts.isEmpty()) {
            Toast.makeText(requireContext(), "No products selected", Toast.LENGTH_SHORT).show()
            return
        }

        // Show confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm Restock")
            .setMessage(buildConfirmationMessage(selectedProducts))
            .setPositiveButton("Confirm Restock") { _, _ ->
                performRestock(selectedProducts)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun buildConfirmationMessage(selectedProducts: Map<Product, Double>): String {
        val builder = StringBuilder()
        builder.append("You are about to restock the following items:\n\n")

        selectedProducts.forEach { (product, quantity) ->
            builder.append("• ${product.name}: +${formatQuantity(product, quantity)}\n")
        }

        builder.append("\nProceed with restock?")
        return builder.toString()
    }

    private fun formatQuantity(product: Product, quantity: Double): String {
        return if (product.allowsFloatQuantity == true) {
            String.format("%.1f", quantity)
        } else {
            String.format("%.0f", quantity)
        }
    }

    private fun performRestock(selectedProducts: Map<Product, Double>) {
        if (isRestocking) return
        isRestocking = true

        lifecycleScope.launch {
            var successCount = 0
            var failCount = 0

            selectedProducts.forEach { (product, quantity) ->
                try {
                    val updatedProduct = product.copy(stock = product.stock + quantity)

                    // Call updateProduct and wait for result
                    val result = viewModel.updateProductAndWait(updatedProduct)

                    if (result) {
                        successCount++
                        println("✅ Successfully restocked ${product.name}: +$quantity, New stock: ${product.stock + quantity}")
                    } else {
                        failCount++
                        println("❌ Failed to restock ${product.name}")
                    }
                } catch (e: Exception) {
                    failCount++
                    println("❌ Error restocking ${product.name}: ${e.message}")
                }
            }

            // Show results
            if (successCount > 0) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "✅ Restocked $successCount product(s) successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            if (failCount > 0) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "⚠️ Failed to restock $failCount product(s)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            isRestocking = false

            // Refresh products to show updated quantities
            delay(1500)
            viewModel.refreshProducts()

            // Navigate back
            withContext(Dispatchers.Main) {
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}