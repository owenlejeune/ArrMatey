package com.dnfapps.arrmatey.ui.menu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.dnfapps.arrmatey.arr.api.model.CustomFilter
import com.dnfapps.arrmatey.compose.utils.FilterBy
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.let

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryFilterMenu(
    type: InstanceType,
    filterBy: FilterBy,
    onFilterByChanged: (FilterBy) -> Unit,
    customFilters: List<CustomFilter>,
    selectedCustomFilterId: Long?,
    onCustomFilterChanged: (Long?) -> Unit,
    sortBy: SortBy,
    onSortByChanged: (SortBy) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit,
    onOpenViewCustomization: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showFilterSubMenu by remember { mutableStateOf(false) }
    var showSortSubMenu by remember { mutableStateOf(false) }
    val groupInteractionSource = remember { MutableInteractionSource() }

    val libraryFilters =
        remember(customFilters) {
            customFilters.filter {
                it.type == "series" || it.type == "movies" || it.type == "artist" || it.type == "books"
            }
        }
    val filterOptions = remember(type) { FilterBy.typeEntries(type) }
    val sortOptions = remember(type) { SortBy.typeEntries(type) }

    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Default.FilterList, null)
        }
        DropdownMenuPopup(
            expanded = showMenu,
            onDismissRequest = {
                showMenu = false
                showFilterSubMenu = false
                showSortSubMenu = false
            },
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, 2),
                interactionSource = groupInteractionSource,
            ) {
                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.view_customization)) },
                    selected = false,
                    onClick = {
                        onOpenViewCustomization()
                        showMenu = false
                    },
                    shapes = MenuDefaults.itemShape(0, 1),
                    leadingIcon = { Icon(Icons.Default.Palette, null) },
                )
            }
            Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))

            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(1, 2),
                interactionSource = groupInteractionSource,
            ) {
                Box {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(mokoString(MR.strings.filter))
                                Text(
                                    text =
                                        selectedCustomFilterId?.let {
                                            customFilters.firstOrNull { it.id == selectedCustomFilterId }?.label
                                        } ?: mokoString(filterBy.resource),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        },
                        selected = false,
                        onClick = {
                            showFilterSubMenu = true
                            showSortSubMenu = false
                        },
                        shapes = MenuDefaults.itemShape(0, 2),
                        leadingIcon = { Icon(Icons.Default.FilterList, null) },
                        trailingIcon = { Icon(Icons.Default.ChevronRight, null) },
                    )

                    DropdownMenuPopup(
                        expanded = showFilterSubMenu,
                        onDismissRequest = { showFilterSubMenu = false },
                        offset = DpOffset(x = 350.dp, y = 0.dp),
                    ) {
                        DropdownMenuGroup(
                            shapes = MenuDefaults.groupShape(0, if (libraryFilters.isNotEmpty()) 2 else 1),
                            interactionSource = groupInteractionSource,
                            containerColor = MenuDefaults.groupVibrantContainerColor,
                        ) {
                            filterOptions.forEachIndexed { index, filter ->
                                DropdownMenuItem(
                                    text = { Text(mokoString(filter.resource)) },
                                    selected = filterBy == filter && selectedCustomFilterId == null,
                                    onClick = {
                                        onFilterByChanged(filter)
                                        showMenu = false
                                        showFilterSubMenu = false
                                    },
                                    shapes = MenuDefaults.itemShape(index, filterOptions.size),
                                    colors = MenuDefaults.selectableItemVibrantColors(),
                                    selectedLeadingIcon = {
                                        Icon(Icons.Default.Check, null)
                                    },
                                )
                            }
                        }

                        if (libraryFilters.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))

                            DropdownMenuGroup(
                                shapes = MenuDefaults.groupShape(1, 2),
                                interactionSource = groupInteractionSource,
                                containerColor = MenuDefaults.groupVibrantContainerColor,
                            ) {
                                libraryFilters.forEachIndexed { index, filter ->
                                    DropdownMenuItem(
                                        text = { Text(filter.label) },
                                        selected = selectedCustomFilterId == filter.id,
                                        onClick = {
                                            onCustomFilterChanged(if (selectedCustomFilterId == filter.id) null else filter.id)
                                            showMenu = false
                                            showFilterSubMenu = false
                                        },
                                        shapes = MenuDefaults.itemShape(index, libraryFilters.size),
                                        colors = MenuDefaults.selectableItemVibrantColors(),
                                        selectedLeadingIcon = {
                                            Icon(Icons.Default.Check, null)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                Box {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(mokoString(MR.strings.sort))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        text = mokoString(sortBy.resource),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                    Icon(
                                        imageVector =
                                            when (sortOrder) {
                                                SortOrder.Asc -> Icons.Default.ArrowDropUp
                                                SortOrder.Desc -> Icons.Default.ArrowDropDown
                                            },
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }
                        },
                        selected = false,
                        onClick = {
                            showSortSubMenu = true
                            showFilterSubMenu = false
                        },
                        shapes = MenuDefaults.itemShape(1, 2),
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, null) },
                        trailingIcon = { Icon(Icons.Default.ChevronRight, null) },
                    )

                    DropdownMenuPopup(
                        expanded = showSortSubMenu,
                        onDismissRequest = { showSortSubMenu = false },
                        offset = DpOffset(x = 350.dp, y = 0.dp),
                    ) {
                        DropdownMenuGroup(
                            shapes = MenuDefaults.groupShape(0, 1),
                            interactionSource = groupInteractionSource,
                            containerColor = MenuDefaults.groupVibrantContainerColor,
                        ) {
                            sortOptions.forEachIndexed { index, sort ->
                                DropdownMenuItem(
                                    text = { Text(mokoString(sort.resource)) },
                                    selected = sortBy == sort,
                                    onClick = {
                                        if (sortBy == sort) {
                                            onSortOrderChanged(
                                                if (sortOrder == SortOrder.Asc) {
                                                    SortOrder.Desc
                                                } else {
                                                    SortOrder.Asc
                                                },
                                            )
                                        } else {
                                            onSortByChanged(sort)
                                        }
                                    },
                                    shapes = MenuDefaults.itemShape(index, sortOptions.size),
                                    colors = MenuDefaults.selectableItemVibrantColors(),
                                    selectedLeadingIcon = {
                                        when (sortOrder) {
                                            SortOrder.Asc -> Icon(Icons.Default.ArrowDropUp, null)
                                            SortOrder.Desc -> Icon(Icons.Default.ArrowDropDown, null)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
