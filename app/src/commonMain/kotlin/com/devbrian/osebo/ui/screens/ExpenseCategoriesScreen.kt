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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devbrian.osebo.resources.Res
import com.devbrian.osebo.resources.iconsax_add
import com.devbrian.osebo.resources.iconsax_category
import com.devbrian.osebo.resources.iconsax_edit
import com.devbrian.osebo.resources.iconsax_search
import com.devbrian.osebo.resources.iconsax_trash
import com.devbrian.osebo.ui.theme.oseboFontFamily
import org.jetbrains.compose.resources.painterResource

data class ExpenseCategoryUi(
    val id: String,
    val name: String,
    val description: String,
    val color: Color = PremiumBlue,
    val isDefault: Boolean = false,
)

@Composable
fun ExpenseCategoriesScreen(
    categories: List<ExpenseCategoryUi>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onCreateCategory: (String, String?) -> Unit,
    onUpdateCategory: (ExpenseCategoryUi, String, String?) -> Unit,
    onDeleteCategory: (ExpenseCategoryUi) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var editor by remember { mutableStateOf<CategoryEditor?>(null) }
    var pendingDelete by remember { mutableStateOf<ExpenseCategoryUi?>(null) }
    val filtered = categories.filter {
        query.isBlank() || it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true)
    }

    Scaffold(containerColor = PremiumCanvas) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FinanceSubscreenHeader(
                    title = "Expense categories",
                    subtitle = "Organize and track spending",
                    onBackClick = onBackClick,
                    trailing = {
                        Button(
                            onClick = { editor = CategoryEditor() },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PremiumInk,
                                contentColor = Color.White,
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.iconsax_add),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.size(7.dp))
                            Text("Add", fontFamily = oseboFontFamily(), fontSize = 12.sp)
                        }
                    },
                )
            }
            item { Spacer(Modifier.height(6.dp)) }
            item {
                CategorySearchField(query = query, onQueryChange = { query = it })
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PremiumInk, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.iconsax_category),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.size(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${categories.size} categories",
                            color = Color.White,
                            fontFamily = oseboFontFamily(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Keep every expense consistently classified",
                            color = Color.White.copy(alpha = 0.66f),
                            fontFamily = oseboFontFamily(),
                            fontSize = 10.sp,
                        )
                    }
                }
            }
            item {
                FinanceSectionTitle(
                    title = "Your categories",
                    subtitle = if (query.isBlank()) "Edit or remove custom categories." else "${filtered.size} matching results",
                )
            }
            if (isLoading && categories.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumBlue, strokeWidth = 2.dp)
                    }
                }
            } else if (filtered.isEmpty()) {
                item {
                    CategoryEmptyState(
                        hasQuery = query.isNotBlank(),
                        onAddClick = { editor = CategoryEditor() },
                    )
                }
            } else {
                items(filtered, key = { it.id }) { category ->
                    CategoryRow(
                        category = category,
                        onEditClick = {
                            editor = CategoryEditor(category, category.name, category.description)
                        },
                        onDeleteClick = { pendingDelete = category },
                    )
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }

    editor?.let { current ->
        CategoryEditorDialog(
            editor = current,
            onDismiss = { editor = null },
            onSave = { name, description ->
                current.category?.let { onUpdateCategory(it, name, description) }
                    ?: onCreateCategory(name, description)
                editor = null
            },
        )
    }

    pendingDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = {
                Text("Delete category?", fontFamily = oseboFontFamily(), fontWeight = FontWeight.SemiBold)
            },
            text = {
                Text(
                    "${category.name} will be removed from your expense categories.",
                    color = PremiumMuted,
                    fontFamily = oseboFontFamily(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteCategory(category)
                    pendingDelete = null
                }) {
                    Text("Delete", color = PremiumRed, fontFamily = oseboFontFamily())
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel", color = PremiumInk, fontFamily = oseboFontFamily())
                }
            },
            containerColor = PremiumSurface,
            shape = RoundedCornerShape(26.dp),
        )
    }
}

@Composable
private fun CategorySearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text("Search categories", color = PremiumMuted, fontFamily = oseboFontFamily(), fontSize = 12.sp)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.iconsax_search),
                contentDescription = null,
                tint = PremiumMuted,
                modifier = Modifier.size(21.dp),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = PremiumInk,
            fontFamily = oseboFontFamily(),
            fontSize = 12.sp,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PremiumWhite,
            unfocusedContainerColor = PremiumWhite,
            focusedBorderColor = PremiumBlue,
            unfocusedBorderColor = PremiumBorder,
            cursorColor = PremiumBlue,
        ),
    )
}

@Composable
private fun CategoryRow(
    category: ExpenseCategoryUi,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PremiumSurface, RoundedCornerShape(22.dp))
            .border(1.dp, PremiumBorder, RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.iconsax_category),
            contentDescription = null,
            tint = PremiumMuted,
            modifier = Modifier.size(23.dp),
        )
        Spacer(Modifier.size(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = category.name,
                    color = PremiumInk,
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (category.isDefault) {
                    Text(
                        text = "  Default",
                        color = PremiumBlue,
                        fontFamily = poppins,
                        fontSize = 9.sp,
                    )
                }
            }
            Text(
                text = category.description.ifBlank { "No description" },
                color = PremiumMuted,
                fontFamily = poppins,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onEditClick) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_edit),
                contentDescription = "Edit ${category.name}",
                tint = PremiumInk,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = onDeleteClick, enabled = !category.isDefault) {
            Icon(
                painter = painterResource(Res.drawable.iconsax_trash),
                contentDescription = "Delete ${category.name}",
                tint = if (category.isDefault) PremiumMuted.copy(alpha = 0.35f) else PremiumRed,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CategoryEmptyState(hasQuery: Boolean, onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PremiumSurface, RoundedCornerShape(24.dp))
            .border(1.dp, PremiumBorder, RoundedCornerShape(24.dp))
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(if (hasQuery) Res.drawable.iconsax_search else Res.drawable.iconsax_category),
            contentDescription = null,
            tint = PremiumBlue,
            modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (hasQuery) "No matching categories" else "No categories yet",
            color = PremiumInk,
            fontFamily = oseboFontFamily(),
            fontWeight = FontWeight.SemiBold,
        )
        if (!hasQuery) {
            Spacer(Modifier.height(14.dp))
            TextButton(onClick = onAddClick) {
                Text("Create first category", color = PremiumBlue, fontFamily = oseboFontFamily())
            }
        }
    }
}

private data class CategoryEditor(
    val category: ExpenseCategoryUi? = null,
    val initialName: String = "",
    val initialDescription: String = "",
)

@Composable
private fun CategoryEditorDialog(
    editor: CategoryEditor,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit,
) {
    var name by remember(editor) { mutableStateOf(editor.initialName) }
    var description by remember(editor) { mutableStateOf(editor.initialDescription) }
    val poppins = oseboFontFamily()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (editor.category == null) "New category" else "Edit category",
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", fontFamily = poppins) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = categoryFieldColors(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", fontFamily = poppins) },
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = categoryFieldColors(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim(), description.trim().ifBlank { null }) }) {
                Text("Save", color = PremiumBlue, fontFamily = poppins, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = PremiumInk, fontFamily = poppins) }
        },
        containerColor = PremiumSurface,
        shape = RoundedCornerShape(26.dp),
    )
}

@Composable
private fun categoryFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = PremiumWhite,
    unfocusedContainerColor = PremiumWhite,
    focusedBorderColor = PremiumBlue,
    unfocusedBorderColor = PremiumBorder,
    cursorColor = PremiumBlue,
)
