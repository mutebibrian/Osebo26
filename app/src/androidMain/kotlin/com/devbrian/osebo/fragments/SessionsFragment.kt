package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.SessionsAdapter
import com.devbrian.osebo.databinding.FragmentSessionsBinding
import com.devbrian.osebo.models.Session
import com.devbrian.osebo.ui.viewmodels.SessionsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SessionsFragment : Fragment() {

    private lateinit var binding: FragmentSessionsBinding
    private val viewModel: SessionsViewModel by viewModels()
    private lateinit var sessionsAdapter: SessionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSessionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
        setupClickListeners()
        loadSessions()
    }

    private fun setupUI() {
        
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        
        sessionsAdapter = SessionsAdapter(emptyList()) { session ->
            showSessionActions(session)
        }

        binding.sessionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sessionsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.sessions.observe(viewLifecycleOwner) { sessions ->
            if (sessions.isEmpty()) {
                showEmptyState()
            } else {
                showSessionsList(sessions)
                sessionsAdapter.updateSessions(sessions)
            }
        }

        viewModel.terminateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                showSnackbar("Session terminated successfully")
                loadSessions()
            }
        }
    }

    private fun setupClickListeners() {
        binding.terminateAllButton.setOnClickListener {
            showTerminateAllConfirmation()
        }

        binding.refreshButton.setOnClickListener {
            loadSessions()
        }
    }

    private fun loadSessions() {
        viewModel.loadSessions()
        binding.swipeRefreshLayout.isRefreshing = true
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.sessionsRecyclerView.visibility = View.GONE
        binding.terminateAllButton.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun showSessionsList(sessions: List<Session>) {
        binding.emptyStateLayout.visibility = View.GONE
        binding.sessionsRecyclerView.visibility = View.VISIBLE
        binding.terminateAllButton.visibility = View.VISIBLE
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun showSessionActions(session: Session) {
        if (session.isCurrent) {
            showSnackbar("This is your current session")
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Session Actions")
            .setMessage("Device: ${session.device}\nIP: ${session.ipAddress}\nLast Active: ${session.lastActive}")
            .setPositiveButton("Terminate") { _, _ ->
                viewModel.terminateSession(session.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTerminateAllConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Terminate All Sessions")
            .setMessage("This will log you out from all other devices except this one.")
            .setPositiveButton("Terminate All") { _, _ ->
                viewModel.terminateAllSessions()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}


