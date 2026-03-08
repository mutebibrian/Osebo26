package com.devbrian.osebo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentAccountBinding
import com.devbrian.osebo.fragments.EditAccountDialogFragment
import com.devbrian.osebo.fragments.dialogs.PaymentMethodDialogFragment
import com.devbrian.osebo.ui.AccountViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding
    private val viewModel: AccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.accountData.observe(viewLifecycleOwner) { account ->
            account?.let {
                binding.businessNameValue.text = it.businessName
                binding.businessTypeValue.text = it.businessType
                binding.registrationValue.text = it.registrationNumber
                binding.taxIdValue.text = it.taxId
                binding.addressValue.text = it.address
                binding.paymentMethodValue.text = it.paymentMethod
                binding.billingCycleValue.text = it.billingCycle
                binding.nextBillingValue.text = it.nextBillingDate

                
                when (it.status) {
                    "active" -> {
                        binding.statusChip.text = "Active"
                        binding.statusChip.setChipBackgroundColorResource(R.color.success_green)
                    }
                    "suspended" -> {
                        binding.statusChip.text = "Suspended"
                        binding.statusChip.setChipBackgroundColorResource(R.color.error_red)
                    }
                    "pending" -> {
                        binding.statusChip.text = "Pending"
                        binding.statusChip.setChipBackgroundColorResource(R.color.warning_yellow)
                    }
                }

                
                binding.twoFactorSwitch.isChecked = it.twoFactorEnabled
                binding.loginNotificationsSwitch.isChecked = it.loginNotificationsEnabled
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                showSnackbar("Account updated successfully")
            }
        }
    }

    private fun setupClickListeners() {
        
        binding.editAccountButton.setOnClickListener {
            openEditAccountDialog()
        }

        
        binding.updatePaymentButton.setOnClickListener {
            openPaymentMethodDialog()
        }

        
        binding.twoFactorSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateTwoFactorAuth(isChecked)
        }

        binding.loginNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateLoginNotifications(isChecked)
        }

        binding.sessionButton.setOnClickListener {
            openSessionManagement()
        }

        
        binding.deactivateButton.setOnClickListener {
            showDeactivateConfirmation()
        }

        binding.deleteButton.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun openEditAccountDialog() {
        val dialog = EditAccountDialogFragment()
        dialog.show(childFragmentManager, "EditAccountDialog")
    }

    private fun openPaymentMethodDialog() {
        val dialog = PaymentMethodDialogFragment()
        dialog.show(childFragmentManager, "PaymentMethodDialog")
    }

    private fun openSessionManagement() {
        val action = AccountFragmentDirections.actionAccountFragmentToSessionsFragment()
        findNavController().navigate(action)
    }

    private fun showDeactivateConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Deactivate Account")
            .setMessage("Are you sure you want to deactivate your account? You can reactivate it later.")
            .setPositiveButton("Deactivate") { _, _ ->
                viewModel.deactivateAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account")
            .setMessage("This action cannot be undone. All your data will be permanently deleted.")
            .setPositiveButton("Delete Account") { _, _ ->
                viewModel.deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}

