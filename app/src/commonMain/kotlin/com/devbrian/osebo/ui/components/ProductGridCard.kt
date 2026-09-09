package com.devbrian.osebo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devbrian.osebo.ui.theme.OseboColors
import com.devbrian.osebo.ui.theme.OseboShapes

/**
 * Grid card for a purchasable/stock item: image area, name, price, and
 * either a quantity stepper (already in cart) or an add button.
 */
@Composable
fun ProductGridCard(
    name: String,
    price: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    quantity: Int = 0,
    onAdd: () -> Unit = {},
    onIncrement: () -> Unit = {},
    onDecrement: () -> Unit = {},
) {
    SectionCard(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.3f)
                    .background(OseboColors.SurfaceVariant, OseboShapes.Card),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = null,
                    tint = OseboColors.TextHint,
                    modifier = Modifier.size(36.dp),
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = OseboColors.Primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(20.dp)
                        .background(OseboColors.Surface, CircleShape),
                )
            }
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(price, style = MaterialTheme.typography.titleSmall, color = OseboColors.Primary)
                if (quantity > 0) {
                    QuantityStepper(quantity = quantity, onIncrement = onIncrement, onDecrement = onDecrement)
                } else {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = OseboColors.OnPrimary,
                        modifier = Modifier
                            .size(26.dp)
                            .background(OseboColors.Primary, CircleShape)
                            .clickable(onClick = onAdd)
                            .padding(4.dp),
                    )
                }
            }
        }
    }
}
