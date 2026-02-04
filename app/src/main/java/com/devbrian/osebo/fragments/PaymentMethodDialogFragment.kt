package com.devbrian.osebo.fragments.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.devbrian.osebo.databinding.DialogPaymentMethodBinding

class PaymentMethodDialogFragment : DialogFragment() {

    private lateinit var binding: DialogPaymentMethodBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPaymentMethodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // Setup payment method options
        // You can add a RecyclerView or RadioGroup here
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            val selectedMethod = getSelectedPaymentMethod()
            // Save the payment method
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun getSelectedPaymentMethod(): String {
        // Implement logic to get selected payment method
        return "Mobile Money" // Default
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}