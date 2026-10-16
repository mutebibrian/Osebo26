package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.devbrian.osebo.models.AiAssistantUiState
import com.devbrian.osebo.models.AiChatMessage
import com.devbrian.osebo.models.AiConversation
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_add
import com.devbrian.osebo.resources.iconsax_arrow_left
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

private data class AiSuggestion(
    val title: String,
    val icon: DrawableResource,
)

@Composable
fun OseboAiScreen(
    firstName: String,
    uiState: AiAssistantUiState,
    onBackClick: () -> Unit,
    onOpenConversation: (String) -> Unit,
    onArchiveConversation: (String) -> Unit,
    onStartNewConversation: () -> Unit,
    onRetry: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    var prompt by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
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

    fun submitPrompt(message: String = prompt) {
        val cleaned = message.trim()
        if (cleaned.isEmpty() || uiState.isSending) return
        onSubmit(cleaned)
        prompt = ""
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    Scaffold(containerColor = Color.White) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .pointerInput(onBackClick) {
                    var horizontalDrag = 0f
                    val backSwipeThreshold = 72.dp.toPx()
                    detectHorizontalDragGestures(
                        onDragStart = { horizontalDrag = 0f },
                        onDragCancel = { horizontalDrag = 0f },
                        onDragEnd = {
                            if (horizontalDrag <= -backSwipeThreshold) onBackClick()
                            horizontalDrag = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            horizontalDrag += dragAmount
                        },
                    )
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(pass = PointerEventPass.Final)
                        if (!down.isConsumed) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    }
                },
        ) {
            AiHeader(onBackClick = onBackClick, onHistoryClick = { showHistory = true })

            if (uiState.messages.isEmpty()) {
                Spacer(Modifier.height(if (isKeyboardVisible) 24.dp else 150.dp))
                AiAssistantHeading(firstName = firstName)
            } else {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = uiState.conversations
                        .firstOrNull { it.id == uiState.activeConversationId }
                        ?.title
                        ?.takeIf(String::isNotBlank)
                        ?: "Current conversation",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    color = AiMuted,
                    fontFamily = oseboFontFamily(),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .aiGlowBackground()
                    .imePadding(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 8.dp,
                            end = 8.dp,
                            bottom = if (isKeyboardVisible) 12.dp else 68.dp,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (uiState.messages.isEmpty()) {
                        Spacer(Modifier.weight(1f))
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(bottom = 18.dp)
                                    .size(22.dp),
                                color = AiBlue,
                                strokeWidth = 2.dp,
                            )
                        }
                        uiState.errorMessage?.let { message ->
                            AiErrorMessage(message = message, onRetry = onRetry)
                            Spacer(Modifier.height(10.dp))
                        }
                        SuggestionRow(
                            suggestions = suggestions.subList(0, 2),
                            modifier = Modifier.padding(start = 20.dp),
                            onSuggestionClick = { submitPrompt(it.title) },
                        )
                        Spacer(Modifier.height(4.dp))
                        SuggestionRow(
                            suggestions = suggestions.subList(2, 5),
                            onSuggestionClick = { submitPrompt(it.title) },
                        )
                        Spacer(Modifier.height(4.dp))
                        SuggestionRow(
                            suggestions = suggestions.subList(5, 7),
                            modifier = Modifier.padding(end = 16.dp),
                            onSuggestionClick = { submitPrompt(it.title) },
                        )
                    } else {
                        AiConversationMessages(
                            messages = uiState.messages,
                            isSending = uiState.isSending,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        )
                        uiState.errorMessage?.let { message ->
                            AiErrorMessage(message = message, onRetry = onRetry)
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    AiPromptBar(
                        prompt = prompt,
                        onPromptChange = { prompt = it },
                        onSubmit = { submitPrompt() },
                        enabled = !uiState.isLoading && !uiState.isSending,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }

    if (showHistory) {
        AiConversationHistoryDialog(
            conversations = uiState.conversations,
            onDismiss = { showHistory = false },
            onNewConversation = {
                showHistory = false
                onStartNewConversation()
            },
            onConversationClick = { conversationId ->
                showHistory = false
                onOpenConversation(conversationId)
            },
            onArchive = onArchiveConversation,
        )
    }
}

@Composable
private fun AiHeader(onBackClick: () -> Unit, onHistoryClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 34.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(42.dp)) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_arrow_left),
                contentDescription = "Back",
                tint = AiInk,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(4.dp))
        Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = AiBlue) {
            Image(
                painter = painterResource(Res.drawable.oseboicon),
                contentDescription = null,
                modifier = Modifier.padding(6.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Osebo AI",
            color = AiInk,
            fontFamily = oseboFontFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        Surface(
            onClick = onHistoryClick,
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
}

@Composable
private fun AiAssistantHeading(firstName: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(shape = RoundedCornerShape(50), color = Color(0xFFF5F6F8)) {
            Text(
                text = "AI assistant",
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp),
                color = AiMuted,
                fontFamily = oseboFontFamily(),
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
            fontFamily = oseboFontFamily(),
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 29.sp,
            textAlign = TextAlign.Center,
        )
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
private fun AiConversationMessages(
    messages: List<AiChatMessage>,
    isSending: Boolean,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val itemCount = messages.size + if (isSending) 1 else 0
    LaunchedEffect(itemCount) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        items(messages, key = AiChatMessage::id) { message -> AiChatBubble(message) }
        if (isSending) item(key = "assistant-typing") { AiTypingBubble() }
    }
}

@Composable
private fun AiChatBubble(message: AiChatMessage) {
    val isUser = message.isFromUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isUser) {
            Surface(modifier = Modifier.size(25.dp), shape = CircleShape, color = AiBlue) {
                Image(
                    painter = painterResource(Res.drawable.oseboicon),
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp),
                )
            }
            Spacer(Modifier.width(6.dp))
        }
        Surface(
            modifier = Modifier.widthIn(max = 278.dp),
            shape = if (isUser) {
                RoundedCornerShape(topStart = 19.dp, topEnd = 6.dp, bottomEnd = 19.dp, bottomStart = 19.dp)
            } else {
                RoundedCornerShape(topStart = 6.dp, topEnd = 19.dp, bottomEnd = 19.dp, bottomStart = 19.dp)
            },
            color = if (isUser) AiBlue else Color.White.copy(alpha = 0.78f),
            border = if (isUser) null else androidx.compose.foundation.BorderStroke(
                0.5.dp,
                Color.White.copy(alpha = 0.42f),
            ),
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                color = if (isUser) Color.White else AiInk,
                fontFamily = oseboFontFamily(),
                fontSize = 11.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
private fun AiTypingBubble() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(25.dp), shape = CircleShape, color = AiBlue) {
            Image(
                painter = painterResource(Res.drawable.oseboicon),
                contentDescription = null,
                modifier = Modifier.padding(4.dp),
            )
        }
        Spacer(Modifier.width(6.dp))
        Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.72f)) {
            Row(
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(3) {
                    Box(Modifier.size(4.dp).background(AiMuted.copy(alpha = 0.7f), CircleShape))
                }
            }
        }
    }
}

@Composable
private fun AiErrorMessage(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clickable(onClick = onRetry),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.76f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.45f)),
    ) {
        Text(
            text = "$message  Tap to retry",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = AiInk,
            fontFamily = oseboFontFamily(),
            fontSize = 9.5.sp,
            textAlign = TextAlign.Center,
        )
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
            AiSuggestionChip(suggestion = suggestion, onClick = { onSuggestionClick(suggestion) })
            if (index < suggestions.lastIndex) Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun AiPromptBar(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean,
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
            enabled = enabled,
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
                            text = if (enabled) "Got questions..." else "Osebo AI is thinking...",
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
            onClick = { if (enabled) onSubmit() },
            modifier = Modifier.size(34.dp),
            shape = CircleShape,
            color = if (enabled) AiBlue else AiBlue.copy(alpha = 0.45f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (enabled) {
                    Icon(
                        painter = painterResource(Res.drawable.iconsax_microphone),
                        contentDescription = "Ask Osebo AI",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp),
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 1.5.dp,
                    )
                }
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

@Composable
private fun AiConversationHistoryDialog(
    conversations: List<AiConversation>,
    onDismiss: () -> Unit,
    onNewConversation: () -> Unit,
    onConversationClick: (String) -> Unit,
    onArchive: (String) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 12.dp,
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Conversation history",
                        modifier = Modifier.weight(1f),
                        color = AiInk,
                        fontFamily = oseboFontFamily(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Surface(
                        onClick = onNewConversation,
                        shape = RoundedCornerShape(18.dp),
                        color = AiBlue,
                    ) {
                        Text(
                            text = "New chat",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            color = Color.White,
                            fontFamily = oseboFontFamily(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (conversations.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No conversations yet",
                            color = AiMuted,
                            fontFamily = oseboFontFamily(),
                            fontSize = 12.sp,
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(conversations, key = AiConversation::id) { conversation ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF5F6F8), RoundedCornerShape(18.dp))
                                    .clickable { onConversationClick(conversation.id) }
                                    .padding(start = 13.dp, top = 11.dp, bottom = 11.dp, end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.iconsax_history),
                                    contentDescription = null,
                                    tint = AiBlue,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(9.dp))
                                Text(
                                    text = conversation.title,
                                    modifier = Modifier.weight(1f),
                                    color = AiInk,
                                    fontFamily = oseboFontFamily(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "Archive",
                                    modifier = Modifier
                                        .clickable { onArchive(conversation.id) }
                                        .padding(7.dp),
                                    color = AiMuted,
                                    fontFamily = oseboFontFamily(),
                                    fontSize = 9.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
