package com.devbrian.osebo.ui

import android.view.LayoutInflater
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.devbrian.osebo.R
import com.devbrian.osebo.ui.theme.oseboFontFamily
import com.hbb20.CountryCodePicker

@Composable
internal fun AuthCountryCodePicker(
    code: String,
    onCodeChange: (String) -> Unit,
    enabled: Boolean
) {
    val picker = remember { mutableStateOf<CountryCodePicker?>(null) }
    Box(
        modifier = Modifier
            .width(84.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFD7E7F1), RoundedCornerShape(27.dp)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                (LayoutInflater.from(context).inflate(R.layout.view_signup_country_picker, null) as CountryCodePicker).apply {
                    setCountryForNameCode("UG")
                    setOnCountryChangeListener { onCodeChange(selectedCountryCodeWithPlus) }
                    picker.value = this
                }
            },
            modifier = Modifier.matchParentSize().alpha(0f)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    enabled = enabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { picker.value?.launchCountrySelectionDialog() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = code,
                color = Color(0xFF14293A),
                fontFamily = oseboFontFamily(),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }
    }
}
