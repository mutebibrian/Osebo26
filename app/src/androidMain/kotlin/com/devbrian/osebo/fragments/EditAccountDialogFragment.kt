package com.devbrian.osebo.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.devbrian.osebo.R
import com.devbrian.osebo.ui.screens.AccountProfileUi
import com.devbrian.osebo.ui.screens.EditAccountScreen
import com.devbrian.osebo.ui.screens.EditableAccountDetails
import com.devbrian.osebo.ui.theme.OseboTheme

class EditAccountDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Osebo)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OseboTheme {
                    EditAccountScreen(
                        initial = EditableAccountDetails(
                            businessName = requireArguments().getString(ARG_NAME).orEmpty(),
                            businessType = requireArguments().getString(ARG_TYPE).orEmpty(),
                            registrationNumber = requireArguments().getString(ARG_REGISTRATION).orEmpty(),
                            taxId = requireArguments().getString(ARG_TAX).orEmpty(),
                            address = requireArguments().getString(ARG_ADDRESS).orEmpty(),
                        ),
                        onClose = ::dismiss,
                        onSave = ::save,
                    )
                }
            }
        }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.transparent)
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }
    }

    private fun save(details: EditableAccountDetails) {
        parentFragmentManager.setFragmentResult(
            RESULT_KEY,
            bundleOf(
                ARG_NAME to details.businessName,
                ARG_TYPE to details.businessType,
                ARG_REGISTRATION to details.registrationNumber,
                ARG_TAX to details.taxId,
                ARG_ADDRESS to details.address,
            ),
        )
        dismiss()
    }

    companion object {
        const val RESULT_KEY = "edit_account_result"
        const val ARG_NAME = "business_name"
        const val ARG_TYPE = "business_type"
        const val ARG_REGISTRATION = "registration_number"
        const val ARG_TAX = "tax_id"
        const val ARG_ADDRESS = "address"

        fun newInstance(profile: AccountProfileUi) = EditAccountDialogFragment().apply {
            arguments = bundleOf(
                ARG_NAME to profile.businessName,
                ARG_TYPE to profile.businessType,
                ARG_REGISTRATION to profile.registrationNumber,
                ARG_TAX to profile.taxId,
                ARG_ADDRESS to profile.address,
            )
        }
    }
}
