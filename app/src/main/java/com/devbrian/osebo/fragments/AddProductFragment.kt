package com.devbrian.osebo.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.databinding.FragmentAddProductBinding
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.ui.viewmodels.InventoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by viewModels()

    private var isEditMode = false
    private var productId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupClickListeners()
        setupObservers()
        checkEditMode()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupClickListeners() {
        binding.btnUploadImage.setOnClickListener {
            // TODO: Implement image upload
            Toast.makeText(requireContext(), "Image upload coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveProduct()
            }
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSave.isEnabled = !isLoading
            binding.btnSave.text = if (isLoading) "Saving..." else if (isEditMode) "Update Product" else "Save Product"
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
                findNavController().navigateUp()
            }
        }

        viewModel.isOffline.observe(viewLifecycleOwner) { isOffline ->
            if (isOffline) {
                Toast.makeText(
                    requireContext(),
                    "You're offline. Product will be saved locally and synced when online.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun checkEditMode() {
        arguments?.let {
            productId = AddProductFragmentArgs.fromBundle(it).productId
            if (!productId.isNullOrEmpty()) {
                isEditMode = true
                binding.toolbar.title = "Edit Product"
                loadProductForEdit(productId!!)
            }
        }
    }

    private fun loadProductForEdit(productId: String) {
        val product = viewModel.getProductById(productId)
        product?.let {
            populateFields(it)
        }
    }

    private fun populateFields(product: Product) {
        binding.etProductName.setText(product.name)
        binding.etSku.setText(product.sku)
        binding.etBarcode.setText(product.barcode ?: "")
        binding.etCategory.setText(product.category)
        binding.etSupplier.setText(product.supplierName ?: "")
        binding.etDescription.setText(product.description ?: "")
        binding.etPrice.setText(product.price.toString())
        binding.etCost.setText(product.cost?.toString() ?: "")
        binding.etStock.setText(product.stock.toString())
        binding.etLowStockThreshold.setText(product.lowStockThreshold.toString())
        binding.etLocation.setText(product.location ?: "")
        binding.etTaxRate.setText(product.taxRate?.toString() ?: "")
        binding.etWeight.setText(product.weight?.toString() ?: "")
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Clear previous errors
        binding.etProductName.error = null
        binding.etSku.error = null
        binding.etPrice.error = null
        binding.etStock.error = null
        binding.etLowStockThreshold.error = null

        val name = binding.etProductName.text.toString().trim()
        if (TextUtils.isEmpty(name)) {
            binding.etProductName.error = "Product name is required"
            isValid = false
        }

        val sku = binding.etSku.text.toString().trim()
        if (TextUtils.isEmpty(sku)) {
            binding.etSku.error = "SKU is required"
            isValid = false
        }

        val priceStr = binding.etPrice.text.toString().trim()
        if (TextUtils.isEmpty(priceStr)) {
            binding.etPrice.error = "Price is required"
            isValid = false
        } else {
            try {
                priceStr.toDouble()
            } catch (e: NumberFormatException) {
                binding.etPrice.error = "Invalid price format"
                isValid = false
            }
        }

        val stockStr = binding.etStock.text.toString().trim()
        if (TextUtils.isEmpty(stockStr)) {
            binding.etStock.error = "Stock quantity is required"
            isValid = false
        } else {
            try {
                stockStr.toInt()
            } catch (e: NumberFormatException) {
                binding.etStock.error = "Invalid stock format"
                isValid = false
            }
        }

        val thresholdStr = binding.etLowStockThreshold.text.toString().trim()
        if (TextUtils.isEmpty(thresholdStr)) {
            binding.etLowStockThreshold.error = "Low stock threshold is required"
            isValid = false
        } else {
            try {
                thresholdStr.toInt()
            } catch (e: NumberFormatException) {
                binding.etLowStockThreshold.error = "Invalid threshold format"
                isValid = false
            }
        }

        return isValid
    }

    private fun saveProduct() {
        val product = Product(
            id = productId ?: "",
            name = binding.etProductName.text.toString().trim(),
            sku = binding.etSku.text.toString().trim(),
            category = binding.etCategory.text.toString().trim(),
            price = binding.etPrice.text.toString().toDouble(),
            cost = binding.etCost.text.toString().trim().takeIf { it.isNotEmpty() }?.toDouble(),
            stock = binding.etStock.text.toString().toInt(),
            lowStockThreshold = binding.etLowStockThreshold.text.toString().toInt(),
            imageUrl = null,
            description = binding.etDescription.text.toString().trim().takeIf { it.isNotEmpty() },
            barcode = binding.etBarcode.text.toString().trim().takeIf { it.isNotEmpty() },
            supplierName = binding.etSupplier.text.toString().trim().takeIf { it.isNotEmpty() },
            taxRate = binding.etTaxRate.text.toString().trim().takeIf { it.isNotEmpty() }?.toDouble(),
            weight = binding.etWeight.text.toString().trim().takeIf { it.isNotEmpty() }?.toDouble(),
            location = binding.etLocation.text.toString().trim().takeIf { it.isNotEmpty() }
        )

        if (isEditMode && !productId.isNullOrEmpty()) {
            viewModel.updateProduct(product)
        } else {
            viewModel.createProduct(product)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}