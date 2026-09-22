package com.devbrian.osebo.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.R
import com.devbrian.osebo.ui.theme.oseboFontFamily

data class MoreMenuItem(
    val menuItemId: Int,
    val label: String,
    @param:DrawableRes val iconRes: Int,
    val isDestructive: Boolean = false,
)

data class MoreMenuSection(
    val title: String,
    val items: List<MoreMenuItem>,
)

private val SheetBackground = Color.White
private val SearchBackground = Color(0xFFF2F4F5)
private val PrimaryText = Color(0xFF171B1F)
private val SecondaryText = Color(0xFF7A8187)
private val DestructiveRed = Color(0xFFD84B4B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuSheet(
    sections: List<MoreMenuSection>,
    onDismiss: () -> Unit,
    onItemSelected: (MoreMenuItem) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val poppins = oseboFontFamily()
    val filteredSections = remember(searchQuery, sections) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            sections
        } else {
            sections.mapNotNull { section ->
                val matchingItems = section.items.filter { item ->
                    item.label.contains(query, ignoreCase = true) ||
                        section.title.contains(query, ignoreCase = true)
                }
                section.takeIf { matchingItems.isNotEmpty() }
                    ?.copy(items = matchingItems)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetBackground,
        contentColor = PrimaryText,
        scrimColor = Color.Black.copy(alpha = 0.28f),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 12.dp)
                    .size(width = 52.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFC8CDD1)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 22.dp),
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                textStyle = TextStyle(
                    color = PrimaryText,
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                ),
                placeholder = {
                    Text(
                        text = "Search pages",
                        color = SecondaryText,
                        fontFamily = poppins,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_iconsax_search),
                        contentDescription = null,
                        tint = SecondaryText,
                        modifier = Modifier.size(21.dp),
                    )
                },
                singleLine = true,
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SearchBackground,
                    unfocusedContainerColor = SearchBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF0783CF),
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                ),
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredSections.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.ic_iconsax_search),
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(30.dp),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No pages found",
                            color = PrimaryText,
                            fontFamily = poppins,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "Try a different search",
                            color = SecondaryText,
                            fontFamily = poppins,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    filteredSections.forEach { section ->
                        item(key = "header-${section.title}") {
                            Text(
                                text = section.title,
                                modifier = Modifier.padding(top = 20.dp, bottom = 5.dp),
                                color = PrimaryText,
                                fontFamily = poppins,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        items(
                            items = section.items,
                            key = { it.menuItemId },
                        ) { item ->
                            MoreMenuRow(
                                item = item,
                                onClick = { onItemSelected(item) },
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreMenuRow(
    item: MoreMenuItem,
    onClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    val rowColor = if (item.isDestructive) DestructiveRed else PrimaryText

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                role = Role.Button,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 4.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(item.iconRes),
            contentDescription = null,
            tint = rowColor,
            modifier = Modifier.size(24.dp),
        )

        Text(
            text = item.label,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            color = rowColor,
            fontFamily = poppins,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
        )

        Icon(
            painter = painterResource(R.drawable.ic_iconsax_chevron_right),
            contentDescription = null,
            tint = if (item.isDestructive) DestructiveRed.copy(alpha = 0.7f) else SecondaryText,
            modifier = Modifier.size(21.dp),
        )
    }
}
