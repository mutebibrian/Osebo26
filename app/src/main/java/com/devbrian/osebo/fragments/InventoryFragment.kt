package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.ProductAdapter
import com.devbrian.osebo.databinding.FragmentInventoryBinding
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.ui.viewmodels.InventoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.filter

@AndroidEntryPoint
class InventoryFragment : Fragment() {
    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private val viewModel: InventoryViewModel by viewModels()

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
        setupObservers()

        
        viewModel.refreshProducts()
        viewModel.loadStats()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            navigateToProductDetails(product)
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

        binding.swipeRefreshLayout?.setOnRefreshListener {
            viewModel.refreshProducts()
        }
    }

    private fun setupObservers() {
        
        viewModel.products.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)

            
            if (products.isEmpty()) {
                binding.tvEmptyState?.visibility = View.VISIBLE
                binding.rvProducts.visibility = View.GONE
            } else {
                binding.tvEmptyState?.visibility = View.GONE
                binding.rvProducts.visibility = View.VISIBLE
            }
        }

        
        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            binding.tvTotalItems.text = stats.totalItems.toString()
            binding.tvLowStock.text = stats.lowStock.toString()
            binding.tvInventoryValue.text = String.format("UGX %,d", stats.totalValue.toInt())
        }

        
        viewModel.isOffline.observe(viewLifecycleOwner) { isOffline ->
            if (isOffline) {
                binding.tvNetworkStatus?.text = "📴 Offline Mode"
                binding.tvNetworkStatus?.visibility = View.VISIBLE
                binding.tvNetworkStatus?.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.orange_500)
                )
            } else {
                binding.tvNetworkStatus?.text = viewModel.connectionType.value ?: "🌐 Online"
                binding.tvNetworkStatus?.visibility = View.VISIBLE
                binding.tvNetworkStatus?.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.green_500)
                )
            }
        }

        
        viewModel.connectionType.observe(viewLifecycleOwner) { connectionType ->
            if (!(viewModel.isOffline.value == true)) {
                binding.tvNetworkStatus?.text = "🌐 $connectionType"
            }
        }

        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        
        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefreshLayout?.isRefreshing = isRefreshing
        }

        
        viewModel.syncPending.observe(viewLifecycleOwner) { isPending ->
            if (isPending) {
                binding.tvSyncStatus?.text = "⏳ Syncing..."
                binding.tvSyncStatus?.visibility = View.VISIBLE
            } else {
                binding.tvSyncStatus?.visibility = View.GONE
            }
        }

        
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }

        
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

        
        binding.tvNetworkStatus?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                viewModel.getNetworkStatusMessage(),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun navigateToAddProduct() {
        
        val action = InventoryFragmentDirections.actionInventoryToAddProduct()
        findNavController().navigate(action)
    }

    private fun navigateToProductDetails(product: Product) {
        
        val action = InventoryFragmentDirections.actionInventoryToProductDetails(product.id)
        findNavController().navigate(action)
    }

    private fun navigateToRestock() {
        Toast.makeText(requireContext(), "Navigate to Restock", Toast.LENGTH_SHORT).show()
        
    }

    private fun navigateToCategories() {
        Toast.makeText(requireContext(), "Navigate to Categories", Toast.LENGTH_SHORT).show()
        
    }

    private fun navigateToLowStock() {
        val lowStockProducts = productAdapter.currentList.filter {
            it.stock <= (it.lowStockThreshold ?: 5)
        }

        if (lowStockProducts.isEmpty()) {
            Toast.makeText(requireContext(), "No low stock items", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                requireContext(),
                "${lowStockProducts.size} low stock items",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun navigateToAllProducts() {
        
        binding.rvProducts.smoothScrollToPosition(0)
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
            R.id.action_sync_now -> {
                syncNow()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun scanBarcode() {
        if (!viewModel.isOffline.value!!) {
            Toast.makeText(requireContext(), "Scan Barcode", Toast.LENGTH_SHORT).show()
            
        } else {
            Toast.makeText(
                requireContext(),
                "Cannot scan while offline",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun exportInventory() {
        if (!viewModel.isOffline.value!!) {
            Toast.makeText(requireContext(), "Export Inventory", Toast.LENGTH_SHORT).show()
            
        } else {
            Toast.makeText(
                requireContext(),
                "Cannot export while offline",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startStockTake() {
        Toast.makeText(requireContext(), "Start Stock Take", Toast.LENGTH_SHORT).show()
        
    }

    private fun syncNow() {
        if (!viewModel.isOffline.value!!) {
            viewModel.refreshProducts()
        } else {
            Toast.makeText(
                requireContext(),
                "Cannot sync while offline",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

