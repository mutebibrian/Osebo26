package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.devbrian.osebo.models.Session
import com.devbrian.osebo.ui.screens.SessionsScreen
import com.devbrian.osebo.ui.theme.OseboTheme
import com.devbrian.osebo.ui.viewmodels.SessionsViewModel
import com.google.android.material.snackbar.Snackbar

class SessionsFragment : Fragment() {

    private val viewModel: SessionsViewModel by viewModels()
    private var sessions by mutableStateOf(emptyList<Session>())
    private var isLoading by mutableStateOf(false)
    private var selectedSession by mutableStateOf<Session?>(null)
    private var confirmAll by mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            OseboTheme {
                SessionsScreen(
                    sessions = sessions,
                    isLoading = isLoading,
                    selectedSession = selectedSession,
                    confirmAll = confirmAll,
                    onBack = { findNavController().navigateUp() },
                    onRefresh = viewModel::loadSessions,
                    onSessionClick = ::selectSession,
                    onTerminateAll = { confirmAll = true },
                    onDismissDialog = ::dismissDialog,
                    onConfirmSession = ::terminateSession,
                    onConfirmAll = ::terminateAll,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.sessions.observe(viewLifecycleOwner) { sessions = it }
        viewModel.loading.observe(viewLifecycleOwner) { isLoading = it }
        viewModel.terminateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) Snackbar.make(view, "Session terminated successfully", Snackbar.LENGTH_SHORT).show()
        }
        viewModel.loadSessions()
    }

    private fun selectSession(session: Session) {
        if (session.isCurrent) {
            view?.let { Snackbar.make(it, "This is your current session", Snackbar.LENGTH_SHORT).show() }
        } else {
            selectedSession = session
        }
    }

    private fun terminateSession(session: Session) {
        dismissDialog()
        viewModel.terminateSession(session.id)
    }

    private fun terminateAll() {
        dismissDialog()
        viewModel.terminateAllSessions()
    }

    private fun dismissDialog() {
        selectedSession = null
        confirmAll = false
    }
}
