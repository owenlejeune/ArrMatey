package com.dnfapps.arrmatey.ui.menu

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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import com.dnfapps.arrmatey.arr.api.model.HistoryStateFilter
import com.dnfapps.arrmatey.arr.state.ActivityTabSegment
import com.dnfapps.arrmatey.compose.utils.QueueSortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivityFilterMenu(
    instances: List<Instance>,
    selectedTab: ActivityTabSegment,
    selectedInstanceId: Long?,
    onInstanceChange: (Long?) -> Unit,
    sortBy: QueueSortBy,
    onSortByChanged: (QueueSortBy) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit,
    selectedHistoryInstanceId: Long?,
    onHistoryInstanceChange: (Long?) -> Unit,
    historyStateFilter: HistoryStateFilter,
    onHistoryStateFilterChanged: (HistoryStateFilter) -> Unit,
    selectedDownloadedInstanceId: Long? = null,
    onDownloadedInstanceChange: (Long?) -> Unit = {},
    onOpenViewCustomization: (() -> Unit)? = null,
) {
    var showSheet by remember { mutableStateOf(false) }

    val filterCount =
        when (selectedTab) {
            ActivityTabSegment.Activity -> if (selectedInstanceId != null) 1 else 0
            ActivityTabSegment.History -> {
                var count = 0
                if (selectedHistoryInstanceId != null) count++
                if (historyStateFilter != HistoryStateFilter.All) count++
                count
            }
            ActivityTabSegment.Downloaded -> if (selectedDownloadedInstanceId != null) 1 else 0
        }
    val isFiltered = filterCount > 0

    Box {
        IconButton(onClick = { showSheet = true }) {
            BadgedBox(
                badge = {
                    if (isFiltered) {
                        Badge { Text(filterCount.toString()) }
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = mokoString(MR.strings.filter),
                    tint = if (isFiltered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            onOpenViewCustomization?.let { openCustomization ->
                                OutlinedButton(
                                    onClick = {
                                        openCustomization()
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
                            if (isFiltered) {
                                TextButton(
                                    onClick = {
                                        when (selectedTab) {
                                            ActivityTabSegment.Activity -> onInstanceChange(null)
                                            ActivityTabSegment.History -> {
                                                onHistoryInstanceChange(null)
                                                onHistoryStateFilterChanged(HistoryStateFilter.All)
                                            }
                                            ActivityTabSegment.Downloaded -> onDownloadedInstanceChange(null)
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
                    }

                    when (selectedTab) {
                        ActivityTabSegment.Activity -> {
                            if (instances.size > 1) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = mokoString(MR.strings.instances),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(0.dp),
                                    ) {
                                        val isAllSelected = selectedInstanceId == null
                                        FilterChip(
                                            selected = isAllSelected,
                                            onClick = { onInstanceChange(null) },
                                            label = { Text(mokoString(MR.strings.all)) },
                                            leadingIcon =
                                            if (isAllSelected) {
                                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                            } else {
                                                null
                                            },
                                            shape = MaterialTheme.shapes.small,
                                        )
                                        instances.forEach { instance ->
                                            val isSelected = selectedInstanceId == instance.id
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { onInstanceChange(instance.id) },
                                                label = { Text(instance.label) },
                                                leadingIcon = {
                                                    if (isSelected) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            null,
                                                            Modifier.size(16.dp),
                                                        )
                                                    } else {
                                                        Icon(
                                                            painter = painterResource(instance.type.tabIcon),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp),
                                                            tint = instance.type.associatedColor,
                                                        )
                                                    }
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

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                ) {
                                    QueueSortBy.entries.forEach { sort ->
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

                        ActivityTabSegment.History -> {
                            if (instances.size > 1) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = mokoString(MR.strings.instances),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(0.dp),
                                    ) {
                                        val isAllSelected = selectedHistoryInstanceId == null
                                        FilterChip(
                                            selected = isAllSelected,
                                            onClick = { onHistoryInstanceChange(null) },
                                            label = { Text(mokoString(MR.strings.all)) },
                                            leadingIcon =
                                            if (isAllSelected) {
                                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                            } else {
                                                null
                                            },
                                            shape = MaterialTheme.shapes.small,
                                        )
                                        instances.forEach { instance ->
                                            val isSelected = selectedHistoryInstanceId == instance.id
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { onHistoryInstanceChange(instance.id) },
                                                label = { Text(instance.label) },
                                                leadingIcon = {
                                                    if (isSelected) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            null,
                                                            Modifier.size(16.dp),
                                                        )
                                                    } else {
                                                        Icon(
                                                            painter = painterResource(instance.type.tabIcon),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp),
                                                            tint = instance.type.associatedColor,
                                                        )
                                                    }
                                                },
                                                shape = MaterialTheme.shapes.small,
                                            )
                                        }
                                    }
                                }
                            }

                            // State Filter Section
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = mokoString(MR.strings.filter_by_state),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                ) {
                                    HistoryStateFilter.entries.forEach { stateFilter ->
                                        val isSelected = historyStateFilter == stateFilter
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onHistoryStateFilterChanged(stateFilter) },
                                            label = { Text(mokoString(stateFilter.resource)) },
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

                        ActivityTabSegment.Downloaded -> {
                            if (instances.size > 1) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = mokoString(MR.strings.instances),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(0.dp),
                                    ) {
                                        val isAllSelected = selectedDownloadedInstanceId == null
                                        FilterChip(
                                            selected = isAllSelected,
                                            onClick = { onDownloadedInstanceChange(null) },
                                            label = { Text(mokoString(MR.strings.all)) },
                                            leadingIcon =
                                            if (isAllSelected) {
                                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                            } else {
                                                null
                                            },
                                            shape = MaterialTheme.shapes.small,
                                        )
                                        instances.forEach { instance ->
                                            val isSelected = selectedDownloadedInstanceId == instance.id
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { onDownloadedInstanceChange(instance.id) },
                                                label = { Text(instance.label) },
                                                leadingIcon = {
                                                    if (isSelected) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            null,
                                                            Modifier.size(16.dp),
                                                        )
                                                    } else {
                                                        Icon(
                                                            painter = painterResource(instance.type.tabIcon),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(16.dp),
                                                            tint = instance.type.associatedColor,
                                                        )
                                                    }
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
        }
    }
}
