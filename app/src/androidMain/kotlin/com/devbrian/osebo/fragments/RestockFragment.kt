package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.ui.screens.AddStockProductUi
import com.devbrian.osebo.ui.screens.AddStockScreen
import com.devbrian.osebo.ui.screens.AddStockUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.InventoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RestockFragment : Fragment() {
    private val viewModel: InventoryViewModel by viewModel()
    private var uiState by mutableStateOf(AddStockUiState())
    private var allProducts: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                OseboTheme {
                    AddStockScreen(
                        state = uiState,
                        onBackClick = { findNavController().navigateUp() },
                        onQueryChange = { query -> uiState = uiState.copy(query = query) },
                        onQuantityChange = ::updateQuantity,
                        onDecreaseQuantity = { productId -> adjustQuantity(productId, increase = false) },
                        onIncreaseQuantity = { productId -> adjustQuantity(productId, increase = true) },
                        onAddStockClick = ::confirmRestock,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        viewModel.refreshProducts()
    }

    private fun setupObservers() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            allProducts = products
            uiState = uiState.copy(
                products = products.map { product ->
                    AddStockProductUi(
                        id = product.id,
                        name = product.name,
                        sku = product.sku,
                        currentStock = "${product.displayStock} ${product.unitDisplay}",
                        unit = product.unitDisplay,
                        allowsDecimalQuantity = product.allowsDecimalQuantity,
                        isLowStock = product.isLowStock,
                    )
                },
            )
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            uiState = uiState.copy(isLoading = isRefreshing)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null && !uiState.isProcessing) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }
    }

    private fun updateQuantity(productId: String, rawQuantity: String) {
        val product = allProducts.firstOrNull { it.id == productId } ?: return
        val sanitized = sanitizeQuantity(rawQuantity, product.allowsDecimalQuantity).take(9)
        uiState = uiState.copy(
            quantities = uiState.quantities.toMutableMap().apply {
                if (sanitized.isBlank()) remove(productId) else put(productId, sanitized)
            },
        )
    }

    private fun sanitizeQuantity(rawQuantity: String, allowsDecimal: Boolean): String {
        if (!allowsDecimal) return rawQuantity.filter(Char::isDigit)

        var decimalSeen = false
        return buildString {
            rawQuantity.forEach { character ->
                when {
                    character.isDigit() -> append(character)
                    character == '.' && !decimalSeen -> {
                        append(character)
                        decimalSeen = true
                    }
                }
            }
        }
    }

    private fun adjustQuantity(productId: String, increase: Boolean) {
        val product = allProducts.firstOrNull { it.id == productId } ?: return
        val current = uiState.quantities[productId]?.toDoubleOrNull() ?: 0.0
        val step = if (product.allowsDecimalQuantity) 0.1 else 1.0
        val updated = if (increase) current + step else (current - step).coerceAtLeast(0.0)
        val formatted = if (product.allowsDecimalQuantity) {
            String.format("%.1f", updated)
        } else {
            updated.toInt().toString()
        }
        updateQuantity(productId, if (updated == 0.0) "" else formatted)
    }

    private fun selectedProducts(): Map<Product, Double> {
        return uiState.quantities.mapNotNull { (productId, rawQuantity) ->
            val product = allProducts.firstOrNull { it.id == productId }
            val quantity = rawQuantity.toDoubleOrNull() ?: 0.0
            if (product != null && quantity > 0.0) product to quantity else null
        }.toMap()
    }

    private fun confirmRestock() {
        val selectedProducts = selectedProducts()
        if (selectedProducts.isEmpty()) {
            Toast.makeText(requireContext(), "Choose a quantity first", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Add Stock")
            .setMessage(buildConfirmationMessage(selectedProducts))
            .setPositiveButton("Add Stock") { _, _ -> performRestock(selectedProducts) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun buildConfirmationMessage(selectedProducts: Map<Product, Double>): String {
        return buildString {
            append("The following quantities will be added:\n\n")
            selectedProducts.forEach { (product, quantity) ->
                append("• ${product.name}: +${formatQuantity(product, quantity)} ${product.unitDisplay}\n")
            }
            append("\nProceed?")
        }
    }

    private fun formatQuantity(product: Product, quantity: Double): String {
        return if (product.allowsDecimalQuantity) {
            String.format("%.1f", quantity)
        } else {
            quantity.toInt().toString()
        }
    }

    private fun performRestock(selectedProducts: Map<Product, Double>) {
        if (uiState.isProcessing) return
        uiState = uiState.copy(isProcessing = true)

        viewLifecycleOwner.lifecycleScope.launch {
            var successCount = 0
            var failureCount = 0

            selectedProducts.forEach { (product, quantity) ->
                val updatedProduct = product.copy(stock = product.stock + quantity)
                if (viewModel.updateProductAndWait(updatedProduct)) {
                    successCount++
                } else {
                    failureCount++
                }
            }

            viewModel.clearMessages()
            uiState = uiState.copy(isProcessing = false)

            if (successCount > 0) {
                Toast.makeText(
                    requireContext(),
                    "Added stock to $successCount product(s)",
                    Toast.LENGTH_SHORT,
                ).show()
            }
            if (failureCount > 0) {
                Toast.makeText(
                    requireContext(),
                    "Could not update $failureCount product(s)",
                    Toast.LENGTH_LONG,
                ).show()
            }

            delay(500)
            viewModel.refreshProducts()
            findNavController().navigateUp()
        }
    }
}
