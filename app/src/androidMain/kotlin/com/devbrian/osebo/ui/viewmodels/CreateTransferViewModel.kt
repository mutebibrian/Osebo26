package com.devbrian.osebo.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.dto.request.TransferItemRequest
import com.devbrian.osebo.data.repository.InventoryRepository
import com.devbrian.osebo.data.repository.ShopRepository
import com.devbrian.osebo.data.repository.TransferRepository
import com.devbrian.osebo.ui.screens.CreateTransferUiState
import com.devbrian.osebo.ui.screens.TransferItemUi
import com.devbrian.osebo.ui.screens.TransferMethod
import com.devbrian.osebo.ui.screens.TransferShopOption
import com.devbrian.osebo.utils.Resource
import kotlinx.coroutines.launch

class CreateTransferViewModel(
    private val transferRepository: TransferRepository,
    private val shopRepository: ShopRepository,
    private val inventoryRepository: InventoryRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val sourceShopId: String get() = preferenceManager.getCurrentShopId()

    private val _uiState = mutableStateOf(
        CreateTransferUiState(sourceShopName = preferenceManager.getCurrentShopName())
    )
    val uiState: State<CreateTransferUiState> = _uiState

    private val _transferComplete = MutableLiveData(false)
    val transferComplete: LiveData<Boolean> = _transferComplete

    private var allItems: List<TransferItemUi> = emptyList()
    private val selections = mutableMapOf<String, String>()

    init {
        loadTargetShops()
        loadSourceShopItems()
    }

    private fun loadTargetShops() {
        viewModelScope.launch {
            when (val result = shopRepository.getShops()) {
                is Resource.Success -> {
                    val options = result.data
                        .filter { it.id != sourceShopId }
                        .map { TransferShopOption(it.id, it.name) }
                    update { it.copy(targetShops = options) }
                }
                is Resource.Error -> update { it.copy(errorMessage = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    private fun loadSourceShopItems() {
        update { it.copy(isLoadingItems = true) }
        viewModelScope.launch {
            inventoryRepository.refreshProducts()
            inventoryRepository.getProductsByShop(sourceShopId).collect { products ->
                allItems = products.filter { it.id.isNotBlank() }.map { product ->
                    TransferItemUi(
                        id = product.id,
                        name = product.name,
                        sku = product.sku,
                        availableQty = product.stock,
                        unit = product.unitDisplay,
                    )
                }
                applyFilter(_uiState.value.searchQuery)
                update { it.copy(isLoadingItems = false) }
            }
        }
    }

    fun onTargetShopSelected(shopId: String) {
        update { it.copy(selectedTargetShopId = shopId, errorMessage = null) }
    }

    fun onMethodChange(method: TransferMethod) {
        update { it.copy(method = method, errorMessage = null) }
    }

    fun onSearchQueryChange(query: String) {
        update { it.copy(searchQuery = query) }
        applyFilter(query)
    }

    private fun applyFilter(query: String) {
        val filtered = allItems
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.sku.contains(query, ignoreCase = true) }
            .map { item ->
                val qtyText = selections[item.id]
                item.copy(isSelected = qtyText != null, transferQtyText = qtyText ?: "")
            }
        update { it.copy(items = filtered) }
    }

    fun onItemToggle(itemId: String) {
        if (selections.containsKey(itemId)) {
            selections.remove(itemId)
        } else {
            selections[itemId] = ""
        }
        applyFilter(_uiState.value.searchQuery)
    }

    fun onItemQtyChange(itemId: String, qtyText: String) {
        if (selections.containsKey(itemId)) {
            selections[itemId] = qtyText
        }
        applyFilter(_uiState.value.searchQuery)
    }

    fun onDownloadTemplateClick() {
        update { it.copy(errorMessage = "Template download isn't wired up yet") }
    }

    fun onChooseFileClick() {
        update { it.copy(errorMessage = "File upload isn't wired up yet — use Manual Selection for now") }
    }

    fun onSubmit() {
        val state = _uiState.value
        val targetShopId = state.selectedTargetShopId
        if (targetShopId.isNullOrBlank()) {
            update { it.copy(errorMessage = "Choose a target shop") }
            return
        }

        if (state.method == TransferMethod.UploadFile) {
            update { it.copy(errorMessage = "File upload isn't wired up yet — use Manual Selection for now") }
            return
        }

        val selectedIds = selections.keys
        val selected = allItems.filter { it.id in selectedIds }
        if (selected.isEmpty()) {
            update { it.copy(errorMessage = "Select at least one item") }
            return
        }

        val items = mutableListOf<TransferItemRequest>()
        for (item in selected) {
            val qty = selections[item.id]?.toDoubleOrNull()
            if (qty == null || qty <= 0) {
                update { it.copy(errorMessage = "Enter a valid quantity for ${item.name}") }
                return
            }
            if (qty > item.availableQty) {
                update { it.copy(errorMessage = "Only ${item.availableQty} ${item.unit} of ${item.name} available") }
                return
            }
            items.add(TransferItemRequest(item.id, qty))
        }

        update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val result = transferRepository.createMultiTransfer(sourceShopId, targetShopId, items)
            update { it.copy(isSubmitting = false) }
            when (result) {
                is Resource.Success -> {
                    selections.clear()
                    applyFilter(_uiState.value.searchQuery)
                    _transferComplete.value = true
                }
                is Resource.Error -> update { it.copy(errorMessage = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    private fun update(transform: (CreateTransferUiState) -> CreateTransferUiState) {
        _uiState.value = transform(_uiState.value)
    }
}
