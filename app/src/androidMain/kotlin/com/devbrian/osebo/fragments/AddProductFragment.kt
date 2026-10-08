package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.ui.screens.AddProductScreen
import com.devbrian.osebo.ui.screens.AddProductUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.InventoryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddProductFragment : Fragment() {
    private val viewModel: InventoryViewModel by viewModel()
    private var uiState by mutableStateOf(AddProductUiState())
    private var productId: String? = null
    private var editingProduct: Product? = null
    private var editFieldsLoaded = false

    private val categories = listOf(
        "Electronics",
        "Clothing",
        "Food & Beverages",
        "Home & Garden",
        "Health & Beauty",
        "Sports",
        "Other",
    )
    private val units = listOf("Piece", "kg", "g", "Litre", "ml", "Meter", "Box", "Pack")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.let(AddProductFragmentArgs::fromBundle)?.productId
        uiState = uiState.copy(isEditMode = !productId.isNullOrBlank())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                AddProductScreen(
                    state = uiState,
                    categories = categories,
                    units = units,
                    onStateChange = { uiState = it },
                    onBackClick = { findNavController().navigateUp() },
                    onUploadImageClick = { showMessage("Image upload coming soon") },
                    onSaveClick = ::validateAndSave,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        if (uiState.isEditMode) viewModel.refreshProducts()
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            if (uiState.isEditMode && !editFieldsLoaded) {
                products.firstOrNull { it.id == productId }?.let(::loadProduct)
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            uiState = uiState.copy(isLoading = loading)
        }
        viewModel.isOffline.observe(viewLifecycleOwner) { offline ->
            uiState = uiState.copy(isOffline = offline)
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                showMessage(it, Toast.LENGTH_LONG)
                viewModel.clearMessages()
            }
        }
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                showMessage(it)
                viewModel.clearMessages()
                findNavController().navigateUp()
            }
        }
    }

    private fun loadProduct(product: Product) {
        editingProduct = product
        editFieldsLoaded = true
        uiState = uiState.copy(
            name = product.name,
            sku = product.sku,
            barcode = product.barcode.orEmpty(),
            category = product.category.orEmpty(),
            unit = product.unit.orEmpty(),
            supplier = product.supplierName.orEmpty(),
            description = product.description.orEmpty(),
            price = product.price.toEditableNumber(),
            cost = product.cost?.toEditableNumber().orEmpty(),
            stock = product.stock.toEditableNumber(),
            lowStockThreshold = product.lowStockThreshold?.toString().orEmpty(),
            location = product.location.orEmpty(),
            taxRate = product.taxRate?.toEditableNumber().orEmpty(),
            weight = product.weight?.toEditableNumber().orEmpty(),
        )
    }

    private fun validateAndSave() {
        val nameError = if (uiState.name.isBlank()) "Product name is required" else null
        val skuError = if (uiState.sku.isBlank()) "SKU is required" else null
        val categoryError = if (uiState.category.isBlank()) "Category is required" else null
        val price = uiState.price.toDoubleOrNull()
        val priceError = when {
            uiState.price.isBlank() -> "Selling price is required"
            price == null -> "Enter a valid price"
            price < 0 -> "Price cannot be negative"
            else -> null
        }
        val cost = uiState.cost.toDoubleOrNull()
        val costError = when {
            uiState.cost.isBlank() -> null
            cost == null -> "Enter a valid cost"
            cost < 0 -> "Cost cannot be negative"
            else -> null
        }
        val stock = uiState.stock.toDoubleOrNull()
        val stockError = when {
            uiState.stock.isBlank() -> "Current stock is required"
            stock == null -> "Enter a valid quantity"
            stock < 0 -> "Stock cannot be negative"
            else -> null
        }
        val threshold = uiState.lowStockThreshold.toIntOrNull()
        val thresholdError = when {
            uiState.lowStockThreshold.isBlank() -> "Low-stock alert is required"
            threshold == null -> "Enter a whole number"
            threshold < 0 -> "Alert cannot be negative"
            else -> null
        }
        val tax = uiState.taxRate.toDoubleOrNull()
        val taxError = when {
            uiState.taxRate.isBlank() -> null
            tax == null -> "Enter a valid rate"
            tax !in 0.0..100.0 -> "Use a rate from 0 to 100"
            else -> null
        }
        val weight = uiState.weight.toDoubleOrNull()
        val weightError = when {
            uiState.weight.isBlank() -> null
            weight == null -> "Enter a valid weight"
            weight < 0 -> "Weight cannot be negative"
            else -> null
        }

        val detailsInvalid = nameError != null || skuError != null || categoryError != null
        val stockInvalid = priceError != null || costError != null || stockError != null ||
            thresholdError != null || taxError != null || weightError != null
        uiState = uiState.copy(
            currentStep = if (detailsInvalid) 0 else 1,
            nameError = nameError,
            skuError = skuError,
            categoryError = categoryError,
            priceError = priceError,
            costError = costError,
            stockError = stockError,
            thresholdError = thresholdError,
            taxError = taxError,
            weightError = weightError,
        )
        if (detailsInvalid || stockInvalid) return

        val existing = editingProduct
        val product = if (existing != null) {
            existing.copy(
                name = uiState.name.trim(),
                sku = uiState.sku.trim(),
                barcode = uiState.barcode.trim().ifBlank { null },
                category = uiState.category,
                unit = uiState.unit.ifBlank { null },
                supplierName = uiState.supplier.trim().ifBlank { null },
                description = uiState.description.trim().ifBlank { null },
                price = price!!,
                cost = cost,
                stock = stock!!,
                lowStockThreshold = threshold,
                location = uiState.location.trim().ifBlank { null },
                taxRate = tax,
                weight = weight,
            )
        } else {
            Product(
                id = "",
                name = uiState.name.trim(),
                sku = uiState.sku.trim(),
                category = uiState.category,
                price = price!!,
                cost = cost,
                stock = stock!!,
                lowStockThreshold = threshold,
                description = uiState.description.trim().ifBlank { null },
                barcode = uiState.barcode.trim().ifBlank { null },
                supplierName = uiState.supplier.trim().ifBlank { null },
                taxRate = tax,
                weight = weight,
                location = uiState.location.trim().ifBlank { null },
                unit = uiState.unit.ifBlank { null },
            )
        }

        if (existing != null) viewModel.updateProduct(product) else viewModel.createProduct(product)
    }

    private fun Double.toEditableNumber(): String =
        if (this == toInt().toDouble()) toInt().toString() else toString()

    private fun showMessage(message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (isAdded) Toast.makeText(requireContext(), message, duration).show()
    }
}
