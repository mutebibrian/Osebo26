package com.devbrian.osebo.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.ui.theme.oseboFontFamily
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_shop_bold
import org.jetbrains.compose.resources.painterResource

data class ShopPickerUiState(
    val shops: List<Shop> = emptyList(),
    val selectedShopId: String = "",
    val fallbackShopName: String = "",
    val isLoading: Boolean = true,
    val isOpen: Boolean = false,
    val isHome: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopPickerOverlay(
    state: ShopPickerUiState,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (Shop?) -> Unit,
) {
    val poppins = oseboFontFamily()
    val selectedShop = state.shops.find { it.id == state.selectedShopId }
    val label = when {
        state.isHome && state.selectedShopId.isBlank() -> "All businesses"
        selectedShop != null -> selectedShop.name
        state.fallbackShopName.isNotBlank() -> state.fallbackShopName
        else -> "Choose a shop"
    }

    if (!state.isHome) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F6F7))
                .clickable(onClick = onOpen),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_shop_bold),
                contentDescription = "Switch shop. Current: $label",
                tint = Color(0xFF171B1F),
                modifier = Modifier.size(22.dp),
            )
        }
    }

    if (state.isOpen) {
        var query by remember { mutableStateOf("") }
        val visibleShops = remember(state.shops, query) {
            state.shops.filter { it.name.contains(query, ignoreCase = true) }
        }
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text(
                    text = "Switch shop",
                    color = Color(0xFF17212A),
                    fontFamily = poppins,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = label,
                    color = Color(0xFF7A838D),
                    fontFamily = poppins,
                    fontSize = 12.sp,
                )
                Spacer(Modifier.height(14.dp))

                if (state.shops.size > 5) {
                    androidx.compose.material3.OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search shops", fontFamily = poppins) },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(10.dp))
                }

                if (state.isLoading && state.shops.isEmpty()) {
                    repeat(4) { ShopPickerSkeletonRow() }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (state.isHome) {
                            ShopPickerRow(
                                name = "All businesses",
                                detail = "Overview of every shop",
                                selected = state.selectedShopId.isBlank(),
                                onClick = { onSelect(null) },
                            )
                        }
                        visibleShops.forEach { shop ->
                            ShopPickerRow(
                                name = shop.name,
                                detail = shop.address.orEmpty().ifBlank { shop.shopTypeDisplay },
                                selected = shop.id == state.selectedShopId,
                                onClick = { onSelect(shop) },
                            )
                        }
                        if (visibleShops.isEmpty()) {
                            Text(
                                text = if (query.isBlank()) "No shops available" else "No matching shops",
                                color = Color(0xFF7A838D),
                                fontFamily = poppins,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 16.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ShopPickerRow(name: String, detail: String, selected: Boolean, onClick: () -> Unit) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) Color(0xFFEAF5FC) else Color(0xFFF7F9FA))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(38.dp).background(if (selected) Color(0xFF087FC4) else Color(0xFFE5F2FA), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_shop_bold),
                contentDescription = null,
                tint = if (selected) Color.White else Color(0xFF087FC4),
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = Color(0xFF17212A), fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(detail, color = Color(0xFF7A838D), fontFamily = poppins, fontSize = 11.sp, maxLines = 1)
        }
        if (selected) {
            Box(
                modifier = Modifier.size(18.dp).border(2.dp, Color(0xFF087FC4), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(8.dp).background(Color(0xFF087FC4), CircleShape))
            }
        }
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp).alpha(opacity),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(42.dp).background(Color(0xFFE5EBEF), CircleShape))
        Spacer(Modifier.width(12.dp))
        Column {
            Box(Modifier.size(width = 145.dp, height = 13.dp).background(Color(0xFFE5EBEF), CircleShape))
            Spacer(Modifier.height(8.dp))
            Box(Modifier.size(width = 96.dp, height = 10.dp).background(Color(0xFFE5EBEF), CircleShape))
        }
    }
}
