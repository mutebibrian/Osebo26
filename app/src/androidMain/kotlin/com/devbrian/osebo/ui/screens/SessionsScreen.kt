package com.devbrian.osebo.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.R
import com.devbrian.osebo.models.Session
import com.devbrian.osebo.ui.theme.oseboFontFamily

private val SessionsCanvas = Color(0xFFF0F3F4)
private val SessionsSurface = Color(0xFFFAFBFB)
private val SessionsInk = Color(0xFF171B1F)
private val SessionsMuted = Color(0xFF78838B)
private val SessionsBlue = Color(0xFF0788CF)
private val SessionsBlueSoft = Color(0xFFDDEEFF)
private val SessionsRed = Color(0xFFD64C4C)

@Composable
fun SessionsScreen(
    sessions: List<Session>,
    isLoading: Boolean,
    selectedSession: Session?,
    confirmAll: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSessionClick: (Session) -> Unit,
    onTerminateAll: () -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmSession: (Session) -> Unit,
    onConfirmAll: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(SessionsCanvas)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { SessionsHeader(isLoading, onBack, onRefresh) }
            item {
                Text(
                    "Review and remove devices that are signed in to your account.",
                    color = SessionsMuted,
                    fontFamily = oseboFontFamily(),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                )
            }
            if (isLoading && sessions.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SessionsBlue, strokeWidth = 2.dp)
                    }
                }
            } else if (sessions.isEmpty()) {
                item { EmptySessions(onRefresh) }
            } else {
                items(sessions, key = { it.id }) { session -> SessionCard(session, onSessionClick) }
                if (sessions.any { !it.isCurrent }) {
                    item { DarkPill("Terminate all other sessions", R.drawable.ic_iconsax_logout, onTerminateAll) }
                }
            }
        }
    }

    selectedSession?.let { session ->
        SessionConfirmationDialog(
            title = "Terminate this session?",
            message = "${session.device} • ${session.location}\n${session.ipAddress} • ${session.lastActive}",
            confirmText = "Terminate session",
            onDismiss = onDismissDialog,
            onConfirm = { onConfirmSession(session) },
        )
    }
    if (confirmAll) {
        SessionConfirmationDialog(
            title = "Terminate other sessions?",
            message = "Every other device will be signed out. This device will remain active.",
            confirmText = "Terminate all",
            onDismiss = onDismissDialog,
            onConfirm = onConfirmAll,
        )
    }
}

@Composable
private fun SessionsHeader(isLoading: Boolean, onBack: () -> Unit, onRefresh: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        SessionIcon(R.drawable.ic_iconsax_arrow_left, "Back", SessionsInk, onBack, 26)
        Column(Modifier.weight(1f)) {
            Text("Active sessions", color = SessionsInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 25.sp)
            Text("Devices with account access", color = SessionsMuted, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 11.sp)
        }
        if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = SessionsBlue, strokeWidth = 2.dp)
        else SessionIcon(R.drawable.ic_iconsax_refresh, "Refresh sessions", SessionsBlue, onRefresh, 26)
    }
}

@Composable
private fun SessionCard(session: Session, onClick: (Session) -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(SessionsSurface)
            .border(1.dp, Color.White, RoundedCornerShape(26.dp)).clickable { onClick(session) }
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalAlignment = Alignment.Top,
    ) {
        SessionIcon(R.drawable.ic_iconsax_devices, null, SessionsInk, size = 24)
        Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(session.device, Modifier.weight(1f), color = SessionsInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (session.isCurrent) {
                    Text("This device", Modifier.clip(RoundedCornerShape(100.dp)).background(SessionsBlueSoft).padding(horizontal = 9.dp, vertical = 5.dp), color = SessionsBlue, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 8.sp)
                }
            }
            Text(session.browser, color = SessionsMuted, fontFamily = poppins, fontSize = 10.sp)
            Text("${session.location} • ${session.ipAddress}", color = SessionsMuted, fontFamily = poppins, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(session.lastActive, color = if (session.isCurrent) SessionsBlue else SessionsInk, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 9.sp)
        }
        if (!session.isCurrent) SessionIcon(R.drawable.ic_iconsax_chevron_right, "Session actions", SessionsMuted, size = 19)
    }
}

@Composable
private fun EmptySessions(onRefresh: () -> Unit) {
    val poppins = oseboFontFamily()
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(SessionsSurface).padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SessionIcon(R.drawable.ic_iconsax_devices, null, SessionsInk, size = 30)
        Text("No active sessions", color = SessionsInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text("No other devices currently have access.", color = SessionsMuted, fontFamily = poppins, fontSize = 10.sp)
        DarkPill("Refresh", R.drawable.ic_iconsax_refresh, onRefresh)
    }
}

@Composable
private fun DarkPill(text: String, icon: Int, onClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(100.dp)).background(SessionsInk).clickable(onClick = onClick).padding(vertical = 15.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SessionIcon(icon, null, Color.White, size = 20)
        Spacer(Modifier.size(9.dp))
        Text(text, color = Color.White, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
    }
}

@Composable
private fun SessionIcon(icon: Int, description: String?, tint: Color, onClick: (() -> Unit)? = null, size: Int = 22) {
    val modifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick).padding(4.dp)
    Icon(painterResource(icon), description, modifier.size(size.dp), tint)
}

@Composable
private fun SessionConfirmationDialog(title: String, message: String, confirmText: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val poppins = oseboFontFamily()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SessionsSurface,
        shape = RoundedCornerShape(30.dp),
        icon = { SessionIcon(R.drawable.ic_iconsax_logout, null, SessionsRed, size = 28) },
        title = { Text(title, color = SessionsInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold) },
        text = { Text(message, color = SessionsMuted, fontFamily = poppins, fontSize = 11.sp, lineHeight = 17.sp) },
        confirmButton = {
            Text(confirmText, Modifier.clip(RoundedCornerShape(100.dp)).background(SessionsRed).clickable(onClick = onConfirm).padding(horizontal = 16.dp, vertical = 10.dp), color = Color.White, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
        },
        dismissButton = {
            Text("Cancel", Modifier.clip(RoundedCornerShape(100.dp)).background(SessionsCanvas).clickable(onClick = onDismiss).padding(horizontal = 16.dp, vertical = 10.dp), color = SessionsInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
        },
    )
}
