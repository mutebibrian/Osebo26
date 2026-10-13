package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.models.CartItem
import com.devbrian.osebo.models.SaleData
import com.devbrian.osebo.ui.screens.PaymentScreen
import com.devbrian.osebo.ui.screens.PaymentUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.SalesViewModel
import com.devbrian.osebo.utils.CurrencyFormatter
import com.devbrian.osebo.utils.Resource
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PaymentFragment : Fragment() {
    private val viewModel: SalesViewModel by viewModel()
    private val preferenceManager: PreferenceManager by inject()
    private val args: PaymentFragmentArgs by navArgs()
    private var uiState by mutableStateOf(PaymentUiState())

    private val customerId: String? by lazy { args.customerId }

    private val cartItems: Array<CartItem> by lazy {
        args.cartItems ?: emptyArray()
    }

    private val totalAmount: Float by lazy { args.totalAmount }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                PaymentScreen(
                    state = uiState,
                    onBackClick = { findNavController().navigateUp() },
                    onAmountChange = ::updateAmountTendered,
                    onMethodSelected = { method ->
                        uiState = uiState.copy(selectedMethod = method)
                    },
                    onPhoneChange = { uiState = uiState.copy(phoneNumber = it) },
                    onReferenceChange = { uiState = uiState.copy(reference = it) },
                    onNotesChange = { uiState = uiState.copy(notes = it) },
                    onCompleteClick = ::processPayment,
                    onConfirmOverpayment = {
                        uiState = uiState.copy(showOverpaymentConfirmation = false)
                        proceedWithPayment()
                    },
                    onDismissOverpayment = {
                        uiState = uiState.copy(showOverpaymentConfirmation = false)
                    },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (cartItems.isEmpty()) {
            showMessage("Error: Cart is empty", Toast.LENGTH_LONG)
            findNavController().navigateUp()
            return
        }

        val currentCustomerId = customerId
        if (currentCustomerId.isNullOrBlank()) {
            showMessage("Error: No customer selected", Toast.LENGTH_LONG)
            findNavController().navigateUp()
            return
        }

        uiState = uiState.copy(
            shopName = preferenceManager.getCurrentShopName().ifBlank { "Selected shop" },
            customerLabel = args.customerName.ifBlank { "Selected customer" },
            customerDetail = currentCustomerId,
            uniqueItemCount = cartItems.size,
            totalQuantity = cartItems.sumOf(CartItem::quantity),
            total = CurrencyFormatter.formatFull(totalAmount.toDouble()),
        )

        observeViewModel()
        viewModel.setCartItems(cartItems.toList())
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            uiState = uiState.copy(isLoading = isLoading)
        }

        viewModel.saleResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> resource.data?.let { saleData ->
                    uiState = uiState.copy(isLoading = false)
                    showMessage("Payment successful!")
                    navigateToReceipt(saleData)
                }

                is Resource.Error -> {
                    uiState = uiState.copy(isLoading = false)
                    showMessage(resource.message ?: "Failed to process payment", Toast.LENGTH_LONG)
                }

                is Resource.Loading -> uiState = uiState.copy(isLoading = true)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                showMessage(it, Toast.LENGTH_LONG)
                viewModel.clearMessages()
            }
        }
    }

    private fun updateAmountTendered(input: String) {
        val normalized = input.filterIndexed { index, character ->
            character.isDigit() || (character == '.' && input.indexOf('.') == index)
        }
        val tendered = normalized.toDoubleOrNull() ?: 0.0
        val change = viewModel.calculateChange(tendered, totalAmount).coerceAtLeast(0.0)
        uiState = uiState.copy(
            amountTendered = normalized,
            amountError = null,
            change = CurrencyFormatter.formatFull(change),
        )
    }

    private fun processPayment() {
        val tendered = uiState.amountTendered.toDoubleOrNull()
        when {
            uiState.amountTendered.isBlank() || tendered == null -> {
                uiState = uiState.copy(amountError = "Enter the amount received")
            }

            tendered < totalAmount -> {
                uiState = uiState.copy(amountError = "Amount is less than the sale total")
            }

            tendered > totalAmount -> {
                uiState = uiState.copy(
                    amountError = null,
                    showOverpaymentConfirmation = true,
                )
            }

            else -> proceedWithPayment()
        }
    }

    private fun proceedWithPayment() {
        viewModel.completeSale(
            customerId = customerId.orEmpty(),
            paidAmount = totalAmount.toDouble(),
            paymentMethod = uiState.selectedMethod.apiValue,
            reference = uiState.reference.trim().ifEmpty { null },
            phoneNumber = uiState.phoneNumber.trim().ifEmpty { null },
            userNotes = uiState.notes.trim().ifEmpty { null },
            totalAmount = totalAmount,
            saleType = "sale",
        )
    }

    private fun navigateToReceipt(saleData: SaleData) {
        runCatching {
            PaymentFragmentDirections.actionPaymentFragmentToReceiptFragment(
                invoiceNumber = saleData.invoiceNumber ?: "INV-${System.currentTimeMillis()}",
                customerName = uiState.customerLabel,
                customerPhone = uiState.phoneNumber.ifBlank { "N/A" },
                cartItems = cartItems,
                totalAmount = saleData.totalAmount.toFloat(),
                paidAmount = saleData.paidAmount.toFloat(),
                change = saleData.change.toFloat(),
                paymentMethod = uiState.selectedMethod.apiValue,
                shopName = saleData.shop?.name.orEmpty(),
                shopAddress = saleData.shop?.address.orEmpty(),
                shopPhone = saleData.shop?.phone.orEmpty(),
                shopDescription = saleData.shop?.description.orEmpty(),
                receiptDate = saleData.createdAt.orEmpty(),
                saleId = saleData.id,
                servedBy = preferenceManager.getCurrentEmployeeName().orEmpty(),
            )
        }.onSuccess(findNavController()::navigate)
            .onFailure {
                showMessage("Payment successful but failed to show receipt")
                findNavController().popBackStack(R.id.salesFragment, false)
            }
    }

    private fun showMessage(message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (isAdded) Toast.makeText(requireContext(), message, duration).show()
    }
}
