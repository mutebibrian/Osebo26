package com.devbrian.osebo.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.databinding.FragmentPaymentDialogBinding
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PaymentDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPaymentDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SubscriptionViewModel

    private var shopId: String? = null
    private var packageId: String? = null
    private var packageName: String? = null
    private var amount: Double = 0.0
    private var selectedMonths: Int = 1
    private var isProcessing: Boolean = false

    private var paymentListener: ((String, String, Int) -> Unit)? = null

    companion object {
        const val TAG = "PaymentDialogFragment"

        fun newInstance(
            shopId: String,
            packageId: String,
            packageName: String,
            amount: Double
        ): PaymentDialogFragment {
            return PaymentDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("shopId", shopId)
                    putString("packageId", packageId)
                    putString("packageName", packageName)
                    putDouble("amount", amount)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Osebo2_BottomSheetDialog)

        arguments?.let {
            shopId = it.getString("shopId")
            packageId = it.getString("packageId")
            packageName = it.getString("packageName")
            amount = it.getDouble("amount")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[SubscriptionViewModel::class.java]

        setupUI()
        setupListeners()
        setupObservers()
    }

    private fun setupUI() {
        binding.tvDialogTitle.text = "Complete Subscription"

        binding.tvPackageName.text = packageName ?: "Subscription Plan"
        binding.tvPackagePrice.text = if (amount > 0) {
            "UGX ${String.format("%,.0f", amount)}/month"
        } else {
            "Custom Pricing"
        }

        if (amount > 0) {
            binding.priceSection.visibility = View.VISIBLE
            binding.customPricingSection.visibility = View.GONE
        } else {
            binding.priceSection.visibility = View.GONE
            binding.customPricingSection.visibility = View.VISIBLE
        }

        setupMonthsSpinner()

        binding.tilPhoneNumber.hint = "Mobile Money Number"
        binding.tilPhoneNumber.placeholderText = "e.g., 0772123456 or 256772123456"
    }

    private fun setupMonthsSpinner() {
        val months = arrayOf("1 month", "3 months", "6 months", "12 months")
        val prices = arrayOf(
            amount,
            amount * 3,
            amount * 6,
            amount * 12
        )

        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            months
        )
        binding.spinnerMonths.setAdapter(adapter)

        binding.spinnerMonths.setOnItemClickListener { _, _, position, _ ->
            val totalAmount = prices[position]
            binding.tvTotalAmount.text = "UGX ${String.format("%,.0f", totalAmount)}"
            binding.tvSavingsBadge.visibility = if (position > 0) View.VISIBLE else View.GONE

            when (position) {
                1 -> binding.tvSavingsBadge.text = "Save 5%"
                2 -> binding.tvSavingsBadge.text = "Save 10%"
                3 -> binding.tvSavingsBadge.text = "Save 15%"
                else -> binding.tvSavingsBadge.text = ""
            }

            selectedMonths = when (position) {
                0 -> 1
                1 -> 3
                2 -> 6
                3 -> 12
                else -> 1
            }
        }

        binding.spinnerMonths.setText(months[0], false)
        binding.tvTotalAmount.text = "UGX ${String.format("%,.0f", amount)}"
        binding.tvSavingsBadge.visibility = View.GONE
        selectedMonths = 1
    }

    // In PaymentDialogFragment.kt, update the createSubscription call:

    // In PaymentDialogFragment.kt, update the createSubscription function:

    private fun createSubscription(
        shopId: String,
        packageId: String,
        phoneNumber: String,
        months: Int
    ) {
        if (!isAdded) return

        // CRITICAL: Ensure shop UUID is saved before creating subscription
        val prefs = PreferenceManager.getInstance(requireContext())
        prefs.saveCurrentShopId(shopId)
        prefs.saveCurrentShopUuid(shopId)  // Save UUID format if needed

        println("✅ Creating subscription with shop ID: $shopId")
        println("✅ Package ID: $packageId")
        println("✅ Months: $months")
        println("✅ Phone: $phoneNumber")

        viewModel.createSubscription(shopId, packageId, phoneNumber, months)

        try {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.subscriptionResult.observe(viewLifecycleOwner) { resource ->
                    if (!isAdded) return@observe

                    when (resource) {
                        is Resource.Success -> {
                            resource.data?.let { response ->
                                if (response.success) {
                                    val paymentId = response.data?.paymentId
                                    if (!paymentId.isNullOrBlank()) {
                                        try {
                                            // Use the amount from arguments instead of selectedPackage
                                            val totalAmount = amount * months

                                            if (isAdded) {
                                                val action = PaymentDialogFragmentDirections
                                                    .actionPaymentDialogFragmentToPaymentStatusFragment(
                                                        transactionId = paymentId,
                                                        shopId = shopId,
                                                        amount = totalAmount.toFloat(),
                                                        currency = "UGX",
                                                        phoneNumber = phoneNumber
                                                    )
                                                findNavController().navigate(action)
                                            }
                                        } catch (e: Exception) {
                                            if (isAdded) {
                                                Toast.makeText(requireContext(),
                                                    "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        if (isAdded) {
                                            Toast.makeText(requireContext(),
                                                "Subscription created successfully!",
                                                Toast.LENGTH_SHORT).show()
                                            findNavController().popBackStack()
                                        }
                                    }
                                } else {
                                    if (isAdded) {
                                        Toast.makeText(requireContext(),
                                            response.message ?: "Failed to create subscription",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                            if (isAdded) {
                                Toast.makeText(requireContext(),
                                    resource.message ?: "Error creating subscription",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                        is Resource.Loading -> {
                            // Show loading if needed
                        }
                    }
                }
            }
        } catch (e: IllegalStateException) {
            println("⚠️ ViewLifecycleOwner not available: ${e.message}")
        }
    }

    private fun setupListeners() {
        binding.btnPayNow.setOnClickListener {
            if (!isProcessing) {
                processPayment()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilPhoneNumber.error = null
                binding.tvErrorMessage.visibility = View.GONE
            }
        })
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            isProcessing = isLoading
            if (isLoading) {
                binding.btnPayNow.isEnabled = false
                binding.btnPayNow.text = "Processing..."
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.btnPayNow.isEnabled = true
                binding.btnPayNow.text = "Pay Now"
                binding.progressBar.visibility = View.GONE
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                showError(it)
            }
        }

        // Observe subscriptionResult for navigation
        viewModel.subscriptionResult.observe(viewLifecycleOwner) { resource ->
            println("🔔 subscriptionResult received: $resource")
            when (resource) {
                is Resource.Loading -> {
                    // Already handled by isLoading
                }
                is Resource.Success -> {
                    resource.data?.let { response ->
                        if (response.success) {
                            val paymentId = response.data?.paymentId
                            if (!paymentId.isNullOrBlank()) {
                                // Navigate to PaymentStatusFragment
                                try {
                                    val action = PaymentDialogFragmentDirections
                                        .actionPaymentDialogFragmentToPaymentStatusFragment(
                                            transactionId = paymentId,
                                            shopId = shopId!!,
                                            amount = (amount * selectedMonths).toFloat(),
                                            currency = "UGX",
                                            phoneNumber = binding.etPhoneNumber.text.toString()
                                        )

                                    // Dismiss dialog and navigate
                                    dismiss()
                                    findNavController().navigate(action)

                                    Toast.makeText(
                                        requireContext(),
                                        "Payment initiated. Check your phone.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } catch (e: Exception) {
                                    showError("Navigation error: ${e.message}")
                                }
                            } else {
                                // No payment ID needed (maybe free trial)
                                dismiss()
                                Toast.makeText(
                                    requireContext(),
                                    "Subscription created successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            showError(response.message ?: "Failed to create subscription")
                        }
                    }
                }
                is Resource.Error -> {
                    showError(resource.message ?: "Failed to create subscription")
                }
            }
        }
    }

    private fun processPayment() {
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()

        // Validation
        val validationError = validatePhoneNumber(phoneNumber)
        if (validationError != null) {
            binding.tilPhoneNumber.error = validationError
            binding.tvErrorMessage.text = validationError
            binding.tvErrorMessage.visibility = View.VISIBLE
            return
        }

        val formattedPhone = formatPhoneNumberWithCountryCode(phoneNumber)

        if (packageId.isNullOrEmpty()) {
            showError("Invalid package selected")
            return
        }

        if (shopId.isNullOrEmpty()) {
            showError("No shop selected")
            return
        }

        if (amount <= 0) {
            showCustomPlanConfirmation()
            return
        }

        // Show confirmation dialog
        showPaymentConfirmationDialog(formattedPhone, selectedMonths)
    }

    private fun showPaymentConfirmationDialog(phoneNumber: String, months: Int) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm Payment")
            .setMessage(buildString {
                appendLine("Package: $packageName")
                appendLine("Amount: UGX ${String.format("%,.0f", amount * months)}")
                appendLine("Duration: $months month${if (months > 1) "s" else ""}")
                appendLine("Phone: $phoneNumber")
                appendLine("\nYou will receive a payment prompt on your phone.")
            })
            .setPositiveButton("Confirm") { _, _ ->
                // Call the payment listener to create subscription
                paymentListener?.invoke(phoneNumber, packageId!!, months)
                // Navigation will happen in the observer when the result comes back
                // DO NOT dismiss here - let the observer handle it
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomPlanConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Custom Plan Request")
            .setMessage("You're about to request a custom plan. Our sales team will contact you within 24 hours.")
            .setPositiveButton("Request Callback") { _, _ ->
                openContactSales()
                dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openContactSales() {
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val formattedPhone = if (phoneNumber.isNotEmpty()) {
            formatPhoneNumberWithCountryCode(phoneNumber)
        } else {
            "Not provided"
        }

        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = "mailto:sales@osebo.ai".toUri()
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Custom Plan Request - ${packageName ?: "Enterprise"}")
            putExtra(android.content.Intent.EXTRA_TEXT, buildString {
                appendLine("Hello,")
                appendLine("\nI'm interested in a custom subscription plan.")
                appendLine("\nShop ID: ${shopId ?: "N/A"}")
                appendLine("Package: ${packageName ?: "Enterprise"}")
                appendLine("Phone: $formattedPhone")
                appendLine("\nPlease contact me with more information.")
            })
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatPhoneNumberWithCountryCode(phone: String): String {
        val cleaned = phone.replace("\\s".toRegex(), "").replace("-", "")

        return when {
            cleaned.startsWith("+256") -> cleaned
            cleaned.startsWith("256") -> "+$cleaned"
            cleaned.startsWith("0") && cleaned.length == 10 -> "+256${cleaned.substring(1)}"
            cleaned.length == 9 -> "+256$cleaned"
            else -> "+256$cleaned"
        }
    }

    private fun validatePhoneNumber(phone: String): String? {
        if (phone.isEmpty()) {
            return "Phone number is required"
        }

        val cleaned = phone.replace("\\s".toRegex(), "").replace("-", "")
        val digits = cleaned.filter { it.isDigit() }

        return when {
            digits.length < 9 -> "Phone number must have at least 9 digits"
            digits.length > 12 -> "Phone number too long"
            else -> null
        }
    }

    private fun showError(message: String) {
        binding.tvErrorMessage.text = message
        binding.tvErrorMessage.visibility = View.VISIBLE
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun setPaymentListener(listener: (String, String, Int) -> Unit) {
        this.paymentListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}