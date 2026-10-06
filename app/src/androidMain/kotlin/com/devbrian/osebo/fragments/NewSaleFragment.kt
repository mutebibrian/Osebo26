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
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.models.CartItem
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.ui.screens.NewSaleCartItemUi
import com.devbrian.osebo.ui.screens.NewSaleCustomerUi
import com.devbrian.osebo.ui.screens.NewSaleProductUi
import com.devbrian.osebo.ui.screens.NewSaleScreen
import com.devbrian.osebo.ui.screens.NewSaleUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.SalesViewModel
import com.devbrian.osebo.utils.CurrencyFormatter
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewSaleFragment : Fragment() {
    private val viewModel: SalesViewModel by viewModel()
    private lateinit var preferenceManager: PreferenceManager
    private var uiState by mutableStateOf(NewSaleUiState())

    private val walkInCustomer = Customer(
        id = "797231da-d7c2-4f8c-bf13-1e3fc08b2e8e",
        name = "Walk-in Customer",
        phone = "",
        email = "",
        address = "",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                NewSaleScreen(
                    state = uiState,
                    onBackClick = { findNavController().navigateUp() },
                    onSearchChange = ::searchProducts,
                    onScanClick = { checkShopBeforeAction(::showBarcodeScanner) },
                    onCustomerSelected = ::selectCustomer,
                    onAddProduct = ::addProduct,
                    onQuantityChanged = ::changeQuantity,
                    onRemoveItem = ::removeItem,
                    onDiscountApplied = ::applyDiscount,
                    onClearCart = viewModel::clearCart,
                    onHoldSale = viewModel::holdSale,
                    onCheckout = ::checkShopAndCheckout,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiState = uiState.copy(
            shopName = preferenceManager.getCurrentShopName().ifBlank { "Selected shop" },
        )

        if (!preferenceManager.isShopProperlySelected()) {
            showShopSelectionErrorDialog()
            return
        }

        observeViewModel()
        if (viewModel.selectedCustomer.value == null) {
            viewModel.selectCustomer(walkInCustomer)
        }
        viewModel.loadProducts()
        viewModel.loadCustomers()
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner, ::updateProducts)
        viewModel.cartItems.observe(viewLifecycleOwner) { items ->
            updateCart(items)
            updateProducts(viewModel.products.value.orEmpty())
        }
        viewModel.customers.observe(viewLifecycleOwner) { customers ->
            val options = (listOf(walkInCustomer) + customers)
                .distinctBy(Customer::id)
                .map { it.toUi() }
            uiState = uiState.copy(customers = options)
        }
        viewModel.selectedCustomer.observe(viewLifecycleOwner) { customer ->
            val selected = customer ?: walkInCustomer
            uiState = uiState.copy(
                selectedCustomerId = selected.id,
                selectedCustomerName = selected.name,
            )
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            uiState = uiState.copy(isLoading = loading)
        }
        viewModel.isOffline.observe(viewLifecycleOwner) { offline ->
            uiState = uiState.copy(isOffline = offline)
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                showMessage(it)
                viewModel.clearMessages()
            }
        }
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                showMessage(it)
                viewModel.clearMessages()
            }
        }
    }

    private fun updateProducts(products: List<Product>) {
        val quantities = viewModel.cartItems.value.orEmpty().associate { it.product.id to it.quantity }
        uiState = uiState.copy(
            products = products.map { product ->
                NewSaleProductUi(
                    id = product.id,
                    name = product.name,
                    sku = product.sku.ifBlank { "No SKU" },
                    category = product.categoryDisplay.ifBlank { "Other" },
                    price = CurrencyFormatter.formatFull(product.price),
                    stock = if (product.isOutOfStock) {
                        "Out of stock"
                    } else {
                        "${product.displayStock} ${product.unitDisplay} available"
                    },
                    cartQuantity = quantities[product.id] ?: 0,
                    isOutOfStock = product.isOutOfStock,
                )
            },
        )
    }

    private fun updateCart(items: List<CartItem>) {
        val summary = viewModel.getCartSummary()
        uiState = uiState.copy(
            cartItems = items.map { item ->
                NewSaleCartItemUi(
                    productId = item.product.id,
                    name = item.product.name,
                    sku = item.product.sku.ifBlank { "No SKU" },
                    quantity = item.quantity,
                    unitPrice = CurrencyFormatter.formatFull(item.unitPrice),
                    subtotal = CurrencyFormatter.formatFull(item.subtotal),
                    discount = item.discount,
                )
            },
            subtotal = CurrencyFormatter.formatFull(summary.subtotal),
            discount = CurrencyFormatter.formatFull(summary.totalDiscount),
            tax = CurrencyFormatter.formatFull(summary.tax),
            total = CurrencyFormatter.formatFull(summary.total),
        )
    }

    private fun searchProducts(query: String) {
        when {
            query.isBlank() -> viewModel.loadProducts()
            query.trim().length >= 2 -> viewModel.searchProducts(query.trim())
        }
    }

    private fun addProduct(productId: String) {
        viewModel.products.value.orEmpty().firstOrNull { it.id == productId }?.let(viewModel::addToCart)
    }

    private fun changeQuantity(productId: String, quantity: Int) {
        viewModel.cartItems.value.orEmpty()
            .firstOrNull { it.product.id == productId }
            ?.let { viewModel.updateCartItemQuantity(it, quantity) }
    }

    private fun removeItem(productId: String) {
        viewModel.cartItems.value.orEmpty()
            .firstOrNull { it.product.id == productId }
            ?.let(viewModel::removeFromCart)
    }

    private fun applyDiscount(productId: String, discount: Double) {
        viewModel.cartItems.value.orEmpty()
            .firstOrNull { it.product.id == productId }
            ?.let { viewModel.applyDiscount(it, discount) }
    }

    private fun selectCustomer(customerId: String) {
        val customer = if (customerId == walkInCustomer.id) {
            walkInCustomer
        } else {
            viewModel.customers.value.orEmpty().firstOrNull { it.id == customerId }
        }
        customer?.let(viewModel::selectCustomer)
    }

    private fun showBarcodeScanner() {
        BarcodeScannerFragment.newInstance().also { scanner ->
            scanner.setOnBarcodeScannedListener(::searchProductByBarcode)
            scanner.show(parentFragmentManager, "barcode_scanner")
        }
    }

    private fun searchProductByBarcode(barcode: String) {
        viewModel.searchProductByBarcode(barcode) { product ->
            if (!isAdded) return@searchProductByBarcode
            if (product == null) {
                showMessage("No product found for this barcode")
            } else {
                viewModel.addToCart(product)
                showMessage("Added ${product.name}")
            }
        }
    }

    private fun checkShopBeforeAction(action: () -> Unit) {
        if (preferenceManager.getCurrentShopUuid().isBlank()) showNoShopSelectedDialog() else action()
    }

    private fun checkShopAndCheckout() {
        when {
            preferenceManager.getCurrentShopUuid().isBlank() -> showNoShopSelectedDialog()
            !preferenceManager.hasActiveSubscription() -> showNoActiveSubscriptionDialog()
            else -> proceedToPayment()
        }
    }

    private fun proceedToPayment() {
        val customer = viewModel.selectedCustomer.value ?: walkInCustomer
        val cartItems = viewModel.cartItems.value.orEmpty()
        val totalAmount = viewModel.getCartSummary().total.toFloat()

        when {
            cartItems.isEmpty() -> showMessage("Cart is empty")
            cartItems.any { it.product.id.startsWith("temp_") } -> {
                showMessage("Some products have not synced yet. Please wait or refresh products.", Toast.LENGTH_LONG)
            }
            totalAmount <= 0 -> showMessage("Invalid total amount")
            else -> runCatching {
                NewSaleFragmentDirections.actionNewSaleFragmentToPaymentFragment(
                    customerId = customer.id,
                    cartItems = cartItems.toTypedArray(),
                    totalAmount = totalAmount,
                )
            }.onSuccess(findNavController()::navigate)
                .onFailure { showMessage("Navigation error: ${it.message}") }
        }
    }

    private fun showShopSelectionErrorDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Shop selection error")
            .setMessage("There was a problem loading the shop. Select it again to continue.")
            .setPositiveButton("Select shop") { _, _ -> openShops() }
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show()
    }

    private fun showNoShopSelectedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("No shop selected")
            .setMessage("Select a shop before completing this sale.")
            .setPositiveButton("Select shop") { _, _ -> openShops() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNoActiveSubscriptionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Subscription required")
            .setMessage("This shop needs an active subscription to complete a sale.")
            .setPositiveButton("View subscriptions") { _, _ -> openShops() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openShops() {
        runCatching { findNavController().navigate(R.id.shopsFragment) }
            .onFailure { showMessage("Unable to open shops") }
    }

    private fun showMessage(message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (isAdded) Toast.makeText(requireContext(), message, duration).show()
    }

    private fun Customer.toUi() = NewSaleCustomerUi(
        id = id,
        name = name,
        detail = phone.ifBlank { if (isDefault || id == walkInCustomer.id) "Default customer" else "No phone number" },
    )
}
