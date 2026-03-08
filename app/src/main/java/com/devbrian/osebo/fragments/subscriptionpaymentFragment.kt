package com.devbrian.osebo.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devbrian.osebo.databinding.FragmentSubscriptionPaymentBinding
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.snackbar.Snackbar
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
        setupPhoneNumberWatcher()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.toolbar.title = "Complete Payment"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Set package details
        binding.tvPackageName.text = args.packageName
        binding.tvTotalAmount.text = "UGX ${String.format("%,d", args.packagePrice.toInt())}"
        binding.tvMonths.text = "1 month"

        // Set shop name if available
        args.shop?.let { shop ->
            binding.toolbar.subtitle = shop.name
        }

        // Set default phone number format
        binding.etPhoneNumber.setText("+256")
        binding.etPhoneNumber.setSelection(binding.etPhoneNumber.text?.length ?: 4)
    }

    private fun setupPhoneNumberWatcher() {
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Clear error when user starts typing
                binding.tilPhoneNumber.error = null
            }

            override fun afterTextChanged(s: Editable?) {
                // Auto-format: ensure it starts with +256
                if (!s.isNullOrEmpty() && !s.startsWith("+")) {
                    binding.etPhoneNumber.removeTextChangedListener(this)
                    binding.etPhoneNumber.setText("+256${s}")
                    binding.etPhoneNumber.setSelection(binding.etPhoneNumber.text?.length ?: 4)
                    binding.etPhoneNumber.addTextChangedListener(this)
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnProceedToPay.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            if (isValidPhoneNumber(phoneNumber)) {
                binding.tilPhoneNumber.error = null
                proceedToPayment(formatPhoneNumber(phoneNumber))
            } else {
                binding.tilPhoneNumber.error = "Please enter a valid phone number (e.g., +2567XXXXXXXX)"
            }
        }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val cleaned = phone.replace("\\s".toRegex(), "").replace("-", "")
        val digits = cleaned.filter { it.isDigit() }

        return when {
            cleaned.isEmpty() -> false
            cleaned.startsWith("+256") && digits.length >= 12 -> true
            cleaned.startsWith("0") && digits.length >= 10 -> true
            digits.length >= 9 -> true
            else -> false
        }
    }

    private fun formatPhoneNumber(phone: String): String {
        val cleaned = phone.replace("\\s".toRegex(), "").replace("-", "")

        return when {
            cleaned.startsWith("+256") -> cleaned
            cleaned.startsWith("256") -> "+$cleaned"
            cleaned.startsWith("0") -> "+256${cleaned.substring(1)}"
            else -> "+256$cleaned"
        }
    }

    private fun proceedToPayment(phoneNumber: String) {
        args.shop?.let { shop ->
            showLoading(true)

            println("💳 Processing payment for:")
            println("  - Shop ID: ${shop.id}")
            println("  - Package ID: ${args.packageId}")
            println("  - Phone: $phoneNumber")
            println("  - Amount: ${args.packagePrice}")

            viewModel.createSubscription(
                shopId = shop.id,
                packageId = args.packageId,
                phoneNumber = phoneNumber,
                months = 1
            )
        } ?: run {
            showError("Shop information not available")
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
                                Toast.makeText(
                                    requireContext(),
                                    "Payment request sent. Check your phone.",
                                    Toast.LENGTH_LONG
                                ).show()

                                // Navigate to PaymentStatusFragment with the transaction ID
                                navigateToPaymentStatus(paymentId, phoneNumber = binding.etPhoneNumber.text.toString())
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
                showLoading(false)
                showError(it)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun navigateToPaymentStatus(transactionId: String, phoneNumber: String) {
        args.shop?.let { shop ->
            try {
                val action = SubscriptionPaymentFragmentDirections
                    .actionSubscriptionPaymentFragmentToPaymentStatusFragment(
                        transactionId = transactionId,
                        shopId = shop.id,
                        amount = args.packagePrice,
                        currency = "UGX",
                        phoneNumber = phoneNumber
                    )
                findNavController().navigate(action)
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Navigation error: ${e.message}")
            }
        } ?: run {
            showError("Shop information not available")
        }
    }

    private fun showLoading(show: Boolean) {
        binding.overlayProgress.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnProceedToPay.isEnabled = !show
    }

    private fun showError(message: String?) {
        Snackbar.make(
            binding.root,
            message ?: "An error occurred",
            Snackbar.LENGTH_LONG
        ).apply {
            setAction("Dismiss") { dismiss() }
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}