package com.dnfapps.arrmatey.ui.menu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dnfapps.arrmatey.arr.api.model.CustomFilter
import com.dnfapps.arrmatey.compose.utils.FilterBy
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryFilterMenu(
    type: InstanceType,
    filterBy: FilterBy,
    onFilterByChanged: (FilterBy) -> Unit,
    customFilters: List<CustomFilter>,
    selectedCustomFilterId: Int?,
    onCustomFilterChanged: (Int?) -> Unit,
    sortBy: SortBy,
    onSortByChanged: (SortBy) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit,
    onOpenViewCustomization: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val groupInteractionSource = remember { MutableInteractionSource() }

    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Default.FilterList, null)
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
                    text = { Text(mokoString(MR.strings.view_customization)) },
                    selected = false,
                    onClick = {
                        onOpenViewCustomization()
                        showMenu = false
                    },
                    shapes = MenuDefaults.itemShape(0, 1),
                    leadingIcon = { Icon(Icons.Default.Palette, null) }
                )
            }
            Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))

            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(1, 3),
                interactionSource = groupInteractionSource
            ) {
                val filterOptions = FilterBy.typeEntries(type)
                filterOptions.forEachIndexed { index, filter ->
                    DropdownMenuItem(
                        text = { Text(mokoString(filter.resource)) },
                        selected = filterBy == filter && selectedCustomFilterId == null,
                        onClick = {
                            onFilterByChanged(filter)
                            showMenu = false
                        },
                        shapes = MenuDefaults.itemShape(index, filterOptions.size),
                        selectedLeadingIcon = {
                            Icon(Icons.Default.Check, null)
                        }
                    )
                }
            }

            if (customFilters.isNotEmpty()) {
                val libraryFilters = customFilters.filter {
                    it.type == "series" || it.type == "movies" || it.type == "artist" || it.type == "books"
                }
                if (libraryFilters.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))

                    DropdownMenuGroup(
                        shapes = MenuDefaults.groupShape(2, 4),
                        interactionSource = groupInteractionSource
                    ) {
                        libraryFilters.forEachIndexed { index, filter ->
                            DropdownMenuItem(
                                text = { Text(filter.label) },
                                selected = selectedCustomFilterId == filter.id,
                                onClick = {
                                    onCustomFilterChanged(if (selectedCustomFilterId == filter.id) null else filter.id)
                                    showMenu = false
                                },
                                shapes = MenuDefaults.itemShape(index, libraryFilters.size),
                                selectedLeadingIcon = {
                                    Icon(Icons.Default.Check, null)
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))

            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(if (customFilters.isNotEmpty()) 3 else 2, if (customFilters.isNotEmpty()) 4 else 3),
                interactionSource = groupInteractionSource
            ) {
                val sortOptions = SortBy.typeEntries(type)
                sortOptions.forEachIndexed { index, sort ->
                    DropdownMenuItem(
                        text = { Text(mokoString(sort.resource)) },
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
                        shapes = MenuDefaults.itemShape(index, sortOptions.size),
                        selectedLeadingIcon = { when(sortOrder) {
                            SortOrder.Asc -> Icon(Icons.Default.ArrowDropUp, null)
                            SortOrder.Desc -> Icon(Icons.Default.ArrowDropDown, null)
                        } }
                    )
                }
            }
        }
    }
}