package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.CartAdapter
import com.devbrian.osebo.adapters.ProductAdapter
import com.devbrian.osebo.databinding.FragmentNewSaleBinding
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.ui.viewmodels.SalesViewModel
import com.devbrian.osebo.utils.CurrencyFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewSaleFragment : Fragment() {

    private var _binding: FragmentNewSaleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SalesViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartAdapter: CartAdapter

    // Default Walk-in Customer
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerViews()
        setupClickListeners()
        setupObservers()
        setupSearchListener()

        // Load data
        viewModel.loadProducts()
        viewModel.loadCustomers()

        // Select Walk-in Customer by default
        viewModel.selectCustomer(walkInCustomer)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerViews() {
        // Products RecyclerView - CHANGE TO 1 COLUMN
        productAdapter = ProductAdapter(
            onItemClick = { product ->
                println("📱 FRAGMENT - Product clicked: ${product.name}")
                viewModel.addToCart(product)
            }
        )

        binding.rvProducts.apply {
            // Use LinearLayoutManager for single column
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
            setHasFixedSize(true)
        }

        // Cart RecyclerView (keep as is)
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

        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
            setHasFixedSize(true)
        }
    }
    // Update showDiscountDialog to accept discount percentage
    private fun showDiscountDialog(item: com.devbrian.osebo.models.CartItem, initialDiscount: Double = 0.0) {
        // Create an EditText for discount input
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Discount %"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(initialDiscount.toString())
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Apply Discount")
            .setMessage("Enter discount percentage for ${item.product.name}")
            .setView(input)
            .setPositiveButton("Apply") { dialog, _ ->
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

    private fun setupClickListeners() {
        binding.btnCheckout.setOnClickListener {
            checkout()
        }

        binding.btnClearCart.setOnClickListener {
            viewModel.clearCart()
        }

        binding.btnHold.setOnClickListener {
            viewModel.holdSale()
            Toast.makeText(requireContext(), "Sale held", Toast.LENGTH_SHORT).show()
        }

        binding.btnSelectCustomer.setOnClickListener {
            showCustomerSelectionDialog()
        }

        binding.btnAddCustomer.setOnClickListener {
            // Navigate to add customer fragment
            Toast.makeText(requireContext(), "Add customer coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        // Observe products
        viewModel.products.observe(viewLifecycleOwner) { products ->
            println("📱 NewSaleFragment - Products received: ${products.size}")

            if (products.isEmpty()) {
                println("📱 No products to display")
                binding.rvProducts.visibility = View.GONE
                // Show empty state if you have one
                // binding.tvEmptyProducts.visibility = View.VISIBLE
            } else {
                println("📱 Showing ${products.size} products")
                // Log first few products for debugging
                products.take(3).forEachIndexed { index, product ->
                    println("   Product[$index]: ${product.name} - ${product.price} - SKU: ${product.sku}")
                }
                binding.rvProducts.visibility = View.VISIBLE
                // binding.tvEmptyProducts.visibility = View.GONE
                productAdapter.submitList(products)
            }
        }

        // Observe cart items
        viewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            println("📱 Cart updated - ${cartItems.size} items")

            if (cartItems.isEmpty()) {
                binding.layoutEmptyCart.visibility = View.VISIBLE
                binding.rvCart.visibility = View.GONE
                binding.layoutCartSummary.visibility = View.GONE
            } else {
                binding.layoutEmptyCart.visibility = View.GONE
                binding.rvCart.visibility = View.VISIBLE
                binding.layoutCartSummary.visibility = View.VISIBLE
                cartAdapter.submitList(cartItems)
            }
            updateCartSummary()
        }

        // Observe selected customer
        viewModel.selectedCustomer.observe(viewLifecycleOwner) { customer ->
            customer?.let {
                binding.tvSelectedCustomer.text = it.name
                binding.tvCustomerPhone.text = it.phone ?: "No phone"
                println("✅ Customer selected: ${it.id} - ${it.name}")
            } ?: run {
                binding.tvSelectedCustomer.text = "Walk-in Customer"
                binding.tvCustomerPhone.text = "Default customer"
                println("ℹ️ Using default Walk-in Customer")
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            println("📱 Loading state: $isLoading")
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                println("❌ Error: $it")
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

        // Observe success messages
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                println("✅ Success: $it")
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }

        // Observe offline status
        viewModel.isOffline.observe(viewLifecycleOwner) { isOffline ->
            binding.tvOfflineIndicator.visibility = if (isOffline) View.VISIBLE else View.GONE
            println("📱 Offline mode: $isOffline")
        }
    }

    private fun setupSearchListener() {
        binding.etSearchProducts.addTextChangedListener(object : android.text.TextWatcher {
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
        binding.tvSubtotal.text = CurrencyFormatter.formatFull(summary.subtotal)
        binding.tvDiscount.text = CurrencyFormatter.formatFull(summary.totalDiscount)
        binding.tvTax.text = CurrencyFormatter.formatFull(summary.tax)
        binding.tvTotal.text = CurrencyFormatter.formatFull(summary.total)
        binding.tvItemCount.text = "${summary.itemCount} items"
    }

    private fun showDiscountDialog(item: com.devbrian.osebo.models.CartItem) {
        // Simple discount dialog
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Apply Discount")
            .setMessage("Enter discount percentage for ${item.product.name}")
            .setView(android.widget.EditText(requireContext()).apply {
                hint = "Discount %"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            })
            .setPositiveButton("Apply") { dialog, _ ->
                val editText = (dialog as android.app.AlertDialog).findViewById<android.widget.EditText>(android.R.id.custom)
                val discountStr = editText?.text.toString()
                val discount = discountStr.toDoubleOrNull() ?: 0.0
                if (discount in 0.0..100.0) {
                    viewModel.applyDiscount(item, discount)
                } else {
                    Toast.makeText(requireContext(), "Invalid discount", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomerSelectionDialog() {
        // Simple customer selection dialog with Walk-in Customer as default
        val customers = listOf(walkInCustomer) // You can add more customers here

        val customerNames = customers.map { it.name }.toTypedArray()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Customer")
            .setItems(customerNames) { _, which ->
                viewModel.selectCustomer(customers[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkout() {
        val customer = viewModel.selectedCustomer.value ?: walkInCustomer
        val cartItems = viewModel.cartItems.value
        val totalAmount = viewModel.getCartSummary().total.toFloat()

        if (cartItems.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        println("✅ Checkout - Total: $totalAmount, Customer: ${customer.name} (${customer.id}), Items: ${cartItems.size}")

        try {
            // Create a new list and convert to array
            val itemsList = ArrayList(cartItems)
            val itemsArray = itemsList.toTypedArray()

            // Use Safe Args to navigate
            val action = NewSaleFragmentDirections.actionNewSaleFragmentToPaymentFragment(
                customerId = customer.id,
                cartItems = itemsArray,
                totalAmount = totalAmount
            )
            findNavController().navigate(action)
        } catch (e: Exception) {
            println("❌ Navigation error: ${e.message}")
            e.printStackTrace()
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}