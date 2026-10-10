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
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.ui.screens.OseboAiScreen
import com.devbrian.osebo.ui.theme.OseboTheme

class OseboAiFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val preferences = PreferenceManager.getInstance(requireContext())
        val firstName = preferences.getFirstName().ifBlank {
            preferences.getUserName().trim().substringBefore(" ").ifBlank { "there" }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OseboTheme {
                    OseboAiScreen(
                        firstName = firstName,
                        onBackClick = { findNavController().navigateUp() },
                        onSettingsClick = {
                            Toast.makeText(requireContext(), "AI settings are coming soon", Toast.LENGTH_SHORT).show()
                        },
                        onSubmit = {
                            Toast.makeText(requireContext(), "Osebo AI is coming soon", Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }
        }
    }
}
