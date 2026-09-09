package com.devbrian.osebo.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devbrian.osebo.ui.theme.OseboColors
import com.devbrian.osebo.ui.theme.OseboShapes

/** Full pill-shaped primary action button (e.g. "Continue Billing", "Save Item"). */
@Composable
fun OseboPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = OseboShapes.Pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = OseboColors.Primary,
            contentColor = OseboColors.OnPrimary,
        ),
        contentPadding = PaddingValues(vertical = 14.dp),
        modifier = modifier.height(52.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Pill-shaped secondary/outlined button (e.g. "Cancel", "Change"). */
@Composable
fun OseboOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = OseboShapes.Pill,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = OseboColors.Primary),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        modifier = modifier,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
