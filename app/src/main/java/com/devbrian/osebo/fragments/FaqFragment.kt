package com.devbrian.osebo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbrian.osebo.adapters.FaqAdapter
import com.devbrian.osebo.databinding.FragmentFaqBinding

class FaqFragment : Fragment() {

    private lateinit var binding: FragmentFaqBinding
    private lateinit var faqAdapter: FaqAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFaqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadFaqs()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        faqAdapter = FaqAdapter(emptyList()) { faq ->
            openFaqDetail(faq.id)
        }

        binding.faqRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = faqAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadFaqs() {
        
        val faqs = listOf(
            FaqItem(1, "How do I reset my password?", "Account", false),
            FaqItem(2, "How to update billing information?", "Billing", false),
            FaqItem(3, "How to add new employees?", "Management", false),
            FaqItem(4, "How to generate sales reports?", "Reports", false),
            FaqItem(5, "How to manage inventory?", "Inventory", false),
            FaqItem(6, "What payment methods do you accept?", "Billing", false),
            FaqItem(7, "How to contact customer support?", "Support", false),
            FaqItem(8, "How to cancel my subscription?", "Subscription", false)
        )

        faqAdapter.updateFaqs(faqs)
    }

    private fun openFaqDetail(faqId: Int) {
        val dialog = FaqDetailDialogFragment.newInstance(faqId)
        dialog.show(childFragmentManager, "FaqDetailDialog")
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.searchButton.setOnClickListener {
            
        }

        binding.contactSupportButton.setOnClickListener {
            
            requireActivity().onBackPressed()
        }
    }
}


data class FaqItem(
    val id: Int,
    val question: String,
    val category: String,
    var isExpanded: Boolean = false
)

