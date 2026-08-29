package com.dnfapps.arrmatey.ui.menu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.FilterList
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
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchSortMenu(
    sortBy: SortBy,
    onSortChanged: (SortBy) -> Unit,
    sortOrder: SortOrder,
    onOrderChanged: (SortOrder) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = remember { SortBy.lookupEntries() }
    val groupInteractionSource = remember { MutableInteractionSource() }

    Box {
        IconButton(
            onClick = { expanded = !expanded },
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = mokoString(MR.strings.sort),
            )
        }
        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, 1),
                interactionSource = groupInteractionSource,
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(mokoString(option.resource)) },
                        onClick = {
                            if (option == sortBy) {
                                onOrderChanged(
                                    when (sortOrder) {
                                        SortOrder.Asc -> SortOrder.Desc
                                        SortOrder.Desc -> SortOrder.Asc
                                    },
                                )
                            } else {
                                onSortChanged(option)
                            }
                        },
                        selected = option == sortBy,
                        selectedLeadingIcon = {
                            when (sortOrder) {
                                SortOrder.Asc -> Icon(Icons.Default.ArrowDropUp, null)
                                SortOrder.Desc -> Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                        shapes = MenuDefaults.itemShape(index, options.size),
                    )
                }
            }
        }
    }
}
