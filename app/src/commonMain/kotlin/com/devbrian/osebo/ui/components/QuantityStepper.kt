package com.devbrian.osebo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.devbrian.osebo.ui.theme.OseboColors

/** Rounded -/+ quantity control used on product cards and cart rows. */
@Composable
fun QuantityStepper(
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        StepperCircle(icon = Icons.Filled.Remove, onClick = onDecrement, filled = false)
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
        StepperCircle(icon = Icons.Filled.Add, onClick = onIncrement, filled = true)
    }
}

@Composable
private fun StepperCircle(
    icon: ImageVector,
    onClick: () -> Unit,
    filled: Boolean,
) {
    val bg = if (filled) OseboColors.Primary else OseboColors.SurfaceVariant
    val tint = if (filled) OseboColors.OnPrimary else OseboColors.OnSurfaceVariant
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .size(26.dp)
            .background(bg, CircleShape)
            .clickable(onClick = onClick)
            .padding(4.dp),
    )
}
