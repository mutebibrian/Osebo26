package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager // Updated import
import com.devbrian.osebo.databinding.FragmentShopsBinding
import com.devbrian.osebo.models.Shop
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ShopsFragment : Fragment() {
    private var _binding: FragmentShopsBinding? = null
    private val binding get() = _binding!!

    private lateinit var shopsAdapter: MyShopsAdapter
    private lateinit var preferencesManager: PreferenceManager // Updated type

    // Define interface inside fragment
    interface OnShopClickListener {
        fun onShopClick(shop: Shop)
        fun onEditClick(shop: Shop)
        fun onDeleteClick(shop: Shop)
        fun onSetActiveClick(shop: Shop)
    }

    // Create the adapter as inner class
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
            val binding = com.devbrian.osebo.databinding.ItemShopBinding.inflate(
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
            private val binding: com.devbrian.osebo.databinding.ItemShopBinding
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
                    tvShopDescription.text = shop.description
                    tvShopLocation.text = shop.location
                    tvShopCategory.text = shop.category

                    // Format currency values
                    tvRevenue.text = String.format("KES %.2f", shop.totalRevenue)
                    tvExpenses.text = String.format("KES %.2f", shop.totalExpenses)
                    tvProfit.text = String.format("KES %.2f", shop.profit)

                    tvProductsCount.text = shop.totalProducts.toString()
                    tvEmployeesCount.text = shop.totalEmployees.toString()

                    // Set shop logo
                    if (!shop.logoUrl.isNullOrEmpty()) {
                        com.bumptech.glide.Glide.with(root.context)
                            .load(shop.logoUrl)
                            .placeholder(R.drawable.ic_shop_placeholder)
                            .into(ivShopLogo)
                    } else {
                        ivShopLogo.setImageResource(R.drawable.ic_shop_placeholder)
                    }

                    // Highlight active shop
                    val isActive = shop.id == activeShopId
                    if (isActive) {
                        root.setBackgroundResource(R.drawable.bg_active_shop)
                        tvActiveBadge.visibility = View.VISIBLE
                        btnSetActive.visibility = View.GONE
                    } else {
                        root.setBackgroundResource(R.drawable.bg_shop_item)
                        tvActiveBadge.visibility = View.GONE
                        btnSetActive.visibility = View.VISIBLE
                    }

                    // Show/hide action buttons
                    val currentShopId = preferencesManager.getCurrentShopId()
                    val showActions = currentShopId == shop.id
                    layoutActions.visibility = if (showActions) View.VISIBLE else View.GONE
                }
            }
        }
    }

    // Create click listener
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

        // Initialize PreferenceManager using singleton instance
        preferencesManager = PreferenceManager.getInstance(requireContext())
        setupRecyclerView()
        setupClickListeners()
        loadSampleShops()
    }

    private fun setupRecyclerView() {
        shopsAdapter = MyShopsAdapter(shopClickListener)
        binding.rvShops.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shopsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.fabAddShop.setOnClickListener {
            navigateToAddShop()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadSampleShops()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun loadSampleShops() {
        val sampleShops = listOf(
            Shop(
                id = "1",
                name = "Main Electronics Store",
                description = "Electronics and gadgets",
                location = "Ntinda, CBD",
                category = "Electronics",
                totalRevenue = 150000.0,
                totalExpenses = 45000.0,
                totalProducts = 120,
                totalEmployees = 5,
                profit = 105000.0,
                logoUrl = "" // Add empty string for logoUrl
            ),
            Shop(
                id = "2",
                name = "Fashion Boutique",
                description = "Clothing and accessories",
                location = "Kampala",
                category = "Fashion",
                totalRevenue = 75000.0,
                totalExpenses = 25000.0,
                totalProducts = 200,
                totalEmployees = 3,
                profit = 50000.0,
                logoUrl = "" // Add empty string for logoUrl
            )
        )

        shopsAdapter.submitList(sampleShops)
        binding.progressBar.visibility = View.GONE

        // Set the active shop if one is already selected
        val currentShopId = preferencesManager.getCurrentShopId()
        shopsAdapter.setActiveShopId(currentShopId)
    }

    private fun navigateToAddShop() {
        Toast.makeText(requireContext(), "Add Shop", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToEditShop(shop: Shop) {
        Toast.makeText(requireContext(), "Edit ${shop.name}", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToShopDetail(shop: Shop) {
        // Save the shop as active
        preferencesManager.saveCurrentShopId(shop.id)
        preferencesManager.saveCurrentShopName(shop.name)

        // Update MainActivity header if available
        activity?.let {
            if (it is com.devbrian.osebo.ui.MainActivity) {
                it.updateHeaderShopInfo(shop.name)
            }
        }

        // Update adapter to highlight active shop
        shopsAdapter.setActiveShopId(shop.id)

        Toast.makeText(
            requireContext(),
            "${shop.name} selected as active shop",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showDeleteConfirmationDialog(shop: Shop) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete ${shop.name}")
            .setMessage("Are you sure you want to delete this shop?")
            .setPositiveButton("Delete") { _, _ ->
                Toast.makeText(requireContext(), "Shop deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setActiveShop(shop: Shop) {
        // Save the shop as active
        preferencesManager.saveCurrentShopId(shop.id)
        preferencesManager.saveCurrentShopName(shop.name)

        // Update MainActivity header if available
        activity?.let {
            if (it is com.devbrian.osebo.ui.MainActivity) {
                it.updateHeaderShopInfo(shop.name)
            }
        }

        // Update adapter to highlight active shop
        shopsAdapter.setActiveShopId(shop.id)

        Toast.makeText(
            requireContext(),
            "${shop.name} is now your active shop",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}