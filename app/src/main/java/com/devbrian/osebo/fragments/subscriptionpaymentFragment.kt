package com.devbrian.osebo.fragments.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devbrian.osebo.databinding.FragmentSubscriptionPaymentBinding
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubscriptionPaymentFragment : Fragment() {

    private var _binding: FragmentSubscriptionPaymentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SubscriptionViewModel by viewModels()
    private val args: SubscriptionPaymentFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.toolbar.title = "Complete Payment"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Set package info
        binding.tvPackageName.text = args.packageName
        binding.tvTotalAmount.text = "UGX ${args.packagePrice.toInt()}"
        binding.tvMonths.text = "1 month"

        // Set shop info in toolbar subtitle
        args.shop?.let { shop ->
            binding.toolbar.subtitle = shop.name
        }
    }

    private fun setupClickListeners() {
        binding.btnProceedToPay.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            if (phoneNumber.isNotEmpty() && phoneNumber.length >= 10) {
                binding.tilPhoneNumber.error = null
                proceedToPayment(phoneNumber)
            } else {
                binding.tilPhoneNumber.error = "Please enter valid phone number"
            }
        }
    }

    private fun proceedToPayment(phoneNumber: String) {
        args.shop?.let { shop ->
            viewModel.createSubscription(
                shopId = shop.id,
                packageId = args.packageId,
                phoneNumber = phoneNumber,
                months = 1
            )
        }
    }

    private fun observeViewModel() {
        viewModel.createSubscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    resource.data?.let { response ->
                        if (response.success) {
                            val paymentId = response.data?.paymentId
                            if (!paymentId.isNullOrBlank()) {
                                navigateToPaymentStatus(paymentId, binding.etPhoneNumber.text.toString())
                            } else {
                                showError("No payment ID received")
                            }
                        } else {
                            showError(response.message)
                        }
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    showError(resource.message)
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
            }
        }
    }

    private fun navigateToPaymentStatus(transactionId: String?, phoneNumber: String) {
        transactionId?.let { id ->
            args.shop?.let { shop ->
                val action = SubscriptionPaymentFragmentDirections
                    .actionSubscriptionPaymentFragmentToPaymentStatusFragment(
                        transactionId = id,
                        shopId = shop.id,
                        amount = args.packagePrice,
                        currency = "UGX",
                        phoneNumber = phoneNumber
                    )
                findNavController().navigate(action)
            }
        } ?: run {
            showError("No transaction ID received")
        }
    }

    private fun showLoading(show: Boolean) {
        binding.overlayProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String?) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message ?: "An error occurred",
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}