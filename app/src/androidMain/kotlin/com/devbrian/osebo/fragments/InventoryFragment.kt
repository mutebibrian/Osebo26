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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.ui.screens.InventoryProductUi
import com.devbrian.osebo.ui.screens.InventoryScreen
import com.devbrian.osebo.ui.screens.InventoryUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.InventoryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class InventoryFragment : Fragment(), ShopSelectionAware {
    private val viewModel: InventoryViewModel by viewModel()
    private var uiState by mutableStateOf(InventoryUiState())

    override fun onShopSelectionChanged() {
        uiState = uiState.copy(products = emptyList(), isLoading = true)
        viewModel.onShopSelectionChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                OseboTheme {
                    InventoryScreen(
                        state = uiState,
                        onQueryChange = { query -> uiState = uiState.copy(query = query) },
                        onAddProductClick = ::navigateToAddProduct,
                        onStockTransfersClick = {
                            showUpcomingMessage("Stock transfers")
                        },
                        onAddStockClick = ::navigateToRestock,
                        onRemoveStockClick = {
                            showUpcomingMessage("Remove stock")
                        },
                        onProductClick = ::navigateToProductDetails,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        viewModel.refreshProducts()
        viewModel.loadStats()
    }

    private fun setupObservers() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            uiState = uiState.copy(
                products = products.map { product ->
                    InventoryProductUi(
                        id = product.id,
                        name = product.name,
                        sku = product.sku,
                        price = product.displayPrice,
                        stock = "${product.displayStock} ${product.unitDisplay}",
                        isLowStock = product.isLowStock,
                    )
                },
            )
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            uiState = uiState.copy(isLoading = isRefreshing)
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
    }

    private fun navigateToProductDetails(productId: String) {
        val action = InventoryFragmentDirections.actionInventoryToProductDetails(productId)
        findNavController().navigate(action)
    }

    private fun navigateToRestock() {
        val action = InventoryFragmentDirections.actionInventoryToRestock()
        findNavController().navigate(action)
    }

    private fun navigateToAddProduct() {
        val action = InventoryFragmentDirections.actionInventoryToAddProduct()
        findNavController().navigate(action)
    }

    private fun showUpcomingMessage(feature: String) {
        Toast.makeText(requireContext(), "$feature coming soon", Toast.LENGTH_SHORT).show()
    }
}
