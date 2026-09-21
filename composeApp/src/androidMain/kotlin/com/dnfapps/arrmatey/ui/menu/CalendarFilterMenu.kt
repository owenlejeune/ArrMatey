package com.dnfapps.arrmatey.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurtainsClosed
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
import com.dnfapps.arrmatey.arr.state.CalendarFilterState
import com.dnfapps.arrmatey.arr.state.ContentFilter
import com.dnfapps.arrmatey.entensions.imageVector
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CalendarFilterMenu(
    filterState: CalendarFilterState,
    instances: List<Instance>,
    onContentFilterChanged: (ContentFilter) -> Unit,
    onToggleFilterMonitored: () -> Unit,
    onToggleFilterPremiersOnly: () -> Unit,
    onToggleFilterFinalesOnly: () -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }

    val configuredTypes = remember(instances) { instances.map { it.type }.toSet() }
    val contentFilters =
        remember(configuredTypes) {
            ContentFilter.entries.filter { it.instanceType == null || configuredTypes.contains(it.instanceType) }
        }

    val activeFiltersCount =
        remember(filterState) {
            var count = 0
            if (filterState.contentFilter != ContentFilter.All) count++
            if (filterState.showMonitoredOnly) count++
            if (filterState.showPremiersOnly) count++
            if (filterState.showFinalesOnly) count++
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
                                    if (filterState.contentFilter != ContentFilter.All) {
                                        onContentFilterChanged(ContentFilter.All)
                                    }
                                    if (filterState.showMonitoredOnly) {
                                        onToggleFilterMonitored()
                                    }
                                    if (filterState.showPremiersOnly) {
                                        onToggleFilterPremiersOnly()
                                    }
                                    if (filterState.showFinalesOnly) {
                                        onToggleFilterFinalesOnly()
                                    }
                                },
                            ) {
                                Text(
                                    text = mokoString(MR.strings.clear_all),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }

                    // Content Type Filter Section
                    if (contentFilters.size > 1) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = mokoString(MR.strings.type),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp),
                            ) {
                                contentFilters.forEach { contentFilter ->
                                    val isSelected = filterState.contentFilter == contentFilter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onContentFilterChanged(contentFilter) },
                                        label = { Text(mokoString(contentFilter.resource)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.Check else contentFilter.imageVector,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        },
                                        shape = MaterialTheme.shapes.small,
                                    )
                                }
                            }
                        }
                    }

                    // Display Flags Section
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
                                selected = filterState.showMonitoredOnly,
                                onClick = onToggleFilterMonitored,
                                label = { Text(mokoString(MR.strings.monitored)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (filterState.showMonitoredOnly) Icons.Default.Check else Icons.Default.Bookmark,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                                shape = MaterialTheme.shapes.small,
                            )

                            FilterChip(
                                selected = filterState.showPremiersOnly,
                                onClick = onToggleFilterPremiersOnly,
                                label = { Text(mokoString(MR.strings.premiers_only)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (filterState.showPremiersOnly) Icons.Default.Check else Icons.Default.Celebration,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                                shape = MaterialTheme.shapes.small,
                            )

                            FilterChip(
                                selected = filterState.showFinalesOnly,
                                onClick = onToggleFilterFinalesOnly,
                                label = { Text(mokoString(MR.strings.finales_only)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (filterState.showFinalesOnly) Icons.Default.Check else Icons.Default.CurtainsClosed,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
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
