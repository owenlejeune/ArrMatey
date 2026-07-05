package com.dnfapps.arrmatey.ui.menu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueFilterState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    onClearFilters: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showStatusSubMenu by remember { mutableStateOf(false) }
    var showTagsSubMenu by remember { mutableStateOf(false) }
    val groupInteractionSource = remember { MutableInteractionSource() }

    val statusScrollState = rememberScrollState()
    val tagsScrollState = rememberScrollState()

    val activeFiltersCount = remember(filterState) {
        var count = 0
        if (filterState.activeOnly) count++
        if (filterState.completedOnly) count++
        count += filterState.selectedStatuses.size
        count += filterState.selectedTags.size
        count
    }

    Box {
        IconButton(onClick = { showMenu = true }) {
            BadgedBox(
                badge = {
                    if (activeFiltersCount > 0) {
                        Badge { Text(activeFiltersCount.toString()) }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = mokoString(MR.strings.filter)
                )
            }
        }
        DropdownMenuPopup(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, 3),
                interactionSource = groupInteractionSource
            ) {
                DropdownMenuItem(
                    selected = filterState.activeOnly,
                    onClick = { onUpdateActiveOnly(!filterState.activeOnly) },
                    text = { Text(mokoString(MR.strings.active_only)) },
                    selectedLeadingIcon = { Icon(Icons.Default.Check, null) },
                    shapes = MenuDefaults.itemShape(0, 2)
                )
                DropdownMenuItem(
                    selected = filterState.completedOnly,
                    onClick = { onUpdateCompletedOnly(!filterState.completedOnly) },
                    text = { Text(mokoString(MR.strings.completed_only)) },
                    selectedLeadingIcon = { Icon(Icons.Default.Check, null) },
                    shapes = MenuDefaults.itemShape(1, 2)
                )
            }

            Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))

            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(1, 3),
                interactionSource = groupInteractionSource
            ) {
                Box {
                    DropdownMenuItem(
                        selected = false,
                        onClick = { showStatusSubMenu = true },
                        text = { Text(mokoString(MR.strings.status)) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (filterState.selectedStatuses.isNotEmpty()) {
                                    Badge { Text(filterState.selectedStatuses.size.toString()) }
                                }
                                Icon(Icons.Default.ChevronRight, null)
                            }
                        },
                        shapes = MenuDefaults.itemShape(0, if (availableTags.isEmpty()) 1 else 2)
                    )
                    DropdownMenuPopup(
                        expanded = showStatusSubMenu,
                        onDismissRequest = { showStatusSubMenu = false },
                        offset = DpOffset(x = 350.dp, y = 0.dp)
                    ) {
                        DropdownMenuGroup(
                            modifier = Modifier
                                .heightIn(max = 600.dp)
                                .verticalScroll(statusScrollState),
                            shapes = MenuDefaults.groupShape(0, 1),
                            interactionSource = groupInteractionSource,
                            containerColor = MenuDefaults.groupVibrantContainerColor
                        ) {
                            DropdownMenuItem(
                                selected = filterState.excludeStatuses,
                                onClick = { onUpdateExcludeStatuses(!filterState.excludeStatuses) },
                                text = { Text(mokoString(MR.strings.exclude)) },
                                selectedLeadingIcon = { Icon(Icons.Default.Check, null) },
                                shapes = MenuDefaults.itemShape(0, DownloadItemStatus.entries.size + 1),
                                colors = MenuDefaults.selectableItemVibrantColors()
                            )
                            HorizontalDivider(Modifier.padding(MenuDefaults.HorizontalDividerPadding))
                            DownloadItemStatus.entries.forEachIndexed { index, status ->
                                DropdownMenuItem(
                                    selected = filterState.selectedStatuses.contains(status),
                                    onClick = { onToggleStatus(status) },
                                    text = { Text(mokoString(status.resource)) },
                                    selectedLeadingIcon = { Icon(Icons.Default.Check, null) },
                                    shapes = MenuDefaults.itemShape(index + 1, DownloadItemStatus.entries.size + 1),
                                    colors = MenuDefaults.selectableItemVibrantColors()
                                )
                            }
                        }
                    }
                }

                if (availableTags.isNotEmpty()) {
                    Box {
                        DropdownMenuItem(
                            selected = false,
                            onClick = { showTagsSubMenu = true },
                            text = { Text(mokoString(MR.strings.tags)) },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (filterState.selectedTags.isNotEmpty()) {
                                        Badge { Text(filterState.selectedTags.size.toString()) }
                                    }
                                    Icon(Icons.Default.ChevronRight, null)
                                }
                            },
                            leadingIcon = { Icon(Icons.Default.Sell, null) },
                            shapes = MenuDefaults.itemShape(1, 2)
                        )
                        DropdownMenuPopup(
                            expanded = showTagsSubMenu,
                            onDismissRequest = { showTagsSubMenu = false },
                            offset = DpOffset(x = 350.dp, y = 0.dp)
                        ) {
                            DropdownMenuGroup(
                                modifier = Modifier
                                    .heightIn(max = 400.dp)
                                    .verticalScroll(tagsScrollState),
                                shapes = MenuDefaults.groupShape(0, 1),
                                interactionSource = groupInteractionSource,
                                containerColor = MenuDefaults.groupVibrantContainerColor
                            ) {
                                DropdownMenuItem(
                                    selected = filterState.excludeTags,
                                    onClick = { onUpdateExcludeTags(!filterState.excludeTags) },
                                    text = { Text(mokoString(MR.strings.exclude)) },
                                    selectedLeadingIcon = { Icon(Icons.Default.Check, null) },
                                    shapes = MenuDefaults.itemShape(0, availableTags.size + 1),
                                    colors = MenuDefaults.selectableItemVibrantColors()
                                )
                                HorizontalDivider(Modifier.padding(MenuDefaults.HorizontalDividerPadding))
                                availableTags.forEachIndexed { index, tag ->
                                    DropdownMenuItem(
                                        selected = filterState.selectedTags.contains(tag),
                                        onClick = { onToggleTag(tag) },
                                        text = { Text(tag) },
                                        selectedLeadingIcon = { Icon(Icons.Default.Check, null) },
                                        shapes = MenuDefaults.itemShape(index + 1, availableTags.size + 1),
                                        colors = MenuDefaults.selectableItemVibrantColors()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))

            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(2, 3),
                interactionSource = groupInteractionSource
            ) {
                val sortOptions = SortBy.downloadClientEntries()
                sortOptions.forEachIndexed { index, sort ->
                    DropdownMenuItem(
                        selected = sortBy == sort,
                        onClick = {
                            if (sortBy == sort) {
                                onSortOrderChanged(
                                    if (sortOrder == SortOrder.Asc) {
                                        SortOrder.Desc
                                    } else SortOrder.Asc
                                )
                            } else {
                                onSortByChanged(sort)
                            }
                        },
                        text = { Text(mokoString(sort.resource)) },
                        shapes = MenuDefaults.itemShape(index, sortOptions.size),
                        selectedLeadingIcon = {
                            when (sortOrder) {
                                SortOrder.Asc -> Icon(Icons.Default.ArrowDropUp, null)
                                SortOrder.Desc -> Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                }
            }

            if (activeFiltersCount > 0) {
                Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))
                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShape(3, 4),
                    interactionSource = groupInteractionSource
                ) {
                    DropdownMenuItem(
                        selected = false,
                        onClick = {
                            onClearFilters()
                            showMenu = false
                        },
                        text = { Text(mokoString(MR.strings.clear_all)) },
                        shapes = MenuDefaults.itemShape(0, 1)
                    )
                }
            }
        }
    }
}
