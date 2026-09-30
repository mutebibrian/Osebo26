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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.DrawableRes
import com.devbrian.osebo.R
import com.devbrian.osebo.ui.theme.oseboFontFamily

enum class UserNavigationItem(
    val label: String,
    @param:DrawableRes val iconRes: Int,
) {
    Home("Home", R.drawable.ic_iconsax_home),
    Inventory("Inventory", R.drawable.ic_iconsax_inventory),
    Sales("Sales", R.drawable.ic_iconsax_sales),
    More("More", R.drawable.ic_iconsax_more),
}

@Composable
fun UserBottomNavigation(
    selectedItem: UserNavigationItem,
    onItemSelected: (UserNavigationItem) -> Unit,
    onOseboAiClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color(0x180C2434),
                    spotColor = Color(0x180C2434),
                )
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFFF7F8F9))
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserNavigationItem.entries.forEach { item ->
                NavigationTab(
                    item = item,
                    selected = item == selectedItem,
                    onClick = { onItemSelected(item) },
                )
            }
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x260075BC),
                    spotColor = Color(0x260075BC),
                )
                .clip(CircleShape)
                .background(Color(0xFF0783CF))
                .clickable(
                    role = Role.Button,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onOseboAiClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.oseboicon),
                contentDescription = "Open Osebo AI",
                modifier = Modifier.size(39.dp),
            )
        }
    }
}

@Composable
private fun NavigationTab(
    item: UserNavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = if (selected) Color(0xFF101820) else Color(0xFF92999F)
    val poppins = oseboFontFamily()

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
            painter = painterResource(item.iconRes),
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
                    fontFamily = poppins,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}
