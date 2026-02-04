package com.devbrian.osebo.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.FragmentContactUsBinding
import com.devbrian.osebo.ui.ContactUsViewModel
import com.google.android.material.snackbar.Snackbar

class ContactUsFragment : Fragment() {

    private lateinit var binding: FragmentContactUsBinding
    private val viewModel: ContactUsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactUsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
        setupClickListeners()
    }

    private fun setupUI() {
        // Setup category dropdown
        val categories = listOf(
            "General Inquiry",
            "Technical Support",
            "Billing Issue",
            "Account Issue",
            "Feature Request",
            "Bug Report",
            "Partnership"
        )

        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, categories)
        binding.categoryDropdown.setAdapter(adapter)
    }

    private fun setupObservers() {
        viewModel.contactInfo.observe(viewLifecycleOwner) { contact ->
            contact?.let {
                binding.phoneValue.text = it.phone
                binding.emailValue.text = it.email
                binding.addressValue.text = it.address
                binding.hoursValue.text = it.workingHours
            }
        }

        viewModel.messageSent.observe(viewLifecycleOwner) { success ->
            if (success) {
                showSnackbar("Message sent successfully")
                clearForm()
            }
        }
    }

    private fun setupClickListeners() {
        // Quick actions
        binding.callButton.setOnClickListener {
            makePhoneCall()
        }

        binding.emailButton.setOnClickListener {
            sendEmail()
        }

        binding.mapsButton.setOnClickListener {
            openMaps()
        }

        // FAQ buttons
        binding.faq1Button.setOnClickListener {
            openFaqDetail(1)
        }

        binding.faq2Button.setOnClickListener {
            openFaqDetail(2)
        }

        binding.faq3Button.setOnClickListener {
            openFaqDetail(3)
        }

        binding.viewAllFaqButton.setOnClickListener {
            openAllFaq()
        }

        // Submit button
        binding.submitButton.setOnClickListener {
            submitSupportMessage()
        }
    }

    private fun makePhoneCall() {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${binding.phoneValue.text}")
            }
            startActivity(intent)
        } catch (e: Exception) {
            showSnackbar("Cannot make phone call")
        }
    }

    private fun sendEmail() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${binding.emailValue.text}")
                putExtra(Intent.EXTRA_SUBJECT, "Osebo Support Request")
            }
            startActivity(intent)
        } catch (e: Exception) {
            showSnackbar("No email app found")
        }
    }

    private fun openMaps() {
        try {
            val uri = Uri.parse("geo:0,0?q=${Uri.encode(binding.addressValue.text.toString())}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception) {
            showSnackbar("Cannot open maps")
        }
    }

    private fun openFaqDetail(faqId: Int) {
        val dialog = FaqDetailDialogFragment.newInstance(faqId)
        dialog.show(childFragmentManager, "FaqDetailDialog")
    }

    private fun openAllFaq() {
        val action = ContactUsFragmentDirections.actionContactUsFragmentToFaqFragment()
        findNavController().navigate(action)
    }

    private fun submitSupportMessage() {
        val name = binding.nameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val subject = binding.subjectEditText.text.toString()
        val category = binding.categoryDropdown.text.toString()
        val message = binding.messageEditText.text.toString()

        if (name.isBlank() || email.isBlank() || subject.isBlank() || category.isBlank() || message.isBlank()) {
            showSnackbar("Please fill in all fields")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showSnackbar("Please enter a valid email address")
            return
        }

        viewModel.sendSupportMessage(name, email, subject, category, message)
    }

    private fun clearForm() {
        binding.nameEditText.text?.clear()
        binding.emailEditText.text?.clear()
        binding.subjectEditText.text?.clear()
        binding.categoryDropdown.text?.clear()
        binding.messageEditText.text?.clear()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}