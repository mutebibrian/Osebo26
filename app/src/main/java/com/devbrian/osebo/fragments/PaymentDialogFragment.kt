package com.devbrian.osebo.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentPaymentDialogBinding
import com.devbrian.osebo.ui.viewmodels.SubscriptionViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaymentDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPaymentDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SubscriptionViewModel

    // Arguments
    private var shopId: String? = null
    private var packageId: String? = null
    private var packageName: String? = null
    private var amount: Double = 0.0

    // Callback
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
        // Set dialog title
        binding.tvDialogTitle.text = "Complete Subscription"

        // Display package info
        binding.tvPackageName.text = packageName ?: "Subscription Plan"
        binding.tvPackagePrice.text = if (amount > 0) {
            "UGX ${String.format("%,.0f", amount)}/month"
        } else {
            "Custom Pricing"
        }

        // Show/hide price section
        if (amount > 0) {
            binding.priceSection.visibility = View.VISIBLE
            binding.customPricingSection.visibility = View.GONE
        } else {
            binding.priceSection.visibility = View.GONE
            binding.customPricingSection.visibility = View.VISIBLE
        }

        // Set up months spinner
        setupMonthsSpinner()

        // Format phone number hint
        binding.tilPhoneNumber.hint = "Mobile Money Number"
        binding.tilPhoneNumber.placeholderText = "e.g., 256700000000 or 0700000000"

        // Set up provider chips
        setupProviderChips()
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
        }

        // Set default selection to 1 month
        binding.spinnerMonths.setText(months[0], false)
        binding.tvTotalAmount.text = "UGX ${String.format("%,.0f", amount)}"
        binding.tvSavingsBadge.visibility = View.GONE
    }

    private fun setupProviderChips() {
        binding.chipMtn.setOnClickListener {
            binding.chipMtn.isChecked = true
            binding.chipAirtel.isChecked = false
            binding.tilPhoneNumber.hint = "MTN Mobile Money Number"
            binding.etPhoneNumber.hint = "e.g., 0772123456 or 256772123456"
        }

        binding.chipAirtel.setOnClickListener {
            binding.chipAirtel.isChecked = true
            binding.chipMtn.isChecked = false
            binding.tilPhoneNumber.hint = "Airtel Money Number"
            binding.etPhoneNumber.hint = "e.g., 0750123456 or 256750123456"
        }

        // Default selection
        binding.chipMtn.isChecked = true
    }

    private fun setupListeners() {
        binding.btnPayNow.setOnClickListener {
            processPayment()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        // Phone number validation
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilPhoneNumber.error = null
            }
        })
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
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
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processPayment() {
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val selectedMonths = when (binding.spinnerMonths.text.toString()) {
            "1 month" -> 1
            "3 months" -> 3
            "6 months" -> 6
            "12 months" -> 12
            else -> 1
        }

        // Validate phone number
        val validationError = validatePhoneNumber(phoneNumber)
        if (validationError != null) {
            binding.tilPhoneNumber.error = validationError
            return
        }

        // ✅ FORMAT PHONE NUMBER WITH COUNTRY CODE (+256)
        val formattedPhone = formatPhoneNumberWithCountryCode(phoneNumber)

        // Validate package ID
        if (packageId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Invalid package selected", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate shop ID
        if (shopId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No shop selected", Toast.LENGTH_SHORT).show()
            return
        }

        // Process payment for custom plans
        if (amount <= 0) {
            showCustomPlanConfirmation()
            return
        }

        // Show confirmation dialog with formatted phone
        showPaymentConfirmationDialog(formattedPhone, selectedMonths)
    }

    /**
     * Formats phone number with Uganda country code (+256)
     * Handles various input formats:
     * - 0754665613 → +256754665613
     * - 0754665613 → +256754665613 (removes leading 0)
     * - +256754665613 → +256754665613 (keeps as is)
     * - 256754665613 → +256754665613 (adds + if missing)
     */
    private fun formatPhoneNumberWithCountryCode(phone: String): String {
        val cleaned = phone.replace("\\s".toRegex(), "").replace("-", "")

        return when {
            // Already has country code with +
            cleaned.startsWith("+256") -> cleaned

            // Has country code without +
            cleaned.startsWith("256") -> "+$cleaned"

            // Ugandan number starting with 0 (e.g., 0754665613)
            cleaned.startsWith("0") && cleaned.length == 10 -> "+256${cleaned.substring(1)}"

            // Ugandan number without 0 (e.g., 754665613)
            cleaned.length == 9 -> "+256$cleaned"

            // Default: assume it's a local number, add +256
            else -> "+256$cleaned"
        }
    }

    private fun validatePhoneNumber(phone: String): String? {
        if (phone.isEmpty()) {
            return "Phone number is required"
        }

        // Clean the phone number for validation
        val cleaned = phone.replace("\\s".toRegex(), "").replace("-", "")

        // Ugandan phone number validation patterns
        val mtnRegex = Regex("^(\\+256|0)?(77|78|79|70|71|72)[0-9]{7}$")
        val airtelRegex = Regex("^(\\+256|0)?(74|75|76)[0-9]{7}$")
        val standardRegex = Regex("^(\\+256|0)?[0-9]{9}$")

        return when {
            !standardRegex.matches(cleaned) -> "Enter a valid Ugandan phone number"
            mtnRegex.matches(cleaned) && !binding.chipMtn.isChecked -> "Select MTN as your provider"
            airtelRegex.matches(cleaned) && !binding.chipAirtel.isChecked -> "Select Airtel as your provider"
            else -> null
        }
    }

    private fun showPaymentConfirmationDialog(phoneNumber: String, months: Int) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm Payment")
            .setMessage(buildString {
                appendLine("Package: $packageName")
                appendLine("Amount: UGX ${String.format("%,.0f", amount * months)}")
                appendLine("Duration: $months month${if (months > 1) "s" else ""}")
                appendLine("Phone: $phoneNumber")  // Shows formatted number with +256
                appendLine("\nYou will receive a payment prompt on your phone.")
            })
            .setPositiveButton("Confirm") { _, _ ->
                // Call the payment listener with formatted phoneNumber
                paymentListener?.invoke(phoneNumber, packageId!!, months)
                dismiss()
                Toast.makeText(
                    requireContext(),
                    "Payment request sent. Check your phone.",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomPlanConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Custom Plan Request")
            .setMessage("You're about to request a custom plan. Our sales team will contact you within 24 hours.")
            .setPositiveButton("Request Callback") { _, _ ->
                // Navigate to contact sales or send request
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

    fun setPaymentListener(listener: (String, String, Int) -> Unit) {
        this.paymentListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}