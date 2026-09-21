package com.dnfapps.arrmatey.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueFilterState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DownloadQueueFilterMenu(
    filterState: DownloadQueueFilterState,
    sortBy: SortBy,
    onSortByChanged: (SortBy) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit,
    availableTags: List<String>,
    onToggleStatus: (DownloadItemStatus) -> Unit,
    onToggleTag: (String) -> Unit,
    onUpdateActiveOnly: (Boolean) -> Unit,
    onUpdateCompletedOnly: (Boolean) -> Unit,
    onUpdateExcludeStatuses: (Boolean) -> Unit,
    onUpdateExcludeTags: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }

    val activeFiltersCount =
        remember(filterState) {
            var count = 0
            if (filterState.activeOnly) count++
            if (filterState.completedOnly) count++
            count += filterState.selectedStatuses.size
            count += filterState.selectedTags.size
            count
        }

    Box {
        IconButton(onClick = { showSheet = true }) {
            BadgedBox(
                badge = {
                    if (activeFiltersCount > 0) {
                        Badge { Text(activeFiltersCount.toString()) }
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = mokoString(MR.strings.filter),
                    tint = if (activeFiltersCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp)
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = mokoString(MR.strings.filters),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        if (activeFiltersCount > 0) {
                            TextButton(
                                onClick = {
                                    onClearFilters()
                                },
                            ) {
                                Text(
                                    text = mokoString(MR.strings.clear_all),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }

                    // Quick State Filters
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = mokoString(MR.strings.filter_by),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            FilterChip(
                                selected = filterState.activeOnly,
                                onClick = { onUpdateActiveOnly(!filterState.activeOnly) },
                                label = { Text(mokoString(MR.strings.active_only)) },
                                leadingIcon =
                                    if (filterState.activeOnly) {
                                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                    } else {
                                        null
                                    },
                                shape = MaterialTheme.shapes.small,
                            )
                            FilterChip(
                                selected = filterState.completedOnly,
                                onClick = { onUpdateCompletedOnly(!filterState.completedOnly) },
                                label = { Text(mokoString(MR.strings.completed_only)) },
                                leadingIcon =
                                    if (filterState.completedOnly) {
                                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                    } else {
                                        null
                                    },
                                shape = MaterialTheme.shapes.small,
                            )
                        }
                    }

                    // Status Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = mokoString(MR.strings.status),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            FilterChip(
                                selected = filterState.excludeStatuses,
                                onClick = { onUpdateExcludeStatuses(!filterState.excludeStatuses) },
                                label = { Text(mokoString(MR.strings.exclude)) },
                                leadingIcon =
                                    if (filterState.excludeStatuses) {
                                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                    } else {
                                        null
                                    },
                                shape = MaterialTheme.shapes.small,
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            DownloadItemStatus.entries.forEach { status ->
                                val isSelected = filterState.selectedStatuses.contains(status)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onToggleStatus(status) },
                                    label = { Text(mokoString(status.resource)) },
                                    leadingIcon =
                                        if (isSelected) {
                                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                        } else {
                                            null
                                        },
                                    shape = MaterialTheme.shapes.small,
                                )
                            }
                        }
                    }

                    // Tags Section
                    if (availableTags.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = mokoString(MR.strings.tags),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                FilterChip(
                                    selected = filterState.excludeTags,
                                    onClick = { onUpdateExcludeTags(!filterState.excludeTags) },
                                    label = { Text(mokoString(MR.strings.exclude)) },
                                    leadingIcon =
                                        if (filterState.excludeTags) {
                                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                        } else {
                                            null
                                        },
                                    shape = MaterialTheme.shapes.small,
                                )
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp),
                            ) {
                                availableTags.forEach { tag ->
                                    val isSelected = filterState.selectedTags.contains(tag)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onToggleTag(tag) },
                                        label = { Text(tag) },
                                        leadingIcon =
                                            if (isSelected) {
                                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                            } else {
                                                null
                                            },
                                        shape = MaterialTheme.shapes.small,
                                    )
                                }
                            }
                        }
                    }

                    // Sort By Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = mokoString(MR.strings.sort_by),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            // Asc / Desc toggle
                            FilterChip(
                                selected = true,
                                onClick = {
                                    onSortOrderChanged(
                                        if (sortOrder == SortOrder.Asc) SortOrder.Desc else SortOrder.Asc,
                                    )
                                },
                                label = {
                                    Text(
                                        if (sortOrder == SortOrder.Asc) {
                                            mokoString(MR.strings.sort_ascending)
                                        } else {
                                            mokoString(MR.strings.sort_descending)
                                        },
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector =
                                            if (sortOrder == SortOrder.Asc) {
                                                Icons.Default.ArrowUpward
                                            } else {
                                                Icons.Default.ArrowDownward
                                            },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                                shape = MaterialTheme.shapes.small,
                            )
                        }

                        val sortOptions = remember { SortBy.downloadClientEntries() }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            sortOptions.forEach { sort ->
                                val isSelected = sortBy == sort
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSortByChanged(sort) },
                                    label = { Text(mokoString(sort.resource)) },
                                    leadingIcon =
                                        if (isSelected) {
                                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                        } else {
                                            null
                                        },
                                    shape = MaterialTheme.shapes.small,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

