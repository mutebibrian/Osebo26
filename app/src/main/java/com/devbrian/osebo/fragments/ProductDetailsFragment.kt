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
import androidx.navigation.fragment.navArgs
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentProductDetailsBinding
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.ui.viewmodels.InventoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {

    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by viewModels()
    private val args: ProductDetailsFragmentArgs by navArgs()

    private var currentProduct: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupClickListeners()
        setupObservers()
        loadProduct()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            navigateToEditProduct()
        }

        binding.btnRestock.setOnClickListener {
            showRestockDialog()
        }
    }

    private fun setupObservers() {
        viewModel.isOffline.observe(viewLifecycleOwner) { isOffline ->
            if (isOffline) {
                binding.tvNetworkStatus?.text = "📴 Offline Mode"
                binding.tvNetworkStatus?.visibility = View.VISIBLE
                binding.tvNetworkStatus?.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.orange_500)
                )
            } else {
                binding.tvNetworkStatus?.visibility = View.GONE
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
                loadProduct() 
            }
        }
    }

    private fun loadProduct() {
        val product = viewModel.getProductById(args.productId)
        if (product != null) {
            currentProduct = product
            displayProductDetails(product)
        } else {
            Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun displayProductDetails(product: Product) {
        binding.apply {
            tvProductName.text = product.name
            tvProductSku.text = "SKU: ${product.sku}"
            tvCategory.text = product.category
            tvSupplier.text = product.supplierName ?: "No supplier"
            tvDescription.text = product.description ?: "No description available"

            
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
                currency = java.util.Currency.getInstance("UGX")
            }

            tvSellingPrice.text = currencyFormat.format(product.price)
            tvCostPrice.text = product.cost?.let { currencyFormat.format(it) } ?: "N/A"

            
            val profitMargin = if (product.cost != null && product.cost > 0) {
                ((product.price - product.cost) / product.price * 100).toInt()
            } else {
                0
            }
            tvProfitMargin.text = "$profitMargin%"

            
            tvCurrentStock.text = "${product.stock} units"
            tvLowStockThreshold.text = "${product.lowStockThreshold} units"

            val stockStatus = when {
                product.stock <= 0 -> "Out of Stock"
                product.stock <= product.lowStockThreshold -> "Low Stock"
                else -> "Healthy Stock"
            }
            tvStockStatus.text = stockStatus

            
            val maxStock = product.lowStockThreshold * 3 
            val progress = ((product.stock.toFloat() / maxStock) * 100).toInt().coerceIn(0, 100)
            progressStock.progress = progress

            when {
                product.stock <= 0 -> progressStock.progressTintList = ContextCompat.getColorStateList(requireContext(), R.color.red_error)
                product.stock <= product.lowStockThreshold -> progressStock.progressTintList = ContextCompat.getColorStateList(requireContext(), R.color.orange_500)
                else -> progressStock.progressTintList = ContextCompat.getColorStateList(requireContext(), R.color.green_success)
            }

            
            tvBarcode.text = product.barcode ?: "N/A"
            tvLocation.text = product.location ?: "N/A"
            tvTaxRate.text = product.taxRate?.let { "$it%" } ?: "N/A"
        }
    }

    private fun navigateToEditProduct() {
        val action = ProductDetailsFragmentDirections
            .actionProductDetailsToAddProduct(args.productId)
        findNavController().navigate(action)
    }

    private fun showRestockDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Enter quantity to add"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Restock Product")
            .setMessage("Add quantity to ${currentProduct?.name}")
            .setView(input)
            .setPositiveButton("Restock") { _, _ ->
                val quantityStr = input.text.toString()
                if (quantityStr.isNotEmpty()) {
                    try {
                        val quantity = quantityStr.toInt()
                        if (quantity > 0) {
                            restockProduct(quantity)
                        } else {
                            Toast.makeText(requireContext(), "Please enter a valid quantity", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: NumberFormatException) {
                        Toast.makeText(requireContext(), "Invalid quantity", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun restockProduct(quantity: Int) {
        currentProduct?.let { product ->
            val updatedProduct = product.copy(stock = product.stock + quantity)
            viewModel.updateProduct(updatedProduct)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_product_details, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                navigateToEditProduct()
                true
            }
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            R.id.action_share -> {
                shareProduct()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete ${currentProduct?.name}?")
            .setPositiveButton("Delete") { _, _ ->
                currentProduct?.let {
                    viewModel.deleteProduct(it.id)
                    findNavController().navigateUp()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareProduct() {
        currentProduct?.let { product ->
            val shareText = buildString {
                appendLine("📦 ${product.name}")
                appendLine("SKU: ${product.sku}")
                appendLine("Price: UGX ${product.price}")
                appendLine("Stock: ${product.stock} units")
                product.description?.let { appendLine("\n$it") }
            }

            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "Share Product"))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

