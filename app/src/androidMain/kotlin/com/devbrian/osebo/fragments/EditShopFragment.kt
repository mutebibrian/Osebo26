package com.devbrian.osebo.fragments

import android.os.Bundle
import android.util.Patterns
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
import com.devbrian.osebo.ui.screens.ShopFormMode
import com.devbrian.osebo.ui.screens.ShopFormScreen
import com.devbrian.osebo.ui.screens.ShopFormUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditShopFragment : Fragment() {
    private val args: EditShopFragmentArgs by navArgs()
    private lateinit var originalShop: Shop
    private lateinit var initialState: ShopFormUiState
    private var formState by mutableStateOf(ShopFormUiState(mode = ShopFormMode.Edit))

    private val businessTypes = listOf(
        "Retail Store",
        "Wholesale",
        "Service Business",
        "Manufacturing",
        "Restaurant",
        "Salon & Spa",
        "Grocery Store",
        "Pharmacy",
        "Hardware Store",
        "Fashion & Clothing",
        "Electronics Store",
        "Online Store",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        originalShop = args.shop
        initialState = stateFromShop(originalShop)
        formState = initialState
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                ShopFormScreen(
                    state = formState,
                    shopTypes = businessTypes,
                    onStateChange = { formState = it },
                    onBackClick = ::discardChanges,
                    onLogoClick = { showMessage("Logo upload coming soon") },
                    onContinue = ::validatePrimaryStep,
                    onSave = ::saveChanges,
                )
            }
        }
    }

    private fun stateFromShop(shop: Shop) = ShopFormUiState(
        mode = ShopFormMode.Edit,
        name = shop.name,
        address = shop.address.orEmpty(),
        type = shop.shopTypeDisplay,
        registrationNumber = shop.registrationNumber.orEmpty(),
        tin = shop.taxIdentificationNumber.orEmpty(),
        description = shop.description.orEmpty(),
        phone = shop.phone.orEmpty(),
        email = shop.email.orEmpty(),
        website = shop.website.orEmpty(),
        city = shop.city.orEmpty(),
        country = shop.country.orEmpty(),
        postalCode = shop.postalCode.orEmpty(),
        isActive = shop.isActive,
    )

    private fun validatePrimaryStep() {
        val state = formState
        val nameError = "Shop name is required".takeIf { state.name.trim().isEmpty() }
        val addressError = "Address is required".takeIf { state.address.trim().isEmpty() }
        val typeError = "Choose a business type".takeIf { state.type.isBlank() }
        val phoneError = "Phone number is required".takeIf { state.phone.trim().isEmpty() }
        val emailError = if (state.email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches()) {
            "Enter a valid email"
        } else {
            null
        }
        val hasError = listOf(nameError, addressError, typeError, phoneError, emailError).any { it != null }
        formState = state.copy(
            nameError = nameError,
            addressError = addressError,
            typeError = typeError,
            phoneError = phoneError,
            emailError = emailError,
            currentStep = if (hasError) 0 else 1,
            submitError = if (hasError) "Check the highlighted shop details." else null,
        )
    }

    private fun saveChanges() {
        validatePrimaryStep()
        if (formState.currentStep == 0) return

        originalShop = originalShop.copy(
            name = formState.name.trim(),
            address = formState.address.trim(),
            phone = formState.phone.trim(),
            email = formState.email.trim(),
            website = formState.website.trim(),
            registrationNumber = formState.registrationNumber.trim(),
            taxIdentificationNumber = formState.tin.trim(),
            city = formState.city.trim(),
            country = formState.country.trim(),
            postalCode = formState.postalCode.trim(),
            description = formState.description.trim(),
            shopType = businessTypeKey(formState.type),
            isActive = formState.isActive,
        )
        initialState = stateFromShop(originalShop)
        showMessage("Shop updated successfully")
        findNavController().navigateUp()
    }

    private fun businessTypeKey(displayName: String): String = when (displayName) {
        "Wholesale" -> "wholesale"
        "Service Business" -> "service"
        "Manufacturing" -> "manufacturing"
        "Restaurant" -> "restaurant"
        "Salon & Spa" -> "salon"
        "Grocery Store" -> "grocery"
        "Pharmacy" -> "pharmacy"
        "Hardware Store" -> "hardware"
        "Fashion & Clothing" -> "fashion"
        "Electronics Store" -> "electronics"
        "Online Store" -> "online"
        else -> "retail"
    }

    private fun discardChanges() {
        if (!hasChanges()) {
            findNavController().navigateUp()
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Discard changes")
            .setMessage("Your unsaved shop changes will be lost.")
            .setPositiveButton("Discard") { _, _ -> findNavController().navigateUp() }
            .setNegativeButton("Keep editing", null)
            .show()
    }

    private fun hasChanges(): Boolean = formState.copy(
        currentStep = initialState.currentStep,
        submitError = null,
        nameError = null,
        addressError = null,
        typeError = null,
        phoneError = null,
        emailError = null,
    ) != initialState

    private fun showMessage(message: String) {
        if (isAdded) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
