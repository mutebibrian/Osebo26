package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_add
import com.devbrian.osebo.resources.iconsax_box
import com.devbrian.osebo.resources.iconsax_calendar
import com.devbrian.osebo.resources.iconsax_history
import com.devbrian.osebo.resources.iconsax_microphone
import com.devbrian.osebo.resources.iconsax_reports
import com.devbrian.osebo.resources.iconsax_total_expenses
import com.devbrian.osebo.resources.iconsax_total_sales
import com.devbrian.osebo.resources.oseboicon
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val AiInk = Color(0xFF171B1F)
private val AiMuted = Color(0xFF7D8793)
private val AiBlue = Color(0xFF0783CF)
private val AiBorder = Color(0xFFE3E8EC)

private data class AiSuggestion(
    val title: String,
    val icon: DrawableResource,
)

@Composable
fun OseboAiScreen(
    firstName: String,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    val font = oseboFontFamily()
    var prompt by remember { mutableStateOf("") }
    val suggestions = remember {
        listOf(
            AiSuggestion("Summarize sales", Res.drawable.iconsax_total_sales),
            AiSuggestion("Cash flow", Res.drawable.iconsax_reports),
            AiSuggestion("Check low stock", Res.drawable.iconsax_box),
            AiSuggestion("Review expenses", Res.drawable.iconsax_total_expenses),
            AiSuggestion("Top products", Res.drawable.iconsax_reports),
            AiSuggestion("Business insights", Res.drawable.iconsax_reports),
            AiSuggestion("Plan my day", Res.drawable.iconsax_calendar),
        )
    }

    fun submitPrompt() {
        val message = prompt.trim()
        if (message.isNotEmpty()) onSubmit(message)
    }

    Scaffold(containerColor = Color.White) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .imePadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 34.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onBackClick),
                    shape = CircleShape,
                    color = AiBlue,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.oseboicon),
                        contentDescription = "Back",
                        modifier = Modifier.padding(6.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Osebo AI",
                    color = AiInk,
                    fontFamily = font,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .offset(y = 24.dp)
                        .size(42.dp)
                        .shadow(12.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.08f)),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.88f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(Res.drawable.iconsax_history),
                            contentDescription = "Conversation history",
                            tint = AiInk,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(150.dp))
            Surface(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(50),
                color = Color(0xFFF5F6F8),
            ) {
                Text(
                    text = "AI assistant",
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp),
                    color = AiMuted,
                    fontFamily = font,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(13.dp))
            Text(
                text = "Hello ${firstName.ifBlank { "there" }},\nHow can I help today?",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                color = AiInk,
                fontFamily = font,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 29.sp,
                textAlign = TextAlign.Center,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .aiGlowBackground(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 68.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SuggestionRow(
                        suggestions = suggestions.subList(0, 2),
                        modifier = Modifier.padding(start = 20.dp),
                        onSuggestionClick = { prompt = it.title },
                    )
                    Spacer(Modifier.height(4.dp))
                    SuggestionRow(
                        suggestions = suggestions.subList(2, 5),
                        onSuggestionClick = { prompt = it.title },
                    )
                    Spacer(Modifier.height(4.dp))
                    SuggestionRow(
                        suggestions = suggestions.subList(5, 7),
                        modifier = Modifier.padding(end = 16.dp),
                        onSuggestionClick = { prompt = it.title },
                    )
                    Spacer(Modifier.height(14.dp))
                    AiPromptBar(
                        prompt = prompt,
                        onPromptChange = { prompt = it },
                        onSubmit = ::submitPrompt,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

private fun Modifier.aiGlowBackground(): Modifier = drawWithCache {
    val glow = Brush.verticalGradient(
        0.00f to Color.Transparent,
        0.12f to Color.Transparent,
        0.22f to Color(0x24E9DCFF),
        0.36f to Color(0x7EBFB7FF),
        0.58f to Color(0xC87FA2F3),
        1.00f to Color(0xF2698DE2),
    )
    var noiseSeed = 0x4F1BBCDCL
    fun nextNoise(): Float {
        noiseSeed = (noiseSeed * 1_664_525L + 1_013_904_223L) and 0xFFFFFFFFL
        return noiseSeed.toFloat() / 0xFFFFFFFFL.toFloat()
    }
    val grain = List(1_100) {
        val xFraction = nextNoise()
        val scatter = nextNoise() - 0.5f
        val arc = (xFraction - 0.5f) * (xFraction - 0.5f) * size.height * 0.10f
        val center = Offset(
            x = size.width * xFraction,
            y = size.height * (0.24f + scatter * 0.13f) + arc,
        )
        val radius = 0.35f + nextNoise() * 1.05f
        center to radius
    }
    onDrawBehind {
        drawRect(brush = glow)
        grain.forEachIndexed { index, (center, radius) ->
            drawCircle(
                color = if (index % 3 == 0) {
                    Color.White.copy(alpha = 0.10f)
                } else {
                    Color(0xFF7568D8).copy(alpha = 0.07f)
                },
                radius = radius,
                center = center,
            )
        }
    }
}

@Composable
private fun SuggestionRow(
    suggestions: List<AiSuggestion>,
    modifier: Modifier = Modifier,
    onSuggestionClick: (AiSuggestion) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        suggestions.forEachIndexed { index, suggestion ->
            AiSuggestionChip(
                suggestion = suggestion,
                onClick = { onSuggestionClick(suggestion) },
            )
            if (index < suggestions.lastIndex) Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun AiPromptBar(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val font = oseboFontFamily()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(49.dp)
            .shadow(1.dp, RoundedCornerShape(25.dp), ambientColor = Color.Black.copy(alpha = 0.025f))
            .background(Color.White.copy(alpha = 0.74f), RoundedCornerShape(25.dp))
            .border(0.75.dp, Color.White.copy(alpha = 0.52f), RoundedCornerShape(25.dp))
            .padding(start = 8.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = { },
            modifier = Modifier.size(24.dp),
            shape = CircleShape,
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(1.dp, AiMuted.copy(alpha = 0.56f)),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_add),
                    contentDescription = "Add attachment",
                    tint = AiMuted,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
        BasicTextField(
            value = prompt,
            onValueChange = onPromptChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp),
            singleLine = true,
            cursorBrush = SolidColor(AiBlue),
            textStyle = TextStyle(color = AiInk, fontFamily = font, fontSize = 11.5.sp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSubmit() }),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (prompt.isBlank()) {
                        Text(
                            text = "Got questions...",
                            color = AiMuted,
                            fontFamily = font,
                            fontSize = 11.5.sp,
                        )
                    }
                    innerTextField()
                }
            },
        )
        Surface(
            onClick = onSubmit,
            modifier = Modifier.size(34.dp),
            shape = CircleShape,
            color = AiBlue,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(Res.drawable.iconsax_microphone),
                    contentDescription = "Ask Osebo AI",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}

@Composable
private fun AiSuggestionChip(
    suggestion: AiSuggestion,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(34.dp)
            .widthIn(min = 138.dp)
            .background(Color.White.copy(alpha = 0.66f), RoundedCornerShape(18.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(suggestion.icon),
            contentDescription = null,
            tint = AiInk,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = suggestion.title,
            color = AiInk,
            fontFamily = oseboFontFamily(),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
