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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.ui.MainActivity
import com.devbrian.osebo.ui.ShopCreationActivity
import com.devbrian.osebo.ui.ShopViewModel
import com.devbrian.osebo.ui.screens.ShopItemUi
import com.devbrian.osebo.ui.screens.ShopsScreen
import com.devbrian.osebo.ui.screens.ShopsUiState
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

class ShopsFragment : Fragment() {
    private val shopViewModel: ShopViewModel by viewModel()
    private lateinit var preferenceManager: PreferenceManager
    private var uiState by mutableStateOf(ShopsUiState())
    private var shopsById: Map<String, Shop> = emptyMap()
    private var refreshOnNextResume = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        preferenceManager = PreferenceManager.getInstance(requireContext())

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OseboTheme {
                    ShopsScreen(
                        state = uiState,
                        onAddShopClick = ::navigateToShopCreation,
                        onRefreshClick = shopViewModel::loadShops,
                        onShopClick = ::openShop,
                        onSubscribeClick = ::openSubscription,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        shopViewModel.loadShops()
        preferenceManager.debugCurrentShop()
    }

    override fun onResume() {
        super.onResume()
        if (refreshOnNextResume) {
            shopViewModel.loadShops()
        }
        refreshOnNextResume = true
    }

    private fun setupObservers() {
        shopViewModel.shops.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    uiState = uiState.copy(
                        isLoading = uiState.shops.isEmpty(),
                        isRefreshing = uiState.shops.isNotEmpty(),
                        errorMessage = null,
                    )
                }

                is Resource.Success -> {
                    val shops = resource.data
                    shopsById = shops.associateBy(Shop::id)
                    val currentShopId = preferenceManager.getCurrentShopId()
                    uiState = uiState.copy(
                        shops = shops.map { it.toShopItemUi(currentShopId) },
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = null,
                    )
                }

                is Resource.Error -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = resource.message,
                    )
                }
            }
        }

        shopViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            uiState = uiState.copy(
                isLoading = isLoading && uiState.shops.isEmpty(),
                isRefreshing = isLoading && uiState.shops.isNotEmpty(),
            )
        }

        shopViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank() && uiState.shops.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun Shop.toShopItemUi(currentShopId: String) = ShopItemUi(
        id = id,
        name = name.ifBlank { "Unnamed shop" },
        type = shopTypeDisplay,
        location = fullAddress.ifBlank { "Location not set" },
        isSubscriptionActive = isSubscriptionActive,
        isTrial = subscriptionStatus.equals("trial", ignoreCase = true) ||
            subscription?.isTrial == true,
        isCurrent = id == currentShopId,
    )

    private fun openShop(shopId: String) {
        val shop = shopsById[shopId] ?: return
        if (!shop.isSubscriptionActive) {
            showSubscriptionRequiredDialog(shop)
            return
        }

        setActiveShop(shop)
        navigateToShopDashboard(shop)
    }

    private fun openSubscription(shopId: String) {
        shopsById[shopId]?.let(::navigateToSubscriptionPackages)
    }

    private fun navigateToShopCreation() {
        val intent = Intent(requireContext(), ShopCreationActivity::class.java).apply {
            putExtra("USER_ID", preferenceManager.getUserId())
            putExtra("EMAIL", preferenceManager.getUserEmail())
            putExtra("PHONE", preferenceManager.getUserPhone())
            putExtra("FIRST_NAME", preferenceManager.getFirstName())
            putExtra("LAST_NAME", preferenceManager.getLastName())
        }
        startActivity(intent)
    }

    private fun navigateToShopDashboard(shop: Shop) {
        runCatching {
            val action = ShopsFragmentDirections.actionShopsFragmentToShopDashboardFragment(shop.id)
            findNavController().navigate(action)
        }.onFailure {
            Toast.makeText(requireContext(), "Unable to open this shop", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToSubscriptionPackages(shop: Shop) {
        runCatching {
            val action = ShopsFragmentDirections.actionShopsFragmentToSubscriptionPackagesFragment(shop)
            findNavController().navigate(action)
        }.onFailure {
            Toast.makeText(requireContext(), "Unable to open subscriptions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setActiveShop(shop: Shop) {
        if (shop.id.isBlank() || !Shop.isValidUUID(shop.id)) {
            Toast.makeText(requireContext(), "This shop has an invalid ID", Toast.LENGTH_LONG).show()
            return
        }

        preferenceManager.saveCurrentShop(
            shopId = shop.id,
            shopUuid = shop.effectiveUuid.ifBlank { shop.id },
            shopName = shop.name,
        )

        if (shop.isSubscriptionActive) {
            val status = if (
                shop.subscription?.isTrial == true ||
                shop.subscriptionStatus.equals("trial", ignoreCase = true)
            ) "TRIAL" else "ACTIVE"

            preferenceManager.saveSubscriptionInfo(
                subscriptionId = shop.subscription?.id,
                status = status,
                type = shop.subscription?.packageType ?: shop.subscriptionType,
                expiry = shop.subscription?.endsAt ?: shop.subscriptionExpiry,
                packageId = shop.planId,
            )
        }

        (activity as? MainActivity)?.apply {
            updateHeaderShopInfo(shop.name)
            refreshNavigationMenu()
        }

        uiState = uiState.copy(
            shops = uiState.shops.map { it.copy(isCurrent = it.id == shop.id) },
        )
        Toast.makeText(requireContext(), "${shop.name} is now your active shop", Toast.LENGTH_SHORT).show()
    }

    private fun showSubscriptionRequiredDialog(shop: Shop) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Subscription required")
            .setMessage("${shop.name} needs an active subscription before it can be opened.")
            .setPositiveButton("View plans") { _, _ -> navigateToSubscriptionPackages(shop) }
            .setNegativeButton("Later", null)
            .show()
    }
}
