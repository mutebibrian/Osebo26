package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.devbrian.osebo.databinding.DialogEditAccountBinding

class EditAccountDialogFragment : DialogFragment() {

    private lateinit var binding: DialogEditAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogEditAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            saveAccountChanges()
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun saveAccountChanges() {
        val businessName = binding.businessNameEditText.text.toString()
        val businessType = binding.businessTypeEditText.text.toString()
        val registrationNumber = binding.registrationEditText.text.toString()
        val taxId = binding.taxIdEditText.text.toString()
        val address = binding.addressEditText.text.toString()

        
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}


