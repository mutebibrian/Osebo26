package com.devbrian.osebo.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.oseboicon
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

data class NavItem(val icon: Painter, val label: String)

private val NavPillBackground = Color(0xFFF7F8F9)
private val NavSelectedContent = Color(0xFF101820)
private val NavUnselectedContent = Color(0xFF92999F)
private val AiCircleColor = Color(0xFF0783CF)

/**
 * Mirrors androidMain's UserBottomNavigation exactly (same colors, sizes,
 * shadow, and the AI button inline in the same row rather than floated
 * separately) so the two platforms look identical.
 */
@Composable
fun BottomNavBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onAiClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color(0x300C2434),
                    spotColor = Color(0x400C2434),
                )
                .clip(RoundedCornerShape(32.dp))
                .background(NavPillBackground)
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                NavigationTab(
                    item = item,
                    selected = index == selectedIndex,
                    onClick = { onItemSelected(index) },
                )
            }
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x300075BC),
                    spotColor = Color(0x400075BC),
                )
                .clip(CircleShape)
                .background(AiCircleColor)
                .clickable(
                    role = Role.Button,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onAiClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.oseboicon),
                contentDescription = "Open Osebo AI",
                modifier = Modifier.size(39.dp),
            )
        }
    }
}

@Composable
private fun NavigationTab(item: NavItem, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = if (selected) NavSelectedContent else NavUnselectedContent
    val font = oseboFontFamily()

    Row(
        modifier = Modifier
            .fillMaxHeight()
            .animateContentSize()
            .clip(CircleShape)
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(
                role = Role.Tab,
                indication = null,
                interactionSource = interactionSource,
                onClick = onClick,
            )
            .padding(horizontal = if (selected) 14.dp else 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        if (selected) {
            Text(
                text = item.label,
                modifier = Modifier.padding(start = 7.dp),
                color = contentColor,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = font,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}
