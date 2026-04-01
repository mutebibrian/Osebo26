package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.CartAdapter
import com.devbrian.osebo.adapters.ProductAdapter
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.databinding.FragmentNewSaleBinding
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.ui.viewmodels.SalesViewModel
import com.devbrian.osebo.utils.CurrencyFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewSaleFragment : Fragment() {

    private var _binding: FragmentNewSaleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SalesViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartAdapter: CartAdapter
    private lateinit var preferenceManager: PreferenceManager

    private val walkInCustomer = Customer(
        id = "797231da-d7c2-4f8c-bf13-1e3fc08b2e8e",
        name = "Walk-in Customer",
        phone = "",
        email = "",
        address = ""
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewSaleBinding.inflate(inflater, container, false)

        // Create adapters
        productAdapter = ProductAdapter(
            onItemClick = { product ->
                println("📱 Product clicked: ${product.name}")
                viewModel.addToCart(product)
            }
        )

        cartAdapter = CartAdapter(
            onQuantityChanged = { item, newQuantity ->
                viewModel.updateCartItemQuantity(item, newQuantity)
            },
            onRemoveItem = { item ->
                viewModel.removeFromCart(item)
            },
            onDiscountApplied = { item, discountPercentage ->
                showDiscountDialog(item, discountPercentage)
            }
        )

        // Set adapters on RecyclerViews
        binding.productsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }

        binding.cartRecyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager.getInstance(requireContext())

        // Validate shop
        if (!preferenceManager.isShopProperlySelected()) {
            showShopSelectionErrorDialog()
            return
        }

        // Setup UI components
        setupToolbar()
        setupClickListeners()
        setupObservers()
        setupSearchListener()

        // Load data
        viewModel.loadProducts()
        viewModel.loadCustomers()
        viewModel.selectCustomer(walkInCustomer)

        debugShopInfo()

        // Force a layout update after a delay to ensure products appear
        binding.productsRecyclerView?.postDelayed({
            forceProductDisplay()
        }, 500)
    }

    private fun forceProductDisplay() {
        val products = viewModel.products.value
        if (!products.isNullOrEmpty()) {
            println("✅ Force displaying ${products.size} products")
            binding.productsRecyclerView?.visibility = View.VISIBLE
            binding.noProductsTextView?.visibility = View.GONE
            productAdapter.submitList(products)
            productAdapter.notifyDataSetChanged()
            binding.productsRecyclerView?.invalidate()
            binding.productsRecyclerView?.requestLayout()
            binding.productsRecyclerView?.smoothScrollToPosition(0)
        } else {
            println("⚠️ No products available for force display")
            // Check local DB for cached products
            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getInstance(requireContext())
                    val shopId = preferenceManager.getCurrentShopId()
                    val localProducts = db.productDao().getAllProductsSuspend(shopId)
                    if (localProducts.isNotEmpty()) {
                        val productList = localProducts.map { it.toProduct() }
                        println("✅ Found ${productList.size} products in cache")
                        binding.productsRecyclerView?.visibility = View.VISIBLE
                        binding.noProductsTextView?.visibility = View.GONE
                        productAdapter.submitList(productList)
                        productAdapter.notifyDataSetChanged()
                        binding.productsRecyclerView?.invalidate()
                        binding.productsRecyclerView?.requestLayout()
                        Toast.makeText(requireContext(), "Loaded ${productList.size} products from cache", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    println("❌ Error loading from cache: ${e.message}")
                }
            }
        }
    }

    private fun showShopSelectionErrorDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Shop Selection Error")
            .setMessage("There was a problem loading the shop data. Please select your shop again.")
            .setPositiveButton("Select Shop") { _, _ ->
                try {
                    findNavController().navigate(R.id.shopsFragment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error navigating to shops", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val shopName = preferenceManager.getCurrentShopName()
        if (shopName.isNotEmpty()) {
            binding.toolbar.subtitle = shopName
        }
    }

    private fun setupClickListeners() {
        binding.checkoutButton?.setOnClickListener {
            checkShopAndCheckout()
        }

        binding.scanBarcodeButton?.setOnClickListener {
            checkShopBeforeAction {
                showBarcodeScanner()
            }
        }

        binding.clearCartButton?.setOnClickListener {
            viewModel.clearCart()
        }

        binding.holdButton?.setOnClickListener {
            viewModel.holdSale()
            Toast.makeText(requireContext(), "Sale held", Toast.LENGTH_SHORT).show()
        }

        binding.changeCustomerButton?.setOnClickListener {
            showCustomerSelectionDialog()
        }

        binding.addCustomerButton?.setOnClickListener {
            Toast.makeText(requireContext(), "Add customer coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkShopBeforeAction(action: () -> Unit) {
        val shopUuid = preferenceManager.getCurrentShopUuid()
        if (shopUuid.isEmpty()) {
            showNoShopSelectedDialog()
        } else {
            action.invoke()
        }
    }

    private fun checkShopAndCheckout() {
        val shopUuid = preferenceManager.getCurrentShopUuid()

        if (shopUuid.isEmpty()) {
            showNoShopSelectedDialog()
            return
        }

        if (!preferenceManager.hasActiveSubscription()) {
            showNoActiveSubscriptionDialog()
            return
        }

        proceedToPayment()
    }

    private fun showNoShopSelectedDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("No Shop Selected")
            .setMessage("Please select a shop before completing the sale.")
            .setPositiveButton("Select Shop") { _, _ ->
                try {
                    findNavController().navigate(R.id.shopsFragment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error navigating to shops", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNoActiveSubscriptionDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Subscription Required")
            .setMessage("This shop doesn't have an active subscription. Please activate a subscription to continue.")
            .setPositiveButton("View Subscriptions") { _, _ ->
                try {
                    findNavController().navigate(R.id.shopsFragment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error navigating to subscriptions", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun proceedToPayment() {
        val customer = viewModel.selectedCustomer.value ?: walkInCustomer
        val cartItems = viewModel.cartItems.value
        val totalAmount = viewModel.getCartSummary().total.toFloat()

        if (cartItems.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val hasTempProducts = cartItems.any { it.product.id.startsWith("temp_") }
        if (hasTempProducts) {
            Toast.makeText(
                requireContext(),
                "Some products haven't been synced yet. Please wait or refresh products.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (totalAmount <= 0) {
            Toast.makeText(requireContext(), "Invalid total amount", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val itemsList = ArrayList(cartItems)
            val itemsArray = itemsList.toTypedArray()

            val action = NewSaleFragmentDirections.actionNewSaleFragmentToPaymentFragment(
                customerId = customer.id,
                cartItems = itemsArray,
                totalAmount = totalAmount
            )
            findNavController().navigate(action)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        // Products observer
        viewModel.products.observe(viewLifecycleOwner) { products ->
            println("📱 Products received: ${products.size}")

            if (products.isNotEmpty()) {
                println("📱 Showing ${products.size} products")
                binding.productsRecyclerView?.visibility = View.VISIBLE
                binding.noProductsTextView?.visibility = View.GONE

                // Submit to adapter
                productAdapter.submitList(products)

                // Force update
                binding.productsRecyclerView?.post {
                    productAdapter.notifyDataSetChanged()
                    binding.productsRecyclerView?.invalidate()
                    binding.productsRecyclerView?.requestLayout()
                    binding.productsRecyclerView?.smoothScrollToPosition(0)
                    println("Adapter count: ${productAdapter.itemCount}")
                }
            } else {
                val isLoading = viewModel.isLoading.value == true
                if (!isLoading) {
                    println("No products to display after loading complete")
                    binding.productsRecyclerView?.visibility = View.GONE
                    binding.noProductsTextView?.visibility = View.VISIBLE
                } else {
                    println("Still loading products...")
                    binding.productsRecyclerView?.visibility = View.GONE
                    binding.noProductsTextView?.visibility = View.GONE
                    binding.loadingProgressBar?.visibility = View.VISIBLE
                }
            }
        }

        // Cart items observer
        viewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            if (cartItems.isEmpty()) {
                binding.emptyCartLayout?.visibility = View.VISIBLE
                binding.cartRecyclerView?.visibility = View.GONE
                binding.cartSummaryLayout?.visibility = View.GONE
            } else {
                binding.emptyCartLayout?.visibility = View.GONE
                binding.cartRecyclerView?.visibility = View.VISIBLE
                binding.cartSummaryLayout?.visibility = View.VISIBLE
                cartAdapter.submitList(cartItems)
                binding.cartRecyclerView?.post {
                    cartAdapter.notifyDataSetChanged()
                    binding.cartRecyclerView?.invalidate()
                    binding.cartRecyclerView?.requestLayout()
                }
            }
            updateCartSummary()
        }

        // Selected customer observer
        viewModel.selectedCustomer.observe(viewLifecycleOwner) { customer ->
            customer?.let {
                binding.selectedCustomerTextView?.text = it.name
                binding.customerPhoneTextView?.text = it.phone ?: "No phone"
            } ?: run {
                binding.selectedCustomerTextView?.text = "Walk-in Customer"
                binding.customerPhoneTextView?.text = "Default customer"
            }
        }

        // Loading state observer
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Error messages observer
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

        // Success messages observer
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

        // Offline mode observer
        viewModel.isOffline.observe(viewLifecycleOwner) { isOffline ->
            binding.offlineIndicatorTextView?.visibility = if (isOffline) View.VISIBLE else View.GONE
        }
    }

    private fun debugShopInfo() {
        val shopId = preferenceManager.getCurrentShopId()
        val shopUuid = preferenceManager.getCurrentShopUuid()
        val shopName = preferenceManager.getCurrentShopName()

        println("Shop Info - ID: '$shopId', UUID: '$shopUuid', Name: '$shopName'")

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                val localProducts = db.productDao().getAllProductsSuspend(shopId)
                println("Local products count: ${localProducts.size}")
                if (localProducts.isNotEmpty() && viewModel.products.value.isNullOrEmpty()) {
                    println("⚠️ Local products exist but ViewModel is empty, forcing display")
                    val productList = localProducts.map { it.toProduct() }
                    productAdapter.submitList(productList)
                    productAdapter.notifyDataSetChanged()
                    binding.productsRecyclerView?.visibility = View.VISIBLE
                    binding.noProductsTextView?.visibility = View.GONE
                }
            } catch (e: Exception) {
                println("Error reading local DB: ${e.message}")
            }
        }
    }

    private fun showBarcodeScanner() {
        val scannerFragment = BarcodeScannerFragment.newInstance()
        scannerFragment.setOnBarcodeScannedListener { barcode ->
            searchProductByBarcode(barcode)
        }
        scannerFragment.show(parentFragmentManager, "barcode_scanner")
    }

    private fun searchProductByBarcode(barcode: String) {
        binding.loadingProgressBar?.visibility = View.VISIBLE

        viewModel.searchProductByBarcode(barcode) { product ->
            binding.loadingProgressBar?.visibility = View.GONE

            if (product != null) {
                viewModel.addToCart(product)
                Toast.makeText(requireContext(), "Added: ${product.name}", Toast.LENGTH_SHORT).show()
            } else {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Product Not Found")
                    .setMessage("No product found with barcode: $barcode")
                    .setPositiveButton("Search Manually") { _, _ ->
                        binding.searchEditText?.text?.clear()
                        binding.searchEditText?.setText(barcode)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun setupSearchListener() {
        binding.searchEditText?.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s?.toString() ?: ""
                if (query.length >= 2) {
                    viewModel.searchProducts(query)
                } else if (query.isEmpty()) {
                    viewModel.loadProducts()
                }
            }
        })
    }

    private fun updateCartSummary() {
        val summary = viewModel.getCartSummary()
        binding.subtotalTextView?.text = CurrencyFormatter.formatFull(summary.subtotal)
        binding.discountTextView?.text = "-${CurrencyFormatter.formatFull(summary.totalDiscount)}"
        binding.taxTextView?.text = CurrencyFormatter.formatFull(summary.tax)
        binding.totalTextView?.text = CurrencyFormatter.formatFull(summary.total)
        binding.cartItemCountTextView?.text = "${summary.itemCount} items"
    }

    private fun showDiscountDialog(
        item: com.devbrian.osebo.models.CartItem,
        initialDiscount: Double = 0.0
    ) {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Discount %"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(initialDiscount.toString())
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Apply Discount")
            .setMessage("Enter discount percentage for ${item.product.name}")
            .setView(input)
            .setPositiveButton("Apply") { _, _ ->
                val discountStr = input.text.toString()
                val discount = discountStr.toDoubleOrNull() ?: 0.0
                if (discount in 0.0..100.0) {
                    viewModel.applyDiscount(item, discount)
                } else {
                    Toast.makeText(requireContext(), "Invalid discount (0-100)", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomerSelectionDialog() {
        val customers = listOf(walkInCustomer)
        val customerNames = customers.map { it.name }.toTypedArray()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Customer")
            .setItems(customerNames) { _, which ->
                viewModel.selectCustomer(customers[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}