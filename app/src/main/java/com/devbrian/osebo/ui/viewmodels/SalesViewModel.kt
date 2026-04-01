package com.devbrian.osebo.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.repository.CustomerRepository
import com.devbrian.osebo.data.repository.ProductRepository
import com.devbrian.osebo.data.repository.SalesRepository
import com.devbrian.osebo.models.CartItem
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.models.Sale
import com.devbrian.osebo.models.SaleData
import com.devbrian.osebo.utils.CurrencyFormatter
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val apiService: ApiService,
    private val preferences: PreferenceManager,
    private val database: AppDatabase
) : ViewModel() {

    // ==================== LIVEDATA ====================

    private val _recentSales = MutableLiveData<List<Sale>>(emptyList())
    val recentSales: LiveData<List<Sale>> = _recentSales

    private val _customers = MutableLiveData<List<Customer>>(emptyList())
    val customers: LiveData<List<Customer>> = _customers

    private val _todaySalesTotal = MutableLiveData(0.0)
    val todaySalesTotal: LiveData<Double> = _todaySalesTotal

    private val _monthSalesTotal = MutableLiveData(0.0)
    val monthSalesTotal: LiveData<Double> = _monthSalesTotal

    private val _monthSalesCount = MutableLiveData(0)
    val monthSalesCount: LiveData<Int> = _monthSalesCount

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _saleResult = MutableLiveData<Resource<SaleData>>()
    val saleResult: LiveData<Resource<SaleData>> = _saleResult

    private val _products = MutableLiveData<List<Product>>(emptyList())
    val products: LiveData<List<Product>> = _products

    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _isOffline = MutableLiveData(false)
    val isOffline: LiveData<Boolean> = _isOffline

    private val _selectedCustomer = MutableLiveData<Customer?>()
    val selectedCustomer: LiveData<Customer?> = _selectedCustomer

    // ==================== INIT ====================

    init {
        // FIX: loadProducts() intentionally removed from here.
        //
        // Previously calling loadProducts() in init caused it to fire BEFORE
        // the fragment's RecyclerView adapter was attached. This meant:
        //   1. Observer fired with 0 products → RecyclerView set to GONE
        //   2. API returned 6 products → submitList called, but RecyclerView
        //      had already been hidden and "No adapter attached" error occurred
        //
        // Now loadProducts() is only called from NewSaleFragment.onViewCreated()
        // AFTER setupAdapters() and setupRecyclerViews() have run.
        // This guarantees the adapter is attached before any data arrives.
        loadRecentSales()
    }

    // ==================== PRODUCT FUNCTIONS ====================

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            println("📱 SalesViewModel - Loading products...")

            val shopUuid = preferences.getCurrentShopUuid()
            if (shopUuid.isEmpty()) {
                println("❌ SalesViewModel - No shop UUID! Cannot load products.")
                _errorMessage.value = "Please select a shop first"
                _products.value = emptyList()
                _isLoading.value = false
                return@launch
            }

            try {
                val result = productRepository.getProducts()

                when (result) {
                    is Resource.Success -> {
                        val products = result.data ?: emptyList()
                        _products.value = products
                        _isOffline.value = false
                        println("📱 SalesViewModel - Products loaded: ${products.size}")

                        if (products.isEmpty()) {
                            _errorMessage.value = "No products found. Please add products first."
                        } else {
                            products.take(3).forEachIndexed { index, product ->
                                println("📱 Product[$index]: ID=${product.id}, Name=${product.name}, Price=${product.price}")
                            }
                        }
                    }
                    is Resource.Error -> {
                        _errorMessage.value = result.message
                        _isOffline.value = true
                        println("📱 SalesViewModel - Error loading products: ${result.message}")
                        loadProductsFromCache()
                    }
                    is Resource.Loading -> {
                        println("📱 SalesViewModel - Loading products...")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load products: ${e.message}"
                _isOffline.value = true
                println("📱 SalesViewModel - Exception: ${e.message}")
                e.printStackTrace()
                loadProductsFromCache()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadProductsFromCache() {
        viewModelScope.launch {
            try {
                productRepository.observeProducts().collect { cachedProducts ->
                    if (cachedProducts.isNotEmpty()) {
                        _products.value = cachedProducts
                        println("📱 SalesViewModel - Loaded ${cachedProducts.size} products from cache")
                        _isOffline.value = true
                    } else {
                        println("📱 SalesViewModel - Cache is empty")
                    }
                }
            } catch (e: Exception) {
                println("📱 SalesViewModel - Error loading from cache: ${e.message}")
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            println("📱 SalesViewModel - Searching products: '$query'")

            try {
                val result = productRepository.searchProducts(query)

                when (result) {
                    is Resource.Success -> {
                        val products = result.data ?: emptyList()
                        _products.value = products
                        println("📱 SalesViewModel - Search results: ${products.size}")

                        if (products.isNotEmpty()) {
                            products.take(3).forEachIndexed { index, product ->
                                println("   Result[$index]: ${product.name}")
                            }
                        }
                    }
                    is Resource.Error -> {
                        _errorMessage.value = result.message
                        println("📱 SalesViewModel - Search error: ${result.message}")
                    }
                    is Resource.Loading -> {
                        println("📱 SalesViewModel - Searching...")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Search failed: ${e.message}"
                println("📱 SalesViewModel - Search exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchProductByBarcode(barcode: String, callback: (Product?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            println("📱 SalesViewModel - Searching for barcode: $barcode")

            try {
                val localProductEntity = withContext(Dispatchers.IO) {
                    try {
                        database.productDao().getProductByBarcode(barcode)
                    } catch (e: Exception) {
                        println("📱 SalesViewModel - Error searching local DB: ${e.message}")
                        null
                    }
                }

                if (localProductEntity != null) {
                    val localProduct = localProductEntity.toProduct()
                    println("📱 SalesViewModel - Product found in local database: ${localProduct.name}")
                    callback(localProduct)
                    _isLoading.value = false
                    return@launch
                }

                val token = preferences.getAuthToken()
                val shopUuid = preferences.getCurrentShopUuid()

                if (token.isEmpty() || shopUuid.isEmpty()) {
                    println("📱 SalesViewModel - Missing token or shop UUID")
                    callback(null)
                    _isLoading.value = false
                    return@launch
                }

                val response = apiService.searchProductByBarcode(
                    "Bearer $token",
                    shopUuid,
                    barcode
                )

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val product = apiResponse.data
                        if (product != null) {
                            println("📱 SalesViewModel - Product found on server: ${product.name}")
                            callback(product)
                        } else {
                            println("📱 SalesViewModel - Product not found on server")
                            callback(null)
                        }
                    } else {
                        println("📱 SalesViewModel - API returned error: ${apiResponse?.message}")
                        callback(null)
                    }
                } else {
                    println("📱 SalesViewModel - API request failed: ${response.code()} - ${response.message()}")
                    callback(null)
                }
            } catch (e: Exception) {
                println("📱 SalesViewModel - Error searching barcode: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Error searching barcode: ${e.message}"
                callback(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCustomers() {
        viewModelScope.launch {
            customerRepository.getAllCustomers().collect { customers ->
                _customers.value = customers
            }
        }
    }

    // ==================== CART FUNCTIONS ====================

    fun addToCart(product: Product) {
        println("📱 VIEWMODEL - addToCart called with: ${product.name}")
        val currentCart = _cartItems.value?.toMutableList() ?: mutableListOf()
        println("📱 VIEWMODEL - Current cart size: ${currentCart.size}")

        val existingItemIndex = currentCart.indexOfFirst { it.product.id == product.id }

        if (existingItemIndex != -1) {
            val existingItem = currentCart[existingItemIndex]
            val updatedItem = CartItem(
                product = existingItem.product,
                quantity = existingItem.quantity + 1,
                discount = existingItem.discount,
                isCustomPrice = existingItem.isCustomPrice,
                customPrice = existingItem.customPrice
            )
            currentCart[existingItemIndex] = updatedItem
            println("📱 VIEWMODEL - Incremented ${product.name} to ${updatedItem.quantity}")
        } else {
            val cartItem = CartItem(
                product = product,
                quantity = 1,
                discount = 0.0,
                isCustomPrice = false,
                customPrice = null
            )
            currentCart.add(cartItem)
            println("📱 VIEWMODEL - Added new item: ${product.name}")
        }

        _cartItems.value = currentCart
        println("📱 VIEWMODEL - Cart updated, new size: ${_cartItems.value?.size}")
    }

    fun updateCartItemQuantity(item: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(item)
            return
        }

        val currentCart = _cartItems.value?.toMutableList() ?: return
        val index = currentCart.indexOfFirst { it.product.id == item.product.id }

        if (index != -1) {
            val updatedItem = CartItem(
                product = item.product,
                quantity = newQuantity,
                discount = item.discount,
                isCustomPrice = item.isCustomPrice,
                customPrice = item.customPrice
            )
            currentCart[index] = updatedItem
            _cartItems.value = currentCart
        }
    }

    fun removeFromCart(item: CartItem) {
        val currentCart = _cartItems.value?.toMutableList() ?: return
        currentCart.removeAll { it.product.id == item.product.id }
        _cartItems.value = currentCart
        _successMessage.value = "${item.product.name} removed from cart"
    }

    fun applyDiscount(item: CartItem, discountPercentage: Double) {
        val currentCart = _cartItems.value?.toMutableList() ?: return
        val index = currentCart.indexOfFirst { it.product.id == item.product.id }

        if (index != -1) {
            val updatedItem = CartItem(
                product = item.product,
                quantity = item.quantity,
                discount = discountPercentage,
                isCustomPrice = item.isCustomPrice,
                customPrice = item.customPrice
            )
            currentCart[index] = updatedItem
            _cartItems.value = currentCart
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _successMessage.value = "Cart cleared"
    }

    fun holdSale() {
        _successMessage.value = "Sale held successfully"
    }

    // ==================== CART SUMMARY ====================

    fun getCartSummary(): CartSummary {
        val items = _cartItems.value ?: emptyList()

        val subtotal = items.sumOf { it.unitPrice * it.quantity }
        val totalDiscount = items.sumOf { it.totalDiscount }
        val tax = calculateTotalTax(items)
        val total = items.sumOf { it.subtotal } + tax

        return CartSummary(
            subtotal = subtotal,
            totalDiscount = totalDiscount,
            tax = tax,
            total = total,
            itemCount = items.size
        )
    }

    private fun calculateTotalTax(items: List<CartItem>): Double {
        return items.sumOf { item ->
            val price = item.customPrice ?: item.product.price
            val taxRate = item.product.taxRate ?: 0.0
            price * item.quantity * taxRate / 100
        }
    }

    // ==================== CUSTOMER FUNCTIONS ====================

    fun selectCustomer(customer: Customer) {
        _selectedCustomer.value = customer
        _successMessage.value = "Customer selected: ${customer.name}"
        println("📱 VIEWMODEL - Customer selected: ${customer.name} (${customer.id})")
    }

    fun clearSelectedCustomer() {
        _selectedCustomer.value = null
        println("📱 VIEWMODEL - Customer cleared")
    }

    // ==================== SALE FUNCTIONS ====================

    fun loadRecentSales() {
        viewModelScope.launch {
            _isLoading.value = true
            println("📊 SalesViewModel - Loading recent sales...")

            try {
                salesRepository.getRecentSales().collect { sales ->
                    println("📊 SalesViewModel - Received ${sales.size} sales")

                    sales.forEachIndexed { index, sale ->
                        println("📊 Sale[$index]: ID=${sale.id}, Amount=${sale.amount}, Date=${sale.date}, Status=${sale.status}")
                    }

                    _recentSales.value = sales

                    val todayTotal = calculateTodayTotal(sales)
                    _todaySalesTotal.value = todayTotal
                    println("📊 Today's sales total calculated: $todayTotal")

                    val monthStats = calculateMonthStats(sales)
                    _monthSalesTotal.value = monthStats.first
                    _monthSalesCount.value = monthStats.second
                    println("📊 Month's sales total: ${monthStats.first}, Count: ${monthStats.second}")
                }
            } catch (e: Exception) {
                println("❌ Error loading sales: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = e.message ?: "Failed to load sales"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun calculateTodayTotal(sales: List<Sale>): Double {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        return sales.filter { sale ->
            try {
                if (sale.amount <= 0) return@filter false

                val saleDate = parseSaleDate(sale.date)
                if (saleDate == null) {
                    println("⚠️ Could not parse date for sale: ${sale.id}, date: ${sale.date}")
                    return@filter false
                }

                val isToday = saleDate.get(Calendar.DAY_OF_YEAR) == today &&
                        saleDate.get(Calendar.YEAR) == currentYear

                if (isToday) {
                    println("✅ Sale ${sale.id} is from today, amount: ${sale.amount}")
                }

                isToday
            } catch (e: Exception) {
                println("❌ Error checking if sale is from today: ${e.message}")
                false
            }
        }.sumOf { it.amount }
    }

    private fun calculateMonthStats(sales: List<Sale>): Pair<Double, Int> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val monthSales = sales.filter { sale ->
            try {
                if (sale.amount <= 0) return@filter false

                val saleDate = parseSaleDate(sale.date)
                if (saleDate == null) {
                    println("⚠️ Could not parse date for sale: ${sale.id}, date: ${sale.date}")
                    return@filter false
                }

                val isThisMonth = saleDate.get(Calendar.MONTH) == currentMonth &&
                        saleDate.get(Calendar.YEAR) == currentYear

                if (isThisMonth) {
                    println("✅ Sale ${sale.id} is from this month, amount: ${sale.amount}")
                }

                isThisMonth
            } catch (e: Exception) {
                println("❌ Error checking if sale is from this month: ${e.message}")
                false
            }
        }

        val total = monthSales.sumOf { it.amount }
        val count = monthSales.size

        return Pair(total, count)
    }

    private fun parseSaleDate(dateString: String): Calendar? {
        return try {
            val calendar = Calendar.getInstance()

            val timestamp = dateString.toLongOrNull()
            if (timestamp != null) {
                calendar.timeInMillis = timestamp
                return calendar
            }

            val timestampSeconds = dateString.toLongOrNull()
            if (timestampSeconds != null && timestampSeconds < 10000000000L) {
                calendar.timeInMillis = timestampSeconds * 1000
                return calendar
            }

            val dateFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            )

            for (format in dateFormats) {
                try {
                    val date = format.parse(dateString)
                    calendar.time = date
                    return calendar
                } catch (e: Exception) {
                    // Try next format
                }
            }

            val patterns = listOf(
                Regex("""(\d{4})-(\d{2})-(\d{2})"""),
                Regex("""(\d{2})/(\d{2})/(\d{4})"""),
                Regex("""(\d{2})-(\d{2})-(\d{4})""")
            )

            for (pattern in patterns) {
                val matchResult = pattern.find(dateString)
                if (matchResult != null) {
                    val (first, second, third) = matchResult.destructured

                    val year = when {
                        first.length == 4 -> first.toInt()
                        third.length == 4 -> third.toInt()
                        else -> continue
                    }

                    val month = when {
                        first.length == 4 -> second.toInt()
                        third.length == 4 -> first.toInt()
                        else -> continue
                    }

                    val day = when {
                        first.length == 4 -> third.toInt()
                        third.length == 4 -> second.toInt()
                        else -> continue
                    }

                    if (month in 1..12 && day in 1..31) {
                        calendar.set(year, month - 1, day)
                        return calendar
                    }
                }
            }

            println("⚠️ Could not parse date with any method: $dateString")
            null
        } catch (e: Exception) {
            println("❌ Error parsing date: $dateString, ${e.message}")
            null
        }
    }

    fun setCartItems(items: List<CartItem>) {
        _cartItems.value = items
    }

    fun completeSale(
        customerId: String?,
        paidAmount: Double,
        paymentMethod: String,
        reference: String?,
        phoneNumber: String?,
        totalAmount: Float,
        saleType: String = "sale"
    ) {
        viewModelScope.launch {
            _saleResult.value = Resource.Loading
            _isLoading.value = true

            try {
                val customer = if (customerId != null) {
                    Customer(id = customerId, name = "", phone = "", email = "", address = "")
                } else {
                    null
                }

                val notes = buildString {
                    reference?.let { append("Ref: $it") }
                    if (phoneNumber != null) {
                        if (isNotEmpty()) append(", ")
                        append("Phone: $phoneNumber")
                    }
                    val change = paidAmount - totalAmount
                    if (change > 0) {
                        if (isNotEmpty()) append(", ")
                        append("Change: ${CurrencyFormatter.formatFull(change)}")
                    }
                }.takeIf { it.isNotEmpty() }

                val result = salesRepository.createSale(
                    customer = customer,
                    cartItems = _cartItems.value ?: emptyList(),
                    paidAmount = paidAmount,
                    saleType = saleType,
                    paymentMethod = paymentMethod,
                    notes = notes
                )

                when (result) {
                    is Resource.Success -> {
                        result.data?.let { saleData ->
                            _saleResult.value = Resource.Success(saleData)
                            clearCart()
                            refreshSales()
                            _successMessage.value = "Sale completed successfully!"
                        } ?: run {
                            _saleResult.value = Resource.Error("Failed to complete sale: No data returned")
                        }
                    }
                    is Resource.Error -> {
                        _saleResult.value = Resource.Error(result.message)
                        _errorMessage.value = result.message
                    }
                    is Resource.Loading -> { }
                }
            } catch (e: Exception) {
                _saleResult.value = Resource.Error(e.message ?: "An error occurred while processing payment")
                _errorMessage.value = e.message ?: "Payment processing failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun calculateChange(tendered: Double, totalAmount: Float): Double {
        return salesRepository.calculateChange(tendered, totalAmount.toDouble())
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun resetSaleResult() {
        _saleResult.value = Resource.Loading
    }

    fun refreshSales() {
        viewModelScope.launch {
            loadRecentSales()
        }
    }

    fun refreshSalesData() {
        println("🔄 Refreshing sales data...")
        loadRecentSales()
    }

    fun getSalesInDateRange(startDate: Long, endDate: Long): List<Sale> {
        return _recentSales.value?.filter { sale ->
            try {
                val saleDate = parseSaleDate(sale.date)
                if (saleDate != null) {
                    val saleTime = saleDate.timeInMillis
                    saleTime in startDate..endDate && sale.amount > 0
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        } ?: emptyList()
    }

    fun getTotalInDateRange(startDate: Long, endDate: Long): Double {
        return getSalesInDateRange(startDate, endDate).sumOf { it.amount }
    }
}

data class CartSummary(
    val subtotal: Double,
    val totalDiscount: Double,
    val tax: Double,
    val total: Double,
    val itemCount: Int
)