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
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.ui.screens.SubscriptionPaymentScreen
import com.devbrian.osebo.ui.screens.SubscriptionPaymentUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import org.koin.androidx.viewmodel.ext.android.viewModel

class SubscriptionPaymentFragment : Fragment() {
    private val viewModel: SubscriptionViewModel by viewModel()
    private val args: SubscriptionPaymentFragmentArgs by navArgs()
    private var shop: Shop? = null
    private var packageId: String = ""
    private var uiState by mutableStateOf(SubscriptionPaymentUiState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shop = args.shop
        packageId = args.packageId
        uiState = SubscriptionPaymentUiState(
            shopName = shop?.name ?: "Your shop",
            packageName = args.packageName,
            monthlyAmount = args.packagePrice.toDouble(),
        )
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
                    isSheet = false,
                    onStateChange = { uiState = it },
                    onDismiss = { findNavController().navigateUp() },
                    onSubmit = ::processPayment,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observePayment()
    }

    private fun observePayment() {
        viewModel.subscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> uiState = uiState.copy(isLoading = true)
                is Resource.Error -> showError(resource.message ?: "Failed to create subscription")
                is Resource.Success -> {
                    uiState = uiState.copy(isLoading = false)
                    val paymentId = resource.data?.paymentId
                    if (!paymentId.isNullOrBlank()) {
                        val action = SubscriptionPaymentFragmentDirections.actionSubscriptionPaymentFragmentToPaymentStatusFragment(
                            transactionId = paymentId,
                            shopId = shop?.id.orEmpty(),
                            amount = (uiState.monthlyAmount * uiState.months).toFloat(),
                            currency = "UGX",
                            phoneNumber = uiState.phone,
                        )
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(requireContext(), resource.data?.message ?: "Subscription created successfully", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { uiState = uiState.copy(isLoading = it) }
        viewModel.errorMessage.observe(viewLifecycleOwner) { it?.let(::showError) }
    }

    private fun processPayment() {
        val digits = uiState.phone.filter(Char::isDigit)
        val error = when {
            digits.isEmpty() -> "Phone number is required"
            digits.length < 9 -> "Use at least 9 digits"
            digits.length > 12 -> "Phone number is too long"
            else -> null
        }
        if (error != null) {
            uiState = uiState.copy(phoneError = error, submitError = error)
            return
        }
        if (shop == null) {
            showError("No shop selected")
            return
        }
        if (packageId.isBlank()) {
            showError("Invalid package")
            return
        }
        val phone = formatPhoneNumber(uiState.phone)
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm payment")
            .setMessage(
                "Package: ${uiState.packageName}\n" +
                    "Amount: UGX ${String.format("%,.0f", uiState.monthlyAmount * uiState.months)}\n" +
                    "Duration: ${uiState.months} month${if (uiState.months == 1) "" else "s"}\n" +
                    "Phone: $phone\n\nA payment prompt will be sent to your phone.",
            )
            .setPositiveButton("Confirm") { _, _ ->
                viewModel.createSubscription(
                    shopId = shop?.id.orEmpty(),
                    packageIds = listOf(packageId),
                    phoneNumber = phone,
                    months = uiState.months,
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
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
}
