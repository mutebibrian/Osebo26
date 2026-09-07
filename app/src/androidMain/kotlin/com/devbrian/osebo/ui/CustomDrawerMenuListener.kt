package com.devbrian.osebo.ui

import android.util.Log
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView

class CustomDrawerMenuListener(
    private val navigationView: NavigationView,
    private val onItemSelected: (MenuItem) -> Boolean
) : NavigationView.OnNavigationItemSelectedListener {

    init {
        setupMenuItemListeners()
    }

    private fun setupMenuItemListeners() {
        Log.d("NavDrawer_DEBUG", "🔥 Setting up CUSTOM menu item listeners...")

        val menu = navigationView.menu
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            try {
                
                item.isCheckable = false
                Log.d("NavDrawer_DEBUG", "✅ Menu item ready: ${item.title} (ID: ${item.itemId})")
            } catch (e: Exception) {
                Log.e("NavDrawer_DEBUG", "❌ Error: ${e.message}")
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d("NavDrawer_DEBUG", "🟢 CUSTOM listener invoked for: ${item.title}")
        return onItemSelected(item)
    }
}
