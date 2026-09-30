package com.devbrian.osebo.ui.screens

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.R
import com.devbrian.osebo.ui.theme.oseboFontFamily

data class ContactInfoUi(
    val phone: String = "",
    val email: String = "",
    val whatsapp: String = "",
    val address: String = "",
    val workingHours: String = "",
)

data class SupportMessageUi(
    val name: String,
    val email: String,
    val subject: String,
    val category: String,
    val message: String,
)

private val ContactCanvas = Color(0xFFF0F3F4)
private val ContactSurface = Color(0xFFFAFBFB)
private val ContactInk = Color(0xFF171B1F)
private val ContactMuted = Color(0xFF78838B)
private val ContactBorder = Color(0xFFDCE3E5)
private val ContactBlue = Color(0xFF0788CF)
private val ContactMint = Color(0xFFDDF5F2)
private val ContactPeach = Color(0xFFFFE5D3)

private val supportCategories = listOf(
    "General inquiry",
    "Technical support",
    "Billing issue",
    "Account issue",
    "Feature request",
    "Bug report",
    "Partnership",
)

@Composable
fun ContactUsScreen(
    contact: ContactInfoUi,
    isLoading: Boolean,
    resetKey: Int,
    onBack: () -> Unit,
    onCall: () -> Unit,
    onEmail: () -> Unit,
    onWhatsApp: () -> Unit,
    onMaps: () -> Unit,
    onFaq: (Int) -> Unit,
    onViewAllFaqs: () -> Unit,
    onSubmit: (SupportMessageUi) -> Unit,
) {
    var name by remember(resetKey) { mutableStateOf("") }
    var email by remember(resetKey) { mutableStateOf("") }
    var subject by remember(resetKey) { mutableStateOf("") }
    var category by remember(resetKey) { mutableStateOf("") }
    var message by remember(resetKey) { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(ContactCanvas),
        contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 132.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { ContactHeader(onBack) }
        item { SupportHero(contact.workingHours) }
        item { SectionHeading("Reach us directly", "Choose the channel that works best for you") }
        item {
            ContactChannels(
                contact = contact,
                onCall = onCall,
                onEmail = onEmail,
                onWhatsApp = onWhatsApp,
                onMaps = onMaps,
            )
        }
        item { SectionHeading("Send us a message", "Tell us what you need and our team will respond") }
        item {
            SupportForm(
                name = name,
                onNameChange = { name = it },
                email = email,
                onEmailChange = { email = it },
                subject = subject,
                onSubjectChange = { subject = it },
                category = category,
                onCategoryChange = { category = it },
                message = message,
                onMessageChange = { message = it },
                isLoading = isLoading,
                onSubmit = { onSubmit(SupportMessageUi(name, email, subject, category, message)) },
            )
        }
        item { SectionHeading("Quick answers", "Popular questions from Osebo businesses") }
        item { FaqCard(onFaq, onViewAllFaqs) }
    }
}

@Composable
private fun ContactHeader(onBack: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        ContactIcon(R.drawable.ic_iconsax_arrow_left, "Back", ContactInk, onBack, 26)
        Column(Modifier.weight(1f)) {
            Text("Contact us", color = ContactInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 25.sp)
            Text("Help, support and quick answers", color = ContactMuted, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 11.sp)
        }
        ContactIcon(R.drawable.ic_iconsax_contact, null, ContactBlue, size = 27)
    }
}

@Composable
private fun SupportHero(workingHours: String) {
    val poppins = oseboFontFamily()
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp)).background(ContactMint)
            .border(1.dp, Color.White, RoundedCornerShape(30.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ContactIcon(R.drawable.ic_iconsax_contact, null, ContactInk, size = 28)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Let’s solve it together", color = ContactInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 19.sp)
            Text("Our support team is ready to help you keep business moving.", color = ContactMuted, fontFamily = poppins, fontSize = 10.sp, lineHeight = 15.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            ContactIcon(R.drawable.ic_iconsax_clock, null, ContactInk, size = 20)
            Text(workingHours.ifBlank { "Monday–Friday, 8:00 AM–6:00 PM" }, color = ContactInk, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 9.sp)
        }
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    val poppins = oseboFontFamily()
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, color = ContactInk, fontFamily = poppins, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
        Text(subtitle, color = ContactMuted, fontFamily = poppins, fontSize = 9.sp)
    }
}

@Composable
private fun ContactChannels(
    contact: ContactInfoUi,
    onCall: () -> Unit,
    onEmail: () -> Unit,
    onWhatsApp: () -> Unit,
    onMaps: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(ContactSurface)
            .border(1.dp, Color.White, RoundedCornerShape(28.dp)).padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        ContactRow(R.drawable.ic_iconsax_call, "Call support", contact.phone.ifBlank { "Phone unavailable" }, onCall)
        ContactDivider()
        ContactRow(R.drawable.ic_iconsax_sms, "Email support", contact.email.ifBlank { "Email unavailable" }, onEmail)
        if (contact.whatsapp.isNotBlank()) {
            ContactDivider()
            ContactRow(R.drawable.ic_iconsax_contact, "WhatsApp", contact.whatsapp, onWhatsApp)
        }
        ContactDivider()
        ContactRow(R.drawable.ic_iconsax_location, "Visit us", contact.address.ifBlank { "Kampala, Uganda" }, onMaps)
    }
}

@Composable
private fun ContactRow(@DrawableRes icon: Int, title: String, value: String, onClick: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContactIcon(icon, null, ContactInk, size = 22)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, color = ContactInk, fontFamily = poppins, fontWeight = FontWeight.Medium, fontSize = 11.sp)
            Text(value, color = ContactMuted, fontFamily = poppins, fontSize = 8.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        ContactIcon(R.drawable.ic_iconsax_chevron_right, null, ContactMuted, size = 20)
    }
}

@Composable
private fun SupportForm(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    subject: String,
    onSubjectChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    message: String,
    onMessageChange: (String) -> Unit,
    isLoading: Boolean,
    onSubmit: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(ContactSurface)
            .border(1.dp, Color.White, RoundedCornerShape(28.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ContactField("Your name", name, onNameChange)
        ContactField("Email address", email, onEmailChange, keyboardType = KeyboardType.Email)
        ContactField("Subject", subject, onSubjectChange)
        CategoryField(category, onCategoryChange)
        ContactField("How can we help?", message, onMessageChange, minLines = 4)
        Row(
            Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(100.dp)).background(ContactInk)
                .clickable(enabled = !isLoading, onClick = onSubmit),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                ContactIcon(R.drawable.ic_iconsax_send, null, Color.White, size = 20)
                Spacer(Modifier.size(9.dp))
                Text("Send message", color = Color.White, fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ContactField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
) {
    val poppins = oseboFontFamily()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, fontFamily = poppins, fontSize = 10.sp) },
        textStyle = androidx.compose.ui.text.TextStyle(color = ContactInk, fontFamily = poppins, fontSize = 12.sp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        minLines = minLines,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ContactBlue,
            unfocusedBorderColor = ContactBorder,
            focusedLabelColor = ContactBlue,
            unfocusedLabelColor = ContactMuted,
            cursorColor = ContactBlue,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        ),
    )
}

@Composable
private fun CategoryField(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val poppins = oseboFontFamily()
    Box {
        Row(
            Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(20.dp)).background(Color.White)
                .border(1.dp, ContactBorder, RoundedCornerShape(20.dp)).clickable { expanded = true }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(selected.ifBlank { "Category" }, Modifier.weight(1f), color = if (selected.isBlank()) ContactMuted else ContactInk, fontFamily = poppins, fontSize = 11.sp)
            ContactIcon(R.drawable.ic_iconsax_arrow_down, null, ContactMuted, size = 19)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ContactSurface),
        ) {
            supportCategories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category, color = ContactInk, fontFamily = poppins, fontSize = 11.sp) },
                    onClick = { onSelected(category); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun FaqCard(onFaq: (Int) -> Unit, onViewAll: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(ContactPeach)
            .border(1.dp, Color.White, RoundedCornerShape(28.dp)).padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        FaqRow("How do I reset my password?", { onFaq(1) })
        ContactDivider()
        FaqRow("How do I update billing information?", { onFaq(2) })
        ContactDivider()
        FaqRow("How do I add new employees?", { onFaq(3) })
        ContactDivider()
        FaqRow("View all frequently asked questions", onViewAll, highlight = true)
    }
}

@Composable
private fun FaqRow(text: String, onClick: () -> Unit, highlight: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContactIcon(R.drawable.ic_iconsax_contact, null, if (highlight) ContactBlue else ContactInk, size = 21)
        Text(text, Modifier.weight(1f).padding(horizontal = 13.dp), color = if (highlight) ContactBlue else ContactInk, fontFamily = oseboFontFamily(), fontWeight = FontWeight.Medium, fontSize = 10.sp)
        ContactIcon(R.drawable.ic_iconsax_chevron_right, null, ContactMuted, size = 19)
    }
}

@Composable
private fun ContactDivider() {
    HorizontalDivider(color = ContactBorder.copy(alpha = 0.8f), thickness = 1.dp)
}

@Composable
private fun ContactIcon(
    @DrawableRes icon: Int,
    description: String?,
    tint: Color,
    onClick: (() -> Unit)? = null,
    size: Int = 22,
) {
    val modifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick).padding(4.dp)
    Icon(painterResource(icon), description, modifier.size(size.dp), tint)
}
