package com.devbrian.osebo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.devbrian.osebo.ui.theme.OseboColors
import com.devbrian.osebo.ui.theme.OseboShapes

enum class BadgeTone { Success, Warning, Error, Neutral }

private fun BadgeTone.colors(): Pair<Color, Color> = when (this) {
    BadgeTone.Success -> OseboColors.SuccessBg to OseboColors.Success
    BadgeTone.Warning -> OseboColors.WarningBg to OseboColors.Warning
    BadgeTone.Error -> OseboColors.ErrorBg to OseboColors.Error
    BadgeTone.Neutral -> OseboColors.PrimaryLight to OseboColors.PrimaryDark
}

/** Small rounded status label, e.g. "Paid" / "Pending" / "General". */
@Composable
fun StatusBadge(text: String, tone: BadgeTone, modifier: Modifier = Modifier) {
    val (bg, fg) = tone.colors()
    Text(
        text = text,
        color = fg,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .background(bg, OseboShapes.Chip)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
