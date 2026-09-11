package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentShopDetailsBinding
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.ui.ShopViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ShopDetailsFragment : Fragment() {

    private var _binding: FragmentShopDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: ShopDetailsFragmentArgs by navArgs()
    private val shopViewModel: ShopViewModel by viewModel()

    private lateinit var shop: Shop

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        
        shop = args.shop

        setupToolbar()
        displayShopInfo()
        setupClickListeners()
        loadShopData()
        observeViewModels()
    }

    private fun setupToolbar() {
        binding.toolbar.title = shop.name
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    editShop()
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmationDialog()
                    true
                }
                R.id.action_refresh -> {
                    refreshData()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnViewAllStats.setOnClickListener {
            navigateToStatistics()
        }
    }

    private fun loadShopData() {
        lifecycleScope.launch {
            shopViewModel.loadShops()
        }
    }

    private fun observeViewModels() {
        
        shopViewModel.shops.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.find { it.id == shop.id }?.let { updatedShop ->
                        shop = updatedShop
                        displayShopInfo()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    
                }
            }
        }
    }

    private fun displayShopInfo() {
        
        binding.tvShopName.text = shop.name
        binding.tvShopType.text = shop.shopTypeDisplay
        binding.tvShopAddress.text = shop.fullAddress.ifEmpty { "No address provided" }

        
        binding.tvShopPhone.text = shop.phone ?: "No phone provided"
        binding.tvShopEmail.text = shop.email ?: "No email provided"
        binding.tvShopWebsite.text = shop.website ?: "No website provided"

        
        binding.tvRegistrationNumber.text = shop.registrationNumber ?: "Not registered"
        binding.tvTaxNumber.text = shop.taxIdentificationNumber ?: "Not provided"

        
        binding.tvCreatedAt.text = formatDate(shop.createdAt)
        binding.tvUpdatedAt.text = formatDate(shop.updatedAt)

        
        updateShopStatus()
    }

    private fun updateShopStatus() {
        val statusText = if (shop.isActive) "Active" else "Inactive"
        val statusColor = if (shop.isActive) R.color.success_green else R.color.error_red

        binding.tvShopStatus.text = statusText
        binding.tvShopStatus.setTextColor(ContextCompat.getColor(requireContext(), statusColor))
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "N/A"

        return try {
            val inputFormat = if (dateString.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            }

            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun editShop() {
        
        Toast.makeText(requireContext(), "Edit shop", Toast.LENGTH_SHORT).show()
        
        
        
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Shop")
            .setMessage("Are you sure you want to delete ${shop.name}? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteShop()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteShop() {
        lifecycleScope.launch {
            shopViewModel.deleteShop(shop.id)
            Toast.makeText(requireContext(), "Shop deleted successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun refreshData() {
        loadShopData()
        Toast.makeText(requireContext(), "Refreshing data...", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToStatistics() {
        
        Toast.makeText(requireContext(), "View Statistics", Toast.LENGTH_SHORT).show()
        
        
        
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
