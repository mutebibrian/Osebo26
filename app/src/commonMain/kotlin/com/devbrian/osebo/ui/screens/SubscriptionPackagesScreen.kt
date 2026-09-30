package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.ui.theme.oseboFontFamily

data class SubscriptionBundleUi(
    val id: String,
    val name: String,
    val description: String,
    val price: String,
    val features: List<String>,
    val isPopular: Boolean,
    val isAddOn: Boolean,
    val isCustomPricing: Boolean,
    val hasFreeTrial: Boolean,
    val isActive: Boolean,
)

data class SubscriptionPackagesUiState(
    val shopName: String = "Current shop",
    val currentPlan: String? = null,
    val bundles: List<SubscriptionBundleUi> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val totalLabel: String = "UGX 0",
    val isTrialEligible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

private val BundleCanvas = Color(0xFFF0F3F4)
private val BundleWhite = Color(0xFFFAFBFB)
private val BundleInk = Color(0xFF171B1F)
private val BundleMuted = Color(0xFF78838B)
private val BundleBorder = Color(0xFFDCE3E5)
private val BundleBlue = Color(0xFF0788CF)
private val BundleMint = Color(0xFFDDF5F2)
private val BundlePeach = Color(0xFFFFE8D6)
private val BundlePink = Color(0xFFFBE4EC)
private val BundleLavender = Color(0xFFECEAFB)

@Composable
fun SubscriptionPackagesScreen(
    state: SubscriptionPackagesUiState,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSelectionChange: (Set<String>) -> Unit,
    onCancelClick: () -> Unit,
    onCheckoutClick: () -> Unit,
) {
    val poppins = oseboFontFamily()

    Scaffold(containerColor = BundleCanvas) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    RoundHeaderAction(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BundleInk,
                            modifier = Modifier.size(21.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Choose bundles",
                            color = BundleInk,
                            fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 25.sp,
                            lineHeight = 31.sp,
                        )
                        Text(
                            text = state.shopName,
                            color = BundleMuted,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    RoundHeaderAction(onClick = onRefreshClick, enabled = !state.isLoading) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = BundleBlue,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = "↻",
                                color = BundleBlue,
                                fontSize = 24.sp,
                                lineHeight = 24.sp,
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.currentPlan?.let { plan ->
                        Text(
                            text = "CURRENT PLAN  •  ${plan.uppercase()}",
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(BundleWhite)
                                .border(1.dp, BundleBorder, RoundedCornerShape(100.dp))
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            color = BundleMuted,
                            fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.4.sp,
                        )
                    }
                    Text(
                        text = "Build a plan that fits",
                        color = BundleInk,
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                    )
                    Text(
                        text = "Select one or more bundles. You can change them as your business grows.",
                        color = BundleMuted,
                        fontFamily = poppins,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    )
                }
            }

            state.errorMessage?.let { message ->
                item {
                    ErrorBundleCard(message = message, onRetry = onRefreshClick)
                }
            }

            if (state.isLoading && state.bundles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = BundleBlue, strokeWidth = 2.dp)
                    }
                }
            } else if (state.bundles.isEmpty() && state.errorMessage == null) {
                item {
                    EmptyBundleCard(onRefreshClick)
                }
            } else {
                itemsIndexed(state.bundles, key = { _, bundle -> bundle.id }) { index, bundle ->
                    SubscriptionBundleCard(
                        bundle = bundle,
                        selected = bundle.id in state.selectedIds,
                        tint = listOf(BundleMint, BundlePeach, BundlePink, BundleLavender)[index % 4],
                        onClick = {
                            if (bundle.isActive) {
                                val next = state.selectedIds.toMutableSet().apply {
                                    if (!add(bundle.id)) remove(bundle.id)
                                }
                                onSelectionChange(next)
                            }
                        },
                    )
                }
            }

            if (state.bundles.isNotEmpty()) {
                item {
                    CheckoutSummary(
                        state = state,
                        onCancelClick = onCancelClick,
                        onCheckoutClick = onCheckoutClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun RoundHeaderAction(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(BundleWhite)
            .border(1.dp, BundleBorder, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun SubscriptionBundleCard(
    bundle: SubscriptionBundleUi,
    selected: Boolean,
    tint: Color,
    onClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(30.dp)
    val outline = if (selected) BundleBlue else Color.White.copy(alpha = 0.9f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(tint)
            .border(if (selected) 2.dp else 1.dp, outline, shape)
            .clickable(enabled = bundle.isActive, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (bundle.isAddOn) BundleTag("ADD-ON", BundleBlue)
                    if (bundle.isPopular) BundleTag("POPULAR", Color(0xFFE35C86))
                    if (bundle.hasFreeTrial) BundleTag("FREE TRIAL", Color(0xFF139B78))
                }
                if (bundle.isAddOn || bundle.isPopular || bundle.hasFreeTrial) {
                    Spacer(Modifier.height(10.dp))
                }
                Text(
                    text = bundle.name,
                    color = BundleInk,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (selected) BundleBlue else BundleWhite.copy(alpha = 0.82f))
                    .border(1.dp, if (selected) BundleBlue else BundleBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = bundle.description.ifBlank { "Everything you need to keep work moving." },
                modifier = Modifier.weight(1f).padding(end = 12.dp),
                color = BundleMuted,
                fontFamily = poppins,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = bundle.price,
                color = BundleInk,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
            )
        }

        if (bundle.features.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                bundle.features.take(2).forEach { feature ->
                    Text(
                        text = feature.removePrefix("✓").trim(),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BundleWhite.copy(alpha = 0.72f))
                            .padding(horizontal = 9.dp, vertical = 7.dp),
                        color = BundleInk,
                        fontFamily = poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        lineHeight = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (!bundle.isActive) {
            Text(
                text = "CURRENTLY UNAVAILABLE",
                color = BundleMuted,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
            )
        }
    }
}

@Composable
private fun BundleTag(label: String, color: Color) {
    val poppins = oseboFontFamily()
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White.copy(alpha = 0.78f))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        color = color,
        fontFamily = poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 8.sp,
        letterSpacing = 0.35.sp,
    )
}

@Composable
private fun CheckoutSummary(
    state: SubscriptionPackagesUiState,
    onCancelClick: () -> Unit,
    onCheckoutClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    val selectedCount = state.selectedIds.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(BundleWhite)
            .border(1.dp, BundleBorder, RoundedCornerShape(30.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (selectedCount == 0) "No bundles selected" else "$selectedCount bundle${if (selectedCount == 1) "" else "s"} selected",
                    color = BundleInk,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
                Text(
                    text = when {
                        selectedCount == 0 -> "Choose at least one to continue"
                        state.isTrialEligible -> "Free trial — nothing due today"
                        else -> "Estimated monthly total"
                    },
                    color = BundleMuted,
                    fontFamily = poppins,
                    fontSize = 10.sp,
                )
            }
            Text(
                text = state.totalLabel,
                color = BundleInk,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier.weight(0.72f).height(52.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BundleBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BundleInk),
            ) {
                Text("Cancel", fontFamily = poppins, fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onCheckoutClick,
                enabled = selectedCount > 0,
                modifier = Modifier.weight(1.28f).height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BundleInk,
                    contentColor = Color.White,
                    disabledContainerColor = BundleBorder,
                    disabledContentColor = BundleMuted,
                ),
            ) {
                Text(
                    text = when {
                        selectedCount == 0 -> "Select bundles"
                        state.isTrialEligible -> "Start free trial"
                        else -> "Continue"
                    },
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ErrorBundleCard(message: String, onRetry: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(BundlePink)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            color = BundleInk,
            fontFamily = poppins,
            fontSize = 11.sp,
        )
        Text(
            text = "Retry",
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(BundleWhite)
                .clickable(onClick = onRetry)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            color = BundleBlue,
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun EmptyBundleCard(onRefreshClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(BundleWhite)
            .padding(horizontal = 20.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "No bundles available",
            color = BundleInk,
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Text(
            text = "Refresh to check the latest subscription options.",
            color = BundleMuted,
            fontFamily = poppins,
            fontSize = 11.sp,
        )
        Text(
            text = "Refresh",
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(BundleInk)
                .clickable(onClick = onRefreshClick)
                .padding(horizontal = 18.dp, vertical = 9.dp),
            color = Color.White,
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
        )
    }
}
