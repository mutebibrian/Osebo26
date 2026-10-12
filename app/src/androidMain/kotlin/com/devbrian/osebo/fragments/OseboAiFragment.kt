package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.ui.screens.OseboAiScreen
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.AiAssistantViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class OseboAiFragment : Fragment() {
    private val viewModel: AiAssistantViewModel by viewModel()

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
                        uiState = viewModel.uiState.value,
                        onBackClick = { findNavController().navigateUp() },
                        onOpenConversation = viewModel::openConversation,
                        onArchiveConversation = viewModel::archiveConversation,
                        onStartNewConversation = viewModel::startNewConversation,
                        onRetry = viewModel::refresh,
                        onSubmit = viewModel::sendMessage,
                    )
                }
            }
        }
    }
}
