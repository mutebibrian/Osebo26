package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.domain.model.Account
import com.devbrian.osebo.fragments.dialogs.PaymentMethodDialogFragment
import com.devbrian.osebo.ui.AccountViewModel
import com.devbrian.osebo.ui.screens.AccountConfirmation
import com.devbrian.osebo.ui.screens.AccountProfileUi
import com.devbrian.osebo.ui.screens.AccountScreen
import com.devbrian.osebo.ui.screens.AccountScreenState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountFragment : Fragment() {

    private val viewModel: AccountViewModel by viewModel()
    private var screenState by mutableStateOf(AccountScreenState())
    private var confirmation by mutableStateOf<AccountConfirmation?>(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                AccountScreen(
                    state = screenState,
                    confirmation = confirmation,
                    onBack = { findNavController().navigateUp() },
                    onRefresh = viewModel::loadAccountData,
                    onEditAccount = ::openEditAccountDialog,
                    onUpdatePayment = ::openPaymentMethodDialog,
                    onTwoFactorChanged = ::updateTwoFactor,
                    onLoginNotificationsChanged = ::updateLoginNotifications,
                    onManageSessions = ::openSessionManagement,
                    onDeactivateAccount = ::showDeactivateConfirmation,
                    onDeleteAccount = ::showDeleteConfirmation,
                    onDismissConfirmation = { confirmation = null },
                    onConfirmAction = ::confirmAccountAction,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.setFragmentResultListener(
            EditAccountDialogFragment.RESULT_KEY,
            viewLifecycleOwner,
        ) { _, result ->
            screenState = screenState.copy(
                profile = screenState.profile?.copy(
                    businessName = result.getString(EditAccountDialogFragment.ARG_NAME).orEmpty(),
                    businessType = result.getString(EditAccountDialogFragment.ARG_TYPE).orEmpty(),
                    registrationNumber = result.getString(EditAccountDialogFragment.ARG_REGISTRATION).orEmpty(),
                    taxId = result.getString(EditAccountDialogFragment.ARG_TAX).orEmpty(),
                    address = result.getString(EditAccountDialogFragment.ARG_ADDRESS).orEmpty(),
                ),
            )
            showSnackbar("Account details updated")
        }
        childFragmentManager.setFragmentResultListener(
            PaymentMethodDialogFragment.RESULT_KEY,
            viewLifecycleOwner,
        ) { _, result ->
            screenState = screenState.copy(
                profile = screenState.profile?.copy(
                    paymentMethod = result.getString(PaymentMethodDialogFragment.ARG_METHOD).orEmpty(),
                ),
            )
            showSnackbar("Payment method updated")
        }

        viewModel.accountData.observe(viewLifecycleOwner) { account ->
            account ?: return@observe
            screenState = screenState.copy(profile = account.toUi())
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            screenState = screenState.copy(isLoading = isLoading)
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) showSnackbar("Account updated successfully")
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotBlank()) showSnackbar(error)
        }
    }

    private fun updateTwoFactor(enabled: Boolean) {
        screenState = screenState.copy(
            profile = screenState.profile?.copy(twoFactorEnabled = enabled),
        )
        viewModel.updateTwoFactorAuth(enabled)
    }

    private fun updateLoginNotifications(enabled: Boolean) {
        screenState = screenState.copy(
            profile = screenState.profile?.copy(loginNotificationsEnabled = enabled),
        )
        viewModel.updateLoginNotifications(enabled)
    }

    private fun openEditAccountDialog() {
        val profile = screenState.profile ?: return
        EditAccountDialogFragment.newInstance(profile).show(childFragmentManager, "EditAccountDialog")
    }

    private fun openPaymentMethodDialog() {
        PaymentMethodDialogFragment.newInstance(screenState.profile?.paymentMethod.orEmpty())
            .show(childFragmentManager, "PaymentMethodDialog")
    }

    private fun openSessionManagement() {
        findNavController().navigate(AccountFragmentDirections.actionAccountFragmentToSessionsFragment())
    }

    private fun showDeactivateConfirmation() {
        confirmation = AccountConfirmation.DEACTIVATE
    }

    private fun showDeleteConfirmation() {
        confirmation = AccountConfirmation.DELETE
    }

    private fun confirmAccountAction() {
        when (confirmation) {
            AccountConfirmation.DEACTIVATE -> viewModel.deactivateAccount()
            AccountConfirmation.DELETE -> viewModel.deleteAccount()
            null -> Unit
        }
        confirmation = null
    }

    private fun showSnackbar(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }
}

private fun Account.toUi() = AccountProfileUi(
    id = id,
    businessName = businessName,
    businessType = businessType,
    registrationNumber = registrationNumber,
    taxId = taxId,
    address = address,
    status = status,
    paymentMethod = paymentMethod,
    billingCycle = billingCycle,
    nextBillingDate = nextBillingDate,
    twoFactorEnabled = twoFactorEnabled,
    loginNotificationsEnabled = loginNotificationsEnabled,
)
