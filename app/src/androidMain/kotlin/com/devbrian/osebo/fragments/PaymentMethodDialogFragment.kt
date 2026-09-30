package com.devbrian.osebo.fragments.dialogs

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
import com.devbrian.osebo.ui.screens.PaymentMethodScreen
import com.devbrian.osebo.ui.theme.OseboTheme

class PaymentMethodDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Osebo)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OseboTheme {
                    PaymentMethodScreen(
                        initialMethod = requireArguments().getString(ARG_METHOD).orEmpty(),
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

    private fun save(method: String) {
        parentFragmentManager.setFragmentResult(RESULT_KEY, bundleOf(ARG_METHOD to method))
        dismiss()
    }

    companion object {
        const val RESULT_KEY = "payment_method_result"
        const val ARG_METHOD = "payment_method"

        fun newInstance(method: String) = PaymentMethodDialogFragment().apply {
            arguments = bundleOf(ARG_METHOD to method)
        }
    }
}
