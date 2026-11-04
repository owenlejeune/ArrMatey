package com.dnfapps.arrmatey.ui.components.mediaserver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.dnfapps.arrmatey.mediaserver.model.WatchedFilter
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MediaServerFilterSortMenu(
    sortBy: SortBy,
    onSortByChanged: (SortBy) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit,
    watchedFilter: WatchedFilter,
    onWatchedFilterChanged: (WatchedFilter) -> Unit,
    modifier: Modifier = Modifier,
    onOpenViewCustomization: (() -> Unit)? = null,
) {
    var showSheet by remember { mutableStateOf(false) }

    val isFiltered = watchedFilter != WatchedFilter.ALL
    val sortOptions = remember {
        listOf(
            SortBy.Title,
            SortBy.Year,
            SortBy.Added,
            SortBy.Rating,
        )
    }

    Box(modifier = modifier) {
        IconButton(onClick = { showSheet = true }) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = mokoString(MR.strings.filter),
                tint = if (isFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
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
                        if (onOpenViewCustomization != null) {
                            OutlinedButton(
                                onClick = {
                                    onOpenViewCustomization()
                                    showSheet = false
                                },
                            ) {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    text = mokoString(MR.strings.view_customization),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }

                    // Filter By (Watched) Section
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
                            WatchedFilter.entries.forEach { filter ->
                                val isSelected = watchedFilter == filter
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onWatchedFilterChanged(filter) },
                                    label = { Text(mokoString(filter.resource)) },
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
