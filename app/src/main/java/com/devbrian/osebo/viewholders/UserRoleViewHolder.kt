package com.devbrian.osebo.viewholders

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.data.models.UserRole
import java.util.Locale

class UserRoleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val roleNameTextView: TextView = itemView.findViewById(R.id.roleNameTextView)
    private val roleDescriptionTextView: TextView = itemView.findViewById(R.id.roleDescriptionTextView)
    val editButton: Button = itemView.findViewById(R.id.editButton)

    fun bind(role: UserRole) {
        roleNameTextView.text = role.name
        roleDescriptionTextView.text = role.description

        
        when (role.name.lowercase(Locale.ROOT)) {
            "owner" -> {
                editButton.visibility = View.GONE 
            }
            "manager" -> {
                editButton.text = "Edit Permissions"
            }
            "staff" -> {
                editButton.text = "Edit Permissions"
            }
        }
    }
}


