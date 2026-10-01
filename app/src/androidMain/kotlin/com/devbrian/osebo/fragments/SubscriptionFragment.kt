package com.devbrian.osebo.fragments

import android.content.Context
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
import androidx.fragment.app.Fragment
import com.devbrian.osebo.data.remote.dto.response.PackageDto
import com.devbrian.osebo.data.remote.dto.response.fromSubscriptionPackage
import com.devbrian.osebo.models.Payment
import com.devbrian.osebo.models.Subscription
import com.devbrian.osebo.models.SubscriptionPackage
import com.devbrian.osebo.ui.screens.SubscriptionCurrentPlanUi
import com.devbrian.osebo.ui.screens.SubscriptionPaymentUi
import com.devbrian.osebo.ui.screens.SubscriptionPlanUi
import com.devbrian.osebo.ui.screens.SubscriptionScreen
import com.devbrian.osebo.ui.screens.SubscriptionScreenState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import org.koin.androidx.viewmodel.ext.android.viewModel

class SubscriptionFragment : Fragment() {

    private val viewModel: SubscriptionViewModel by viewModel()

    private var shopId: String = ""
    private var currentSubscription: Subscription? = null
    private var packageDtos: List<PackageDto> = emptyList()
    private var currentSubscriptionLoading = false
    private var plansLoading = false
    private var uiState by mutableStateOf(SubscriptionScreenState())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        shopId = arguments?.getString("shopId") ?: getCurrentShopId()
        val shopName = arguments?.getString("shopName").orEmpty().ifBlank { "Shop billing" }
        uiState = uiState.copy(shopName = shopName)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OseboTheme {
                    SubscriptionScreen(
                        state = uiState,
                        onRefreshClick = ::loadSubscriptionData,
                        onPlanClick = ::selectPlan,
                        onContactSalesClick = ::openContactSales,
                        onCancelSubscription = ::cancelCurrentSubscription,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()

        if (shopId.isBlank()) {
            uiState = uiState.copy(errorMessage = "No shop selected")
            Toast.makeText(requireContext(), "No shop selected", Toast.LENGTH_SHORT).show()
        } else {
            loadSubscriptionData()
        }
    }

    private fun setupObservers() {
        viewModel.currentSubscription.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    currentSubscriptionLoading = true
                    updateLoadingState()
                }

                is Resource.Success -> {
                    currentSubscriptionLoading = false
                    currentSubscription = resource.data
                    uiState = uiState.copy(
                        currentPlan = resource.data?.toUi(),
                        errorMessage = null,
                    )
                    updateLoadingState()
                }

                is Resource.Error -> {
                    currentSubscriptionLoading = false
                    currentSubscription = null
                    uiState = uiState.copy(currentPlan = null)
                    updateLoadingState()

                    if (resource.message != "No active subscription found") {
                        resource.message?.let(::showToast)
                    }
                }
            }
        }

        viewModel.subscriptionPackages.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    plansLoading = true
                    updateLoadingState()
                }

                is Resource.Success -> {
                    plansLoading = false
                    val packages = resource.data.orEmpty()
                    packageDtos = packages.map(::fromSubscriptionPackage)
                    uiState = uiState.copy(
                        plans = packages.map(SubscriptionPackage::toUi),
                        errorMessage = null,
                    )
                    updateLoadingState()
                }

                is Resource.Error -> {
                    plansLoading = false
                    uiState = uiState.copy(
                        plans = emptyList(),
                        errorMessage = resource.message ?: "Failed to load plans",
                    )
                    updateLoadingState()
                }
            }
        }

        viewModel.paymentHistory.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> uiState = uiState.copy(isHistoryLoading = true)
                is Resource.Success -> uiState = uiState.copy(
                    payments = resource.data.orEmpty().map(Payment::toUi),
                    isHistoryLoading = false,
                )
                is Resource.Error -> {
                    uiState = uiState.copy(isHistoryLoading = false)
                    resource.message?.let(::showToast)
                }
            }
        }

        viewModel.subscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    val response = resource.data
                    if (response?.paymentId != null) {
                        showToast("Payment initiated. Check your phone to approve it.")
                    } else {
                        showToast(response?.message ?: "Subscription created")
                    }
                    loadSubscriptionData()
                }
                is Resource.Error -> showToast(resource.message ?: "Failed to create subscription")
            }
        }

        viewModel.paymentPollingStatus.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                resource.data?.let { payment ->
                    when {
                        payment.isActive -> {
                            showToast("Payment successful. Your subscription is active.")
                            loadSubscriptionData()
                        }
                        payment.isFailed || payment.isCancelled -> {
                            showToast("Payment ${payment.payment?.status ?: "failed"}. Please try again.")
                        }
                    }
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                showToast(it)
                viewModel.clearMessages()
            }
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                showToast(it)
                viewModel.clearMessages()
            }
        }
    }

    private fun loadSubscriptionData() {
        if (shopId.isBlank()) {
            uiState = uiState.copy(errorMessage = "No shop selected")
            return
        }

        uiState = uiState.copy(errorMessage = null)
        viewModel.getShopActiveSubscription(shopId)
        viewModel.loadSubscriptionPackages()
        viewModel.getPaymentHistory(shopId)
    }

    private fun selectPlan(packageId: String) {
        if (shopId.isBlank()) {
            showToast("No shop selected")
            return
        }

        val selectedPackage = packageDtos.firstOrNull { it.id == packageId }
        if (selectedPackage == null) {
            showToast("That plan is no longer available. Refresh and try again.")
            return
        }

        if (selectedPackage.isCustomPlan) {
            openContactSales()
        } else {
            showPaymentDialog(selectedPackage)
        }
    }

    private fun showPaymentDialog(packageDto: PackageDto) {
        val dialog = PaymentDialogFragment.newInstance(
            shopId = shopId,
            packageId = packageDto.id,
            packageName = packageDto.displayName,
            amount = packageDto.monthlyAmount,
        )

        dialog.setPaymentListener { phoneNumber, packageId, months, _ ->
            viewModel.createSubscription(
                shopId = shopId,
                packageIds = listOf(packageId),
                phoneNumber = phoneNumber,
                months = months,
            )
        }

        dialog.show(parentFragmentManager, PaymentDialogFragment.TAG)
    }

    private fun cancelCurrentSubscription() {
        val subscription = currentSubscription
        if (subscription == null || shopId.isBlank()) {
            showToast("No active subscription")
            return
        }
        viewModel.cancelSubscription(shopId, subscription.id)
    }

    private fun openContactSales() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:sales@osebo.ai".toUri()
            putExtra(Intent.EXTRA_SUBJECT, "Custom Plan Inquiry")
            putExtra(Intent.EXTRA_TEXT, buildString {
                append("Hello,\n\n")
                append("I'm interested in a custom subscription plan for my shop.\n\n")
                append("Shop ID: $shopId\n")
                append("Please contact me with more information.")
            })
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            showToast("No email app found")
        }
    }

    private fun updateLoadingState() {
        uiState = uiState.copy(isLoading = currentSubscriptionLoading || plansLoading)
    }

    private fun getCurrentShopId(): String {
        val prefs = requireContext().getSharedPreferences("OseboPrefs", Context.MODE_PRIVATE)
        return prefs.getString("current_shop_id", "") ?: ""
    }

    private fun showToast(message: String) {
        if (isAdded) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        viewModel.stopPaymentPolling()
        super.onDestroyView()
    }
}

private fun Subscription.toUi() = SubscriptionCurrentPlanUi(
    id = id,
    name = displayPackage,
    status = displayStatus,
    duration = durationText,
    expiry = formattedEndDate,
    daysRemaining = displayDaysRemaining,
    amount = monthlyPrice,
    canCancel = isActiveStatus || isTrial,
)

private fun SubscriptionPackage.toUi() = SubscriptionPlanUi(
    id = id,
    name = displayName,
    tier = tier,
    description = description,
    price = displayPrice,
    features = featureList,
    isPopular = isPopular,
    isCustom = isCustom,
    canTry = hasFreeTrial,
)

private fun Payment.toUi() = SubscriptionPaymentUi(
    id = id,
    title = description ?: "Subscription payment",
    date = (paymentDate ?: paidAt ?: createdAt).substringBefore("T"),
    method = displayMethod,
    amount = formattedAmount,
    status = displayStatus,
    isSuccessful = isSuccessful,
)
