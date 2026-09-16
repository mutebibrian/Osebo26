package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devbrian.osebo.ui.components.BadgeTone
import com.devbrian.osebo.ui.components.ListRow
import com.devbrian.osebo.ui.components.OseboPrimaryButton
import com.devbrian.osebo.ui.components.SearchField
import com.devbrian.osebo.ui.components.StatusBadge
import com.devbrian.osebo.ui.theme.OseboColors

data class TransferListItemUi(
    val id: String,
    val itemName: String,
    val fromShopName: String,
    val toShopName: String,
    val quantityLabel: String,
    val status: String,
    val createdAt: String? = null,
)

data class TransfersListUiState(
    val shopName: String = "",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val transfers: List<TransferListItemUi> = emptyList(),
)

private fun statusTone(status: String): BadgeTone = when (status.lowercase()) {
    "approved", "completed" -> BadgeTone.Success
    "rejected", "failed" -> BadgeTone.Error
    "pending" -> BadgeTone.Warning
    else -> BadgeTone.Neutral
}

@Composable
fun TransfersListScreen(
    state: TransfersListUiState,
    onSearchQueryChange: (String) -> Unit = {},
    onBulkTransferClick: () -> Unit = {},
    onTransferClick: (TransferListItemUi) -> Unit = {},
) {
    Scaffold(containerColor = OseboColors.Background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            Text("Transfers", style = MaterialTheme.typography.headlineSmall, color = OseboColors.OnSurface)
            Text(state.shopName, style = MaterialTheme.typography.bodyMedium, color = OseboColors.OnSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            OseboPrimaryButton(text = "Bulk Transfer", onClick = onBulkTransferClick, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            SearchField(query = state.searchQuery, onQueryChange = onSearchQueryChange, onFilterClick = {}, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            state.errorMessage?.let { message ->
                Text(message, style = MaterialTheme.typography.bodySmall, color = OseboColors.Error)
                Spacer(Modifier.height(8.dp))
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when {
                    state.isLoading -> CircularProgressIndicator(color = OseboColors.Primary, modifier = Modifier.align(Alignment.Center))
                    state.transfers.isEmpty() -> EmptyTransfersState()
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        items(state.transfers, key = { it.id }) { transfer ->
                            ListRow(
                                title = transfer.itemName,
                                subtitle = "${transfer.fromShopName} → ${transfer.toShopName}",
                                leadingIcon = Icons.Filled.SwapHoriz,
                                onClick = { onTransferClick(transfer) },
                                trailing = {
                                    Text(transfer.quantityLabel, style = MaterialTheme.typography.bodyMedium, color = OseboColors.OnSurface)
                                    StatusBadge(text = transfer.status, tone = statusTone(transfer.status))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTransfersState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(32.dp),
    ) {
        Spacer(Modifier.height(48.dp))
        Icon(
            Icons.Filled.SwapHoriz,
            contentDescription = null,
            tint = OseboColors.IconInactive,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "No results available",
            style = MaterialTheme.typography.bodyMedium,
            color = OseboColors.OnSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
