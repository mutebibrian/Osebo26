package com.devbrian.osebo.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.models.contact.ContactInfo
import com.devbrian.osebo.ui.screens.ContactInfoUi
import com.devbrian.osebo.ui.screens.ContactUsScreen
import com.devbrian.osebo.ui.screens.SupportMessageUi
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.ContactUsViewModel
import com.google.android.material.snackbar.Snackbar

class ContactUsFragment : Fragment() {

    private val viewModel: ContactUsViewModel by viewModels()
    private var contactInfo by mutableStateOf(ContactInfoUi())
    private var isLoading by mutableStateOf(false)
    private var resetKey by mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                ContactUsScreen(
                    contact = contactInfo,
                    isLoading = isLoading,
                    resetKey = resetKey,
                    onBack = { findNavController().navigateUp() },
                    onCall = ::makePhoneCall,
                    onEmail = ::sendEmail,
                    onWhatsApp = ::openWhatsApp,
                    onMaps = ::openMaps,
                    onFaq = ::openFaqDetail,
                    onViewAllFaqs = ::openAllFaq,
                    onSubmit = ::submitSupportMessage,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.contactInfo.observe(viewLifecycleOwner) { contactInfo = it.toUi() }
        viewModel.loading.observe(viewLifecycleOwner) { isLoading = it }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                showSnackbar(error)
                viewModel.clearError()
            }
        }
        viewModel.messageSent.observe(viewLifecycleOwner) { success ->
            if (success) {
                showSnackbar("Message sent successfully")
                resetKey++
                viewModel.clearMessageSent()
            }
        }
    }

    private fun makePhoneCall() = launchIntent(
        intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contactInfo.phone}")),
        failureMessage = "Cannot make phone call",
    )

    private fun sendEmail() = launchIntent(
        intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${contactInfo.email}")
            putExtra(Intent.EXTRA_SUBJECT, "Osebo Support Request")
        },
        failureMessage = "No email app found",
    )

    private fun openWhatsApp() {
        val number = contactInfo.whatsapp.filter(Char::isDigit)
        if (number.isBlank()) {
            showSnackbar("WhatsApp contact is unavailable")
            return
        }
        launchIntent(
            intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number")),
            failureMessage = "Cannot open WhatsApp",
        )
    }

    private fun openMaps() {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(contactInfo.address)}")
        launchIntent(Intent(Intent.ACTION_VIEW, uri), "Cannot open maps")
    }

    private fun launchIntent(intent: Intent, failureMessage: String) {
        runCatching { startActivity(intent) }
            .onFailure { showSnackbar(failureMessage) }
    }

    private fun openFaqDetail(faqId: Int) {
        FaqDetailDialogFragment.newInstance(faqId)
            .show(childFragmentManager, "FaqDetailDialog")
    }

    private fun openAllFaq() {
        findNavController().navigate(ContactUsFragmentDirections.actionContactUsFragmentToFaqFragment())
    }

    private fun submitSupportMessage(message: SupportMessageUi) {
        if (
            message.name.isBlank() ||
            message.email.isBlank() ||
            message.subject.isBlank() ||
            message.category.isBlank() ||
            message.message.isBlank()
        ) {
            showSnackbar("Please fill in all fields")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(message.email).matches()) {
            showSnackbar("Please enter a valid email address")
            return
        }
        viewModel.sendSupportMessage(
            name = message.name.trim(),
            email = message.email.trim(),
            subject = message.subject.trim(),
            category = message.category,
            message = message.message.trim(),
        )
    }

    private fun showSnackbar(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }
}

private fun ContactInfo.toUi() = ContactInfoUi(
    phone = phone,
    email = email,
    whatsapp = whatsapp.orEmpty(),
    address = address.orEmpty(),
    workingHours = workingHours.orEmpty(),
)
