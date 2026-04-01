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
import com.bumptech.glide.Glide
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.databinding.FragmentShopsBinding
import com.devbrian.osebo.databinding.ItemShopBinding
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.ui.MainActivity
import com.devbrian.osebo.ui.ShopViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopsFragment : Fragment() {
    private var _binding: FragmentShopsBinding? = null
    private val binding get() = _binding!!

    private lateinit var shopsAdapter: MyShopsAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var shopViewModel: ShopViewModel

    interface OnShopClickListener {
        fun onShopClick(shop: Shop)
        fun onEditClick(shop: Shop)
        fun onDeleteClick(shop: Shop)
        fun onSetActiveClick(shop: Shop)
    }

    inner class MyShopsAdapter(private val listener: OnShopClickListener) :
        androidx.recyclerview.widget.RecyclerView.Adapter<MyShopsAdapter.ShopViewHolder>() {

        private var shops = mutableListOf<Shop>()
        private var activeShopId: String? = null

        fun setActiveShopId(shopId: String?) {
            activeShopId = shopId
            notifyDataSetChanged()
        }

        fun submitList(newShops: List<Shop>) {
            shops.clear()
            shops.addAll(newShops)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
            val binding = ItemShopBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ShopViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
            holder.bind(shops[position])
        }

        override fun getItemCount(): Int = shops.size

        inner class ShopViewHolder(
            private val binding: ItemShopBinding
        ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

            init {
                binding.root.setOnClickListener {
                    val position = adapterPosition
                    if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                        listener.onShopClick(shops[position])
                    }
                }

                binding.btnEdit.setOnClickListener {
                    val position = adapterPosition
                    if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                        listener.onEditClick(shops[position])
                    }
                }

                binding.btnDelete.setOnClickListener {
                    val position = adapterPosition
                    if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(shops[position])
                    }
                }

                binding.btnSetActive.setOnClickListener {
                    val position = adapterPosition
                    if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                        listener.onSetActiveClick(shops[position])
                    }
                }
            }

            fun bind(shop: Shop) {
                binding.apply {
                    tvShopName.text = shop.name
                    tvShopDescription.text = shop.description ?: "No description available"
                    tvShopLocation.text = shop.address ?: "Location not set"
                    tvShopCategory.text = shop.shopType ?: "General"

                    if (!shop.logoUrl.isNullOrEmpty()) {
                        Glide.with(root.context)
                            .load(shop.logoUrl)
                            .placeholder(R.drawable.ic_shop_placeholder)
                            .into(ivShopLogo)
                    } else {
                        ivShopLogo.setImageResource(R.drawable.ic_shop_placeholder)
                    }

                    // Check if this shop is the active one
                    val currentShopId = preferenceManager.getCurrentShopId()
                    val isActive = shop.id == currentShopId

                    if (isActive) {
                        root.setBackgroundResource(R.drawable.bg_active_shop)
                        tvActiveBadge.visibility = View.VISIBLE
                        layoutActions.visibility = View.VISIBLE
                        btnSetActive.visibility = View.GONE
                        btnEdit.visibility = View.VISIBLE
                        btnDelete.visibility = View.VISIBLE
                    } else {
                        root.setBackgroundResource(android.R.color.transparent)
                        tvActiveBadge.visibility = View.GONE
                        layoutActions.visibility = View.VISIBLE
                        btnSetActive.visibility = View.VISIBLE
                        btnEdit.visibility = View.GONE
                        btnDelete.visibility = View.GONE
                    }
                }
            }
        }
    }

    private val shopClickListener = object : OnShopClickListener {
        override fun onShopClick(shop: Shop) {
            navigateToShopDetail(shop)
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

        // Initialize PreferenceManager
        preferenceManager = PreferenceManager.getInstance(requireContext())
        shopViewModel = ViewModelProvider(this).get(ShopViewModel::class.java)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        loadUserShops()

        // Debug current shop info
        preferenceManager.debugCurrentShop()
    }

    private fun setupRecyclerView() {
        shopsAdapter = MyShopsAdapter(shopClickListener)
        binding.rvShops.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shopsAdapter
            setHasFixedSize(true)
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
                        shopsAdapter.setActiveShopId(currentShopId)
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
        Toast.makeText(requireContext(), "Navigate to Edit ${shop.name}", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToShopDetail(shop: Shop) {
        debugShopData(shop)

        // Defensive check for valid UUID
        if (shop.id.isBlank() || !Shop.isValidUUID(shop.id)) {
            Toast.makeText(requireContext(), "Error: Selected shop has an invalid ID.", Toast.LENGTH_LONG).show()
            println("❌ CRITICAL: Attempted to select a shop with an invalid ID: ${shop.name} (${shop.id})")
            return
        }

        println("✅ Selecting shop '${shop.name}' with valid ID: ${shop.id}")

        // CRITICAL FIX: Save BOTH the shop ID and UUID using the correct method
        // The shop.id is the UUID (same value for both)
        preferenceManager.saveCurrentShop(
            shopId = shop.id,      // For local database
            shopUuid = shop.id,    // For API calls
            shopName = shop.name
        )

        // Handle subscription
        if (shop.subscription != null && shop.subscription.isActive) {
            val subscription = shop.subscription
            val status = if (subscription.isTrial) "TRIAL" else "ACTIVE"

            preferenceManager.saveSubscriptionInfo(
                subscriptionId = subscription.id,
                status = status,
                type = subscription.packageType,
                expiry = subscription.endsAt,
                packageId = subscription.subscriptionPackage?.id
            )
            println("✅ Subscription saved to preferences with status: $status")

            (activity as? MainActivity)?.updateHeaderShopInfo(shop.name)
            shopsAdapter.setActiveShopId(shop.id)

            Toast.makeText(requireContext(), "${shop.name} selected", Toast.LENGTH_SHORT).show()

            // Navigate to dashboard
            try {
                findNavController().navigate(ShopsFragmentDirections.actionShopsFragmentToShopDashboardFragment(shop.id))
            } catch (e: Exception) {
                println("❌ Navigation Error: ${e.message}")
            }
        } else {
            // Clear old subscription and show dialog
            preferenceManager.clearSubscriptionInfo()
            showSubscriptionRequiredDialog(shop)
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

    private fun setActiveShop(shop: Shop) {
        // Defensive check
        if (shop.id.isBlank() || !Shop.isValidUUID(shop.id)) {
            Toast.makeText(requireContext(), "Error: Cannot set active shop due to invalid ID.", Toast.LENGTH_LONG).show()
            return
        }

        println("✅ Setting '${shop.name}' as active shop with ID: ${shop.id}")

        // CRITICAL FIX: Save BOTH the shop ID and UUID using the correct method
        preferenceManager.saveCurrentShop(
            shopId = shop.id,      // For local database
            shopUuid = shop.id,    // For API calls
            shopName = shop.name
        )

        if (shop.subscription != null && shop.subscription.isActive) {
            val subscription = shop.subscription
            val status = if (subscription.isTrial) "TRIAL" else "ACTIVE"

            preferenceManager.saveSubscriptionInfo(
                subscriptionId = subscription.id,
                status = status,
                type = subscription.packageType,
                expiry = subscription.endsAt,
                packageId = subscription.subscriptionPackage?.id
            )
            println("✅ Saved subscription with status: $status")
        } else {
            println("⚠️ No active subscription found for shop, clearing local subscription info.")
            preferenceManager.clearSubscriptionInfo()
        }

        (activity as? MainActivity)?.updateHeaderShopInfo(shop.name)
        shopsAdapter.setActiveShopId(shop.id)
        Toast.makeText(requireContext(), "${shop.name} is now your active shop", Toast.LENGTH_SHORT).show()

        if (shop.subscription == null || !shop.subscription.isActive) {
            showSubscriptionRequiredDialog(shop)
        }
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

    private fun debugShopData(shop: Shop) {
        println("🔍 ===== DEBUG SHOP DATA =====")
        println("  id (UUID): ${shop.id}")
        println("  name: ${shop.name}")
        println("  subscription present: ${shop.subscription != null}")
        shop.subscription?.let {
            println("    - isActive: ${it.isActive}")
            println("    - isTrial: ${it.isTrial}")
        }
        println("🔍 ===== END DEBUG =====\n")
    }
}