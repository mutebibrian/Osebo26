package com.devbrian.osebo.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.devbrian.osebo.ui.theme.oseboFontFamily

data class AccountProfileUi(
    val id: String = "",
    val businessName: String = "",
    val businessType: String = "",
    val registrationNumber: String = "",
    val taxId: String = "",
    val address: String = "",
    val status: String = "",
    val paymentMethod: String = "",
    val billingCycle: String = "",
    val nextBillingDate: String = "",
    val twoFactorEnabled: Boolean = false,
    val loginNotificationsEnabled: Boolean = false,
)

data class AccountScreenState(
    val profile: AccountProfileUi? = null,
    val isLoading: Boolean = false,
)

enum class AccountConfirmation { DEACTIVATE, DELETE }

private val AccountCanvas = Color(0xFFF0F3F4)
private val AccountSurface = Color(0xFFFAFBFB)
private val AccountInk = Color(0xFF171B1F)
private val AccountMuted = Color(0xFF78838B)
private val AccountBorder = Color(0xFFDCE3E5)
private val AccountBlue = Color(0xFF0788CF)
private val AccountBlueSoft = Color(0xFFDDEEFF)
private val AccountMint = Color(0xFFDDF5F2)
private val AccountPeach = Color(0xFFFFE5D3)
private val AccountRed = Color(0xFFD64C4C)
private val AccountRedSoft = Color(0xFFFFE8E8)

@Composable
fun AccountScreen(
    state: AccountScreenState,
    confirmation: AccountConfirmation? = null,
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onEditAccount: () -> Unit = {},
    onUpdatePayment: () -> Unit = {},
    onTwoFactorChanged: (Boolean) -> Unit = {},
    onLoginNotificationsChanged: (Boolean) -> Unit = {},
    onManageSessions: () -> Unit = {},
    onDeactivateAccount: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onDismissConfirmation: () -> Unit = {},
    onConfirmAction: () -> Unit = {},
) {
    Scaffold(
        containerColor = AccountCanvas,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { AccountHeader(state.isLoading, onBack, onRefresh) }
            val profile = state.profile
            if (profile == null) {
                item { AccountLoadingCard(state.isLoading, onRefresh) }
            } else {
                item { ProfileCard(profile, onEditAccount) }
                item { SectionHeading("Business details", "Your registered business information") }
                item { BusinessDetailsCard(profile) }
                item { SectionHeading("Billing", "Payment method and renewal details") }
                item { BillingCard(profile, onUpdatePayment) }
                item { SectionHeading("Security", "Protect your account and review access") }
                item {
                    SecurityCard(
                        profile,
                        !state.isLoading,
                        onTwoFactorChanged,
                        onLoginNotificationsChanged,
                        onManageSessions,
                    )
                }
                item { DangerZoneCard(!state.isLoading, onDeactivateAccount, onDeleteAccount) }
            }
        }
    }

    confirmation?.let {
        AccountConfirmationDialog(
            action = it,
            onDismiss = onDismissConfirmation,
            onConfirm = onConfirmAction,
        )
    }
}

@Composable
private fun AccountHeader(isLoading: Boolean, onBack: () -> Unit, onRefresh: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        BareIcon(R.drawable.ic_iconsax_arrow_left, "Back", onBack, AccountInk, 26.dp)
        Column(Modifier.weight(1f)) {
            Text("Account", color = AccountInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 25.sp)
            Text("Profile, billing and security", color = AccountMuted, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 11.sp)
        }
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(20.dp), color = AccountBlue, strokeWidth = 2.dp)
        } else {
            BareIcon(R.drawable.ic_iconsax_refresh, "Refresh account", onRefresh, AccountBlue, 26.dp)
        }
    }
}

@Composable
private fun BareIcon(
    @DrawableRes icon: Int,
    description: String?,
    onClick: (() -> Unit)? = null,
    tint: Color = AccountInk,
    size: androidx.compose.ui.unit.Dp = 22.dp,
) {
    val modifier = if (onClick == null) Modifier else Modifier.clip(CircleShape).clickable(onClick = onClick).padding(4.dp)
    Icon(
        painter = painterResource(icon),
        contentDescription = description,
        tint = tint,
        modifier = modifier.size(size),
    )
}

@Composable
private fun AccountLoadingCard(isLoading: Boolean, onRefresh: () -> Unit) {
    val poppins = oseboFontFamily()
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(AccountSurface).padding(horizontal = 24.dp, vertical = 52.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = AccountBlue, strokeWidth = 2.dp)
            Text("Loading your account", color = AccountMuted, fontFamily = poppins, fontSize = 11.sp)
        } else {
            Text("Account details are unavailable", color = AccountInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            ActionPill("Try again", onRefresh, AccountInk, Color.White)
        }
    }
}

@Composable
private fun ProfileCard(profile: AccountProfileUi, onEditAccount: () -> Unit) {
    val poppins = oseboFontFamily()
    val status = profile.status.ifBlank { "Unknown" }
    val active = status.equals("active", true)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(AccountBlueSoft).border(1.dp, Color.White, RoundedCornerShape(30.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(58.dp).clip(CircleShape).background(AccountBlue), contentAlignment = Alignment.Center) {
                Text(profile.businessName.trim().take(1).uppercase().ifBlank { "A" }, color = Color.White, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
            }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(profile.businessName.displayOr("Your business"), color = AccountInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(profile.businessType.displayOr("Business account"), color = AccountMuted, fontFamily = poppins, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(
                status.replaceFirstChar { it.uppercase() },
                Modifier.clip(RoundedCornerShape(100.dp)).background(if (active) AccountMint else AccountPeach).padding(horizontal = 10.dp, vertical = 6.dp),
                color = if (active) Color(0xFF087E67) else Color(0xFFAD601C), fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 9.sp,
            )
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Business profile", color = AccountInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Keep your public account information current", color = AccountMuted, fontFamily = poppins, fontSize = 9.sp)
            }
            ActionPill("Edit profile", onEditAccount, AccountSurface, AccountInk, R.drawable.ic_iconsax_edit)
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    val poppins = oseboFontFamily()
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, color = AccountInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
        Text(subtitle, color = AccountMuted, fontFamily = poppins, fontSize = 9.sp)
    }
}

@Composable
private fun BusinessDetailsCard(profile: AccountProfileUi) {
    DetailCard {
        DetailRow(R.drawable.ic_iconsax_shop, "Business type", profile.businessType.displayOr())
        CardDivider()
        DetailRow(R.drawable.ic_iconsax_document_text, "Registration number", profile.registrationNumber.displayOr())
        CardDivider()
        DetailRow(R.drawable.ic_iconsax_document_text, "Tax ID", profile.taxId.displayOr())
        CardDivider()
        DetailRow(R.drawable.ic_iconsax_location, "Business address", profile.address.displayOr())
    }
}

@Composable
private fun BillingCard(profile: AccountProfileUi, onUpdatePayment: () -> Unit) {
    val poppins = oseboFontFamily()
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(AccountPeach).border(1.dp, Color.White, RoundedCornerShape(28.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BillingMetric(Modifier.weight(1f), R.drawable.ic_iconsax_card, "Payment method", profile.paymentMethod.displayOr())
            BillingMetric(Modifier.weight(1f), R.drawable.ic_iconsax_calendar, "Billing cycle", profile.billingCycle.displayOr())
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Next billing date", color = AccountMuted, fontFamily = poppins, fontSize = 9.sp)
                Text(profile.nextBillingDate.displayOr(), color = AccountInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            ActionPill("Update payment", onUpdatePayment, AccountInk, Color.White)
        }
    }
}

@Composable
private fun BillingMetric(modifier: Modifier, @DrawableRes icon: Int, label: String, value: String) {
    val poppins = oseboFontFamily()
    Column(modifier.clip(RoundedCornerShape(19.dp)).background(AccountSurface.copy(alpha = 0.82f)).padding(13.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        BareIcon(icon, null, tint = AccountInk, size = 21.dp)
        Text(label, color = AccountMuted, fontFamily = poppins, fontSize = 8.sp)
        Text(value, color = AccountInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SecurityCard(
    profile: AccountProfileUi,
    enabled: Boolean,
    onTwoFactorChanged: (Boolean) -> Unit,
    onLoginNotificationsChanged: (Boolean) -> Unit,
    onManageSessions: () -> Unit,
) {
    DetailCard {
        SecurityToggleRow(R.drawable.ic_iconsax_shield_tick, "Two-factor authentication", "Require a second step when signing in", profile.twoFactorEnabled, enabled, onTwoFactorChanged)
        CardDivider()
        SecurityToggleRow(R.drawable.ic_iconsax_notification, "Login notifications", "Get notified when a new device signs in", profile.loginNotificationsEnabled, enabled, onLoginNotificationsChanged)
        CardDivider()
        NavigationRow(R.drawable.ic_iconsax_devices, "Active sessions", "Review devices with access to your account", onManageSessions)
    }
}

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(AccountSurface).border(1.dp, Color.White, RoundedCornerShape(28.dp)).padding(horizontal = 16.dp, vertical = 4.dp),
        content = content,
    )
}

@Composable
private fun DetailRow(@DrawableRes icon: Int, label: String, value: String) {
    val poppins = oseboFontFamily()
    Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        BareIcon(icon, null, tint = AccountInk, size = 22.dp)
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(label, color = AccountMuted, fontFamily = poppins, fontSize = 8.sp)
            Text(value, color = AccountInk, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SecurityToggleRow(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val poppins = oseboFontFamily()
    Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        BareIcon(icon, null, tint = AccountInk, size = 22.dp)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, color = AccountInk, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 11.sp)
            Text(subtitle, color = AccountMuted, fontFamily = poppins, fontSize = 8.sp, lineHeight = 12.sp)
        }
        Switch(
            checked = checked, onCheckedChange = onCheckedChange, enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccountBlue, uncheckedThumbColor = AccountSurface, uncheckedTrackColor = AccountBorder, uncheckedBorderColor = AccountBorder),
        )
    }
}

@Composable
private fun NavigationRow(@DrawableRes icon: Int, title: String, subtitle: String, onClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        BareIcon(icon, null, tint = AccountInk, size = 22.dp)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, color = AccountInk, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 11.sp)
            Text(subtitle, color = AccountMuted, fontFamily = poppins, fontSize = 8.sp, lineHeight = 12.sp)
        }
        BareIcon(R.drawable.ic_iconsax_chevron_right, null, tint = AccountMuted, size = 20.dp)
    }
}

@Composable
private fun DangerZoneCard(enabled: Boolean, onDeactivateAccount: () -> Unit, onDeleteAccount: () -> Unit) {
    val poppins = oseboFontFamily()
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(AccountRedSoft).border(1.dp, Color.White, RoundedCornerShape(28.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BareIcon(R.drawable.ic_iconsax_warning, null, tint = AccountRed, size = 24.dp)
            Column(Modifier.padding(start = 13.dp)) {
                Text("Account controls", color = AccountInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("These actions can restrict or remove access", color = AccountMuted, fontFamily = poppins, fontSize = 8.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DangerAction(Modifier.weight(1f), "Deactivate", R.drawable.ic_iconsax_pause, enabled, onDeactivateAccount)
            DangerAction(Modifier.weight(1f), "Delete account", R.drawable.ic_iconsax_trash, enabled, onDeleteAccount)
        }
    }
}

@Composable
private fun DangerAction(modifier: Modifier, text: String, @DrawableRes icon: Int, enabled: Boolean, onClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        modifier.clip(RoundedCornerShape(100.dp)).background(AccountSurface.copy(alpha = if (enabled) 0.9f else 0.5f)).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        BareIcon(icon, null, tint = AccountRed, size = 18.dp)
        Text(text, Modifier.padding(start = 7.dp), color = AccountRed, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 9.sp)
    }
}

@Composable
private fun ActionPill(
    text: String,
    onClick: () -> Unit,
    background: Color,
    contentColor: Color,
    @DrawableRes icon: Int? = null,
) {
    val poppins = oseboFontFamily()
    Row(
        Modifier.clip(RoundedCornerShape(100.dp)).background(background).clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        icon?.let { BareIcon(it, null, tint = contentColor, size = 17.dp); Spacer(Modifier.size(7.dp)) }
        Text(text, color = contentColor, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 9.sp)
    }
}

@Composable
private fun CardDivider() {
    HorizontalDivider(color = AccountBorder.copy(alpha = 0.8f), thickness = 1.dp)
}

@Composable
private fun AccountConfirmationDialog(action: AccountConfirmation, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val poppins = oseboFontFamily()
    val deleting = action == AccountConfirmation.DELETE
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AccountSurface,
        shape = RoundedCornerShape(30.dp),
        icon = { BareIcon(if (deleting) R.drawable.ic_iconsax_trash else R.drawable.ic_iconsax_pause, null, tint = AccountRed, size = 28.dp) },
        title = { Text(if (deleting) "Delete account?" else "Deactivate account?", color = AccountInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold) },
        text = { Text(if (deleting) "This cannot be undone. Your account data will be permanently removed." else "You can reactivate your account later by signing in again.", color = AccountMuted, fontFamily = poppins, fontSize = 11.sp) },
        confirmButton = { ActionPill(if (deleting) "Delete account" else "Deactivate", onConfirm, AccountRed, Color.White) },
        dismissButton = { ActionPill("Cancel", onDismiss, AccountCanvas, AccountInk) },
    )
}

private fun String.displayOr(fallback: String = "Not provided"): String = trim().ifBlank { fallback }
