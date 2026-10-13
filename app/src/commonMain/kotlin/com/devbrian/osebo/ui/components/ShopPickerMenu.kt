package com.devbrian.osebo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class ShopPickerItem(val id: String, val name: String)

/**
 * First cut of shop switching for the shared (iOS) app — functional, not
 * yet styled to match ShopPickerOverlay on Android (which has its own
 * skeleton-loading state and icon-per-shop-type treatment). Anchor this in
 * a Box alongside a ShopPickerButton; [expanded] controls visibility.
 */
@Composable
fun ShopPickerMenu(
    shops: List<ShopPickerItem>,
    selectedShopId: String?,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit,
) {
    Box {
        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "All businesses",
                        fontWeight = if (selectedShopId == null) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                    )
                },
                onClick = { onSelect(null) },
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
            )
            shops.forEach { shop ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = shop.name,
                            fontWeight = if (shop.id == selectedShopId) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                        )
                    },
                    onClick = { onSelect(shop.id) },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
