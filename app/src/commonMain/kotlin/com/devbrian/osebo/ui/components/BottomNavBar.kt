package com.devbrian.osebo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.devbrian.osebo.ui.theme.OseboColors
import com.devbrian.osebo.ui.theme.OseboShapes

data class NavItem(val icon: ImageVector, val label: String)

/**
 * Floating rounded bottom navigation bar; the selected item renders as a
 * filled rounded-square badge in the brand color, matching the mockup.
 */
@Composable
fun BottomNavBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = OseboShapes.BottomBar,
        color = OseboColors.Surface,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (selected) OseboColors.OnPrimary else OseboColors.IconInactive,
                    modifier = Modifier
                        .size(44.dp)
                        .let { base ->
                            if (selected) base.background(OseboColors.Primary, OseboShapes.CardSmall) else base
                        }
                        .clickable { onItemSelected(index) }
                        .padding(10.dp),
                )
            }
        }
    }
}
