package com.devbrian.osebo.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.databinding.FragmentPaymentBinding
import com.devbrian.osebo.ui.viewmodels.SalesViewModel
import com.devbrian.osebo.utils.Resource
import com.devbrian.osebo.models.SaleData
import com.devbrian.osebo.models.CartItem
import com.devbrian.osebo.utils.CurrencyFormatter
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SalesViewModel by viewModels()

    private val args: PaymentFragmentArgs by navArgs()

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency = Currency.getInstance("UGX")
    }

    private var selectedPaymentMethod = "cash"

    @Inject
    lateinit var preferenceManager: PreferenceManager

    
    private var saleResponseData: SaleData? = null

    private val customerId: String? by lazy {
        args.customerId ?: run {
            println("ℹ️ PaymentFragment - customerId is null, proceeding as Walk-in Customer")
            null
        }
    }

    private val cartItems: Array<CartItem> by lazy {
        args.cartItems ?: run {
            println("❌ PaymentFragment - cartItems is null, using empty array")
            emptyArray()
        }
    }

    private val totalAmount: Float by lazy {
        args.totalAmount
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println("📱 PaymentFragment - Arguments received:")
        println("   - Customer ID: $customerId")
        println("   - Cart items count: ${cartItems.size}")
        println("   - Total amount: $totalAmount")

        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Error: Cart is empty", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
            return
        }

        setupToolbar()
        setupUI()
        setupListeners()
        setupObservers()
        displayCustomerInfo()
        displayCartSummary()

        viewModel.setCartItems(cartItems.toList())
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupUI() {
        binding.tvTotalAmount.text = currencyFormat.format(totalAmount)

        binding.chipCash.setOnClickListener {
            binding.chipCash.isChecked = true
            binding.chipCard.isChecked = false
            binding.chipMobileMoney.isChecked = false
            binding.chipBankTransfer.isChecked = false
            selectedPaymentMethod = "cash"
            updatePaymentFields()
        }

        binding.chipCard.setOnClickListener {
            binding.chipCash.isChecked = false
            binding.chipCard.isChecked = true
            binding.chipMobileMoney.isChecked = false
            binding.chipBankTransfer.isChecked = false
            selectedPaymentMethod = "card"
            updatePaymentFields()
        }

        binding.chipMobileMoney.setOnClickListener {
            binding.chipCash.isChecked = false
            binding.chipCard.isChecked = false
            binding.chipMobileMoney.isChecked = true
            binding.chipBankTransfer.isChecked = false
            selectedPaymentMethod = "mobile_money"
            updatePaymentFields()
        }

        binding.chipBankTransfer.setOnClickListener {
            binding.chipCash.isChecked = false
            binding.chipCard.isChecked = false
            binding.chipMobileMoney.isChecked = false
            binding.chipBankTransfer.isChecked = true
            selectedPaymentMethod = "bank_transfer"
            updatePaymentFields()
        }

        binding.chipCash.isChecked = true
    }

    private fun displayCustomerInfo() {
        binding.tvCustomerInfo.visibility = View.VISIBLE
        binding.tvCustomerInfo.text = customerId?.let { "Customer ID: $it" } ?: "Walk-in Customer"
    }

    private fun displayCartSummary() {
        val itemCount = cartItems.size
        binding.tvItemCount.text = "$itemCount items"

        val totalQuantity = cartItems.sumOf { it.quantity }

        println("📱 PaymentFragment - Cart has $itemCount unique items, $totalQuantity total items")
        cartItems.forEachIndexed { index, item ->
            println("   Item $index: ${item.product.name} x${item.quantity} = ${currencyFormat.format(item.subtotal)}")
        }
    }

    private fun setupListeners() {
        binding.etAmountTendered.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                calculateChange()
            }
        })

        binding.btnProcessPayment.setOnClickListener {
            processPayment()
        }

        binding.btnAddCustomer.setOnClickListener {
            addCustomer()
        }
    }

    private fun getCurrentEmployeeName(): String {
        // Get from your auth/session manager
        return preferenceManager.getCurrentEmployeeName() ?: ""
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnProcessPayment.isEnabled = !isLoading
            binding.btnProcessPayment.text = if (isLoading) "Processing..." else "Complete Payment"
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.saleResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { saleData ->
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Payment successful!", Toast.LENGTH_SHORT).show()

                        
                        saleResponseData = saleData

                        
                        println("📱 PaymentFragment - Shop data from response:")
                        println("   - Shop Name: ${saleData.shop?.name ?: "Not available"}")
                        println("   - Shop Address: ${saleData.shop?.address ?: "Not available"}")
                        println("   - Shop Phone: ${saleData.shop?.phone ?: "Not available"}")
                        println("   - Shop Description: ${saleData.shop?.description ?: "Not available"}")

                        navigateToReceipt(saleData)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message ?: "Failed to process payment", Toast.LENGTH_LONG).show()
                    println("❌ PaymentFragment - Error: ${resource.message}")
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }
    }

    private fun navigateToReceipt(saleData: SaleData) {
        try {
            
            val shopName = saleData.shop?.name
            val shopAddress = saleData.shop?.address
            val shopPhone = saleData.shop?.phone
            val shopDescription = saleData.shop?.description
            val saleDate = saleData.createdAt

            println("📱 PaymentFragment - Navigating to receipt with shop data:")
            println("   - Shop Name: $shopName")
            println("   - Shop Address: $shopAddress")
            println("   - Shop Phone: $shopPhone")

            
            val customerPhone = binding.etPhoneNumber.text.toString().ifEmpty { "N/A" }

            val action = PaymentFragmentDirections.actionPaymentFragmentToReceiptFragment(
                invoiceNumber = saleData.invoiceNumber ?: "INV-${System.currentTimeMillis()}",
                customerName = "Walk-in Customer",
                customerPhone = customerPhone,
                cartItems = cartItems,
                totalAmount = saleData.totalAmount.toFloat(),
                paidAmount = saleData.paidAmount.toFloat(),
                change = saleData.change.toFloat(),
                paymentMethod = selectedPaymentMethod,
                
                shopName = shopName ?: "",
                shopAddress = shopAddress ?: "",
                shopPhone = shopPhone ?: "",
                shopDescription = shopDescription ?: "",
                receiptDate = saleDate ?: "",
                saleId = saleData.id ,
                        servedBy = getCurrentEmployeeName()

            )
            findNavController().navigate(action)
        } catch (e: Exception) {
            println("❌ Navigation to receipt error: ${e.message}")
            e.printStackTrace()
            Toast.makeText(requireContext(), "Payment successful but failed to show receipt", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack(R.id.salesFragment, false)
        }
    }

    private fun updatePaymentFields() {
        when (selectedPaymentMethod) {
            "mobile_money" -> {
                binding.etPhoneNumber.visibility = View.VISIBLE
                binding.tilPhoneNumber.visibility = View.VISIBLE
                binding.etReference.visibility = View.VISIBLE
                binding.tilReference.visibility = View.VISIBLE
                binding.tilReference.hint = "Transaction ID"
            }
            "card" -> {
                binding.etPhoneNumber.visibility = View.GONE
                binding.tilPhoneNumber.visibility = View.GONE
                binding.etReference.visibility = View.VISIBLE
                binding.tilReference.visibility = View.VISIBLE
                binding.tilReference.hint = "Card Reference"
            }
            "bank_transfer" -> {
                binding.etPhoneNumber.visibility = View.GONE
                binding.tilPhoneNumber.visibility = View.GONE
                binding.etReference.visibility = View.VISIBLE
                binding.tilReference.visibility = View.VISIBLE
                binding.tilReference.hint = "Transfer Reference"
            }
            else -> {
                binding.etPhoneNumber.visibility = View.GONE
                binding.tilPhoneNumber.visibility = View.GONE
                binding.etReference.visibility = View.GONE
                binding.tilReference.visibility = View.GONE
            }
        }
    }

    private fun calculateChange() {
        val tenderedStr = binding.etAmountTendered.text.toString()
        val tendered = tenderedStr.toDoubleOrNull() ?: 0.0
        val change = viewModel.calculateChange(tendered, totalAmount)
        binding.tvChange.text = currencyFormat.format(change)
        println("📱 PaymentFragment - Change calculated: $change")
    }

    private fun processPayment() {
        val tenderedStr = binding.etAmountTendered.text.toString()
        if (tenderedStr.isEmpty()) {
            binding.etAmountTendered.error = "Amount is required"
            binding.etAmountTendered.requestFocus()
            return
        }

        val tendered = tenderedStr.toDoubleOrNull() ?: 0.0

        if (tendered < totalAmount) {
            Toast.makeText(requireContext(), "Insufficient amount", Toast.LENGTH_SHORT).show()
            binding.etAmountTendered.error = "Amount is less than total"
            binding.etAmountTendered.requestFocus()
            return
        }

        if (tendered > totalAmount) {
            val change = tendered - totalAmount
            AlertDialog.Builder(requireContext())
                .setTitle("Overpayment")
                .setMessage("You are paying ${CurrencyFormatter.formatFull(tendered)} for a total of ${CurrencyFormatter.formatFull(totalAmount.toDouble())}.\n\nChange to give back: ${CurrencyFormatter.formatFull(change)}\n\nNote: The API only accepts exact amounts. The sale will be recorded with the exact total.")
                .setPositiveButton("Proceed") { _, _ ->
                    proceedWithPayment(totalAmount.toDouble(), "sale", change)
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        proceedWithPayment(totalAmount.toDouble(), "sale", 0.0)
    }

    private fun proceedWithPayment(paidAmount: Double, saleType: String, change: Double) {
        val reference = binding.etReference.text.toString()
        val phoneNumber = binding.etPhoneNumber.text.toString()

        println("📱 PaymentFragment - Processing payment:")
        println("   - Customer ID: $customerId")
        println("   - Amount Paid to API: $paidAmount")
        println("   - Total Amount: $totalAmount")
        println("   - Change to give: $change")
        println("   - Sale Type: $saleType")
        println("   - Items Count: ${cartItems.size}")
        println("   - Payment Method: $selectedPaymentMethod")
        println("   - Reference: $reference")
        println("   - Phone: $phoneNumber")

        viewModel.completeSale(
            customerId = customerId,
            paidAmount = paidAmount,
            paymentMethod = selectedPaymentMethod,
            reference = reference.ifEmpty { null },
            phoneNumber = phoneNumber.ifEmpty { null },
            totalAmount = totalAmount,
            saleType = saleType
        )
    }

    private fun addCustomer() {
        Toast.makeText(requireContext(), "Customer already selected", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
