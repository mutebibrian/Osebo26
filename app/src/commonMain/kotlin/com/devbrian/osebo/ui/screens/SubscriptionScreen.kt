package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.ui.theme.oseboFontFamily

data class SubscriptionCurrentPlanUi(
    val id: String,
    val name: String,
    val status: String,
    val duration: String,
    val expiry: String,
    val daysRemaining: String,
    val amount: String,
    val canCancel: Boolean,
)

data class SubscriptionPlanUi(
    val id: String,
    val name: String,
    val tier: String,
    val description: String,
    val price: String,
    val features: List<String>,
    val isPopular: Boolean,
    val isCustom: Boolean,
    val canTry: Boolean,
)

data class SubscriptionPaymentUi(
    val id: String,
    val title: String,
    val date: String,
    val method: String,
    val amount: String,
    val status: String,
    val isSuccessful: Boolean,
)

data class SubscriptionScreenState(
    val shopName: String = "Current shop",
    val currentPlan: SubscriptionCurrentPlanUi? = null,
    val plans: List<SubscriptionPlanUi> = emptyList(),
    val payments: List<SubscriptionPaymentUi> = emptyList(),
    val isLoading: Boolean = false,
    val isHistoryLoading: Boolean = false,
    val errorMessage: String? = null,
)

private enum class SubscriptionTab(val label: String) {
    Overview("Overview"),
    History("History"),
}

private val SubscriptionCanvas = Color(0xFFF0F3F4)
private val SubscriptionSurface = Color(0xFFFAFBFB)
private val SubscriptionWhite = Color.White
private val SubscriptionInk = Color(0xFF171B1F)
private val SubscriptionMuted = Color(0xFF768087)
private val SubscriptionBorder = Color(0xFFDDE3E5)
private val SubscriptionBlue = Color(0xFF087FC4)
private val SubscriptionGreen = Color(0xFF23A36D)
private val SubscriptionGold = Color(0xFFE88B48)
private val SubscriptionRed = Color(0xFFE75A67)

@Composable
fun SubscriptionScreen(
    state: SubscriptionScreenState,
    onRefreshClick: () -> Unit,
    onPlanClick: (String) -> Unit,
    onContactSalesClick: () -> Unit,
    onCancelSubscription: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(SubscriptionTab.Overview) }
    var showCancelDialog by remember { mutableStateOf(false) }
    val poppins = oseboFontFamily()

    Scaffold(containerColor = SubscriptionCanvas) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Subscriptions",
                            color = SubscriptionInk,
                            fontFamily = poppins,
                            fontSize = 27.sp,
                            lineHeight = 34.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            state.shopName,
                            color = SubscriptionMuted,
                            fontFamily = poppins,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SubscriptionWhite)
                            .border(1.dp, SubscriptionBorder, CircleShape)
                            .clickable(enabled = !state.isLoading, onClick = onRefreshClick),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = SubscriptionBlue,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                "↻",
                                color = SubscriptionBlue,
                                fontSize = 24.sp,
                                lineHeight = 24.sp,
                            )
                        }
                    }
                }
            }

            item {
                SubscriptionTabs(
                    selected = selectedTab,
                    onSelected = { selectedTab = it },
                )
            }

            when (selectedTab) {
                SubscriptionTab.Overview -> {
                    item {
                        CurrentPlanCard(
                            plan = state.currentPlan,
                            onCancelClick = { showCancelDialog = true },
                        )
                    }

                    state.errorMessage?.let { message ->
                        item { SubscriptionErrorCard(message = message, onRetry = onRefreshClick) }
                    }

                    item {
                        SectionHeading(
                            title = "Available plans",
                            subtitle = "Choose what fits your business today",
                        )
                    }

                    if (state.isLoading && state.plans.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(180.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = SubscriptionBlue, strokeWidth = 2.dp)
                            }
                        }
                    } else if (state.plans.isEmpty()) {
                        item {
                            EmptySubscriptionCard(
                                title = "No plans available",
                                message = "Refresh to check for available subscription plans.",
                            )
                        }
                    } else {
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(end = 4.dp),
                            ) {
                                items(state.plans, key = { it.id }) { plan ->
                                    SubscriptionPlanCard(
                                        plan = plan,
                                        onClick = {
                                            if (plan.isCustom) onContactSalesClick()
                                            else onPlanClick(plan.id)
                                        },
                                    )
                                }
                            }
                        }
                    }

                    item { ContactSalesCard(onClick = onContactSalesClick) }
                }

                SubscriptionTab.History -> {
                    item {
                        SectionHeading(
                            title = "Payment history",
                            subtitle = "Receipts and subscription payments",
                        )
                    }

                    if (state.isHistoryLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(220.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = SubscriptionBlue, strokeWidth = 2.dp)
                            }
                        }
                    } else if (state.payments.isEmpty()) {
                        item {
                            EmptySubscriptionCard(
                                title = "No payment history",
                                message = "Completed subscription payments will appear here.",
                            )
                        }
                    } else {
                        items(state.payments, key = { it.id }) { payment ->
                            SubscriptionPaymentRow(payment)
                        }
                    }
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = SubscriptionWhite,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    "Cancel subscription?",
                    color = SubscriptionInk,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    "Your current plan will be cancelled. This action cannot be undone.",
                    color = SubscriptionMuted,
                    fontFamily = poppins,
                    fontSize = 12.sp,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        onCancelSubscription()
                    },
                ) {
                    Text("Cancel plan", color = SubscriptionRed, fontFamily = poppins)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep plan", color = SubscriptionMuted, fontFamily = poppins)
                }
            },
        )
    }
}

@Composable
private fun SubscriptionTabs(
    selected: SubscriptionTab,
    onSelected: (SubscriptionTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SubscriptionWhite, RoundedCornerShape(50))
            .border(1.dp, SubscriptionBorder, RoundedCornerShape(50))
            .padding(4.dp),
    ) {
        SubscriptionTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) SubscriptionInk else Color.Transparent)
                    .clickable { onSelected(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    tab.label,
                    color = if (isSelected) SubscriptionWhite else SubscriptionMuted,
                    fontFamily = oseboFontFamily(),
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun CurrentPlanCard(
    plan: SubscriptionCurrentPlanUi?,
    onCancelClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(30.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFD6E8FF), Color(0xFFF1F6FF)),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .border(1.dp, SubscriptionWhite.copy(alpha = 0.9f), shape)
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "CURRENT PLAN",
                    color = SubscriptionBlue,
                    fontFamily = poppins,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.9.sp,
                )
                Text(
                    plan?.name ?: "No active plan",
                    color = Color(0xFF14243A),
                    fontFamily = poppins,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                plan?.status ?: "Inactive",
                color = if (plan != null) SubscriptionGreen else SubscriptionMuted,
                fontFamily = poppins,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(SubscriptionWhite.copy(alpha = 0.78f), RoundedCornerShape(50))
                    .padding(horizontal = 11.dp, vertical = 7.dp),
            )
        }

        Spacer(Modifier.height(22.dp))

        if (plan == null) {
            Text(
                "Choose a plan below to unlock billing and subscription features for this shop.",
                color = SubscriptionMuted,
                fontFamily = poppins,
                fontSize = 10.sp,
                lineHeight = 15.sp,
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PlanDetail("Duration", plan.duration, Modifier.weight(1f))
                PlanDetail("Expires", plan.expiry, Modifier.weight(1f))
                PlanDetail("Remaining", plan.daysRemaining, Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    plan.amount,
                    color = Color(0xFF14243A),
                    fontFamily = poppins,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (plan.canCancel) {
                    Text(
                        "Cancel plan",
                        color = SubscriptionRed,
                        fontFamily = poppins,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SubscriptionWhite.copy(alpha = 0.72f))
                            .clickable(onClick = onCancelClick)
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanDetail(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(SubscriptionWhite.copy(alpha = 0.66f), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
    ) {
        Text(
            label,
            color = SubscriptionMuted,
            fontFamily = oseboFontFamily(),
            fontSize = 7.sp,
        )
        Text(
            value,
            color = SubscriptionInk,
            fontFamily = oseboFontFamily(),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column {
        Text(
            title,
            color = SubscriptionInk,
            fontFamily = oseboFontFamily(),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            subtitle,
            color = SubscriptionMuted,
            fontFamily = oseboFontFamily(),
            fontSize = 9.sp,
        )
    }
}

@Composable
private fun SubscriptionPlanCard(plan: SubscriptionPlanUi, onClick: () -> Unit) {
    val gradient = when (plan.tier.lowercase()) {
        "pro" -> listOf(Color(0xFFE3E3FF), Color(0xFFF6F5FF))
        "premium", "enterprise" -> listOf(Color(0xFFFFE6CF), Color(0xFFFFF7EF))
        "custom" -> listOf(Color(0xFFECE3FA), Color(0xFFF9F5FD))
        else -> listOf(Color(0xFFD7F2F5), Color(0xFFF0FAFA))
    }
    val accent = when (plan.tier.lowercase()) {
        "pro" -> Color(0xFF6673E8)
        "premium", "enterprise" -> SubscriptionGold
        "custom" -> Color(0xFF8D5FD3)
        else -> Color(0xFF2DAFC1)
    }
    val poppins = oseboFontFamily()
    val shape = RoundedCornerShape(28.dp)

    Column(
        modifier = Modifier
            .width(252.dp)
            .height(278.dp)
            .clip(shape)
            .background(Brush.linearGradient(gradient, Offset.Zero, Offset.Infinite))
            .border(1.dp, SubscriptionWhite.copy(alpha = 0.92f), shape)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                plan.name,
                color = Color(0xFF14243A),
                fontFamily = poppins,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (plan.isPopular) {
                Text(
                    "POPULAR",
                    color = accent,
                    fontFamily = poppins,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(SubscriptionWhite.copy(alpha = 0.72f), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                )
            }
        }
        Text(
            plan.price,
            color = Color(0xFF14243A),
            fontFamily = poppins,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            plan.description,
            color = SubscriptionMuted,
            fontFamily = poppins,
            fontSize = 9.sp,
            lineHeight = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            val features = plan.features.take(4).ifEmpty { listOf("Core business tools included") }
            features.forEach { feature ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("✓", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        feature.removePrefix("✓ "),
                        color = Color(0xFF4F5B66),
                        fontFamily = poppins,
                        fontSize = 8.sp,
                        lineHeight = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        if (plan.canTry && !plan.isCustom) {
            Text(
                "Free trial available",
                color = accent,
                fontFamily = poppins,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SubscriptionInk)
                .clickable(onClick = onClick)
                .padding(vertical = 11.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (plan.isCustom) "Contact sales" else "Choose plan",
                color = SubscriptionWhite,
                fontFamily = poppins,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ContactSalesCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SubscriptionSurface)
            .border(1.dp, SubscriptionBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color(0xFFECE3FA), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("✦", color = Color(0xFF8D5FD3), fontSize = 17.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Need something custom?",
                color = SubscriptionInk,
                fontFamily = oseboFontFamily(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Talk to sales about a plan built for you",
                color = SubscriptionMuted,
                fontFamily = oseboFontFamily(),
                fontSize = 8.sp,
            )
        }
        Text(
            "Contact",
            color = SubscriptionBlue,
            fontFamily = oseboFontFamily(),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SubscriptionPaymentRow(payment: SubscriptionPaymentUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SubscriptionSurface)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    if (payment.isSuccessful) Color(0xFFDDF5E9) else Color(0xFFFFE6CF),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (payment.isSuccessful) "✓" else "•",
                color = if (payment.isSuccessful) SubscriptionGreen else SubscriptionGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                payment.title,
                color = SubscriptionInk,
                fontFamily = oseboFontFamily(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOf(payment.date, payment.method).filter { it.isNotBlank() }.joinToString(" · "),
                color = SubscriptionMuted,
                fontFamily = oseboFontFamily(),
                fontSize = 8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                payment.amount,
                color = SubscriptionInk,
                fontFamily = oseboFontFamily(),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                payment.status,
                color = if (payment.isSuccessful) SubscriptionGreen else SubscriptionGold,
                fontFamily = oseboFontFamily(),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun SubscriptionErrorCard(message: String, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFEBEE))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            message,
            color = SubscriptionRed,
            fontFamily = oseboFontFamily(),
            fontSize = 9.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            "Retry",
            color = SubscriptionRed,
            fontFamily = oseboFontFamily(),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onRetry).padding(8.dp),
        )
    }
}

@Composable
private fun EmptySubscriptionCard(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SubscriptionSurface, RoundedCornerShape(24.dp))
            .border(1.dp, SubscriptionBorder, RoundedCornerShape(24.dp))
            .padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            title,
            color = SubscriptionInk,
            fontFamily = oseboFontFamily(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            message,
            color = SubscriptionMuted,
            fontFamily = oseboFontFamily(),
            fontSize = 9.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}
