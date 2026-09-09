package com.devbrian.osebo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devbrian.osebo.ui.theme.OseboColors
import com.devbrian.osebo.ui.theme.OseboShapes

/**
 * Rounded pill search bar with a trailing filter button, matching the
 * search-plus-filter pattern used across the list/grid screens.
 */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search dishes by name...",
    onFilterClick: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = OseboColors.OnSurfaceVariant) },
        trailingIcon = onFilterClick?.let {
            {
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = it, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Tune, contentDescription = "Filter", tint = OseboColors.Primary)
                    }
                }
            }
        },
        singleLine = true,
        shape = OseboShapes.Pill,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = OseboColors.Surface,
            unfocusedContainerColor = OseboColors.Surface,
            focusedBorderColor = OseboColors.Divider,
            unfocusedBorderColor = OseboColors.Divider,
        ),
    )
}
