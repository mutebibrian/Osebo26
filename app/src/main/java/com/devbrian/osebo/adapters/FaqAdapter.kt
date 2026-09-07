package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.databinding.ItemFaqBinding
import com.devbrian.osebo.fragments.FaqItem

class FaqAdapter(
    private var faqs: List<FaqItem>,
    private val onFaqClick: (FaqItem) -> Unit
) : RecyclerView.Adapter<FaqAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemFaqBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(faq: FaqItem) {
            binding.questionTextView.text = faq.question
            binding.categoryTextView.text = faq.category
            binding.answerTextView.text = getAnswerForFaq(faq.id)
            binding.answerTextView.visibility = if (faq.isExpanded) ViewGroup.VISIBLE else ViewGroup.GONE
            binding.expandIcon.setImageResource(
                if (faq.isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
            )

            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    faqs[position].isExpanded = !faqs[position].isExpanded
                    notifyItemChanged(position)
                }
                onFaqClick(faq)
            }
        }

        private fun getAnswerForFaq(faqId: Int): String {
            return when (faqId) {
                1 -> "To reset your password, go to Login screen, click 'Forgot Password', enter your email, and follow the instructions."
                2 -> "Update billing info in Settings → Account → Update Payment Method."
                3 -> "Add employees from Management → Employees → Add Employee button."
                4 -> "Generate reports from Dashboard → Reports section."
                5 -> "Manage inventory from Business Operations → Inventory."
                6 -> "We accept mobile money, credit cards, and bank transfers."
                7 -> "Contact support via the Contact Us page or email support@osebo.ai."
                8 -> "Cancel subscription from Settings → Subscription → Cancel."
                else -> "Please contact support for more information."
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFaqBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(faqs[position])
    }

    override fun getItemCount() = faqs.size

    fun updateFaqs(newFaqs: List<FaqItem>) {
        faqs = newFaqs
        notifyDataSetChanged()
    }
}


