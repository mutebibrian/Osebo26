package com.devbrian.osebo.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devbrian.osebo.ui.components.OseboPrimaryButton
import com.devbrian.osebo.ui.components.SearchField
import com.devbrian.osebo.ui.theme.OseboColors

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
)

data class EditPermissionsState(
    val role: UserRoleUi,
    val searchQuery: String = "",
    val categories: List<PermissionCategoryUi> = emptyList(),
    val isSaving: Boolean = false,
)

data class UserRolesUiState(
    val shopLabel: String = "",
    val isLoading: Boolean = false,
    val roles: List<UserRoleUi> = emptyList(),
    val emptyMessage: String? = null,
    val editState: EditPermissionsState? = null,
)

@Composable
fun UserRolesScreen(
    state: UserRolesUiState,
    onBack: () -> Unit = {},
    onEditPermissions: (UserRoleUi) -> Unit = {},
    onDismissEdit: () -> Unit = {},
    onEditSearchQueryChange: (String) -> Unit = {},
    onPermissionToggle: (categoryName: String, permissionId: String) -> Unit = { _, _ -> },
    onSaveChanges: () -> Unit = {},
) {
    Scaffold(
        containerColor = OseboColors.Background,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    if (state.shopLabel.isNotEmpty()) "${state.shopLabel} · User Roles" else "Shops · User Roles",
                    style = MaterialTheme.typography.titleLarge,
                    color = OseboColors.OnSurface,
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(color = OseboColors.Primary, modifier = Modifier.align(Alignment.Center))
                state.roles.isEmpty() -> EmptyRolesState(message = state.emptyMessage ?: "No roles found")
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.roles, key = { it.id }) { role ->
                        RoleCard(role = role, onEditClick = { onEditPermissions(role) })
                    }
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
private fun RoleCard(role: UserRoleUi, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OseboColors.Surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(role.name, style = MaterialTheme.typography.titleMedium, color = OseboColors.OnSurface)
            Text(
                role.description.ifBlank { "No description" },
                style = MaterialTheme.typography.bodySmall,
                color = OseboColors.OnSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onEditClick) { Text("Edit Permissions") }
        }
    }
}

@Composable
private fun EmptyRolesState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Icon(Icons.Filled.Groups, contentDescription = null, tint = OseboColors.IconInactive, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = OseboColors.OnSurfaceVariant)
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
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            containerColor = OseboColors.Background,
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                    Text(
                        "Edit Permissions - ${state.role.name}",
                        style = MaterialTheme.typography.titleLarge,
                        color = OseboColors.OnSurface,
                    )
                }
            },
            bottomBar = {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    OseboPrimaryButton(
                        text = if (state.isSaving) "Saving..." else "Save Changes",
                        onClick = onSaveChanges,
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(8.dp))
                SearchField(
                    query = state.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = "Search permissions...",
                    onFilterClick = null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    state.categories.forEach { category ->
                        item(key = "header_${category.name}") {
                            Text(
                                category.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = OseboColors.Primary,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                        items(category.permissions, key = { "${category.name}_${it.id}" }) { permission ->
                            PermissionRow(
                                permission = permission,
                                onToggle = { onPermissionToggle(category.name, permission.id) },
                            )
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(permission: PermissionItemUi, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = permission.isChecked, onCheckedChange = { onToggle() })
        Text(permission.displayName, style = MaterialTheme.typography.bodyMedium, color = OseboColors.OnSurface)
    }
}
