package com.devbrian.osebo.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.devbrian.osebo.R
import com.devbrian.osebo.data.remote.dto.response.UserDto
import com.devbrian.osebo.databinding.FragmentAccountBinding
import com.devbrian.osebo.ui.viewmodels.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModel()

    private val titles = arrayOf("Mr", "Mrs", "Ms", "Miss", "Dr")
    private var selectedPhotoUri: Uri? = null

    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedPhotoUri = it
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivProfilePhoto)
            // No confirmed avatar-upload endpoint exists yet - this previews the pick
            // locally only. Uploading it to the server is a follow-up once that endpoint
            // is available.
            Toast.makeText(requireContext(), "Photo selected (not yet uploaded to server)", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupTitleDropdown()
        setupObservers()
        setupClickListeners()
        prefillFromCache()
    }

    private fun setupTitleDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, titles)
        binding.spinnerTitle.setAdapter(adapter)
    }

    private fun prefillFromCache() {
        binding.etFirstName.setText(viewModel.cachedFirstName())
        binding.etLastName.setText(viewModel.cachedLastName())
        binding.tvVerifiedEmail.text = viewModel.cachedEmail().ifEmpty { "No email on file" }
        binding.etPhone.setText(stripCountryCode(viewModel.cachedPhone()))
    }

    private fun stripCountryCode(fullPhone: String): String {
        if (fullPhone.isEmpty()) return ""
        val prefix = binding.ccp.selectedCountryCodeWithPlus
        return if (fullPhone.startsWith(prefix)) fullPhone.removePrefix(prefix) else fullPhone
    }

    private fun setupObservers() {
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile?.let { applyProfile(it) }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isSavingProfile.observe(viewLifecycleOwner) { saving ->
            binding.btnSaveUpdates.isEnabled = !saving
            binding.progressBottom.visibility = if (saving) View.VISIBLE else View.GONE
        }

        viewModel.isSavingPassword.observe(viewLifecycleOwner) { saving ->
            binding.btnSavePassword.isEnabled = !saving
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                showSnackbar(it)
                viewModel.clearMessages()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                showSnackbar(it)
                viewModel.clearMessages()
            }
        }
    }

    private fun applyProfile(profile: UserDto) {
        profile.title?.let { title ->
            if (titles.any { it.equals(title, ignoreCase = true) }) {
                binding.spinnerTitle.setText(title, false)
            }
        }
        if (!profile.firstName.isNullOrEmpty()) binding.etFirstName.setText(profile.firstName)
        if (!profile.lastName.isNullOrEmpty()) binding.etLastName.setText(profile.lastName)
        if (!profile.email.isNullOrEmpty()) binding.tvVerifiedEmail.text = profile.email
        if (!profile.phone.isNullOrEmpty()) binding.etPhone.setText(stripCountryCode(profile.phone))

        binding.chipVerified.visibility = if (profile.isEmailVerified == false) View.GONE else View.VISIBLE

        if (!profile.photo.isNullOrEmpty() && selectedPhotoUri == null) {
            Glide.with(this)
                .load(profile.photo)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivProfilePhoto)
        }
    }

    private fun setupClickListeners() {
        binding.btnUploadPhoto.setOnClickListener {
            pickPhotoLauncher.launch("image/*")
        }

        binding.btnResetPhoto.setOnClickListener {
            selectedPhotoUri = null
            binding.ivProfilePhoto.setImageResource(R.drawable.ic_person)
        }

        binding.btnChangeEmail.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Change Email")
                .setMessage("Email can only be added or changed through the verification flow. This isn't available yet in the app.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.btnSaveUpdates.setOnClickListener {
            saveProfile()
        }

        binding.btnSavePassword.setOnClickListener {
            savePassword()
        }
    }

    private fun saveProfile() {
        val title = binding.spinnerTitle.text?.toString()?.trim().takeUnless { it.isNullOrEmpty() }
        val firstName = binding.etFirstName.text?.toString()?.trim().orEmpty()
        val lastName = binding.etLastName.text?.toString()?.trim().orEmpty()
        val phoneDigits = binding.etPhone.text?.toString()?.trim().orEmpty()

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(requireContext(), "First and last name are required", Toast.LENGTH_SHORT).show()
            return
        }

        val fullPhone = if (phoneDigits.isNotEmpty()) {
            binding.ccp.selectedCountryCodeWithPlus + phoneDigits
        } else {
            ""
        }

        viewModel.saveProfile(title, firstName, lastName, fullPhone)
    }

    private fun savePassword() {
        val currentPassword = binding.etCurrentPassword.text?.toString().orEmpty()
        val newPassword = binding.etNewPassword.text?.toString().orEmpty()
        val confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()

        if (newPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a new password", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword.length < 8) {
            Toast.makeText(requireContext(), "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords don't match", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.savePassword(currentPassword, newPassword)
        binding.etCurrentPassword.text?.clear()
        binding.etNewPassword.text?.clear()
        binding.etConfirmPassword.text?.clear()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
