package com.devbrian.osebo.fragments

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.devbrian.osebo.utils.PrinterConnectionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ReceiptFragment : Fragment() {

    private var _binding: FragmentReceiptBinding? = null
    private val binding get() = _binding!!

    private val args: ReceiptFragmentArgs by navArgs()
    private lateinit var receiptItemAdapter: ReceiptItemAdapter
    private lateinit var printerManager: PrinterConnectionManager

    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 1001

    @Inject
    lateinit var preferenceManager: PreferenceManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        printerManager = PrinterConnectionManager(requireContext())
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
        checkPrinterConnection()
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

    private fun checkPrinterConnection() {
        lifecycleScope.launch {
            val isConnected = printerManager.isPrinterConnected()
            if (!isConnected) {
                binding.btnPrint.text = "Connect Printer"
                binding.btnPrint.setIconResource(R.drawable.ic_bluetooth)
            } else {
                binding.btnPrint.text = "Print"
                binding.btnPrint.setIconResource(R.drawable.ic_print)
            }
        }
    }

    private fun displayReceiptData() {

        val invoiceNumber = args.invoiceNumber
        val customerName = args.customerName
        val customerPhone = args.customerPhone
        val cartItems = args.cartItems?.toList() ?: emptyList()
        val totalAmount = args.totalAmount
        val paidAmount = args.paidAmount
        val changeAmount = args.change
        val paymentMethod = args.paymentMethod
        val servedBy = args.servedBy ?: ""  // GET SERVED BY NAME


        val saleId = args.saleId
        val shopNameArg = args.shopName
        val shopAddressArg = args.shopAddress
        val shopPhoneArg = args.shopPhone
        val shopDescriptionArg = args.shopDescription
        val receiptDateArg = args.receiptDate


        binding.tvReceiptNumber.text = invoiceNumber


        binding.tvReceiptDate.text = if (!receiptDateArg.isNullOrEmpty()) {
            try {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    .parse(receiptDateArg)
                SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(date ?: Date())
            } catch (e: Exception) {
                SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
            }
        } else {
            SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
        }


        val shopName = if (!shopNameArg.isNullOrEmpty()) {
            shopNameArg.uppercase(Locale.getDefault())
        } else {
            preferenceManager.getCurrentShopName().ifEmpty { "AK SHOPPERS" }.uppercase(Locale.getDefault())
        }

        val shopAddress = if (!shopAddressArg.isNullOrEmpty()) {
            shopAddressArg
        } else {
            preferenceManager.getShopLocation().ifEmpty { "Kiwatule, Kampala" }
        }

        val shopContact = if (!shopPhoneArg.isNullOrEmpty()) {
            shopPhoneArg
        } else {
            preferenceManager.getShopContact().ifEmpty { "+256 700 000000" }
        }

        val businessType = if (!shopDescriptionArg.isNullOrEmpty()) {
            shopDescriptionArg
        } else {
            preferenceManager.getBusinessType().ifEmpty { "Retail Store" }
        }


        binding.tvShopName.text = shopName
        binding.tvShopName.textSize = 14f
        binding.tvShopName.setTypeface(null, android.graphics.Typeface.BOLD)
        binding.tvShopName.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

        binding.tvShopAddress.text = shopAddress
        binding.tvShopAddress.textSize = 9f
        binding.tvShopContact.text = "Tel: $shopContact"
        binding.tvShopContact.textSize = 9f
        binding.tvShopContact.visibility = View.VISIBLE

        if (businessType.isNotEmpty()) {
            binding.tvBusinessType.visibility = View.VISIBLE
            binding.tvBusinessType.text = businessType
            binding.tvBusinessType.textSize = 9f
        } else {
            binding.tvBusinessType.visibility = View.GONE
        }


        binding.tvCustomerName.text = customerName.ifEmpty { "Walk-in Customer" }
        binding.tvCustomerName.textSize = 10f
        binding.tvCustomerPhone.text = customerPhone.ifEmpty { "N/A" }
        binding.tvCustomerPhone.textSize = 8f

        // DISPLAY SERVED BY
        if (servedBy.isNotEmpty()) {
            binding.tvServedBy.visibility = View.VISIBLE
            binding.tvServedBy.text = "Served by: $servedBy"
            binding.tvServedBy.textSize = 8f
        } else {
            binding.tvServedBy.visibility = View.GONE
        }


        receiptItemAdapter.submitList(cartItems)


        val subtotal = cartItems.sumOf { it.unitPrice * it.quantity }
        val totalDiscount = cartItems.sumOf {
            (it.unitPrice * it.quantity * it.discount / 100)
        }
        val tax = 0.0


        binding.tvSubtotal.text = formatCompactCurrency(subtotal)
        binding.tvDiscount.text = if (totalDiscount > 0) "-${formatCompactCurrency(totalDiscount)}" else "UGX 0"
        binding.tvTax.text = formatCompactCurrency(tax)
        binding.tvTotal.text = formatCompactCurrency(totalAmount.toDouble())
        binding.tvPaid.text = formatCompactCurrency(paidAmount.toDouble())
        binding.tvChange.text = formatCompactCurrency(changeAmount.toDouble())
        binding.tvPaymentMethod.text = paymentMethod.replaceFirstChar { it.uppercase() }
        binding.tvPaymentMethod.textSize = 9f
    }

    private fun formatCompactCurrency(amount: Double): String {
        return when {
            amount >= 1000000 -> String.format("UGX %.1fM", amount / 1000000)
            amount >= 1000 -> String.format("UGX %.1fK", amount / 1000)
            else -> String.format("UGX %,.0f", amount)
        }
    }

    private fun printReceipt() {
        lifecycleScope.launch {
            try {
                binding.btnPrint.isEnabled = false
                binding.btnPrint.text = "Printing..."

                val cartItems = args.cartItems?.toList() ?: emptyList()


                val isConnected = printerManager.isPrinterConnected()

                if (!isConnected) {
                    showPrinterSelectionDialog()
                    return@launch
                }

                val receiptText = buildReceiptText(cartItems)
                val result = printerManager.printReceipt(receiptText)

                if (result.isSuccess) {
                    Toast.makeText(requireContext(), "✅ Print successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "❌ Print failed: ${result.errorMessage}", Toast.LENGTH_LONG).show()

                    if (result.errorMessage.contains("No printer connected")) {
                        showPrinterSelectionDialog()
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to print: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnPrint.isEnabled = true
                binding.btnPrint.text = "Print"
            }
        }
    }

    private fun buildReceiptText(items: List<CartItem>): String {

        val invoiceNumber = args.invoiceNumber
        val customerName = args.customerName
        val customerPhone = args.customerPhone
        val totalAmount = args.totalAmount.toDouble()
        val paidAmount = args.paidAmount.toDouble()
        val changeAmount = args.change.toDouble()
        val paymentMethod = args.paymentMethod
        val servedBy = args.servedBy ?: ""

        val shopNameArg = args.shopName
        val shopAddressArg = args.shopAddress
        val shopPhoneArg = args.shopPhone
        val shopDescriptionArg = args.shopDescription
        val receiptDateArg = args.receiptDate

        val shopName = if (!shopNameArg.isNullOrEmpty()) {
            shopNameArg.uppercase()
        } else {
            preferenceManager.getCurrentShopName().ifEmpty { "AK SHOPPERS" }.uppercase()
        }

        val shopAddress = if (!shopAddressArg.isNullOrEmpty()) {
            shopAddressArg
        } else {
            preferenceManager.getShopLocation().ifEmpty { "Kiwatule, Kampala" }
        }

        val shopContact = if (!shopPhoneArg.isNullOrEmpty()) {
            shopPhoneArg
        } else {
            preferenceManager.getShopContact().ifEmpty { "+256 700 000000" }
        }

        val businessType = if (!shopDescriptionArg.isNullOrEmpty()) {
            shopDescriptionArg
        } else {
            preferenceManager.getBusinessType().ifEmpty { "Retail Store" }
        }

        val date = binding.tvReceiptDate.text.toString()

        val customerNameDisplay = customerName.ifEmpty { "Walk-in Customer" }
        val customerPhoneDisplay = customerPhone.ifEmpty { "N/A" }

        val subtotal = items.sumOf { it.unitPrice * it.quantity }
        val totalDiscount = items.sumOf { it.unitPrice * it.quantity * it.discount / 100 }

        // REDUCED WIDTH for 58mm printer (32 chars is good, but let's optimize)
        val width = 32

        return buildString {
            // Header
            appendLine()
            appendLine(centerText(shopName, width))
            if (businessType.isNotEmpty()) {
                appendLine(centerText(businessType, width))
            }
            appendLine(centerText(shopAddress, width))
            appendLine(centerText("Tel: $shopContact", width))
            appendLine(repeatChar('=', width))

            // Receipt Info
            appendLine(formatTwoColumns("Receipt:", invoiceNumber, width))
            appendLine(formatTwoColumns("Date:", date, width))
            appendLine(repeatChar('-', width))

            // Customer Info
            appendLine(formatTwoColumns("Customer:", customerNameDisplay, width))
            if (customerPhoneDisplay != "N/A") {
                appendLine(formatTwoColumns("Phone:", customerPhoneDisplay, width))
            }

            // Served By
            if (servedBy.isNotEmpty()) {
                appendLine(formatTwoColumns("Served by:", servedBy, width))
            }
            appendLine(repeatChar('-', width))

            // Items Header - IMPROVED ALIGNMENT
            val itemName = "ITEM"
            val qty = "QTY"
            val price = "PRICE"
            val total = "TOTAL"

            // Adjust column widths for better fit
            val nameWidth = 14
            val qtyWidth = 4
            val priceWidth = 6
            val totalWidth = 6

            val header = itemName.padEnd(nameWidth) +
                    qty.padStart(qtyWidth) +
                    price.padStart(priceWidth) +
                    total.padStart(totalWidth)
            appendLine(header)
            appendLine(repeatChar('-', width))

            // Items
            items.forEach { item ->
                val productName = item.product.name
                val quantity = item.quantity.toString()
                val unitPrice = formatCompactCurrencyShort(item.unitPrice)
                val itemTotal = formatCompactCurrencyShort(item.unitPrice * item.quantity * (1 - item.discount / 100))

                // Format item line
                val nameDisplay = if (productName.length > nameWidth) {
                    productName.substring(0, nameWidth - 1)
                } else {
                    productName
                }

                val itemLine = nameDisplay.padEnd(nameWidth) +
                        quantity.padStart(qtyWidth) +
                        unitPrice.padStart(priceWidth) +
                        itemTotal.padStart(totalWidth)
                appendLine(itemLine)

                // Show discount if applicable
                if (item.discount > 0) {
                    val discountAmount = formatCompactCurrencyShort(item.unitPrice * item.quantity * item.discount / 100)
                    val discountText = "  (${item.discount}% off)"
                    appendLine(discountText.padEnd(nameWidth) + "-$discountAmount".padStart(qtyWidth + priceWidth + totalWidth))
                }
            }

            appendLine(repeatChar('-', width))

            // Totals - IMPROVED ALIGNMENT
            appendLine(formatTwoColumns("Subtotal:", formatCompactCurrencyShort(subtotal), width))
            if (totalDiscount > 0) {
                appendLine(formatTwoColumns("Discount:", "-${formatCompactCurrencyShort(totalDiscount)}", width))
            }
            appendLine(formatTwoColumns("Tax (0%):", "0", width))
            appendLine(repeatChar('=', width))
            appendLine(formatTwoColumns("TOTAL:", formatCompactCurrencyShort(totalAmount), width))
            appendLine(repeatChar('=', width))
            appendLine(formatTwoColumns("Paid:", formatCompactCurrencyShort(paidAmount), width))
            appendLine(formatTwoColumns("Change:", formatCompactCurrencyShort(changeAmount), width))
            appendLine(formatTwoColumns("Payment:", paymentMethod.replaceFirstChar { it.uppercase() }, width))

            appendLine(repeatChar('=', width))
            appendLine()
            appendLine(centerText("THANK YOU!", width))
            appendLine()
            appendLine(centerText("Powered by Oseo", width))
            appendLine(centerText("www.osebo.ai", width))
            appendLine()
            appendLine()
            appendLine()
        }
    }



    // SIMPLIFIED item header formatter
    private fun formatItemHeader(width: Int): String {
        return "ITEM".padEnd(14) + "QTY".padStart(4) + "PRICE".padStart(6) + "TOTAL".padStart(6)
    }

    // SIMPLIFIED item line formatter
    private fun formatItemLine(itemName: String, qty: String, price: String, total: String, width: Int): String {
        val nameMax = 14
        val qtyMax = 4
        val priceMax = 6
        val totalMax = 6

        val namePart = if (itemName.length > nameMax) itemName.substring(0, nameMax - 2) + ".." else itemName
        val qtyPart = if (qty.length > qtyMax) qty.substring(0, qtyMax) else qty
        val pricePart = if (price.length > priceMax) price.substring(0, priceMax) else price
        val totalPart = if (total.length > totalMax) total.substring(0, totalMax) else total

        return namePart.padEnd(nameMax) +
                qtyPart.padStart(qtyMax) +
                pricePart.padStart(priceMax) +
                totalPart.padStart(totalMax)
    }

    private fun formatCompactCurrencyShort(amount: Double): String {
        return when {
            amount >= 1000000 -> String.format("%.1fM", amount / 1000000)
            amount >= 1000 -> String.format("%.1fK", amount / 1000)
            else -> String.format("%.0f", amount)
        }
    }

    private fun centerText(text: String, width: Int): String {
        val padding = width - text.length
        if (padding <= 0) return text
        val leftPadding = padding / 2
        val rightPadding = padding - leftPadding
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding)
    }

    private fun formatTwoColumns(left: String, right: String, width: Int): String {
        val availableWidth = width - left.length
        return left + " ".repeat(availableWidth - right.length) + right
    }

    private fun formatItemHeaderCompact(width: Int): String {
        val itemLabel = "ITEM"
        val qtyLabel = "QTY"
        val priceLabel = "PRICE"
        val totalLabel = "TOTAL"

        val itemWidth = 12
        val qtyWidth = 4
        val priceWidth = 7
        val totalWidth = 7

        return itemLabel.padEnd(itemWidth, ' ') +
                qtyLabel.padStart(qtyWidth, ' ') +
                priceLabel.padStart(priceWidth, ' ') +
                totalLabel.padStart(totalWidth, ' ')
    }

    private fun formatItemLineCompact(item: String, qty: String, price: String, total: String, width: Int): String {
        val itemMax = 12
        val qtyMax = 4
        val priceMax = 7
        val totalMax = 7

        val itemTrimmed = if (item.length > itemMax) item.substring(0, itemMax - 3) + "..." else item
        val qtyTrimmed = if (qty.length > qtyMax) qty.substring(0, qtyMax) else qty
        val priceTrimmed = if (price.length > priceMax) price.substring(0, priceMax) else price
        val totalTrimmed = if (total.length > totalMax) total.substring(0, totalMax) else total

        return itemTrimmed.padEnd(itemMax, ' ') +
                qtyTrimmed.padStart(qtyMax, ' ') +
                priceTrimmed.padStart(priceMax, ' ') +
                totalTrimmed.padStart(totalMax, ' ')
    }

    private fun repeatChar(char: Char, count: Int): String {
        return char.toString().repeat(count)
    }

    private fun showPrinterSelectionDialog() {
        lifecycleScope.launch {
            try {
                if (!checkBluetoothPermissions()) {
                    return@launch
                }

                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

                if (bluetoothAdapter == null) {
                    Toast.makeText(requireContext(), "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (!bluetoothAdapter.isEnabled) {
                    val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivity(enableIntent)
                    return@launch
                }

                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
                if (pairedDevices == null || pairedDevices.isEmpty()) {
                    Toast.makeText(requireContext(), "No paired devices found. Please pair your PM200 printer first.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val devicesList = ArrayList<BluetoothDevice>()
                for (device in pairedDevices) {
                    devicesList.add(device)
                }


                val printerDevices = devicesList.filter { device ->
                    val deviceName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        device.name ?: ""
                    } else {
                        @Suppress("DEPRECATION")
                        device.name ?: ""
                    }

                    deviceName.contains("PM200", ignoreCase = true) ||
                            deviceName.contains("Pegasus", ignoreCase = true) ||
                            deviceName.contains("Printer", ignoreCase = true) ||
                            deviceName.contains("POS", ignoreCase = true) ||
                            deviceName.contains("Thermal", ignoreCase = true) ||
                            deviceName.contains("58MM", ignoreCase = true)
                }

                if (printerDevices.isEmpty()) {
                    showDeviceListDialog(devicesList)
                } else {
                    showDeviceListDialog(printerDevices)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeviceListDialog(devices: List<BluetoothDevice>) {
        val deviceNames = Array(devices.size) { i ->
            val device = devices[i]
            val name = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                device.name ?: "Unknown Device"
            } else {
                @Suppress("DEPRECATION")
                device.name ?: "Unknown Device"
            }
            "$name\n${device.address}"
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select PM200 Printer")
            .setItems(deviceNames) { _, which ->
                val selectedDevice = devices[which]
                connectToPrinter(selectedDevice)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun connectToPrinter(device: BluetoothDevice) {
        lifecycleScope.launch {
            try {
                binding.btnPrint.isEnabled = false
                binding.btnPrint.text = "Connecting..."

                if (!checkBluetoothPermissions()) {
                    binding.btnPrint.isEnabled = true
                    binding.btnPrint.text = "Print"
                    return@launch
                }

                val deviceName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    device.name ?: "Printer"
                } else {
                    @Suppress("DEPRECATION")
                    device.name ?: "Printer"
                }

                val result = printerManager.connectToBluetoothPrinter(device)

                if (result) {
                    Toast.makeText(requireContext(), "✅ Connected to $deviceName", Toast.LENGTH_SHORT).show()
                    binding.btnPrint.text = "Print"
                    binding.btnPrint.setIconResource(R.drawable.ic_print)
                } else {
                    Toast.makeText(requireContext(), "❌ Failed to connect to $deviceName", Toast.LENGTH_SHORT).show()
                    binding.btnPrint.text = "Connect Printer"
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Connection error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnPrint.isEnabled = true
            }
        }
    }

    private fun testPrinterConnection() {
        lifecycleScope.launch {
            try {
                if (!checkBluetoothPermissions()) {
                    return@launch
                }

                binding.btnPrint.isEnabled = false
                binding.btnPrint.text = "Testing..."

                val testText = """
                    
                    ================================
                        PRINTER TEST
                    ================================
                    
                    Model: PM200 Pegasus
                    Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
                    
                    If you can read this,
                    your printer is working!
                    
                    ---------------------------------
                    Normal Text Line 1
                    Normal Text Line 2
                    
                    Thank you!
                    ================================
                    
                    
                    
                """.trimIndent()

                val result = printerManager.printReceipt(testText)

                if (result.isSuccess) {
                    Toast.makeText(requireContext(), "✅ Test print successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "❌ Test failed: ${result.errorMessage}", Toast.LENGTH_SHORT).show()

                    if (result.errorMessage.contains("No printer connected")) {
                        showPrinterSelectionDialog()
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Test error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnPrint.isEnabled = true
                binding.btnPrint.text = "Print"
            }
        }
    }

    private fun checkBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnectPermission = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val hasScanPermission = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

            if (!hasConnectPermission || !hasScanPermission) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    ),
                    BLUETOOTH_PERMISSION_REQUEST_CODE
                )
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    testPrinterConnection()
                } else {
                    Toast.makeText(requireContext(), "Bluetooth permission required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun shareReceipt() {
        lifecycleScope.launch {
            try {
                val cartItems = args.cartItems?.toList() ?: emptyList()

                val pdfFile = PrintUtils.generateReceiptPdf(
                    context = requireContext(),
                    receiptNumber = binding.tvReceiptNumber.text.toString(),
                    date = binding.tvReceiptDate.text.toString(),
                    customerName = binding.tvCustomerName.text.toString(),
                    items = cartItems,
                    subtotal = binding.tvSubtotal.text.toString(),
                    discount = binding.tvDiscount.text.toString(),
                    tax = binding.tvTax.text.toString(),
                    total = binding.tvTotal.text.toString(),
                    paid = binding.tvPaid.text.toString(),
                    change = binding.tvChange.text.toString(),
                    paymentMethod = binding.tvPaymentMethod.text.toString()
                )

                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    pdfFile
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_SUBJECT, "Receipt ${binding.tvReceiptNumber.text}")
                    putExtra(Intent.EXTRA_TEXT, "Please find attached receipt ${binding.tvReceiptNumber.text}")
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
                putExtra(Intent.EXTRA_SUBJECT, "Receipt ${binding.tvReceiptNumber.text}")
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
            appendLine("Receipt Number: ${binding.tvReceiptNumber.text}")
            appendLine("Date: ${binding.tvReceiptDate.text}")
            appendLine()
            appendLine("Customer: ${binding.tvCustomerName.text}")
            appendLine()
            appendLine("Items:")
            appendLine("-------------------")
            cartItems.forEach { item ->
                val itemTotal = item.unitPrice * item.quantity * (1 - item.discount / 100)
                appendLine("${item.product.name} x${item.quantity} - ${formatCompactCurrency(itemTotal)}")
            }
            appendLine("-------------------")
            appendLine("Subtotal: ${binding.tvSubtotal.text}")
            appendLine("Discount: ${binding.tvDiscount.text}")
            appendLine("Tax: ${binding.tvTax.text}")
            appendLine("Total: ${binding.tvTotal.text}")
            appendLine("Paid: ${binding.tvPaid.text}")
            appendLine("Change: ${binding.tvChange.text}")
            appendLine("Payment Method: ${binding.tvPaymentMethod.text}")
            appendLine()
            appendLine("Visit us again!")
        }
    }

    private fun startNewSale() {
        try {
            findNavController().popBackStack(R.id.newSaleFragment, false)
        } catch (e: Exception) {
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
            R.id.action_test_printer -> {
                testPrinterConnection()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        printerManager.closeConnection()
        _binding = null
    }
}