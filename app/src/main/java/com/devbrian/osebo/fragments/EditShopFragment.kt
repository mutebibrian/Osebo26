package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentEditShopBinding
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.ui.ShopViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditShopFragment : Fragment() {

    private var _binding: FragmentEditShopBinding? = null
    private val binding get() = _binding!!

    private val args: EditShopFragmentArgs by navArgs()
    private val shopViewModel: ShopViewModel by viewModels()

    private lateinit var originalShop: Shop

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalShop = args.shop

        setupToolbar()
        setupClickListeners()
        displayShopInfo()
        setupBusinessTypeSpinner()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Edit Shop"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_save -> {
                    saveChanges()
                    true
                }
                R.id.action_discard -> {
                    discardChanges()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveChanges()
        }

        binding.btnCancel.setOnClickListener {
            discardChanges()
        }

        binding.btnUploadLogo.setOnClickListener {
            uploadLogo()
        }
    }

    private fun setupBusinessTypeSpinner() {
        
        val businessTypes = arrayOf(
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
            "Online Store"
        )

        val businessTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            businessTypes
        )
        businessTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBusinessType.adapter = businessTypeAdapter

        
        val businessTypePosition = businessTypes.indexOfFirst {
            it.equals(originalShop.shopTypeDisplay, ignoreCase = true)
        }
        if (businessTypePosition >= 0) {
            binding.spinnerBusinessType.setSelection(businessTypePosition)
        }
    }

    private fun displayShopInfo() {
        
        binding.etShopName.setText(originalShop.name)
        binding.etShopAddress.setText(originalShop.address)
        binding.etShopPhone.setText(originalShop.phone)
        binding.etShopEmail.setText(originalShop.email)
        binding.etShopWebsite.setText(originalShop.website)

        
        binding.etRegistrationNumber.setText(originalShop.registrationNumber)
        binding.etTaxNumber.setText(originalShop.taxIdentificationNumber)

        
        binding.etCity.setText(originalShop.city)
        binding.etCountry.setText(originalShop.country)
        binding.etPostalCode.setText(originalShop.postalCode)

        
        binding.etDescription.setText(originalShop.description)

        
        binding.switchActive.isChecked = originalShop.isActive
    }

    private fun saveChanges() {
        
        if (!validateInputs()) {
            return
        }

        
        val updatedShop = originalShop.copy(
            name = binding.etShopName.text.toString(),
            address = binding.etShopAddress.text.toString(),
            phone = binding.etShopPhone.text.toString(),
            email = binding.etShopEmail.text.toString(),
            website = binding.etShopWebsite.text.toString(),
            registrationNumber = binding.etRegistrationNumber.text.toString(),
            taxIdentificationNumber = binding.etTaxNumber.text.toString(),
            city = binding.etCity.text.toString(),
            country = binding.etCountry.text.toString(),
            postalCode = binding.etPostalCode.text.toString(),
            description = binding.etDescription.text.toString(),
            shopType = getSelectedBusinessType(),
            isActive = binding.switchActive.isChecked
        )

        
        binding.progressBar.visibility = View.VISIBLE

        
        lifecycleScope.launch {
            
            

            
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Shop updated successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        
        if (binding.etShopName.text.isNullOrBlank()) {
            binding.etShopName.error = "Shop name is required"
            isValid = false
        }

        
        if (binding.etShopPhone.text.isNullOrBlank()) {
            binding.etShopPhone.error = "Phone number is required"
            isValid = false
        }

        
        val email = binding.etShopEmail.text.toString()
        if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etShopEmail.error = "Invalid email address"
            isValid = false
        }

        return isValid
    }

    private fun getSelectedBusinessType(): String {
        return when (binding.spinnerBusinessType.selectedItemPosition) {
            0 -> "retail"
            1 -> "wholesale"
            2 -> "service"
            3 -> "manufacturing"
            4 -> "restaurant"
            5 -> "salon"
            6 -> "grocery"
            7 -> "pharmacy"
            8 -> "hardware"
            9 -> "fashion"
            10 -> "electronics"
            11 -> "online"
            else -> "retail"
        }
    }

    private fun discardChanges() {
        if (hasChanges()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Discard Changes")
                .setMessage("You have unsaved changes. Do you want to discard them?")
                .setPositiveButton("Discard") { _, _ ->
                    findNavController().navigateUp()
                }
                .setNegativeButton("Continue Editing", null)
                .show()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun hasChanges(): Boolean {
        return binding.etShopName.text.toString() != originalShop.name ||
                binding.etShopAddress.text.toString() != originalShop.address ||
                binding.etShopPhone.text.toString() != originalShop.phone ||
                binding.etShopEmail.text.toString() != originalShop.email ||
                binding.etShopWebsite.text.toString() != originalShop.website ||
                binding.etRegistrationNumber.text.toString() != originalShop.registrationNumber ||
                binding.etTaxNumber.text.toString() != originalShop.taxIdentificationNumber ||
                binding.etCity.text.toString() != originalShop.city ||
                binding.etCountry.text.toString() != originalShop.country ||
                binding.etPostalCode.text.toString() != originalShop.postalCode ||
                binding.etDescription.text.toString() != originalShop.description ||
                binding.switchActive.isChecked != originalShop.isActive
    }

    private fun uploadLogo() {
        
        Toast.makeText(requireContext(), "Upload Logo", Toast.LENGTH_SHORT).show()
    }

    private fun observeViewModel() {
        
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
