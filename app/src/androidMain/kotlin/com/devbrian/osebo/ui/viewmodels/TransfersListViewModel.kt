package com.devbrian.osebo.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.api.TransferDto
import com.devbrian.osebo.data.repository.InventoryRepository
import com.devbrian.osebo.data.repository.ShopRepository
import com.devbrian.osebo.data.repository.TransferRepository
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.ui.screens.TransferListItemUi
import com.devbrian.osebo.ui.screens.TransfersListUiState
import com.devbrian.osebo.utils.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TransfersListViewModel(
    private val transferRepository: TransferRepository,
    private val shopRepository: ShopRepository,
    private val inventoryRepository: InventoryRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val shopId: String get() = preferenceManager.getCurrentShopId()

    private val _uiState = mutableStateOf(
        TransfersListUiState(shopName = preferenceManager.getCurrentShopName())
    )
    val uiState: State<TransfersListUiState> = _uiState

    private var allTransfers: List<TransferListItemUi> = emptyList()

    init {
        loadTransfers()
    }

    fun loadTransfers() {
        update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val shopNames = when (val shopsResult = shopRepository.getShops()) {
                is Resource.Success -> shopsResult.data.associate { it.id to it.name }
                else -> emptyMap()
            }

            inventoryRepository.refreshProducts()
            val products: List<Product> = inventoryRepository.getProductsByShop(shopId).first()

            val results = fetchTransfersForItems(products, shopNames)
            allTransfers = results.distinctBy { it.id }.sortedByDescending { it.createdAt }
            applyFilter(_uiState.value.searchQuery)
            update { it.copy(isLoading = false) }
        }
    }

    private suspend fun fetchTransfersForItems(
        products: List<Product>,
        shopNames: Map<String, String>
    ): List<TransferListItemUi> = coroutineScope {
        products.filter { it.id.isNotBlank() }.map { product ->
            async {
                when (val result = transferRepository.getTransfersForItem(shopId, product.id)) {
                    is Resource.Success -> result.data.map { it.toUiModel(product, shopNames) }
                    else -> emptyList()
                }
            }
        }.awaitAll().flatten()
    }

    private fun TransferDto.toUiModel(product: Product, shopNames: Map<String, String>): TransferListItemUi {
        val fromName = shopNames[sourceShop] ?: sourceShopName ?: sourceShop ?: "—"
        val toName = shopNames[targetShop] ?: targetShopName ?: targetShop ?: "—"
        val qty = quantity ?: items?.sumOf { it.quantity ?: 0.0 }
        return TransferListItemUi(
            id = id,
            itemName = stockItemName ?: product.name,
            fromShopName = fromName,
            toShopName = toName,
            quantityLabel = qty?.let { "$it ${product.unitDisplay}" } ?: "—",
            status = status?.replaceFirstChar { it.uppercase() } ?: "Pending",
            createdAt = createdAt,
        )
    }

    fun onSearchQueryChange(query: String) {
        update { it.copy(searchQuery = query) }
        applyFilter(query)
    }

    private fun applyFilter(query: String) {
        val filtered = allTransfers.filter {
            query.isBlank() ||
                it.itemName.contains(query, ignoreCase = true) ||
                it.fromShopName.contains(query, ignoreCase = true) ||
                it.toShopName.contains(query, ignoreCase = true)
        }
        update { it.copy(transfers = filtered) }
    }

    private fun update(transform: (TransfersListUiState) -> TransfersListUiState) {
        _uiState.value = transform(_uiState.value)
    }
}
