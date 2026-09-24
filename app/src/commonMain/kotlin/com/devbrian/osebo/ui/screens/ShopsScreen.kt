package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.ui.theme.oseboFontFamily

data class ShopItemUi(
    val id: String,
    val name: String,
    val type: String,
    val location: String,
    val isSubscriptionActive: Boolean,
    val isTrial: Boolean,
    val isCurrent: Boolean,
)

data class ShopsUiState(
    val shops: List<ShopItemUi> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

@Composable
fun ShopsScreen(
    state: ShopsUiState,
    onAddShopClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onShopClick: (String) -> Unit,
    onSubscribeClick: (String) -> Unit,
) {
    val poppins = oseboFontFamily()
    var query by remember { mutableStateOf("") }
    val visibleShops = remember(state.shops, query) {
        state.shops.filter { shop ->
            query.isBlank() ||
                shop.name.contains(query, ignoreCase = true) ||
                shop.type.contains(query, ignoreCase = true) ||
                shop.location.contains(query, ignoreCase = true)
        }
    }

    Scaffold(containerColor = Color.White) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 44.dp, end = 20.dp, bottom = 136.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My shops",
                            color = Ink,
                            fontFamily = poppins,
                            fontSize = 26.sp,
                            lineHeight = 32.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Manage your businesses in one place",
                            color = Muted,
                            fontFamily = poppins,
                            fontSize = 12.sp,
                        )
                    }

                    IconButton(
                        onClick = onRefreshClick,
                        enabled = !state.isRefreshing,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SoftSurface),
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(19.dp),
                                color = OseboBlue,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh shops",
                                tint = Ink,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    IconButton(
                        onClick = onAddShopClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(OseboBlue),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Create shop",
                            tint = Color.White,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    placeholder = {
                        Text(
                            text = "Search shops",
                            color = Color(0xFF9CA6B0),
                            fontFamily = poppins,
                            fontSize = 12.sp,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = Muted,
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SoftSurface,
                        unfocusedContainerColor = SoftSurface,
                        focusedBorderColor = OseboBlue.copy(alpha = 0.55f),
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = OseboBlue,
                    ),
                )

                Spacer(Modifier.height(26.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Your shops",
                        color = Ink,
                        fontFamily = poppins,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${visibleShops.size} ${if (visibleShops.size == 1) "shop" else "shops"}",
                        color = Muted,
                        fontFamily = poppins,
                        fontSize = 11.sp,
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            when {
                state.isLoading && state.shops.isEmpty() -> item { LoadingShopsCard() }
                state.shops.isEmpty() -> item {
                    EmptyShopsCard(
                        errorMessage = state.errorMessage,
                        onCreateClick = onAddShopClick,
                        onRetryClick = onRefreshClick,
                    )
                }
                visibleShops.isEmpty() -> item { NoMatchingShopsCard() }
                else -> items(visibleShops, key = { it.id }) { shop ->
                    ShopCard(
                        shop = shop,
                        onClick = {
                            if (shop.isSubscriptionActive) onShopClick(shop.id)
                            else onSubscribeClick(shop.id)
                        },
                    )
                    Spacer(Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun ShopCard(shop: ShopItemUi, onClick: () -> Unit) {
    val poppins = oseboFontFamily()
    val cardColors = when {
        shop.isCurrent -> listOf(Color(0xFFDCEEFF), Color(0xFFF3F8FF))
        shop.isSubscriptionActive -> listOf(Color(0xFFE8F4FF), Color(0xFFF8FBFF))
        else -> listOf(Color(0xFFFFEBD5), Color(0xFFFFF8F0))
    }
    val shopIconColor = if (shop.isSubscriptionActive) OseboBlue else Color(0xFFD07828)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = RectangleShape,
                ambientColor = Color(0xFF7A8792).copy(alpha = 0.2f),
                spotColor = Color(0xFF7A8792).copy(alpha = 0.18f),
            )
            .background(
                Brush.linearGradient(
                    colors = cardColors,
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 17.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Storefront,
                contentDescription = null,
                tint = shopIconColor,
                modifier = Modifier.size(38.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = shop.name,
                        color = Ink,
                        fontFamily = poppins,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (shop.isCurrent) {
                        Spacer(Modifier.width(7.dp))
                        StatusBadge(label = "CURRENT", active = true)
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = shop.type,
                    color = Muted,
                    fontFamily = poppins,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            StatusBadge(
                label = when {
                    shop.isTrial -> "TRIAL"
                    shop.isSubscriptionActive -> "ACTIVE"
                    else -> "INACTIVE"
                },
                active = shop.isSubscriptionActive,
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = Color(0xFF8A949E),
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = shop.location,
                color = Muted,
                fontFamily = poppins,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 5.dp),
            )

            Spacer(Modifier.width(10.dp))

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (shop.isSubscriptionActive) OseboBlue else Color(0xFFFFE8CF))
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (shop.isSubscriptionActive) "Open shop" else "Activate plan",
                    color = if (shop.isSubscriptionActive) Color.White else Color(0xFF9A5A18),
                    fontFamily = poppins,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(5.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = if (shop.isSubscriptionActive) Color.White else Color(0xFF9A5A18),
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String, active: Boolean) {
    val poppins = oseboFontFamily()
    Text(
        text = label,
        color = if (active) Color(0xFF21866F) else Color(0xFF9A5A18),
        fontFamily = poppins,
        fontSize = 8.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) Color(0xFFDDF3EC) else Color(0xFFFFE8CF))
            .padding(horizontal = 7.dp, vertical = 4.dp),
    )
}

@Composable
private fun LoadingShopsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = OseboBlue, strokeWidth = 2.dp)
    }
}

@Composable
private fun EmptyShopsCard(
    errorMessage: String?,
    onCreateClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(SoftSurface)
            .padding(horizontal = 24.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Storefront,
            contentDescription = null,
            tint = OseboBlue,
            modifier = Modifier.size(52.dp),
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = if (errorMessage == null) "Create your first shop" else "We couldn't load your shops",
            color = Ink,
            fontFamily = poppins,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = errorMessage ?: "Add a business to start managing sales, stock and your team.",
            color = Muted,
            fontFamily = poppins,
            fontSize = 11.sp,
            lineHeight = 17.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = if (errorMessage == null) onCreateClick else onRetryClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OseboBlue),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 11.dp),
        ) {
            Icon(
                imageVector = if (errorMessage == null) Icons.Filled.Add else Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = if (errorMessage == null) "Create shop" else "Try again",
                fontFamily = poppins,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun NoMatchingShopsCard() {
    val poppins = oseboFontFamily()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No matching shops",
            color = Ink,
            fontFamily = poppins,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Try a different name, type or location",
            color = Muted,
            fontFamily = poppins,
            fontSize = 11.sp,
        )
    }
}

private val Ink = Color(0xFF171B1F)
private val Muted = Color(0xFF747E88)
private val SoftSurface = Color(0xFFF6F7F8)
private val OseboBlue = Color(0xFF087FC4)
