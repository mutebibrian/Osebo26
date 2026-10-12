package com.devbrian.osebo.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_box_bold
import com.devbrian.osebo.resources.iconsax_briefcase_bold
import com.devbrian.osebo.resources.iconsax_category_bold
import com.devbrian.osebo.resources.iconsax_coffee_bold
import com.devbrian.osebo.resources.iconsax_global_bold
import com.devbrian.osebo.resources.iconsax_health_bold
import com.devbrian.osebo.resources.iconsax_monitor_bold
import com.devbrian.osebo.resources.iconsax_scissor_bold
import com.devbrian.osebo.resources.iconsax_shop_bold
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val OseboBlue = Color(0xFF087FC4)

data class ShopPickerUiState(
    val shops: List<Shop> = emptyList(),
    val selectedShopId: String = "",
    val fallbackShopName: String = "",
    val isLoading: Boolean = true,
    val isOpen: Boolean = false,
    val isHome: Boolean = false,
    val showTrigger: Boolean = true,
)

@Composable
fun ShopPickerOverlay(
    state: ShopPickerUiState,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (Shop?) -> Unit,
) {
    val selectedShop = state.shops.find { it.id == state.selectedShopId }
    val label = when {
        state.isHome && state.selectedShopId.isBlank() -> "All businesses"
        selectedShop != null -> selectedShop.name
        state.fallbackShopName.isNotBlank() -> state.fallbackShopName
        else -> "Choose a shop"
    }
    val triggerIcon = if (state.isHome && state.selectedShopId.isBlank()) {
        Res.drawable.iconsax_category_bold
    } else {
        shopCategoryIcon(selectedShop?.shopType)
    }

    Box(contentAlignment = Alignment.TopEnd) {
        if (!state.isHome && state.showTrigger) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F6F7))
                    .clickable(onClick = onOpen),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(triggerIcon),
                    contentDescription = "Switch shop. Current: $label",
                    tint = OseboBlue,
                    modifier = Modifier.size(23.dp),
                )
            }
        }

        DropdownMenu(
            expanded = state.isOpen,
            onDismissRequest = onDismiss,
            modifier = Modifier.width(286.dp).heightIn(max = 370.dp),
            offset = DpOffset(0.dp, 7.dp),
            shape = RoundedCornerShape(22.dp),
            containerColor = Color.White,
            shadowElevation = 12.dp,
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = "Switch shop",
                    color = Color(0xFF171B1F),
                    fontFamily = oseboFontFamily(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )

                if (state.isLoading && state.shops.isEmpty()) {
                    repeat(3) { ShopPickerSkeletonRow() }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        if (state.isHome) {
                            ShopPickerRow(
                                name = "All businesses",
                                detail = "All shops",
                                icon = Res.drawable.iconsax_category_bold,
                                selected = state.selectedShopId.isBlank(),
                                onClick = { onSelect(null) },
                            )
                        }
                        state.shops.forEach { shop ->
                            ShopPickerRow(
                                name = shop.name,
                                detail = shop.shopTypeDisplay,
                                icon = shopCategoryIcon(shop.shopType),
                                selected = shop.id == state.selectedShopId,
                                onClick = { onSelect(shop) },
                            )
                        }
                        if (state.shops.isEmpty()) {
                            Text(
                                text = "No shops available",
                                color = Color(0xFF7A838D),
                                fontFamily = oseboFontFamily(),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopPickerRow(
    name: String,
    detail: String,
    icon: DrawableResource,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(15.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) Color(0xFFF0F7FC) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color(0xFF171B1F),
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = Color(0xFF171B1F),
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = detail,
                color = Color(0xFF7A838D),
                fontFamily = oseboFontFamily(),
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) Box(Modifier.size(7.dp).background(OseboBlue, CircleShape))
    }
}

@Composable
private fun ShopPickerSkeletonRow() {
    val transition = rememberInfiniteTransition(label = "shop picker skeleton")
    val opacity by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "skeleton opacity",
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp).alpha(opacity),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(34.dp).background(Color(0xFFE5EBEF), CircleShape))
        Spacer(Modifier.width(10.dp))
        Column {
            Box(Modifier.size(width = 132.dp, height = 10.dp).background(Color(0xFFE5EBEF), CircleShape))
            Spacer(Modifier.height(6.dp))
            Box(Modifier.size(width = 78.dp, height = 8.dp).background(Color(0xFFE5EBEF), CircleShape))
        }
    }
}

fun shopCategoryIcon(shopType: String?): DrawableResource = when (shopType?.trim()?.lowercase()) {
    "restaurant", "food", "cafe" -> Res.drawable.iconsax_coffee_bold
    "salon", "spa", "beauty" -> Res.drawable.iconsax_scissor_bold
    "pharmacy", "medical", "health" -> Res.drawable.iconsax_health_bold
    "electronics", "computer", "technology" -> Res.drawable.iconsax_monitor_bold
    "online", "ecommerce", "e-commerce" -> Res.drawable.iconsax_global_bold
    "wholesale", "manufacturing", "hardware" -> Res.drawable.iconsax_box_bold
    "service", "professional" -> Res.drawable.iconsax_briefcase_bold
    else -> Res.drawable.iconsax_shop_bold
}
