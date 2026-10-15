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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_arrow_left
import com.devbrian.osebo.resources.iconsax_chevron_right
import com.devbrian.osebo.resources.iconsax_close
import com.devbrian.osebo.resources.iconsax_contact
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

/** Mirrors FaqFragment's hardcoded list exactly — Android has no FAQ API either. */
data class FaqItemUi(val id: Int, val question: String, val category: String)

val defaultFaqItems = listOf(
    FaqItemUi(1, "How do I reset my password?", "Account"),
    FaqItemUi(2, "How to update billing information?", "Billing"),
    FaqItemUi(3, "How to add new employees?", "Management"),
    FaqItemUi(4, "How to generate sales reports?", "Reports"),
    FaqItemUi(5, "How to manage inventory?", "Inventory"),
    FaqItemUi(6, "What payment methods do you accept?", "Billing"),
    FaqItemUi(7, "How to contact customer support?", "Support"),
    FaqItemUi(8, "How to cancel my subscription?", "Subscription"),
)

/** Mirrors FaqDetailDialogFragment.setupUI's hardcoded answers (ids 1-3 only; 4-8 are unanswered on Android too). */
fun faqAnswer(id: Int): String = when (id) {
    1 -> "To reset your password:\n\n1. Go to the Login screen\n2. Click on 'Forgot Password'\n3. Enter your registered email address\n4. Check your email for the reset link\n5. Click the link and follow the instructions\n\nIf you don't receive the email, please check your spam folder or contact support."
    2 -> "To update your billing information:\n\n1. Go to Settings → Account\n2. Click on 'Update Payment Method'\n3. Enter your new billing details\n4. Verify the information and save\n\nNote: Changes may take up to 24 hours to reflect in your account."
    3 -> "To add new employees to your shop:\n\n1. Go to Management → Employees\n2. Click on 'Add Employee' button\n3. Fill in employee details (name, email, phone, role)\n4. Set permissions and access levels\n5. Save and the employee will receive an invitation email\n\nYou can manage employee permissions from the User Roles section."
    else -> "Information not available."
}

private val FaqCanvas = Color(0xFFF0F3F4)
private val FaqSurface = Color(0xFFFAFBFB)
private val FaqInk = Color(0xFF171B1F)
private val FaqMuted = Color(0xFF78838B)
private val FaqBlue = Color(0xFF0788CF)

@Composable
fun FaqScreen(items: List<FaqItemUi>, onBack: () -> Unit, onFaqClick: (Int) -> Unit) {
    val poppins = oseboFontFamily()
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(FaqCanvas),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(painterResource(Res.drawable.iconsax_arrow_left), "Back", tint = FaqInk, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text("Frequently asked questions", color = FaqInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                    Text("Quick answers to common questions", color = FaqMuted, fontFamily = poppins, fontSize = 10.sp)
                }
            }
        }
        items(items) { faq ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(FaqSurface)
                    .border(1.dp, Color.White, RoundedCornerShape(20.dp))
                    .clickable { onFaqClick(faq.id) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(faq.question, color = FaqInk, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(faq.category, color = FaqBlue, fontFamily = poppins, fontSize = 9.sp)
                }
                Icon(painterResource(Res.drawable.iconsax_chevron_right), null, tint = FaqMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun FaqDetailDialog(faqId: Int, onDismiss: () -> Unit, onContactSupport: () -> Unit) {
    val poppins = oseboFontFamily()
    val question = defaultFaqItems.firstOrNull { it.id == faqId }?.question ?: "FAQ"
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 18.dp,
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(Res.drawable.iconsax_contact), null, tint = FaqBlue, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(question, Modifier.weight(1f), color = FaqInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(painterResource(Res.drawable.iconsax_close), "Close", tint = FaqMuted, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(faqAnswer(faqId), color = FaqInk.copy(alpha = 0.85f), fontFamily = poppins, fontSize = 11.sp, lineHeight = 17.sp)
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onContactSupport,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = FaqInk, contentColor = Color.White),
                ) {
                    Text("Still need help? Contact support", fontFamily = poppins, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
