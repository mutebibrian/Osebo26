package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.ui.screens.FinanceSettingsScreen
import com.devbrian.osebo.ui.theme.OseboTheme

class FinanceSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                FinanceSettingsScreen(
                    onBackClick = { findNavController().navigateUp() },
                    onSaveClick = {
                        Toast.makeText(
                            requireContext(),
                            "Finance settings saved for this session",
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                )
            }
        }
    }
}
