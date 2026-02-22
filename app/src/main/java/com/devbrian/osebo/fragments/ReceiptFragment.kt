package com.devbrian.osebo.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.ReceiptItemAdapter
import com.devbrian.osebo.databinding.FragmentReceiptBinding
import com.devbrian.osebo.models.CartItem
import com.devbrian.osebo.utils.CurrencyFormatter
import com.devbrian.osebo.utils.PrintUtils
import com.devbrian.osebo.data.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ReceiptFragment : Fragment() {

    private var _binding: FragmentReceiptBinding? = null
    private val binding get() = _binding!!

    private val args: ReceiptFragmentArgs by navArgs()
    private lateinit var receiptItemAdapter: ReceiptItemAdapter

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        displayReceiptData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        receiptItemAdapter = ReceiptItemAdapter()
        binding.rvReceiptItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = receiptItemAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.btnPrint.setOnClickListener {
            printReceipt()
        }

        binding.btnShare.setOnClickListener {
            shareReceipt()
        }

        binding.btnNewSale.setOnClickListener {
            startNewSale()
        }
    }

    private fun displayReceiptData() {
        // Display receipt number and date
        binding.tvReceiptNumber.text = args.invoiceNumber
        binding.tvReceiptDate.text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            .format(Date())

        // Display shop details from PreferenceManager
        val shopName = preferenceManager.getCurrentShopName()
        val shopLocation = preferenceManager.getShopLocation()
        val shopContact = preferenceManager.getShopContact()
        val businessType = preferenceManager.getBusinessType()

        binding.tvShopName.text = if (shopName.isNotEmpty()) shopName else "Osebo POS"
        binding.tvShopAddress.text = if (shopLocation.isNotEmpty()) shopLocation else "Kampala, Uganda"

        // Show phone number with "Tel:" prefix
        if (shopContact.isNotEmpty()) {
            binding.tvShopContact.visibility = View.VISIBLE
            binding.tvShopContact.text = "Tel: $shopContact"
            println("📱 ReceiptFragment - Shop contact: $shopContact")
        } else {
            binding.tvShopContact.visibility = View.GONE
            println("📱 ReceiptFragment - No shop contact available")
        }

        // Show business type if available
        if (businessType.isNotEmpty()) {
            binding.tvBusinessType.visibility = View.VISIBLE
            binding.tvBusinessType.text = businessType
        } else {
            binding.tvBusinessType.visibility = View.GONE
        }

        // Display customer info from arguments
        binding.tvCustomerName.text = args.customerName
        binding.tvCustomerPhone.text = args.customerPhone

        // Load cart items from arguments
        val cartItems = args.cartItems?.toList() ?: emptyList()
        receiptItemAdapter.submitList(cartItems)

        // Calculate totals from arguments - TAX SET TO 0
        val subtotal = cartItems.sumOf { it.unitPrice * it.quantity }
        val totalDiscount = cartItems.sumOf {
            (it.unitPrice * it.quantity * it.discount / 100)
        }
        val tax = 0.0 // Tax set to 0
        val total = args.totalAmount.toDouble()
        val paid = args.paidAmount.toDouble()
        val change = args.change.toDouble()

        // Format and display amounts
        binding.tvSubtotal.text = CurrencyFormatter.formatFull(subtotal)
        binding.tvDiscount.text = "-${CurrencyFormatter.formatFull(totalDiscount)}"
        binding.tvTax.text = CurrencyFormatter.formatFull(tax)
        binding.tvTotal.text = CurrencyFormatter.formatFull(total)
        binding.tvPaid.text = CurrencyFormatter.formatFull(paid)
        binding.tvChange.text = CurrencyFormatter.formatFull(change)
        binding.tvPaymentMethod.text = args.paymentMethod
    }

    private fun printReceipt() {
        lifecycleScope.launch {
            try {
                // Show loading
                binding.btnPrint.isEnabled = false
                binding.btnPrint.text = "Printing..."

                val cartItems = args.cartItems?.toList() ?: emptyList()

                // Generate PDF receipt
                val pdfFile = PrintUtils.generateReceiptPdf(
                    context = requireContext(),
                    receiptNumber = args.invoiceNumber,
                    date = binding.tvReceiptDate.text.toString(),
                    customerName = args.customerName,
                    items = cartItems,
                    subtotal = binding.tvSubtotal.text.toString(),
                    discount = binding.tvDiscount.text.toString(),
                    tax = binding.tvTax.text.toString(),
                    total = binding.tvTotal.text.toString(),
                    paid = binding.tvPaid.text.toString(),
                    change = binding.tvChange.text.toString(),
                    paymentMethod = args.paymentMethod
                )

                Toast.makeText(requireContext(), "Receipt ready for printing", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to print: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnPrint.isEnabled = true
                binding.btnPrint.text = "Print"
            }
        }
    }

    private fun shareReceipt() {
        lifecycleScope.launch {
            try {
                val cartItems = args.cartItems?.toList() ?: emptyList()

                // Generate PDF receipt
                val pdfFile = PrintUtils.generateReceiptPdf(
                    context = requireContext(),
                    receiptNumber = args.invoiceNumber,
                    date = binding.tvReceiptDate.text.toString(),
                    customerName = args.customerName,
                    items = cartItems,
                    subtotal = binding.tvSubtotal.text.toString(),
                    discount = binding.tvDiscount.text.toString(),
                    tax = binding.tvTax.text.toString(),
                    total = binding.tvTotal.text.toString(),
                    paid = binding.tvPaid.text.toString(),
                    change = binding.tvChange.text.toString(),
                    paymentMethod = args.paymentMethod
                )

                // Share the PDF
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    pdfFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_SUBJECT, "Receipt ${args.invoiceNumber}")
                    putExtra(Intent.EXTRA_TEXT, "Please find attached receipt ${args.invoiceNumber}")
                }

                startActivity(Intent.createChooser(shareIntent, "Share Receipt"))

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun emailReceipt() {
        try {
            val cartItems = args.cartItems?.toList() ?: emptyList()

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(""))
                putExtra(Intent.EXTRA_SUBJECT, "Receipt ${args.invoiceNumber}")
                putExtra(Intent.EXTRA_TEXT, buildEmailBody(cartItems))
            }

            startActivity(Intent.createChooser(emailIntent, "Send Email"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildEmailBody(cartItems: List<CartItem>): String {
        return buildString {
            appendLine("Thank you for your purchase!")
            appendLine()
            appendLine("Receipt Number: ${args.invoiceNumber}")
            appendLine("Date: ${binding.tvReceiptDate.text}")
            appendLine()
            appendLine("Customer: ${args.customerName}")
            appendLine()
            appendLine("Items:")
            appendLine("-------------------")
            cartItems.forEach { item ->
                val itemTotal = item.unitPrice * item.quantity * (1 - item.discount / 100)
                appendLine("${item.product.name} x${item.quantity} - ${CurrencyFormatter.formatFull(itemTotal)}")
            }
            appendLine("-------------------")
            appendLine("Subtotal: ${binding.tvSubtotal.text}")
            appendLine("Discount: ${binding.tvDiscount.text}")
            appendLine("Tax: ${binding.tvTax.text}")
            appendLine("Total: ${binding.tvTotal.text}")
            appendLine("Paid: ${binding.tvPaid.text}")
            appendLine("Change: ${binding.tvChange.text}")
            appendLine("Payment Method: ${args.paymentMethod}")
            appendLine()
            appendLine("Visit us again!")
        }
    }

    private fun startNewSale() {
        try {
            findNavController().popBackStack(R.id.newSaleFragment, false)
            println("📱 Navigating back to New Sale screen")
        } catch (e: Exception) {
            println("❌ Navigation error: ${e.message}")
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_receipt, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_print -> {
                printReceipt()
                true
            }
            R.id.action_share -> {
                shareReceipt()
                true
            }
            R.id.action_email -> {
                emailReceipt()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}