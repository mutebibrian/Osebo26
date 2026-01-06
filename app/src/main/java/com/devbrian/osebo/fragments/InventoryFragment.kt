package com.devbrian.osebo.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.ProductAdapter
import com.devbrian.osebo.databinding.FragmentInventoryBinding
import com.devbrian.osebo.models.Product


class InventoryFragment : Fragment() {
    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        loadInventoryData()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            // Handle product item click
            showProductDetails(product)
        }

        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.cardAddProduct.setOnClickListener {
            navigateToAddProduct()
        }

        binding.cardRestock.setOnClickListener {
            navigateToRestock()
        }

        binding.cardCategories.setOnClickListener {
            navigateToCategories()
        }

        binding.tvViewLowStock.setOnClickListener {
            navigateToLowStock()
        }

        binding.tvViewAllProducts.setOnClickListener {
            navigateToAllProducts()
        }

        binding.fabAddProduct.setOnClickListener {
            navigateToAddProduct()
        }
    }

    private fun loadInventoryData() {
        // TODO: Load from ViewModel/Repository
        binding.tvTotalItems.text = "156"
        binding.tvLowStock.text = "12"
        binding.tvInventoryValue.text = "UGX 4,500,000"

        val products = listOf(
            Product(
                id = "PROD001",
                name = "iPhone 15 Pro",
                sku = "IP15P-256",
                category = "Electronics",
                price = 1500000.0,
                stock = 15,
                lowStockThreshold = 5,
                imageUrl = null
            ),
            Product(
                id = "PROD002",
                name = "Samsung TV 55\"",
                sku = "STV55-4K",
                category = "Electronics",
                price = 1200000.0,
                stock = 8,
                lowStockThreshold = 3,
                imageUrl = null
            ),
            Product(
                id = "PROD003",
                name = "Nike Air Max",
                sku = "NAMAX-42",
                category = "Footwear",
                price = 250000.0,
                stock = 3,
                lowStockThreshold = 5,
                imageUrl = null
            ),
            Product(
                id = "PROD004",
                name = "HP Laptop 15\"",
                sku = "HPLP15-i7",
                category = "Electronics",
                price = 2800000.0,
                stock = 6,
                lowStockThreshold = 2,
                imageUrl = null
            )
        )

        productAdapter.submitList(products)
    }

    private fun navigateToAddProduct() {
        Toast.makeText(requireContext(), "Navigate to Add Product", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToRestock() {
        Toast.makeText(requireContext(), "Navigate to Restock", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToCategories() {
        Toast.makeText(requireContext(), "Navigate to Categories", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLowStock() {
        Toast.makeText(requireContext(), "Navigate to Low Stock Items", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAllProducts() {
        Toast.makeText(requireContext(), "Navigate to All Products", Toast.LENGTH_SHORT).show()
    }

    private fun showProductDetails(product: Product) {
        Toast.makeText(requireContext(), "Product: ${product.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_inventory, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_scan_barcode -> {
                scanBarcode()
                true
            }
            R.id.action_export_inventory -> {
                exportInventory()
                true
            }
            R.id.action_stock_take -> {
                startStockTake()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun scanBarcode() {
        Toast.makeText(requireContext(), "Scan Barcode", Toast.LENGTH_SHORT).show()
    }

    private fun exportInventory() {
        Toast.makeText(requireContext(), "Export Inventory", Toast.LENGTH_SHORT).show()
    }

    private fun startStockTake() {
        Toast.makeText(requireContext(), "Start Stock Take", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}