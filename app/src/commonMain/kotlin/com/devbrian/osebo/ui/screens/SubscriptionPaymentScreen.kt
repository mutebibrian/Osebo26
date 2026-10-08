package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_arrow_left
import com.devbrian.osebo.resources.iconsax_call
import com.devbrian.osebo.resources.iconsax_close
import com.devbrian.osebo.resources.iconsax_document
import com.devbrian.osebo.resources.iconsax_lock
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToLong

data class SubscriptionPaymentUiState(
    val shopName: String = "Your shop",
    val packageName: String = "Subscription plan",
    val monthlyAmount: Double = 0.0,
    val months: Int = 1,
    val phone: String = "",
    val phoneError: String? = null,
    val submitError: String? = null,
    val isLoading: Boolean = false,
)

@Composable
fun SubscriptionPaymentScreen(
    state: SubscriptionPaymentUiState,
    isSheet: Boolean,
    onStateChange: (SubscriptionPaymentUiState) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth().then(if (isSheet) Modifier else Modifier.fillMaxSize().safeDrawingPadding())
                .imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
        ) {
            if (isSheet) PaymentHandle()
            PaymentHeader(state, isSheet, onDismiss)
            Spacer(Modifier.height(14.dp))
            PaymentSummaryCard(state)
            Spacer(Modifier.height(14.dp))
            DurationSelector(state.months) { onStateChange(state.copy(months = it, submitError = null)) }
            Spacer(Modifier.height(14.dp))
            PaymentPhoneField(state) { value ->
                onStateChange(state.copy(phone = value.take(16), phoneError = null, submitError = null))
            }
            state.submitError?.let {
                Spacer(Modifier.height(9.dp))
                Text(it, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color(0xFFFFEEF0)).padding(11.dp), color = PremiumRed, fontFamily = oseboFontFamily(), fontSize = 9.sp)
            }
            Spacer(Modifier.height(16.dp))
            PaymentActions(state, onDismiss, onSubmit)
            Text(
                "Your provider is detected automatically. A secure payment prompt will be sent to your phone.",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                color = PremiumMuted,
                fontFamily = oseboFontFamily(),
                fontSize = 8.sp,
                lineHeight = 12.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(if (isSheet) 6.dp else 24.dp))
        }
    }

    if (isSheet) {
        Surface(shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), color = Color.White) { content() }
    } else {
        Scaffold(containerColor = Color.White, contentWindowInsets = WindowInsets(0)) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) { content() }
        }
    }
}

@Composable
private fun PaymentHandle() {
    Box(Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.width(34.dp).height(3.dp).clip(CircleShape).background(Color(0xFFC8CDD0)))
    }
}

@Composable
private fun PaymentHeader(state: SubscriptionPaymentUiState, isSheet: Boolean, onDismiss: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        if (!isSheet) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(42.dp)) {
                Icon(painterResource(Res.drawable.iconsax_arrow_left), "Back", tint = PremiumInk, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(5.dp))
        }
        Box(modifier = Modifier.size(43.dp).clip(CircleShape).background(PremiumCanvas), contentAlignment = Alignment.Center) {
            Icon(painterResource(Res.drawable.iconsax_lock), null, tint = PremiumBlue, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Complete subscription", color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(state.shopName, color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (isSheet) {
            IconButton(onClick = onDismiss, enabled = !state.isLoading, modifier = Modifier.size(40.dp)) {
                Icon(painterResource(Res.drawable.iconsax_close), "Close", tint = PremiumInk, modifier = Modifier.size(21.dp))
            }
        }
    }
}

@Composable
private fun PaymentSummaryCard(state: SubscriptionPaymentUiState) {
    val shape = RoundedCornerShape(27.dp)
    Column(
        modifier = Modifier.fillMaxWidth().shadow(8.dp, shape, ambientColor = Color(0xFFDDEBFF).copy(alpha = 0.22f), spotColor = Color(0xFFDDEBFF).copy(alpha = 0.16f))
            .clip(shape).background(Brush.linearGradient(listOf(Color(0xFFDDEBFF), Color(0xFFF4F8FF)), Offset.Zero, Offset.Infinite)).border(1.dp, Color.White.copy(alpha = 0.92f), shape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.82f)), contentAlignment = Alignment.Center) {
                Icon(painterResource(Res.drawable.iconsax_document), null, tint = PremiumBlue, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(state.packageName, color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(if (state.monthlyAmount > 0) "${formatPaymentAmount(state.monthlyAmount)} per month" else "Custom enterprise pricing", color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Total", color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 9.sp)
                Text(if (state.monthlyAmount > 0) formatPaymentAmount(state.monthlyAmount * state.months) else "Contact sales", color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("${state.months} month${if (state.months == 1) "" else "s"}", modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.82f)).padding(horizontal = 12.dp, vertical = 7.dp), color = PremiumBlue, fontFamily = oseboFontFamily(), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DurationSelector(selected: Int, onSelected: (Int) -> Unit) {
    Column {
        Text("Duration", color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Text("Choose how many months to subscribe for", color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 8.sp)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf(1, 3, 6, 12).forEach { months ->
                val active = months == selected
                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(if (active) PremiumInk else PremiumCanvas)
                        .border(1.dp, if (active) PremiumInk else PremiumBorder, RoundedCornerShape(18.dp)).clickable { onSelected(months) }.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(months.toString(), color = if (active) Color.White else PremiumInk, fontFamily = oseboFontFamily(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(if (months == 1) "month" else "months", color = if (active) Color.White.copy(alpha = 0.72f) else PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 7.sp)
                }
            }
        }
    }
}

@Composable
private fun PaymentPhoneField(state: SubscriptionPaymentUiState, onValueChange: (String) -> Unit) {
    Column {
        Text("Mobile Money number", color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 3.dp, bottom = 6.dp))
        OutlinedTextField(
            value = state.phone,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading,
            singleLine = true,
            textStyle = TextStyle(color = PremiumInk, fontFamily = oseboFontFamily(), fontSize = 12.sp),
            placeholder = { Text("0772 123 456", color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 11.sp) },
            leadingIcon = { Icon(painterResource(Res.drawable.iconsax_call), null, tint = PremiumBlue, modifier = Modifier.size(19.dp)) },
            supportingText = state.phoneError?.let { { Text(it, color = PremiumRed, fontFamily = oseboFontFamily(), fontSize = 8.sp) } },
            isError = state.phoneError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = PremiumCanvas, unfocusedContainerColor = PremiumCanvas, focusedBorderColor = PremiumBlue, unfocusedBorderColor = PremiumBorder, errorBorderColor = PremiumRed, cursorColor = PremiumBlue),
        )
    }
}

@Composable
private fun PaymentActions(state: SubscriptionPaymentUiState, onDismiss: () -> Unit, onSubmit: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Button(onClick = onDismiss, enabled = !state.isLoading, modifier = Modifier.weight(0.8f).height(50.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = PremiumCanvas, contentColor = PremiumInk), elevation = ButtonDefaults.buttonElevation(0.dp)) {
            Text("Cancel", fontFamily = oseboFontFamily(), fontSize = 10.sp)
        }
        Button(onClick = onSubmit, enabled = !state.isLoading, modifier = Modifier.weight(1.4f).height(50.dp), shape = RoundedCornerShape(50), colors = ButtonDefaults.buttonColors(containerColor = PremiumInk), elevation = ButtonDefaults.buttonElevation(0.dp)) {
            if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            else Text(if (state.monthlyAmount > 0) "Pay ${formatPaymentAmount(state.monthlyAmount * state.months)}" else "Request callback", color = Color.White, fontFamily = oseboFontFamily(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun formatPaymentAmount(amount: Double): String = "UGX ${amount.roundToLong().toString().reversed().chunked(3).joinToString(",").reversed()}"
