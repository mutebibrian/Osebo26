package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devbrian.osebo.databinding.FragmentSubscriptionPaymentBinding
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubscriptionPaymentFragment : Fragment() {

    private var _binding: FragmentSubscriptionPaymentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubscriptionViewModel by viewModels()
    private val args: SubscriptionPaymentFragmentArgs by navArgs()

    private var shop: Shop? = null
    private var packageId: String = ""
    private var packageName: String = ""
    private var packagePrice: Float = 0f

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

        setupArguments()
        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupArguments() {
        shop = args.shop
        packageId = args.packageId
        packageName = args.packageName
        packagePrice = args.packagePrice
    }

    private fun setupUI() {
        binding.toolbar.title = "Complete Payment"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvPackageName.text = packageName
        binding.tvAmount.text = "UGX ${String.format("%,.0f", packagePrice)}"
        binding.tvShopName.text = shop?.name ?: "Your Shop"
    }

    private fun setupClickListeners() {
        binding.btnPayNow.setOnClickListener {
            processPayment()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        // Observe subscription result
        viewModel.subscriptionResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    val response = resource.data
                    // Check if we have a paymentId (payment initiated successfully)
                    if (response?.paymentId != null) {
                        Toast.makeText(
                            requireContext(),
                            "Payment initiated! Check your phone for the prompt.",
                            Toast.LENGTH_LONG
                        ).show()

                        // Navigate to payment status fragment
                        val action = SubscriptionPaymentFragmentDirections
                            .actionSubscriptionPaymentFragmentToPaymentStatusFragment(
                                transactionId = response.paymentId,
                                shopId = shop?.id ?: "",
                                amount = packagePrice,
                                currency = "UGX",
                                phoneNumber = binding.etPhoneNumber.text.toString()
                            )
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            response?.message ?: "Subscription created successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Failed to create subscription",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                showLoading(true)
            }
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processPayment() {
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()

        if (phoneNumber.isEmpty()) {
            binding.tilPhoneNumber.error = "Phone number is required"
            return
        }

        val formattedPhone = formatPhoneNumber(phoneNumber)

        if (shop == null) {
            Toast.makeText(requireContext(), "No shop selected", Toast.LENGTH_SHORT).show()
            return
        }

        if (packageId.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid package", Toast.LENGTH_SHORT).show()
            return
        }

        // Show confirmation dialog
        showPaymentConfirmation(formattedPhone)
    }

    private fun showPaymentConfirmation(phoneNumber: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm Payment")
            .setMessage(buildString {
                appendLine("Package: $packageName")
                appendLine("Amount: UGX ${String.format("%,.0f", packagePrice)}")
                appendLine("Phone: $phoneNumber")
                appendLine("\nYou will receive a payment prompt on your phone.")
            })
            .setPositiveButton("Confirm") { _, _ ->
                viewModel.createSubscription(
                    shopId = shop?.id ?: "",
                    packageIds = listOf(packageId),
                    phoneNumber = phoneNumber,
                    months = 1
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatPhoneNumber(phone: String): String {
        val cleaned = phone.replace("\\s".toRegex(), "").replace("-", "")
        return when {
            cleaned.startsWith("+256") -> cleaned
            cleaned.startsWith("256") -> "+$cleaned"
            cleaned.startsWith("0") && cleaned.length == 10 -> "+256${cleaned.substring(1)}"
            cleaned.length == 9 -> "+256$cleaned"
            else -> "+256$cleaned"
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnPayNow.isEnabled = !show
        binding.btnPayNow.text = if (show) "Processing..." else "Pay Now"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}