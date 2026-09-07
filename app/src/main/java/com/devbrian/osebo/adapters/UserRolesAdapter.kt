package com.devbrian.osebo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R
import com.devbrian.osebo.data.models.UserRole
import com.devbrian.osebo.viewholders.UserRoleViewHolder

class UserRolesAdapter(
    private val userRoles: List<UserRole>,
    private val onRoleClick: (UserRole) -> Unit
) : RecyclerView.Adapter<UserRoleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRoleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_role, parent, false)
        return UserRoleViewHolder(view)
    }


    override fun onBindViewHolder(holder: UserRoleViewHolder, position: Int) {
        val role = userRoles[position]
        holder.bind(role)
        holder.itemView.setOnClickListener { onRoleClick(role) }

        holder.editButton.setOnClickListener {
            onRoleClick(role)
        }
    }

    override fun getItemCount(): Int = userRoles.size
}


