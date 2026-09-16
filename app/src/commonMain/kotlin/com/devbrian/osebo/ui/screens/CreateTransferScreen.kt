package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devbrian.osebo.ui.components.OseboOutlinedButton
import com.devbrian.osebo.ui.components.OseboPrimaryButton
import com.devbrian.osebo.ui.components.SearchField
import com.devbrian.osebo.ui.components.SectionCard
import com.devbrian.osebo.ui.theme.OseboColors
import com.devbrian.osebo.ui.theme.OseboShapes

data class TransferShopOption(val id: String, val name: String)

data class TransferItemUi(
    val id: String,
    val name: String,
    val sku: String,
    val availableQty: Double,
    val unit: String,
    val isSelected: Boolean = false,
    val transferQtyText: String = "",
)

enum class TransferMethod { UploadFile, ManualSelection }

data class CreateTransferUiState(
    val sourceShopName: String = "",
    val targetShops: List<TransferShopOption> = emptyList(),
    val selectedTargetShopId: String? = null,
    val method: TransferMethod = TransferMethod.ManualSelection,
    val searchQuery: String = "",
    val items: List<TransferItemUi> = emptyList(),
    val isLoadingItems: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val selectedFileName: String? = null,
)

@Composable
fun CreateTransferScreen(
    state: CreateTransferUiState,
    onBack: () -> Unit = {},
    onTargetShopSelected: (String) -> Unit = {},
    onMethodChange: (TransferMethod) -> Unit = {},
    onDownloadTemplateClick: () -> Unit = {},
    onChooseFileClick: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onItemToggle: (String) -> Unit = {},
    onItemQtyChange: (String, String) -> Unit = { _, _ -> },
    onSubmit: () -> Unit = {},
) {
    Scaffold(containerColor = OseboColors.Background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OseboColors.OnSurface)
                }
                Text("Bulk Transfer", style = MaterialTheme.typography.titleLarge, color = OseboColors.OnSurface)
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Text("From", style = MaterialTheme.typography.labelMedium, color = OseboColors.OnSurfaceVariant)
                Text(state.sourceShopName, style = MaterialTheme.typography.bodyLarge, color = OseboColors.OnSurface)
                Spacer(Modifier.height(16.dp))

                ShopDropdown(
                    label = "To (target shop)",
                    shops = state.targetShops,
                    selectedShopId = state.selectedTargetShopId,
                    onShopSelected = onTargetShopSelected,
                )
                Spacer(Modifier.height(20.dp))

                Text("Choose Transfer Method", style = MaterialTheme.typography.titleMedium, color = OseboColors.OnSurface)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MethodOption("Upload File", state.method == TransferMethod.UploadFile) { onMethodChange(TransferMethod.UploadFile) }
                    Spacer(Modifier.width(24.dp))
                    MethodOption("Manual Selection", state.method == TransferMethod.ManualSelection) { onMethodChange(TransferMethod.ManualSelection) }
                }
                Spacer(Modifier.height(16.dp))

                state.errorMessage?.let { message ->
                    Text(message, style = MaterialTheme.typography.bodySmall, color = OseboColors.Error)
                    Spacer(Modifier.height(8.dp))
                }

                when (state.method) {
                    TransferMethod.UploadFile -> UploadFileSection(state.selectedFileName, onDownloadTemplateClick, onChooseFileClick)
                    TransferMethod.ManualSelection -> ManualSelectionSection(
                        searchQuery = state.searchQuery,
                        items = state.items,
                        isLoading = state.isLoadingItems,
                        onSearchQueryChange = onSearchQueryChange,
                        onItemToggle = onItemToggle,
                        onItemQtyChange = onItemQtyChange,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(12.dp))
                OseboPrimaryButton(
                    text = if (state.isSubmitting) "Submitting…" else "Submit Transfer",
                    onClick = onSubmit,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ShopDropdown(
    label: String,
    shops: List<TransferShopOption>,
    selectedShopId: String?,
    onShopSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = shops.find { it.id == selectedShopId }?.name ?: "Select a shop"

    Text(label, style = MaterialTheme.typography.labelMedium, color = OseboColors.OnSurfaceVariant)
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(OseboColors.Surface, OseboShapes.CardSmall)
                .clickable { expanded = true }
                .padding(14.dp),
        ) {
            Text(selectedName, style = MaterialTheme.typography.bodyLarge, color = OseboColors.OnSurface, modifier = Modifier.weight(1f))
        }
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            shops.forEach { shop ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(shop.name) },
                    onClick = {
                        onShopSelected(shop.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun MethodOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onClick)) {
        Icon(
            imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Filled.Circle,
            contentDescription = null,
            tint = if (selected) OseboColors.Primary else OseboColors.Divider,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OseboColors.OnSurface)
    }
}

@Composable
private fun UploadFileSection(selectedFileName: String?, onDownloadTemplateClick: () -> Unit, onChooseFileClick: () -> Unit) {
    SectionCard(modifier = Modifier.fillMaxWidth()) {
        Text("Files Supported: CSV, XLSX", style = MaterialTheme.typography.bodySmall, color = OseboColors.OnSurfaceVariant, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(OseboColors.PrimaryLight, OseboShapes.CardSmall).padding(12.dp),
        ) {
            Text("Tip: Use the recommended file template for bulk transfers.", style = MaterialTheme.typography.bodySmall, color = OseboColors.OnSurface, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            OseboOutlinedButton(text = "Download", onClick = onDownloadTemplateClick)
        }
        Spacer(Modifier.height(20.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.UploadFile, contentDescription = null, tint = OseboColors.Primary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            OseboOutlinedButton(text = "Choose File", onClick = onChooseFileClick)
            Spacer(Modifier.height(8.dp))
            Text(selectedFileName ?: "No file chosen", style = MaterialTheme.typography.bodySmall, color = OseboColors.OnSurfaceVariant)
        }
    }
}

@Composable
private fun ManualSelectionSection(
    searchQuery: String,
    items: List<TransferItemUi>,
    isLoading: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onItemToggle: (String) -> Unit,
    onItemQtyChange: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SearchField(query = searchQuery, onQueryChange = onSearchQueryChange, onFilterClick = {}, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(color = OseboColors.Primary, modifier = Modifier.align(Alignment.Center).padding(24.dp))
            } else if (items.isEmpty()) {
                Text(
                    "No items found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OseboColors.OnSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    textAlign = TextAlign.Center,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    items(items, key = { it.id }) { item ->
                        TransferItemRow(item, onItemToggle, onItemQtyChange)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransferItemRow(
    item: TransferItemUi,
    onItemToggle: (String) -> Unit,
    onItemQtyChange: (String, String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemToggle(item.id) }
            .padding(vertical = 10.dp),
    ) {
        Icon(
            imageVector = if (item.isSelected) Icons.Filled.CheckCircle else Icons.Filled.Circle,
            contentDescription = null,
            tint = if (item.isSelected) OseboColors.Primary else OseboColors.Divider,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyMedium, color = OseboColors.OnSurface)
            Text("${item.sku} · ${item.availableQty} ${item.unit} available", style = MaterialTheme.typography.bodySmall, color = OseboColors.OnSurfaceVariant)
        }
        if (item.isSelected) {
            androidx.compose.material3.OutlinedTextField(
                value = item.transferQtyText,
                onValueChange = { onItemQtyChange(item.id, it) },
                modifier = Modifier.width(80.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
