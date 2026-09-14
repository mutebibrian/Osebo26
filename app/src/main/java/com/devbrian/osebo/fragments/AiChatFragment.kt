package com.devbrian.osebo.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    // Delegates recording to the system's own speech-recognition UI (Google app on most
    // devices), so no RECORD_AUDIO permission or SpeechRecognizer plumbing is needed here.
    private val voiceInputLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                binding.messageEditText.setText(spokenText)
                binding.messageEditText.setSelection(spokenText.length)
            }
        }
    }

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

        binding.micButton.setOnClickListener {
            startVoiceInput()
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

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask Osebo AI…")
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            voiceInputLauncher.launch(intent)
        } else {
            Toast.makeText(requireContext(), "Voice input isn't available on this device", Toast.LENGTH_SHORT).show()
        }
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
