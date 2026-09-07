package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.CartAdapter
import com.devbrian.osebo.adapters.ProductAdapter
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.local.entity.ProductEntity
import com.devbrian.osebo.databinding.FragmentNewSaleBinding
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.ui.viewmodels.SalesViewModel
import com.devbrian.osebo.utils.CurrencyFormatter
import kotlinx.coroutines.launch

class NewSaleFragment : Fragment() {

    private var _binding: FragmentNewSaleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SalesViewModel by viewModel()
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

        if (!preferenceManager.isShopProperlySelected()) {
            showShopSelectionErrorDialog()
            return
        }

        setupToolbar()
        setupClickListeners()
        setupObservers()
        setupSearchListener()

        viewModel.loadProducts()
        viewModel.loadCustomers()
        viewModel.selectCustomer(walkInCustomer)

        debugShopInfo()

        binding.productsRecyclerView?.postDelayed({
            forceProductDisplay()
        }, 500)
    }

    // Fixed: Use ProductDao methods that exist
    private fun testBarcodeSearch(barcode: String) {
        println("🔍 ===== TESTING BARCODE SEARCH =====")
        println("🔍 Barcode searched: '$barcode'")

        lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                val shopId = preferenceManager.getCurrentShopId()

                println("🔍 Shop ID for search: '$shopId'")

                if (shopId.isEmpty()) {
                    println("❌ Shop ID is empty! Cannot search local DB")
                    Toast.makeText(requireContext(), "No shop selected. Please select a shop first.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Use searchProducts instead of getProductByBarcodeAndShop
                val allProducts = db.productDao().getProductsByShopSuspend(shopId)
                val productByBarcode = allProducts.find { it.barcode == barcode }

                if (productByBarcode != null) {
                    println("✅✅✅ PRODUCT FOUND IN LOCAL DB BY BARCODE! ✅✅✅")
                    println("   Product: ${productByBarcode.name}")
                    println("   ID: ${productByBarcode.id}")
                    println("   Barcode: ${productByBarcode.barcode}")
                    println("   Price: ${productByBarcode.price}")

                    val product = productByBarcode.toProduct()
                    viewModel.addToCart(product)
                    Toast.makeText(requireContext(), "Found via barcode: ${product.name}", Toast.LENGTH_LONG).show()
                } else {
                    println("❌ No product found with barcode: '$barcode'")

                    val allProductsList = db.productDao().getProductsByShopSuspend(shopId)
                    println("📊 Total products in DB: ${allProductsList.size}")

                    val productsWithBarcodes = allProductsList.filter { !it.barcode.isNullOrEmpty() }
                    println("📊 Products with barcodes in DB (${productsWithBarcodes.size}):")
                    if (productsWithBarcodes.isNotEmpty()) {
                        productsWithBarcodes.forEachIndexed { index, product ->
                            println("   ${index + 1}. ${product.name}: barcode='${product.barcode}'")
                        }
                    } else {
                        println("   No products have barcodes set!")
                        Toast.makeText(requireContext(),
                            "No products have barcodes. Please add barcodes to products first.",
                            Toast.LENGTH_LONG).show()
                    }

                    Toast.makeText(requireContext(),
                        "No product found with barcode: '$barcode'\n${productsWithBarcodes.size} products have barcodes",
                        Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                println("❌ Error searching barcode: ${e.message}")
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error searching barcode: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Fixed: Remove isPendingSync and syncAction
    private fun updateProductWithBarcode(productName: String, barcode: String) {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                val shopId = preferenceManager.getCurrentShopId()
                val allProducts = db.productDao().getProductsByShopSuspend(shopId)

                val productToUpdate = if (productName.isNotEmpty()) {
                    allProducts.find { it.name.contains(productName, ignoreCase = true) }
                } else {
                    allProducts.firstOrNull()
                }

                if (productToUpdate != null) {
                    println("📝 Updating product: ${productToUpdate.name}")
                    println("   Current barcode: '${productToUpdate.barcode ?: "NULL"}'")
                    println("   New barcode: '$barcode'")

                    val updatedProduct = productToUpdate.copy(barcode = barcode)
                    db.productDao().updateProduct(updatedProduct)
                    println("✅ Product updated successfully!")

                    // Verify update
                    val verifiedProducts = db.productDao().getProductsByShopSuspend(shopId)
                    val verified = verifiedProducts.find { it.barcode == barcode }
                    if (verified != null) {
                        println("✅ Verification successful! Product '${verified.name}' now has barcode '${verified.barcode}'")
                        Toast.makeText(requireContext(), "Added barcode to: ${verified.name}", Toast.LENGTH_LONG).show()
                    } else {
                        println("❌ Verification failed - barcode not found after update")
                    }
                } else {
                    println("❌ No products found to update")
                    Toast.makeText(requireContext(), "No products found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                println("❌ Error: ${e.message}")
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showAddBarcodeDialog() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                val shopId = preferenceManager.getCurrentShopId()
                val allProducts = db.productDao().getProductsByShopSuspend(shopId)

                if (allProducts.isEmpty()) {
                    Toast.makeText(requireContext(), "No products found", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val productNames = allProducts.map { it.name }.toTypedArray()

                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Select Product to Add Barcode")
                    .setItems(productNames) { _, which ->
                        val selectedProduct = allProducts[which]
                        showEnterBarcodeDialog(selectedProduct)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } catch (e: Exception) {
                println("❌ Error: ${e.message}")
            }
        }
    }

    private fun showEnterBarcodeDialog(product: ProductEntity) {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Enter barcode number"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(product.barcode ?: "")
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Barcode to ${product.name}")
            .setMessage("Current barcode: ${product.barcode ?: "None"}")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val barcode = input.text.toString().trim()
                if (barcode.isNotEmpty()) {
                    updateProductWithBarcode(product.name, barcode)
                } else {
                    Toast.makeText(requireContext(), "Please enter a barcode", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun debugAllProductsWithBarcodes() {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(requireContext())
                val shopId = preferenceManager.getCurrentShopId()
                val allProducts = db.productDao().getProductsByShopSuspend(shopId)

                println("🔍 ===== ALL PRODUCTS =====")
                allProducts.forEachIndexed { index, product ->
                    println("${index + 1}. Name: ${product.name}")
                    println("   SKU: ${product.sku}")
                    println("   Barcode: '${product.barcode ?: "NULL"}'")
                    println("   ID: ${product.id}")
                    println("   ---")
                }
                println("🔍 =======================")

                val productsWithBarcodes = allProducts.filter { !it.barcode.isNullOrEmpty() }
                println("\n📊 Products with barcodes: ${productsWithBarcodes.size} / ${allProducts.size}")

                if (productsWithBarcodes.isEmpty()) {
                    println("⚠️ No products have barcodes! Please add barcodes to products.")
                    Toast.makeText(requireContext(), "No products have barcodes. Please add barcodes first.", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                println("❌ Error: ${e.message}")
            }
        }
    }

    private fun showBarcodeScanner() {
        val scannerFragment = BarcodeScannerFragment.newInstance()
        scannerFragment.setOnBarcodeScannedListener { barcode ->
            println("🔍 Barcode received from scanner: '$barcode'")
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
                testBarcodeSearch(barcode)
            }
        }
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

            lifecycleScope.launch {
                try {
                    val db = AppDatabase.getInstance(requireContext())
                    val shopId = preferenceManager.getCurrentShopId()
                    val localProducts = db.productDao().getProductsByShopSuspend(shopId)
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

        binding.btnDebugBarcode?.setOnClickListener {
            debugAllProductsWithBarcodes()
        }

        binding.btnAddBarcode?.setOnClickListener {
            showAddBarcodeDialog()
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
        viewModel.products.observe(viewLifecycleOwner) { products ->
            println("📱 Products received: ${products.size}")

            if (products.isNotEmpty()) {
                println("📱 Showing ${products.size} products")
                binding.productsRecyclerView?.visibility = View.VISIBLE
                binding.noProductsTextView?.visibility = View.GONE
                productAdapter.submitList(products)
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

        viewModel.selectedCustomer.observe(viewLifecycleOwner) { customer ->
            customer?.let {
                binding.selectedCustomerTextView?.text = it.name
                binding.customerPhoneTextView?.text = it.phone ?: "No phone"
            } ?: run {
                binding.selectedCustomerTextView?.text = "Walk-in Customer"
                binding.customerPhoneTextView?.text = "Default customer"
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

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
                val localProducts = db.productDao().getProductsByShopSuspend(shopId)
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