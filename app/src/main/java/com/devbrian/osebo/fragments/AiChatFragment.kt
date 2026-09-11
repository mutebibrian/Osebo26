package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.adapters.AiChatAdapter
import com.devbrian.osebo.databinding.FragmentAiChatBinding
import com.devbrian.osebo.ui.viewmodels.AiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AiChatFragment : Fragment() {

    private var _binding: FragmentAiChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AiViewModel by viewModels()
    private lateinit var chatAdapter: AiChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        chatAdapter = AiChatAdapter()
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }

        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        binding.sendButton.setOnClickListener {
            sendCurrentMessage()
        }

        binding.messageEditText.setOnEditorActionListener { _, _, _ ->
            sendCurrentMessage()
            true
        }

        binding.chipPrompt1.setOnClickListener { viewModel.sendMessage(binding.chipPrompt1.text.toString()) }
        binding.chipPrompt2.setOnClickListener { viewModel.sendMessage(binding.chipPrompt2.text.toString()) }
        binding.chipPrompt3.setOnClickListener { viewModel.sendMessage(binding.chipPrompt3.text.toString()) }
    }

    private fun sendCurrentMessage() {
        val text = binding.messageEditText.text?.toString().orEmpty()
        if (text.isBlank()) return
        viewModel.sendMessage(text)
        binding.messageEditText.text?.clear()
    }

    private fun setupObservers() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            val hasMessages = messages.isNotEmpty()
            binding.emptyStateLayout.visibility = if (hasMessages) View.GONE else View.VISIBLE
            binding.messagesRecyclerView.visibility = if (hasMessages) View.VISIBLE else View.GONE

            chatAdapter.submitList(messages)
            if (hasMessages) {
                binding.messagesRecyclerView.post {
                    binding.messagesRecyclerView.smoothScrollToPosition(messages.size - 1)
                }
            }
        }

        viewModel.isTyping.observe(viewLifecycleOwner) { isTyping ->
            binding.typingIndicatorLayout.visibility = if (isTyping) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
