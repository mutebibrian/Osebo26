package com.devbrian.osebo.ui.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding

data class MoreMenuEntry(val label: String, val onClick: () -> Unit)

// Named Shared* because androidMain already has its own, unrelated
// MoreMenuSection in this same package (MoreMenuSheet.kt, its real "More"
// bottom sheet, actively used by MainActivity.kt) — same name in the same
// package is a hard redeclaration error once both source sets compile
// together for the Android target.
data class SharedMoreMenuSection(val title: String, val entries: List<MoreMenuEntry>)

/**
 * Mirrors Android's "More" bottom sheet (reached via its 4th bottom-nav item,
 * UserNavigationItem.More — see refreshMoreMenuSections() in MainActivity.kt)
 * section-for-section: Shop, Business, People, Account & Support. Android
 * also filters each section's items by permission/ownership (isOwner,
 * hasPermission(VIEW_FINANCE), etc.) — iOS has no permission system ported
 * yet (a standing, documented gap), so every item here is shown to every
 * signed-in user regardless of role. Transfers, User Roles, Account and
 * Contact Us have no commonMain screen yet and are left out rather than
 * wired to dead buttons.
 */
@Composable
fun MoreMenu(
    sections: List<SharedMoreMenuSection>,
    expanded: Boolean,
    onDismiss: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        sections.forEachIndexed { index, section ->
            if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = section.title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7D8793),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
            section.entries.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(entry.label) },
                    onClick = entry.onClick,
                )
            }
        }
    }
}
