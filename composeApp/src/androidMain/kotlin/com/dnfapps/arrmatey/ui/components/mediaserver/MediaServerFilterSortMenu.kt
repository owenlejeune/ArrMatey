package com.dnfapps.arrmatey.ui.components.mediaserver

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.mediaserver.model.WatchedFilter
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun MediaServerFilterSortMenu(
    sortBy: SortBy,
    onSortByChanged: (SortBy) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit,
    watchedFilter: WatchedFilter,
    onWatchedFilterChanged: (WatchedFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = mokoString(MR.strings.filter),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            // Watched Filter Section
            Text(
                text = mokoString(MR.strings.filter_by),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
            WatchedFilter.entries.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(mokoString(filter.resource)) },
                    trailingIcon = {
                        if (watchedFilter == filter) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        onWatchedFilterChanged(filter)
                        expanded = false
                    },
                )
            }

            HorizontalDivider()

            Text(
                text = mokoString(MR.strings.sort_by),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
            val sortOptions = listOf(
                SortBy.Title to MR.strings.title,
                SortBy.Year to MR.strings.year,
                SortBy.Added to MR.strings.added,
                SortBy.Rating to MR.strings.rating,
            )
            sortOptions.forEach { (option, stringRes) ->
                DropdownMenuItem(
                    text = { Text(mokoString(stringRes)) },
                    trailingIcon = {
                        if (sortBy == option) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        onSortByChanged(option)
                        expanded = false
                    },
                )
            }

            HorizontalDivider()

            // Sort Order Section
            Text(
                text = mokoString(MR.strings.sort_order),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
            DropdownMenuItem(
                text = { Text(mokoString(MR.strings.sort_ascending)) },
                trailingIcon = {
                    if (sortOrder == SortOrder.Asc) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                },
                onClick = {
                    onSortOrderChanged(SortOrder.Asc)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(mokoString(MR.strings.sort_descending)) },
                trailingIcon = {
                    if (sortOrder == SortOrder.Desc) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                },
                onClick = {
                    onSortOrderChanged(SortOrder.Desc)
                    expanded = false
                },
            )
        }
    }
}
