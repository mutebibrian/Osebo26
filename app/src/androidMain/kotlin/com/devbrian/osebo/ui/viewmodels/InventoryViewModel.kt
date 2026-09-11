package com.devbrian.osebo.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.repository.InventoryRepository
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.utils.NetworkUtils
import com.devbrian.osebo.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class InventoryViewModel(
    private val repository: InventoryRepository,
    private val context: Context
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>(emptyList())
    val products: LiveData<List<Product>> = _products

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _isOffline = MutableLiveData(!NetworkUtils.isNetworkAvailable(context))
    val isOffline: LiveData<Boolean> = _isOffline

    private val _connectionType = MutableLiveData(NetworkUtils.getConnectionType(context))
    val connectionType: LiveData<String> = _connectionType

    private val _stats = MutableLiveData<InventoryRepository.InventoryStats>()
    val stats: LiveData<InventoryRepository.InventoryStats> = _stats

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _syncPending = MutableLiveData(false)
    val syncPending: LiveData<Boolean> = _syncPending

    init {
        observeProducts()
        loadStats()
        monitorNetworkChanges()
    }

    private fun observeProducts() {
        viewModelScope.launch {
            repository.getProducts().collectLatest { productList ->
                _products.value = productList
            }
        }
    }

    private fun monitorNetworkChanges() {
        viewModelScope.launch {
            while (true) {
                val isAvailable = NetworkUtils.isNetworkAvailable(context)
                _isOffline.value = !isAvailable
                _connectionType.value = NetworkUtils.getConnectionType(context)

                if (isAvailable && _syncPending.value == true) {
                    triggerSync()
                }

                delay(5000)
            }
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            val stats = repository.getInventoryStats()
            _stats.value = stats
        }
    }

    fun refreshProducts() {
        viewModelScope.launch {
            _isRefreshing.value = true

            if (!NetworkUtils.isNetworkAvailable(context)) {
                _isOffline.value = true
                _errorMessage.value = "You're offline. Showing cached data."
                _isRefreshing.value = false
                return@launch
            }

            _isOffline.value = false
            val result = repository.refreshProducts()

            if (result is Resource.Error) {
                _errorMessage.value = result.message
            }
            // REMOVED the success message for refresh
            // DON'T set _successMessage here - it causes unwanted navigation

            loadStats()
            _isRefreshing.value = false
        }
    }

    fun createProduct(product: Product) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = repository.createProduct(product)

            if (result is Resource.Success) {
                val message = if (!NetworkUtils.isNetworkAvailable(context)) {
                    "✅ Product saved locally. Will sync when online."
                } else {
                    "✅ Product created successfully"
                }
                _successMessage.value = message
                _syncPending.value = true
            } else if (result is Resource.Error) {
                _errorMessage.value = result.message
            }

            loadStats()
            _isLoading.value = false
        }
    }

    // Add this method to InventoryViewModel
    suspend fun updateProductAndWait(product: Product): Boolean {
        return suspendCancellableCoroutine { continuation ->
            viewModelScope.launch {
                _isLoading.value = true

                val result = repository.updateProduct(product)

                if (result is Resource.Success) {
                    val message = if (!NetworkUtils.isNetworkAvailable(context)) {
                        "✅ Changes saved locally. Will sync when online."
                    } else {
                        "✅ Product updated successfully"
                    }
                    _successMessage.value = message
                    _syncPending.value = true

                    // Update local list
                    val currentProducts = _products.value?.toMutableList() ?: mutableListOf()
                    val index = currentProducts.indexOfFirst { it.id == product.id }
                    if (index != -1) {
                        currentProducts[index] = product
                        _products.value = currentProducts
                    }

                    continuation.resume(true)
                } else if (result is Resource.Error) {
                    _errorMessage.value = result.message
                    continuation.resume(false)
                } else {
                    continuation.resume(false)
                }

                loadStats()
                _isLoading.value = false
            }
        }
    }



    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = repository.updateProduct(product)

            if (result is Resource.Success) {
                val message = if (!NetworkUtils.isNetworkAvailable(context)) {
                    "✅ Changes saved locally. Will sync when online."
                } else {
                    "✅ Product updated successfully"
                }
                _successMessage.value = message
                _syncPending.value = true
            } else if (result is Resource.Error) {
                _errorMessage.value = result.message
            }

            loadStats()
            _isLoading.value = false
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = repository.deleteProduct(productId)

            if (result is Resource.Success) {
                val message = if (!NetworkUtils.isNetworkAvailable(context)) {
                    "✅ Product marked for deletion. Will sync when online."
                } else {
                    "✅ Product deleted successfully"
                }
                _successMessage.value = message
                _syncPending.value = true
            } else if (result is Resource.Error) {
                _errorMessage.value = result.message
            }

            loadStats()
            _isLoading.value = false
        }
    }

    private fun triggerSync() {
        viewModelScope.launch {
            
            _syncPending.value = false
            _successMessage.value = "Syncing changes with server..."

            val result = repository.refreshProducts()
            if (result is Resource.Success) {
                _successMessage.value = "Sync complete!"
            } else if (result is Resource.Error) {
                _errorMessage.value = "Sync failed: ${result.message}"
            }
        }
    }

    fun getProductById(productId: String): Product? {
        return _products.value?.find { it.id == productId }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun getNetworkStatusMessage(): String {
        return when {
            !NetworkUtils.isNetworkAvailable(context) ->
                "📴 You're offline. All changes will be saved locally and synced when connection is restored."
            NetworkUtils.isMeteredConnection(context) ->
                "📱 You're on mobile data. Large syncs may use data."
            else ->
                "🌐 You're online. All changes are syncing in real-time."
        }
    }

    
    fun getConnectionType(): String {
        return NetworkUtils.getConnectionType(context)
    }

    
    fun canPerformLargeSync(): Boolean {
        return NetworkUtils.isNetworkAvailable(context) &&
                !NetworkUtils.isMeteredConnection(context)
    }
}


