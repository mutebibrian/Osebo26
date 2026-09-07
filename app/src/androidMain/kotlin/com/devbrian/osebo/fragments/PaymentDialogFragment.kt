package com.devbrian.osebo.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentPaymentDialogBinding
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.devbrian.osebo.utils.Resource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class PaymentDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPaymentDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubscriptionViewModel by activityViewModel()

    private var shopId: String? = null
    private var packageId: String? = null // may be a comma-joined list of ids
    private var packageName: String? = null
    private var amount: Double = 0.0
    private var selectedMonths: Int = 1
    private var isProcessing: Boolean = false
    private var hasNavigated = false

    private var paymentListener: ((String, String, Int, Double) -> Unit)? = null

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

        setupUI()
        setupListeners()
        setupObservers()
    }

    fun setPaymentListener(listener: (String, String, Int, Double) -> Unit) {
        this.paymentListener = listener
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

        viewModel.subscriptionResult.observe(viewLifecycleOwner) { resource ->
            println("🔔 PaymentDialog: subscriptionResult = $resource")

            if (hasNavigated) {
                println("🔔 PaymentDialog: Already navigated, ignoring")
                return@observe
            }

            when (resource) {
                is Resource.Loading -> {
                    println("🔔 PaymentDialog: Loading...")
                }
                is Resource.Success -> {
                    val response = resource.data
                    val paymentId = response?.paymentId
                    println("🔔 PaymentDialog: paymentId = $paymentId")

                    if (!paymentId.isNullOrBlank()) {
                        println("🔔 PaymentDialog: Found paymentId, navigating to PaymentStatusFragment")
                        hasNavigated = true

                        try {
                            dismissAllowingStateLoss()
                        } catch (e: Exception) {
                            println("Error dismissing: ${e.message}")
                        }

                        viewLifecycleOwner.lifecycleScope.launch {
                            delay(300)
                            try {
                                val action = PaymentDialogFragmentDirections
                                    .actionPaymentDialogFragmentToPaymentStatusFragment(
                                        transactionId = paymentId,
                                        shopId = shopId!!,
                                        amount = (amount * selectedMonths).toFloat(),
                                        currency = "UGX",
                                        phoneNumber = binding.etPhoneNumber.text.toString()
                                    )
                                findNavController().navigate(action)
                                println("✅ PaymentDialog: Navigation successful!")
                            } catch (e: Exception) {
                                println("❌ PaymentDialog: Navigation error - ${e.message}")
                                e.printStackTrace()
                                Toast.makeText(requireContext(),
                                    "Payment initiated. Please check your phone.",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        println("🔔 PaymentDialog: No paymentId received")
                        hasNavigated = true
                        try {
                            dismissAllowingStateLoss()
                        } catch (e: Exception) {}
                        Toast.makeText(requireContext(),
                            response?.message ?: "Subscription created successfully!",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    println("🔔 PaymentDialog: Error - ${resource.message}")
                    showError(resource.message ?: "Failed to create subscription")
                }
            }
        }
    }

    private fun processPayment() {
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()

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

        showPaymentConfirmationDialog(formattedPhone, selectedMonths)
    }

    private fun showPaymentConfirmationDialog(phoneNumber: String, months: Int) {
        val totalAmount = amount * months

        println("🔔 PaymentDialog: Showing confirmation dialog for amount: $totalAmount")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm Payment")
            .setMessage(buildString {
                appendLine("Package: $packageName")
                appendLine("Amount: UGX ${String.format("%,.0f", totalAmount)}")
                appendLine("Duration: $months month${if (months > 1) "s" else ""}")
                appendLine("Phone: $phoneNumber")
                appendLine("\nYou will receive a payment prompt on your phone.")
            })
            .setPositiveButton("Confirm") { _, _ ->
                println("✅ User confirmed payment - calling createSubscription")

                // packageId may be a comma-joined string of multiple ids (from
                // SubscriptionPackagesFragment's multi-bundle selection) — split it back
                // into a list for the ViewModel's plural signature.
                val packageIdList = packageId
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()

                if (paymentListener != null) {
                    paymentListener?.invoke(phoneNumber, packageId!!, months, totalAmount)
                } else {
                    viewModel.createSubscription(shopId!!, packageIdList, phoneNumber, months)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                println("❌ User cancelled payment")
            }
            .setCancelable(true)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}