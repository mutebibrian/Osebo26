package com.devbrian.osebo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.devbrian.osebo.ui.theme.OseboColors
import com.devbrian.osebo.ui.theme.OseboShapes

/**
 * Pill-shaped filter/segment control, e.g. the "All / Fruits / Shakes / ..."
 * category row or the "Customer / Supplier / All Payment" segmented filter.
 */
@Composable
fun PillTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) OseboColors.Primary else OseboColors.Surface
    val fg = if (selected) OseboColors.OnPrimary else OseboColors.OnSurfaceVariant

    var pillModifier = modifier
        .clip(OseboShapes.Pill)
        .background(bg)
    if (!selected) {
        pillModifier = pillModifier.border(BorderStroke(1.dp, OseboColors.Divider), OseboShapes.Pill)
    }

    Text(
        text = text,
        color = fg,
        style = MaterialTheme.typography.labelMedium,
        modifier = pillModifier
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
    )
}
