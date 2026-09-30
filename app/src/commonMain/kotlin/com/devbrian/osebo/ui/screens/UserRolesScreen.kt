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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devbrian.osebo.ui.components.SearchField
import com.devbrian.osebo.ui.theme.oseboFontFamily

data class PermissionItemUi(
    val id: String,
    val name: String,
    val displayName: String,
    val isChecked: Boolean,
)

data class PermissionCategoryUi(
    val name: String,
    val permissions: List<PermissionItemUi>,
)

data class UserRoleUi(
    val id: String,
    val name: String,
    val description: String,
    val permissionCount: Int = 0,
)

data class EditPermissionsState(
    val role: UserRoleUi,
    val searchQuery: String = "",
    val categories: List<PermissionCategoryUi> = emptyList(),
    val selectedCount: Int = 0,
    val totalCount: Int = 0,
    val isSaving: Boolean = false,
)

data class UserRolesUiState(
    val shopLabel: String = "",
    val isLoading: Boolean = false,
    val roles: List<UserRoleUi> = emptyList(),
    val emptyMessage: String? = null,
    val editState: EditPermissionsState? = null,
)

private val RolesCanvas = Color(0xFFF0F3F4)
private val RolesSurface = Color(0xFFFAFBFB)
private val RolesInk = Color(0xFF171B1F)
private val RolesMuted = Color(0xFF78838B)
private val RolesBorder = Color(0xFFDCE3E5)
private val RolesBlue = Color(0xFF0788CF)
private val RolesMint = Color(0xFFDDF5F2)
private val RolesLavender = Color(0xFFEDEAFB)

@Composable
fun UserRolesScreen(
    state: UserRolesUiState,
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onEditPermissions: (UserRoleUi) -> Unit = {},
    onDismissEdit: () -> Unit = {},
    onEditSearchQueryChange: (String) -> Unit = {},
    onPermissionToggle: (categoryName: String, permissionId: String) -> Unit = { _, _ -> },
    onSaveChanges: () -> Unit = {},
) {
    val poppins = oseboFontFamily()

    Scaffold(containerColor = RolesCanvas) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                RolesHeader(
                    shopLabel = state.shopLabel,
                    isLoading = state.isLoading,
                    onBack = onBack,
                    onRefresh = onRefresh,
                )
            }
            item { AccessOverviewCard(roles = state.roles) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Roles and access",
                            modifier = Modifier.weight(1f),
                            color = RolesInk,
                            fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                        )
                        Text(
                            text = "${state.roles.size} ${if (state.roles.size == 1) "role" else "roles"}",
                            color = RolesBlue,
                            fontFamily = poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                        )
                    }
                    Text(
                        text = "Tap a role to review or change its permissions",
                        color = RolesMuted,
                        fontFamily = poppins,
                        fontSize = 10.sp,
                    )
                }
            }
            when {
                state.isLoading && state.roles.isEmpty() -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(210.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = RolesBlue, strokeWidth = 2.dp)
                    }
                }
                state.roles.isEmpty() -> item {
                    EmptyRolesState(
                        message = state.emptyMessage ?: "No roles found",
                        onRefresh = onRefresh,
                    )
                }
                else -> items(state.roles, key = { it.id }) { role ->
                    RoleCard(role = role, onEditClick = { onEditPermissions(role) })
                }
            }
        }
    }

    state.editState?.let { editState ->
        EditPermissionsDialog(
            state = editState,
            onDismiss = onDismissEdit,
            onSearchQueryChange = onEditSearchQueryChange,
            onPermissionToggle = onPermissionToggle,
            onSaveChanges = onSaveChanges,
        )
    }
}

@Composable
private fun RolesHeader(
    shopLabel: String,
    isLoading: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HeaderAction(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = RolesInk,
                modifier = Modifier.size(21.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "User roles",
                color = RolesInk,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 25.sp,
                lineHeight = 31.sp,
            )
            Text(
                text = shopLabel.ifBlank { "Current shop" },
                color = RolesMuted,
                fontFamily = poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        HeaderAction(onClick = onRefresh, enabled = !isLoading) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = RolesBlue,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("↻", color = RolesBlue, fontSize = 24.sp, lineHeight = 24.sp)
            }
        }
    }
}

@Composable
private fun HeaderAction(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(RolesSurface)
            .border(1.dp, RolesBorder, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun AccessOverviewCard(roles: List<UserRoleUi>) {
    val poppins = oseboFontFamily()
    val assignedPermissions = roles.sumOf { it.permissionCount }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(RolesLavender)
            .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(30.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(RolesSurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = RolesBlue,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = "Access control",
                    color = RolesInk,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
                Text(
                    text = "Define what each team member can manage",
                    color = RolesMuted,
                    fontFamily = poppins,
                    fontSize = 10.sp,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OverviewMetric(
                modifier = Modifier.weight(1f),
                value = roles.size.toString(),
                label = "active roles",
            )
            OverviewMetric(
                modifier = Modifier.weight(1f),
                value = assignedPermissions.toString(),
                label = "permissions assigned",
            )
        }
    }
}

@Composable
private fun OverviewMetric(modifier: Modifier, value: String, label: String) {
    val poppins = oseboFontFamily()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(RolesSurface.copy(alpha = 0.78f))
            .padding(horizontal = 13.dp, vertical = 11.dp),
    ) {
        Text(
            text = value,
            color = RolesInk,
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
        )
        Text(
            text = label,
            color = RolesMuted,
            fontFamily = poppins,
            fontSize = 9.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun RoleCard(role: UserRoleUi, onEditClick: () -> Unit) {
    val poppins = oseboFontFamily()
    val isManager = role.name.contains("manager", ignoreCase = true)
    val accent = if (isManager) RolesBlue else Color(0xFF159A7B)
    val avatarBackground = if (isManager) Color(0xFFDDEEFF) else RolesMint
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(RolesSurface)
            .border(1.dp, Color.White, RoundedCornerShape(26.dp))
            .clickable(onClick = onEditClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(avatarBackground),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = role.name.trim().take(1).uppercase().ifBlank { "R" },
                color = accent,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
        }
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 13.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = role.name,
                color = RolesInk,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = role.description.ifBlank { "No description" },
                color = RolesMuted,
                fontFamily = poppins,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(
                text = "${role.permissionCount} permissions",
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(avatarBackground)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                color = accent,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 8.sp,
            )
            Text(
                text = "Edit access",
                color = RolesBlue,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun EmptyRolesState(message: String, onRefresh: () -> Unit) {
    val poppins = oseboFontFamily()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(RolesSurface)
            .padding(horizontal = 24.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(RolesLavender),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Groups, contentDescription = null, tint = RolesBlue, modifier = Modifier.size(27.dp))
        }
        Text(
            text = message,
            color = RolesInk,
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
        Text(
            text = "Retry",
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(RolesInk)
                .clickable(onClick = onRefresh)
                .padding(horizontal = 18.dp, vertical = 9.dp),
            color = Color.White,
            fontFamily = poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun EditPermissionsDialog(
    state: EditPermissionsState,
    onDismiss: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onPermissionToggle: (String, String) -> Unit,
    onSaveChanges: () -> Unit,
) {
    val poppins = oseboFontFamily()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RolesCanvas),
        ) {
            Scaffold(
                containerColor = RolesCanvas,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
            ) { scaffoldPadding ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        top = 14.dp,
                        end = 20.dp,
                        bottom = 92.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            HeaderAction(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = RolesInk,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Edit permissions",
                                    color = RolesInk,
                                    fontFamily = poppins,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 24.sp,
                                )
                                Text(
                                    text = state.role.name,
                                    color = RolesMuted,
                                    fontFamily = poppins,
                                    fontSize = 11.sp,
                                )
                            }
                            Text(
                                text = "${state.selectedCount}/${state.totalCount}",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(RolesLavender)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                color = RolesBlue,
                                fontFamily = poppins,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                            )
                        }
                    }
                    item {
                        Text(
                            text = "Choose exactly what this role can see and manage.",
                            color = RolesMuted,
                            fontFamily = poppins,
                            fontSize = 11.sp,
                            lineHeight = 17.sp,
                        )
                    }
                    item {
                        SearchField(
                            query = state.searchQuery,
                            onQueryChange = onSearchQueryChange,
                            placeholder = "Search permissions",
                            onFilterClick = null,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (state.categories.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(160.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "No permissions match your search",
                                    color = RolesMuted,
                                    fontFamily = poppins,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    } else {
                        items(state.categories, key = { it.name }) { category ->
                            PermissionCategoryCard(
                                category = category,
                                onPermissionToggle = { permissionId ->
                                    onPermissionToggle(category.name, permissionId)
                                },
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onSaveChanges,
                enabled = !state.isSaving,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RolesInk,
                    contentColor = Color.White,
                    disabledContainerColor = RolesBorder,
                    disabledContentColor = RolesMuted,
                ),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.size(9.dp))
                }
                Text(
                    text = if (state.isSaving) "Saving changes" else "Save changes",
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun PermissionCategoryCard(
    category: PermissionCategoryUi,
    onPermissionToggle: (String) -> Unit,
) {
    val poppins = oseboFontFamily()
    val selected = category.permissions.count { it.isChecked }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(RolesSurface)
            .border(1.dp, Color.White, RoundedCornerShape(24.dp)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = category.name,
                modifier = Modifier.weight(1f),
                color = RolesInk,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
            )
            Text(
                text = "$selected/${category.permissions.size}",
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(RolesLavender)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                color = RolesBlue,
                fontFamily = poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
            )
        }
        category.permissions.forEachIndexed { index, permission ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = RolesBorder.copy(alpha = 0.7f),
                )
            }
            PermissionRow(
                permission = permission,
                onToggle = { onPermissionToggle(permission.id) },
            )
        }
    }
}

@Composable
private fun PermissionRow(permission: PermissionItemUi, onToggle: () -> Unit) {
    val poppins = oseboFontFamily()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (permission.isChecked) RolesBlue else RolesCanvas)
                .border(1.dp, if (permission.isChecked) RolesBlue else RolesBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (permission.isChecked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Text(
            text = permission.displayName
                .replace("Customers ", "")
                .replace("Financial Statement ", "")
                .replace("Stock Categories ", "")
                .replace("Expense Categories ", "")
                .replace("Procurement ", "")
                .replace("Suppliers ", "")
                .replace("Stock ", "")
                .replace("Sales ", ""),
            modifier = Modifier.weight(1f).padding(start = 12.dp),
            color = RolesInk,
            fontFamily = poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
        )
    }
}
