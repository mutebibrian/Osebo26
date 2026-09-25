package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_arrow_down
import com.devbrian.osebo.resources.iconsax_arrow_left
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

internal val PremiumCanvas = Color(0xFFF0F3F4)
internal val PremiumSurface = Color(0xFFFAFBFB)
internal val PremiumWhite = Color(0xFFFFFFFF)
internal val PremiumInk = Color(0xFF171B1F)
internal val PremiumMuted = Color(0xFF768087)
internal val PremiumBorder = Color(0xFFDDE3E5)
internal val PremiumBlue = Color(0xFF176BFF)
internal val PremiumGreen = Color(0xFF23A36D)
internal val PremiumRed = Color(0xFFE75A67)

@Composable
internal fun FinanceSubscreenHeader(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(42.dp)) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_arrow_left),
                contentDescription = "Back",
                tint = PremiumInk,
                modifier = Modifier.size(23.dp),
            )
        }
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = PremiumInk,
                fontFamily = poppins,
                fontSize = 23.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                color = PremiumMuted,
                fontFamily = poppins,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailing?.invoke()
    }
}

@Composable
internal fun FinanceSectionTitle(title: String, subtitle: String? = null) {
    val poppins = oseboFontFamily()
    Column {
        Text(
            text = title,
            color = PremiumInk,
            fontFamily = poppins,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        subtitle?.let {
            Text(
                text = it,
                color = PremiumMuted,
                fontFamily = poppins,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
internal fun PremiumSelectField(
    label: String,
    value: String,
    icon: DrawableResource,
    placeholder: String,
    onClick: () -> Unit,
    error: String? = null,
) {
    val poppins = oseboFontFamily()
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = PremiumInk,
            fontFamily = poppins,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 7.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PremiumWhite, RoundedCornerShape(18.dp))
                .border(
                    width = 1.dp,
                    color = if (error == null) PremiumBorder else PremiumRed,
                    shape = RoundedCornerShape(18.dp),
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = PremiumBlue,
                modifier = Modifier.size(21.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = value.ifBlank { placeholder },
                color = if (value.isBlank()) PremiumMuted else PremiumInk,
                fontFamily = poppins,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(Res.drawable.iconsax_arrow_down),
                contentDescription = null,
                tint = PremiumMuted,
                modifier = Modifier.size(18.dp),
            )
        }
        error?.let {
            Text(
                text = it,
                color = PremiumRed,
                fontFamily = poppins,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            )
        }
    }
}
