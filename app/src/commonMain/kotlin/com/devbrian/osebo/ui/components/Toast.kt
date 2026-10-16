package com.devbrian.osebo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.devbrian.osebo.ui.theme.oseboFontFamily
import kotlinx.coroutines.delay

/**
 * Android-Toast-style floating message: appears over the content near the
 * bottom of the screen and auto-dismisses, instead of an inline banner that
 * permanently occupies layout space until the user takes another action.
 */
@Composable
fun OseboToast(
    message: String?,
    onDismiss: () -> Unit,
    isError: Boolean = false,
    durationMillis: Long = 3500,
) {
    if (message == null) return

    LaunchedEffect(message) {
        delay(durationMillis)
        onDismiss()
    }

    Popup(alignment = Alignment.BottomCenter) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 48.dp)) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isError) Color(0xFFD9364A) else Color(0xFF1C1C1E),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    color = Color.White,
                    fontFamily = oseboFontFamily(),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
