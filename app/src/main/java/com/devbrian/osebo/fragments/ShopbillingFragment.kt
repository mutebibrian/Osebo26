package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentShopBillingBinding
import com.devbrian.osebo.models.Shop
import java.text.SimpleDateFormat
import java.util.*

class ShopbillingFragment : Fragment() {

    private var _binding: FragmentShopBillingBinding? = null
    private val binding get() = _binding!!

    private lateinit var shop: Shop

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            shop = it.getParcelable<Shop>("shop") ?: Shop()
        } ?: run {
            shop = Shop()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBillingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set shop name
        binding.tvShopName.text = shop.name
        binding.tvShopNameSmall.text = shop.name

        // Show subscription status with color
        val status = shop.subscriptionStatus.lowercase()
        val isActive = status == "active" || status == "trial"
        val isExpired = status == "expired"
        val isInactive = status == "inactive" || status.isEmpty()

        binding.tvSubscriptionStatus.text = when {
            isActive -> if (status == "trial")
                "TRIAL (${getDaysRemaining(shop.subscriptionExpiry)} days left)"
            else "ACTIVE"
            isExpired -> "EXPIRED"
            else -> "INACTIVE"
        }

        // Status color
        when {
            isActive -> binding.tvSubscriptionStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.success)
            )
            isExpired -> binding.tvSubscriptionStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.error)
            )
            else -> binding.tvSubscriptionStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.text_secondary)
            )
        }

        // Active bundles count
        val activeBundles = shop.totalProducts // or real field
        if (activeBundles > 0) {
            binding.tvActiveBundles.text = activeBundles.toString()
            binding.tvNoActiveSubscriptions.visibility = View.GONE
        } else {
            binding.tvActiveBundles.text = "0"
            binding.tvNoActiveSubscriptions.visibility = View.VISIBLE
        }

        // ========== BUTTON LOGIC ==========
        // If shop has NO active subscription → show "Start Free Trial"
        // Otherwise show "Manage Bundles"
        if (isInactive || isExpired) {
            // Show "Start Free Trial" for new/expired shops
            binding.btnManageBundles.text = "+ Start Free Trial"
            // Optionally, change its style to primary or accent
            binding.btnManageBundles.setIconResource(R.drawable.ic_trial) // optional
        } else {
            // Active or trial → "Manage Bundles"
            binding.btnManageBundles.text = "+ Manage Bundles"
            binding.btnManageBundles.setIconResource(R.drawable.ic_bundle) // optional
        }

        // "Manage/Start" button click
        binding.btnManageBundles.setOnClickListener {
            navigateToSubscriptionPackages()
        }

        // "Renew Bundles" button click
        binding.btnRenewBundles.setOnClickListener {
            navigateToSubscriptionPackages()
        }
    }

    private fun getDaysRemaining(expiryDate: String?): Int {
        if (expiryDate.isNullOrEmpty()) return 0
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiry = format.parse(expiryDate)
            val diff = expiry.time - Date().time
            (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }

    private fun navigateToSubscriptionPackages() {
        try {
            val action = ShopbillingFragmentDirections.actionShopBillingToSubscriptionPackages(shop)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error navigating to packages", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}