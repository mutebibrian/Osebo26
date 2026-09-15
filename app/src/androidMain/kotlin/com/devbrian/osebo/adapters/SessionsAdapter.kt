package com.devbrian.osebo.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.databinding.ItemSessionBinding
import com.devbrian.osebo.models.Session

class SessionsAdapter(
    private var sessions: List<Session>,
    private val onSessionClick: (Session) -> Unit
) : RecyclerView.Adapter<SessionsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSessionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(session: Session) {
            binding.deviceNameTextView.text = session.device
            binding.browserTextView.text = session.browser
            binding.ipAddressTextView.text = session.ipAddress
            binding.locationTextView.text = session.location
            binding.lastActiveTextView.text = session.lastActive

            binding.currentSessionBadge.visibility = if (session.isCurrent) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                onSessionClick(session)
            }

            binding.terminateButton.visibility = if (session.isCurrent) View.GONE else View.VISIBLE
            binding.terminateButton.setOnClickListener {
                onSessionClick(session)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount() = sessions.size

    fun updateSessions(newSessions: List<Session>) {
        sessions = newSessions
        notifyDataSetChanged()
    }
}


