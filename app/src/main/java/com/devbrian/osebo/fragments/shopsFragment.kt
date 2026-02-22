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
import dagger.hilt.android.AndroidEntryPoint
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.databinding.FragmentShopsBinding
import com.devbrian.osebo.databinding.ItemShopBinding
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.ui.MainActivity
import com.devbrian.osebo.ui.ShopViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

@AndroidEntryPoint
class ShopsFragment : Fragment() {
    private var _binding: FragmentShopsBinding? = null
    private val binding get() = _binding!!

    private lateinit var shopsAdapter: MyShopsAdapter
    private lateinit var preferencesManager: PreferenceManager
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

        fun addShop(shop: Shop) {
            shops.add(shop)
            notifyItemInserted(shops.size - 1)
        }

        fun updateShop(updatedShop: Shop) {
            val index = shops.indexOfFirst { it.id == updatedShop.id }
            if (index != -1) {
                shops[index] = updatedShop
                notifyItemChanged(index)
            }
        }

        fun removeShop(shopId: String) {
            val index = shops.indexOfFirst { it.id == shopId }
            if (index != -1) {
                shops.removeAt(index)
                notifyItemRemoved(index)
            }
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
                    // Shop basic info
                    tvShopName.text = shop.name
                    tvShopDescription.text = shop.description ?: "No description available"
                    tvShopLocation.text = shop.address ?: "Location not set"
                    tvShopCategory.text = shop.shopType ?: "General"

                    // Format currency values with thousand separators
                    tvRevenue.text = String.format("UGX %,.0f", shop.totalRevenue)
                    tvExpenses.text = String.format("UGX %,.0f", shop.totalExpenses)
                    tvProfit.text = String.format("UGX %,.0f", shop.profit)

                    // Set product and employee counts
                    tvProductsCount.text = shop.totalProducts.toString()
                    tvEmployeesCount.text = shop.totalEmployees.toString()

                    // Set shop logo
                    if (!shop.logoUrl.isNullOrEmpty()) {
                        Glide.with(root.context)
                            .load(shop.logoUrl)
                            .placeholder(R.drawable.ic_shop_placeholder)
                            .into(ivShopLogo)
                    } else {
                        ivShopLogo.setImageResource(R.drawable.ic_shop_placeholder)
                    }

                    // Highlight active shop
                    val currentShopId = preferencesManager.getCurrentShopId()
                    val isActive = shop.id == currentShopId

                    if (isActive) {
                        // This shop is the active one
                        root.setBackgroundResource(R.drawable.bg_active_shop)
                        tvActiveBadge.visibility = View.VISIBLE
                        layoutActions.visibility = View.VISIBLE
                        btnSetActive.visibility = View.GONE
                        btnEdit.visibility = View.VISIBLE
                        btnDelete.visibility = View.VISIBLE
                    } else {
                        // This is not the active shop
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

        preferencesManager = PreferenceManager.getInstance(requireContext())
        shopViewModel = ViewModelProvider(this).get(ShopViewModel::class.java)

        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        loadUserShops()
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
                    binding.tvEmptyMessage.visibility = View.GONE
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false

                    val shops = resource.data ?: emptyList()
                    if (shops.isEmpty()) {
                        binding.tvEmptyMessage.visibility = View.VISIBLE
                        binding.tvEmptyMessage.text = "You haven't created any shops yet"
                        binding.rvShops.visibility = View.GONE
                    } else {
                        binding.tvEmptyMessage.visibility = View.GONE
                        binding.rvShops.visibility = View.VISIBLE
                        shopsAdapter.submitList(shops)
                    }

                    // Update shop count
                    binding.tvShopCount.text = "${shops.size} shops"

                    // Set active shop
                    val currentShopId = preferencesManager.getCurrentShopId()
                    shopsAdapter.setActiveShopId(currentShopId)
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.tvEmptyMessage.visibility = View.VISIBLE
                    binding.tvEmptyMessage.text = "Failed to load shops: ${resource.message}"
                    binding.rvShops.visibility = View.GONE

                    Toast.makeText(requireContext(), "Failed to load shops", Toast.LENGTH_SHORT).show()
                }
            }
        }

        shopViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

        shopViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddShop.setOnClickListener {
            navigateToAddShop()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadUserShops()
        }
    }

    private fun loadUserShops() {
        shopViewModel.loadShops()
    }

    private fun navigateToAddShop() {
        Toast.makeText(requireContext(), "Navigate to Add Shop", Toast.LENGTH_SHORT).show()
        // TODO: Implement navigation to AddShopFragment
    }

    private fun navigateToEditShop(shop: Shop) {
        Toast.makeText(requireContext(), "Navigate to Edit ${shop.name}", Toast.LENGTH_SHORT).show()
        // TODO: Implement navigation to EditShopFragment
    }

    private fun navigateToShopDetail(shop: Shop) {
        debugShopData(shop)
        println("🔍 ===== SELECTING SHOP =====")
        println("🔍 ===== SELECTING SHOP =====")
        println("🔍 Shop ID: ${shop.id}")
        println("🔍 Shop Name: ${shop.name}")
        println("🔍 Subscription object exists: ${shop.subscription != null}")

        // Save basic shop data
        preferencesManager.saveCurrentShopId(shop.id)
        preferencesManager.saveCurrentShopName(shop.name)
        preferencesManager.saveHasShop(true)

        // Save subscription info if it exists
        shop.subscription?.let { subscription ->
            println("🔍 Subscription found:")
            println("  - id: ${subscription.id}")
            println("  - status: ${subscription.status}")
            println("  - isTrial: ${subscription.isTrial}")
            println("  - packageType: ${subscription.packageType}")
            println("  - endsAt: ${subscription.endsAt}")

            val status = if (subscription.isTrial) "TRIAL" else subscription.status.uppercase()

            preferencesManager.saveSubscriptionInfo(
                subscriptionId = subscription.id,
                status = status,
                type = subscription.packageType,
                expiry = subscription.endsAt,
                packageId = subscription.subscriptionPackage?.id
            )
            println("✅ Subscription saved to preferences")
        } ?: run {
            println("⚠️ No subscription found for shop")
            preferencesManager.clearSubscriptionInfo()
        }

        // Verify what was saved
        println("🔍 After save - hasActiveSubscription: ${preferencesManager.hasActiveSubscription()}")
        println("🔍 After save - subscriptionStatus: ${preferencesManager.getSubscriptionStatus()}")
        println("🔍 After save - subscriptionId: ${preferencesManager.getSubscriptionId()}")

        // Update MainActivity header and refresh menu
        activity?.let {
            if (it is MainActivity) {
                it.updateHeaderShopInfo(shop.name)
                it.refreshNavigationMenu()
            }
        }

        shopsAdapter.setActiveShopId(shop.id)

        Toast.makeText(
            requireContext(),
            "${shop.name} selected as active shop",
            Toast.LENGTH_SHORT
        ).show()

        // Navigate to dashboard
        try {
            findNavController().navigate(R.id.dashboardFragment)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setActiveShop(shop: Shop) {
        // Save basic shop data
        preferencesManager.saveCurrentShopId(shop.id)
        preferencesManager.saveCurrentShopName(shop.name)
        preferencesManager.saveHasShop(true)

        // ✅ Save subscription info if it exists
        shop.subscription?.let { subscription ->
            preferencesManager.saveSubscriptionInfo(
                subscriptionId = subscription.id,
                status = if (subscription.isTrial) "TRIAL" else "ACTIVE",
                type = subscription.packageType,
                expiry = subscription.endsAt,
                packageId = subscription.subscriptionPackage?.id
            )
            println("✅ Saved subscription: ${subscription.status}")
        } ?: run {
            preferencesManager.clearSubscriptionInfo()
            println("⚠️ No subscription found for shop")
        }

        // Update MainActivity header and refresh menu
        activity?.let {
            if (it is MainActivity) {
                it.updateHeaderShopInfo(shop.name)
                it.refreshNavigationMenu()
            }
        }

        shopsAdapter.setActiveShopId(shop.id)

        Toast.makeText(
            requireContext(),
            "${shop.name} is now your active shop",
            Toast.LENGTH_SHORT
        ).show()
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
        println("  id: ${shop.id}")
        println("  name: ${shop.name}")
        println("  subscription present: ${shop.subscription != null}")

        // Try to access subscription fields safely
        try {
            val subscription = shop.subscription
            if (subscription != null) {
                println("  ✅ SUBSCRIPTION FOUND:")
                println("    - id: ${subscription.id}")
                println("    - status: ${subscription.status}")
                println("    - isTrial: ${subscription.isTrial}")
                println("    - packageType: ${subscription.packageType}")
                println("    - endsAt: ${subscription.endsAt}")
                println("    - subscriptionPackage present: ${subscription.subscriptionPackage != null}")
            } else {
                println("  ❌ subscription is NULL")

                // Try reflection to see all fields
                println("  All fields in shop object:")
                shop::class.java.declaredFields.forEach { field ->
                    field.isAccessible = true
                    try {
                        println("    - ${field.name}: ${field.get(shop)}")
                    } catch (e: Exception) {
                        println("    - ${field.name}: [error: ${e.message}]")
                    }
                }
            }
        } catch (e: Exception) {
            println("  ❌ Error accessing subscription: ${e.message}")
            e.printStackTrace()
        }
        println("🔍 ===== END DEBUG =====\n")
    }




}