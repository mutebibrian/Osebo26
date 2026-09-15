package com.devbrian.osebo.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.databinding.FragmentAddProductBinding
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.ui.viewmodels.InventoryViewModel

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by viewModel()

    private var isEditMode = false
    private var productId: String? = null
    private var fieldsPopulated = false

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
        setupDropdowns()
        setupClickListeners()
        setupObservers()
        checkEditMode()
    }

    private fun setupDropdowns() {
        // Setup Categories
        val categories = arrayOf("Electronics", "Clothing", "Food & Beverages", "Home & Garden", "Health & Beauty", "Sports", "Other")
        val categoryAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.spinnerCategory.setAdapter(categoryAdapter)

        // Setup Units
        val units = arrayOf("Piece", "kg", "g", "Litre", "ml", "Meter", "Box", "Pack")
        val unitAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, units)
        binding.spinnerUnit.setAdapter(unitAdapter)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupClickListeners() {
        binding.btnUploadImage.setOnClickListener {
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
        // Fresh InventoryViewModel instance for this screen - its product list loads
        // asynchronously (a Flow collected in the ViewModel's init block), so it's almost
        // always still empty right here. Falling straight through to product?.let{} silently
        // left the edit form blank instead of populated. Wait for the list if it's not warm yet.
        val immediate = viewModel.getProductById(productId)
        if (immediate != null) {
            fieldsPopulated = true
            populateFields(immediate)
            return
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            if (fieldsPopulated || products.isEmpty()) return@observe
            products.find { it.id == productId }?.let {
                fieldsPopulated = true
                populateFields(it)
            }
        }
    }

    private fun populateFields(product: Product) {
        binding.etProductName.setText(product.name)
        binding.etSku.setText(product.sku)
        binding.etBarcode.setText(product.barcode ?: "")
        binding.spinnerCategory.setText(product.category, false)
        binding.spinnerUnit.setText(product.unit ?: "", false)
        binding.etSupplier.setText(product.supplierName ?: "")
        binding.etDescription.setText(product.description ?: "")
        binding.etPrice.setText(product.price.toString())
        binding.etCost.setText(product.cost?.toString() ?: "")
        // Convert Double to Int for display (but store as string)
        binding.etStock.setText(if (product.stock == product.stock.toInt().toDouble()) {
            product.stock.toInt().toString()
        } else {
            product.stock.toString()
        })
        binding.etLowStockThreshold.setText(product.lowStockThreshold.toString())
        binding.etLocation.setText(product.location ?: "")
        binding.etTaxRate.setText(product.taxRate?.toString() ?: "")
        binding.etWeight.setText(product.weight?.toString() ?: "")
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        binding.etProductName.error = null
        binding.etSku.error = null
        binding.etPrice.error = null
        binding.etStock.error = null
        binding.etLowStockThreshold.error = null
        binding.spinnerCategory.error = null

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

        val category = binding.spinnerCategory.text.toString().trim()
        if (TextUtils.isEmpty(category)) {
            binding.spinnerCategory.error = "Category is required"
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
                stockStr.toDouble()  // Changed from toInt() to toDouble()
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
        // Parse stock as Double
        val stockValue = binding.etStock.text.toString().trim().toDouble()

        val product = Product(
            id = productId ?: "",
            name = binding.etProductName.text.toString().trim(),
            sku = binding.etSku.text.toString().trim(),
            category = binding.spinnerCategory.text.toString().trim(),
            categoryId = null,
            price = binding.etPrice.text.toString().toDouble(),
            cost = binding.etCost.text.toString().trim().takeIf { it.isNotEmpty() }?.toDouble(),
            stock = stockValue,  // Now using Double
            lowStockThreshold = binding.etLowStockThreshold.text.toString().toInt(),
            imageUrl = null,
            description = binding.etDescription.text.toString().trim().takeIf { it.isNotEmpty() },
            barcode = binding.etBarcode.text.toString().trim().takeIf { it.isNotEmpty() },
            supplierId = null,
            supplierName = binding.etSupplier.text.toString().trim().takeIf { it.isNotEmpty() },
            taxRate = binding.etTaxRate.text.toString().trim().takeIf { it.isNotEmpty() }?.toDouble(),
            weight = binding.etWeight.text.toString().trim().takeIf { it.isNotEmpty() }?.toDouble(),
            dimensions = null,
            location = binding.etLocation.text.toString().trim().takeIf { it.isNotEmpty() },
            isActive = true,
            createdAt = null,
            updatedAt = null,
            maxDiscount = null,
            unit = binding.spinnerUnit.text.toString().trim().takeIf { it.isNotEmpty() },
            allowsFloatQuantity = null,
            shopId = null,
            shopName = null,
            photos = null
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