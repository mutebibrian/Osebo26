package com.devbrian.osebo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.devbrian.osebo.ui.components.BadgeTone
import com.devbrian.osebo.ui.components.ListRow
import com.devbrian.osebo.ui.components.OseboOutlinedButton
import com.devbrian.osebo.ui.components.PillTab
import com.devbrian.osebo.ui.components.SearchField
import com.devbrian.osebo.ui.components.StatusBadge
import com.devbrian.osebo.ui.theme.OseboColors

private data class DemoTransaction(
    val id: String,
    val label: String,
    val timestamp: String,
    val amount: String,
    val paid: Boolean,
)

private val demoTransactions = listOf(
    DemoTransaction("1", "Sale #1 · Cash Sale", "04 Feb 2026 · 1:42PM", "$45.00", paid = true),
    DemoTransaction("2", "Sale #2 · Cash Sale", "04 Feb 2026 · 1:42PM", "$45.00", paid = true),
    DemoTransaction("3", "Sale #3 · Cash Sale", "04 Feb 2026 · 1:42PM", "$45.00", paid = false),
    DemoTransaction("4", "Sale #4 · Cash Sale", "04 Feb 2026 · 1:42PM", "$45.00", paid = true),
)

/**
 * Recreates the mockup's Transactions screen using the shared design
 * system, as a concrete demonstration of the visual language before the
 * full Compose UI migration (Phase 6) begins. Not wired into app
 * navigation yet.
 */
@Composable
fun TransactionsScreenDemo(onBack: () -> Unit = {}) {
    var query by remember { mutableStateOf("") }
    val rangeFilter = "All Time"

    Scaffold(containerColor = OseboColors.Background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OseboColors.OnSurface)
                }
                Text("Transactions", style = MaterialTheme.typography.titleLarge, color = OseboColors.OnSurface)
            }

            SearchField(
                query = query,
                onQueryChange = { query = it },
                onFilterClick = {},
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            ) {
                PillTab(text = rangeFilter, selected = true, onClick = {})
                OseboOutlinedButton(text = "Change", onClick = {})
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
                items(demoTransactions) { tx ->
                    ListRow(
                        title = tx.label,
                        subtitle = tx.timestamp,
                        leadingIcon = Icons.Filled.Payments,
                        onClick = {},
                        trailing = {
                            Text(tx.amount, style = MaterialTheme.typography.bodyLarge, color = OseboColors.OnSurface)
                            StatusBadge(
                                text = if (tx.paid) "Paid" else "Pending",
                                tone = if (tx.paid) BadgeTone.Success else BadgeTone.Warning,
                            )
                        },
                    )
                }
            }
        }
    }
}
