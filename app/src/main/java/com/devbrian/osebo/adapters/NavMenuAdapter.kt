package com.devbrian.osebo.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devbrian.osebo.R

class NavMenuAdapter(
    private val menuItems: List<NavMenuItem>,
    private val onItemClick: (NavMenuItem) -> Unit
) : RecyclerView.Adapter<NavMenuAdapter.MenuViewHolder>() {

    data class NavMenuItem(
        val id: Int,
        val title: String,
        val icon: Int,
        val isEnabled: Boolean = true
    )

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.nav_item_icon)
        private val title: TextView = itemView.findViewById(R.id.nav_item_title)

        fun bind(item: NavMenuItem, onClick: (NavMenuItem) -> Unit) {
            icon.setImageResource(item.icon)
            title.text = item.title
            itemView.isEnabled = item.isEnabled

            itemView.setOnClickListener {
                if (item.isEnabled) {
                    onClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_navigation_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(menuItems[position], onItemClick)
    }

    override fun getItemCount() = menuItems.size
}