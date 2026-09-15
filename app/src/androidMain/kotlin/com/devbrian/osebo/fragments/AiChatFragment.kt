package com.devbrian.osebo.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.R
import com.devbrian.osebo.adapters.AiChatAdapter
import com.devbrian.osebo.databinding.FragmentAiChatBinding
import com.devbrian.osebo.ui.viewmodels.AiViewModel
import com.devbrian.osebo.utils.VoiceRecorder
import org.koin.androidx.viewmodel.ext.android.viewModel

class AiChatFragment : Fragment() {

    private var _binding: FragmentAiChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AiViewModel by viewModel()
    private lateinit var chatAdapter: AiChatAdapter
    private lateinit var voiceRecorder: VoiceRecorder

    private val requestMicPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startRecording()
        } else {
            Toast.makeText(requireContext(), "Microphone permission is needed for voice input", Toast.LENGTH_SHORT).show()
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

        voiceRecorder = VoiceRecorder(requireContext())

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

        binding.micButton.setOnClickListener {
            if (voiceRecorder.isRecording) {
                stopRecordingAndTranscribe()
            } else {
                onMicTapped()
            }
        }
    }

    private fun onMicTapped() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            startRecording()
        } else {
            requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startRecording() {
        if (voiceRecorder.start()) {
            binding.micButton.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(android.R.color.holo_red_dark))
        } else {
            Toast.makeText(requireContext(), "Couldn't start recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecordingAndTranscribe() {
        resetMicButtonTint()
        val audioFile = voiceRecorder.stop()
        if (audioFile != null) {
            viewModel.transcribeAudio(audioFile)
        }
    }

    private fun resetMicButtonTint() {
        binding.micButton.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.secondary_text_color)
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

        viewModel.isTranscribing.observe(viewLifecycleOwner) { isTranscribing ->
            binding.micButton.isEnabled = !isTranscribing
        }

        viewModel.transcribedText.observe(viewLifecycleOwner) { text ->
            text?.let {
                binding.messageEditText.setText(it)
                binding.messageEditText.setSelection(it.length)
                viewModel.consumeTranscribedText()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (voiceRecorder.isRecording) {
            voiceRecorder.cancel()
        }
        _binding = null
    }
}
