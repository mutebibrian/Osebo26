package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.OnShopClickListener
import com.devbrian.osebo.adapters.ShopsAdapter
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.databinding.FragmentShopsBinding
import com.devbrian.osebo.ui.MainActivity
import com.devbrian.osebo.ui.ShopViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopsFragment : Fragment() {

    private var _binding: FragmentShopsBinding? = null
    private val binding get() = _binding!!

    private lateinit var shopsAdapter: ShopsAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var shopViewModel: ShopViewModel

    private val shopClickListener = object : OnShopClickListener {
        override fun onShopClick(shop: Shop) {
            if (shop.isSubscriptionActive) {
                navigateToShopDashboard(shop)
            } else {
                showSubscriptionRequiredDialog(shop)
            }
        }

        override fun onEditClick(shop: Shop) {
            navigateToEditShop(shop)
        }

        override fun onDeleteClick(shop: Shop) {
            showDeleteConfirmationDialog(shop)
        }

        override fun onSetActiveClick(shop: Shop) {
            setActiveShop(shop)
        }

        override fun onSubscribeClick(shop: Shop) {
            navigateToSubscriptionPackages(shop)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager.getInstance(requireContext())
        shopViewModel = ViewModelProvider(this).get(ShopViewModel::class.java)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        loadUserShops()

        preferenceManager.debugCurrentShop()
    }

    private fun setupRecyclerView() {
        shopsAdapter = ShopsAdapter(shopClickListener, requireContext())
        binding.rvShops.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shopsAdapter
            setHasFixedSize(true)
        }

        val currentShopId = preferenceManager.getCurrentShopId()
        if (currentShopId.isNotEmpty()) {
            shopsAdapter.setActiveShopId(currentShopId)
        }
    }

    private fun setupObservers() {
        shopViewModel.shops.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.emptyStateLayout.visibility = View.GONE
                    binding.rvShops.visibility = View.GONE
                    binding.fabAddShop.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false

                    val shops = resource.data ?: emptyList()
                    binding.tvShopCount.text = "${shops.size} shops"

                    if (shops.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.rvShops.visibility = View.GONE
                        binding.fabAddShop.visibility = View.GONE
                        binding.tvEmptyMessage.text = "You haven't created any shops yet"
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.rvShops.visibility = View.VISIBLE
                        binding.fabAddShop.visibility = View.VISIBLE
                        shopsAdapter.submitList(shops)

                        val currentShopId = preferenceManager.getCurrentShopId()
                        if (currentShopId.isNotEmpty()) {
                            shopsAdapter.setActiveShopId(currentShopId)
                        }
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.rvShops.visibility = View.GONE
                    binding.fabAddShop.visibility = View.GONE
                    binding.tvEmptyMessage.text = "Failed to load shops: ${resource.message}"
                    Toast.makeText(requireContext(), "Failed to load shops", Toast.LENGTH_SHORT).show()
                }
            }
        }

        shopViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        shopViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddShop.setOnClickListener {
            navigateToShopCreation()
        }

        binding.btnCreateFirstShop.setOnClickListener {
            navigateToShopCreation()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadUserShops()
        }
    }

    private fun loadUserShops() {
        shopViewModel.loadShops()
    }

    private fun navigateToShopCreation() {
        val intent = android.content.Intent(requireContext(), com.devbrian.osebo.ui.ShopCreationActivity::class.java)
        intent.putExtra("USER_ID", preferenceManager.getUserId())
        intent.putExtra("EMAIL", preferenceManager.getUserEmail())
        intent.putExtra("PHONE", preferenceManager.getUserPhone())
        intent.putExtra("FIRST_NAME", preferenceManager.getFirstName())
        intent.putExtra("LAST_NAME", preferenceManager.getLastName())
        startActivity(intent)
    }

    private fun navigateToEditShop(shop: Shop) {
        Toast.makeText(requireContext(), "Edit ${shop.name}", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToShopDashboard(shop: Shop) {
        try {
            val action = ShopsFragmentDirections.actionShopsFragmentToShopDashboardFragment(shop.id)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToSubscriptionPackages(shop: Shop) {
        try {
            val action = ShopsFragmentDirections.actionShopsFragmentToSubscriptionPackagesFragment(shop)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error navigating to subscriptions", Toast.LENGTH_SHORT).show()
        }
    }

    // ===== FIXED: setActiveShop saves subscription info and refreshes navigation =====
    private fun setActiveShop(shop: Shop) {
        if (shop.id.isBlank() || !Shop.isValidUUID(shop.id)) {
            Toast.makeText(requireContext(), "Error: Invalid shop ID", Toast.LENGTH_LONG).show()
            return
        }

        // Save shop
        preferenceManager.saveCurrentShop(
            shopId = shop.id,
            shopUuid = shop.id,
            shopName = shop.name
        )

        // Save subscription info if active
        if (shop.isSubscriptionActive) {
            val status = if (shop.subscription?.isTrial == true) "TRIAL" else "ACTIVE"
            preferenceManager.saveSubscriptionInfo(
                subscriptionId = shop.subscription?.id,
                status = status,
                type = shop.subscription?.packageType,
                expiry = shop.subscription?.endsAt,
                packageId = shop.planId   // ✅ FIXED: using planId instead of subscriptionPackage?.id
            )
        } else {
            preferenceManager.clearSubscriptionInfo()
        }

        // Update UI in MainActivity
        (activity as? MainActivity)?.apply {
            updateHeaderShopInfo(shop.name)
            refreshNavigationMenu()  // <-- This refreshes the drawer
        }

        shopsAdapter.setActiveShopId(shop.id)
        Toast.makeText(requireContext(), "${shop.name} is now your active shop", Toast.LENGTH_SHORT).show()
    }

    private fun showSubscriptionRequiredDialog(shop: Shop) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Subscription Required")
            .setMessage("${shop.name} doesn't have an active subscription. Would you like to subscribe now to start using the app?")
            .setPositiveButton("Subscribe Now") { _, _ ->
                navigateToSubscriptionPackages(shop)
            }
            .setNegativeButton("Later", null)
            .setCancelable(false)
            .show()
    }

    private fun showDeleteConfirmationDialog(shop: Shop) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete ${shop.name}")
            .setMessage("Are you sure you want to delete this shop? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                shopViewModel.deleteShop(shop.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}