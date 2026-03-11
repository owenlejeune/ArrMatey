package com.dnfapps.arrmatey.ui.menu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IndexersSortMenu(
    sortBy: SortBy,
    onSortByChanged: (SortBy) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val groupInteractionSource = remember { MutableInteractionSource() }

    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(Icons.AutoMirrored.Default.Sort, null)
        }
        DropdownMenuPopup(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, 1),
                interactionSource = groupInteractionSource
            ) {
                val sortOptions = SortBy.typeEntries(InstanceType.Prowlarr)
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