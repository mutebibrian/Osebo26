package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.DialogFaqDetailBinding
class FaqDetailDialogFragment : DialogFragment() {

    private lateinit var binding: DialogFaqDetailBinding
    private var faqId: Int = 1

    companion object {
        private const val ARG_FAQ_ID = "faq_id"

        fun newInstance(faqId: Int): FaqDetailDialogFragment {
            val fragment = FaqDetailDialogFragment()
            val args = Bundle()
            args.putInt(ARG_FAQ_ID, faqId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        faqId = arguments?.getInt(ARG_FAQ_ID) ?: 1

        
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFaqDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        
        when (faqId) {
            1 -> {
                binding.titleTextView.text = "How do I reset my password?"
                binding.answerTextView.text = "To reset your password:\n\n1. Go to the Login screen\n2. Click on 'Forgot Password'\n3. Enter your registered email address\n4. Check your email for the reset link\n5. Click the link and follow the instructions\n\nIf you don't receive the email, please check your spam folder or contact support."
            }
            2 -> {
                binding.titleTextView.text = "How to update billing information?"
                binding.answerTextView.text = "To update your billing information:\n\n1. Go to Settings → Account\n2. Click on 'Update Payment Method'\n3. Enter your new billing details\n4. Verify the information and save\n\nNote: Changes may take up to 24 hours to reflect in your account."
            }
            3 -> {
                binding.titleTextView.text = "How to add new employees?"
                binding.answerTextView.text = "To add new employees to your shop:\n\n1. Go to Management → Employees\n2. Click on 'Add Employee' button\n3. Fill in employee details (name, email, phone, role)\n4. Set permissions and access levels\n5. Save and the employee will receive an invitation email\n\nYou can manage employee permissions from the User Roles section."
            }
            else -> {
                binding.titleTextView.text = "FAQ"
                binding.answerTextView.text = "Information not available."
            }
        }
    }

    private fun setupClickListeners() {
        binding.closeButton.setOnClickListener {
            dismiss()
        }

        binding.contactSupportButton.setOnClickListener {
            
            dismiss()
            
        }
    }

    override fun onStart() {
        super.onStart()

        
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}


