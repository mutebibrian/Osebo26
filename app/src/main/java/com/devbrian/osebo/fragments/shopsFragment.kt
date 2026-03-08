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

                    tvShopName.text = shop.name
                    tvShopDescription.text = shop.description ?: "No description available"
                    tvShopLocation.text = shop.address ?: "Location not set"
                    tvShopCategory.text = shop.shopType ?: "General"


                    tvRevenue.text = String.format("UGX %,.0f", shop.totalRevenue)
                    tvExpenses.text = String.format("UGX %,.0f", shop.totalExpenses)
                    tvProfit.text = String.format("UGX %,.0f", shop.profit)


                    tvProductsCount.text = shop.totalProducts.toString()
                    tvEmployeesCount.text = shop.totalEmployees.toString()


                    if (!shop.logoUrl.isNullOrEmpty()) {
                        Glide.with(root.context)
                            .load(shop.logoUrl)
                            .placeholder(R.drawable.ic_shop_placeholder)
                            .into(ivShopLogo)
                    } else {
                        ivShopLogo.setImageResource(R.drawable.ic_shop_placeholder)
                    }


                    val currentShopId = preferencesManager.getCurrentShopId()
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
                        // Show empty state with Create Shop button
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.rvShops.visibility = View.GONE
                        binding.fabAddShop.visibility = View.GONE
                        binding.tvEmptyMessage.text = "You haven't created any shops yet"
                    } else {
                        // Show shops list and FAB
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.rvShops.visibility = View.VISIBLE
                        binding.fabAddShop.visibility = View.VISIBLE
                        shopsAdapter.submitList(shops)

                        val currentShopId = preferencesManager.getCurrentShopId()
                        shopsAdapter.setActiveShopId(currentShopId)
                    }
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false

                    // Show error in empty state
                    binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.rvShops.visibility = View.GONE
                    binding.fabAddShop.visibility = View.GONE
                    binding.tvEmptyMessage.text = "Failed to load shops: ${resource.message}"

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
        // Navigate to ShopCreationActivity
        val intent = android.content.Intent(requireContext(), com.devbrian.osebo.ui.ShopCreationActivity::class.java)
        intent.putExtra("USER_ID", preferencesManager.getUserId())
        intent.putExtra("EMAIL", preferencesManager.getUserEmail())
        intent.putExtra("PHONE", preferencesManager.getUserPhone())
        intent.putExtra("FIRST_NAME", preferencesManager.getFirstName())
        intent.putExtra("LAST_NAME", preferencesManager.getLastName())
        startActivity(intent)
    }

    private fun navigateToEditShop(shop: Shop) {
        Toast.makeText(requireContext(), "Navigate to Edit ${shop.name}", Toast.LENGTH_SHORT).show()
        // TODO: Implement edit shop navigation
    }

    private fun navigateToShopDetail(shop: Shop) {
        debugShopData(shop)
        println("🔍 ===== SELECTING SHOP =====")
        println("🔍 Shop ID: ${shop.id}")
        println("🔍 Shop Name: ${shop.name}")
        println("🔍 Subscription object exists: ${shop.subscription != null}")

        // Save basic shop info
        preferencesManager.saveCurrentShopId(shop.id)
        preferencesManager.saveCurrentShopName(shop.name)
        preferencesManager.saveHasShop(true)

        // Handle subscription
        if (shop.subscription != null) {
            // Has subscription - save it and go to shop dashboard
            val subscription = shop.subscription
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

            // Update UI
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

            // Navigate to ShopDashboardFragment with shopId
            try {
                val action = ShopsFragmentDirections.actionShopsFragmentToShopDashboardFragment(shop.id)
                findNavController().navigate(action)
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to old navigation
                try {
                    findNavController().navigate(R.id.shopDashboardFragment)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
        } else {
            // No subscription - show dialog
            showSubscriptionRequiredDialog(shop)
        }
    }

    private fun navigateToSubscriptionPackages(shop: Shop) {
        try {
            // Use Safe Args to pass the shop object
            val action = ShopsFragmentDirections.actionShopsFragmentToSubscriptionPackagesFragment(shop)
            findNavController().navigate(action)
        } catch (e: Exception) {
            e.printStackTrace()
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
            .setNegativeButton("Later") { _, _ ->
                // Just stay on shops fragment
                Toast.makeText(
                    requireContext(),
                    "You can subscribe later from the shop menu",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setCancelable(false)
            .show()
    }
    private fun setActiveShop(shop: Shop) {
        preferencesManager.saveCurrentShopId(shop.id)
        preferencesManager.saveCurrentShopName(shop.name)
        preferencesManager.saveHasShop(true)

        if (shop.subscription != null) {
            // Has subscription
            val subscription = shop.subscription
            preferencesManager.saveSubscriptionInfo(
                subscriptionId = subscription.id,
                status = if (subscription.isTrial) "TRIAL" else "ACTIVE",
                type = subscription.packageType,
                expiry = subscription.endsAt,
                packageId = subscription.subscriptionPackage?.id
            )
            println("✅ Saved subscription: ${subscription.status}")

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
        } else {
            // No subscription
            println("⚠️ No subscription found for shop")
            preferencesManager.clearSubscriptionInfo()

            activity?.let {
                if (it is MainActivity) {
                    it.updateHeaderShopInfo(shop.name)
                    it.refreshNavigationMenu()
                }
            }

            shopsAdapter.setActiveShopId(shop.id)

            // Show dialog
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
        println("  id: ${shop.id}")
        println("  name: ${shop.name}")
        println("  subscription present: ${shop.subscription != null}")


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