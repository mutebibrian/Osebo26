package com.devbrian.osebo.fragments

import android.content.Intent
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
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.ui.screens.SubscriptionPaymentScreen
import com.devbrian.osebo.ui.screens.SubscriptionPaymentUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class PaymentDialogFragment : BottomSheetDialogFragment() {
    private val viewModel: SubscriptionViewModel by activityViewModel()
    private var shopId: String = ""
    private var packageId: String = ""
    private var paymentListener: ((String, String, Int, Double) -> Unit)? = null
    private var hasNavigated = false
    private var uiState by mutableStateOf(SubscriptionPaymentUiState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Osebo2_BottomSheetDialog)
        arguments?.let { args ->
            shopId = args.getString(ARG_SHOP_ID).orEmpty()
            packageId = args.getString(ARG_PACKAGE_ID).orEmpty()
            uiState = uiState.copy(
                packageName = args.getString(ARG_PACKAGE_NAME).orEmpty().ifBlank { "Subscription plan" },
                monthlyAmount = args.getDouble(ARG_AMOUNT),
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                SubscriptionPaymentScreen(
                    state = uiState,
                    isSheet = true,
                    onStateChange = { uiState = it },
                    onDismiss = ::dismiss,
                    onSubmit = ::processPayment,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observePayment()
    }

    fun setPaymentListener(listener: (String, String, Int, Double) -> Unit) {
        paymentListener = listener
    }

    private fun observePayment() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            uiState = uiState.copy(isLoading = loading)
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let(::showError)
        }
        viewModel.subscriptionResult.observe(viewLifecycleOwner) { resource ->
            if (hasNavigated) return@observe
            when (resource) {
                is Resource.Loading -> uiState = uiState.copy(isLoading = true)
                is Resource.Error -> showError(resource.message ?: "Failed to create subscription")
                is Resource.Success -> {
                    uiState = uiState.copy(isLoading = false)
                    val response = resource.data
                    val paymentId = response?.paymentId
                    if (!paymentId.isNullOrBlank()) {
                        hasNavigated = true
                        val phone = uiState.phone
                        val months = uiState.months
                        dismissAllowingStateLoss()
                        viewLifecycleOwner.lifecycleScope.launch {
                            delay(250)
                            try {
                                val action = PaymentDialogFragmentDirections.actionPaymentDialogFragmentToPaymentStatusFragment(
                                    transactionId = paymentId,
                                    shopId = shopId,
                                    amount = (uiState.monthlyAmount * months).toFloat(),
                                    currency = "UGX",
                                    phoneNumber = phone,
                                )
                                findNavController().navigate(action)
                            } catch (_: Exception) {
                                Toast.makeText(requireContext(), "Payment initiated. Check your phone.", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        hasNavigated = true
                        dismissAllowingStateLoss()
                        Toast.makeText(requireContext(), response?.message ?: "Subscription created successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun processPayment() {
        val validationError = validatePhoneNumber(uiState.phone)
        if (validationError != null) {
            uiState = uiState.copy(phoneError = validationError, submitError = validationError)
            return
        }
        if (packageId.isBlank()) {
            showError("Invalid package selected")
            return
        }
        if (shopId.isBlank()) {
            showError("No shop selected")
            return
        }
        if (uiState.monthlyAmount <= 0) {
            showCustomPlanConfirmation()
            return
        }
        showPaymentConfirmation(formatPhoneNumber(uiState.phone))
    }

    private fun showPaymentConfirmation(phone: String) {
        val total = uiState.monthlyAmount * uiState.months
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm payment")
            .setMessage(
                "Package: ${uiState.packageName}\n" +
                    "Amount: UGX ${String.format("%,.0f", total)}\n" +
                    "Duration: ${uiState.months} month${if (uiState.months == 1) "" else "s"}\n" +
                    "Phone: $phone\n\nA payment prompt will be sent to your phone.",
            )
            .setPositiveButton("Confirm") { _, _ ->
                val ids = packageId.split(",").map(String::trim).filter(String::isNotEmpty)
                paymentListener?.invoke(phone, packageId, uiState.months, total)
                    ?: viewModel.createSubscription(shopId, ids, phone, uiState.months)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomPlanConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Custom plan request")
            .setMessage("Our sales team will contact you about a plan tailored to your business.")
            .setPositiveButton("Request callback") { _, _ ->
                openContactSales()
                dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openContactSales() {
        val phone = uiState.phone.takeIf(String::isNotBlank)?.let(::formatPhoneNumber) ?: "Not provided"
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:sales@osebo.ai".toUri()
            putExtra(Intent.EXTRA_SUBJECT, "Custom Plan Request - ${uiState.packageName}")
            putExtra(Intent.EXTRA_TEXT, "Shop ID: $shopId\nPackage: ${uiState.packageName}\nPhone: $phone")
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validatePhoneNumber(phone: String): String? {
        val digits = phone.filter(Char::isDigit)
        return when {
            digits.isEmpty() -> "Phone number is required"
            digits.length < 9 -> "Use at least 9 digits"
            digits.length > 12 -> "Phone number is too long"
            else -> null
        }
    }

    private fun formatPhoneNumber(phone: String): String {
        val cleaned = phone.replace("\\s".toRegex(), "").replace("-", "")
        return when {
            cleaned.startsWith("+256") -> cleaned
            cleaned.startsWith("256") -> "+$cleaned"
            cleaned.startsWith("0") && cleaned.length == 10 -> "+256${cleaned.substring(1)}"
            cleaned.length == 9 -> "+256$cleaned"
            else -> "+256$cleaned"
        }
    }

    private fun showError(message: String) {
        uiState = uiState.copy(isLoading = false, submitError = message)
        if (isAdded) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "PaymentDialogFragment"
        private const val ARG_SHOP_ID = "shopId"
        private const val ARG_PACKAGE_ID = "packageId"
        private const val ARG_PACKAGE_NAME = "packageName"
        private const val ARG_AMOUNT = "amount"

        fun newInstance(shopId: String, packageId: String, packageName: String, amount: Double) =
            PaymentDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SHOP_ID, shopId)
                    putString(ARG_PACKAGE_ID, packageId)
                    putString(ARG_PACKAGE_NAME, packageName)
                    putDouble(ARG_AMOUNT, amount)
                }
            }
    }
}
