package com.devbrian.osebo.ui.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

data class MoreMenuEntry(val label: String, val onClick: () -> Unit)

/**
 * First cut of the "More" overflow Android reaches via its 6th bottom-nav
 * item (UserNavigationItem.More) — a bottom sheet there with several
 * sections (Shop, Business, People, Account & Support). This starts with
 * just the entries that already have a real commonMain screen behind them;
 * the rest of Android's menu (Transfers, User Roles, Account, Contact Us,
 * Logout) has no screen here yet.
 */
@Composable
fun MoreMenu(
    entries: List<MoreMenuEntry>,
    expanded: Boolean,
    onDismiss: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        entries.forEach { entry ->
            DropdownMenuItem(
                text = { Text(entry.label) },
                onClick = entry.onClick,
            )
        }
    }
}
