package com.dnfapps.arrmatey.ui.menu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurtainsClosed
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
import androidx.compose.ui.Modifier
import com.dnfapps.arrmatey.arr.state.CalendarFilterState
import com.dnfapps.arrmatey.arr.state.ContentFilter
import com.dnfapps.arrmatey.entensions.imageVector
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalendarFilterMenu(
    filterState: CalendarFilterState,
    onContentFilterChanged: (ContentFilter) -> Unit,
    onToggleFilterMonitored: () -> Unit,
    onToggleFilterPremiersOnly: () -> Unit,
    onToggleFilterFinalesOnly: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val groupInteractionSource = remember { MutableInteractionSource() }

    Box {
        IconButton(onClick = {
            menuExpanded = true
        }) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
            )
        }
        DropdownMenuPopup(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            val contentFilters = ContentFilter.entries
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, 2),
                interactionSource = groupInteractionSource,
            ) {
                contentFilters.forEachIndexed { index, contentFilter ->
                    DropdownMenuItem(
                        text = { Text(mokoString(contentFilter.resource)) },
                        shapes = MenuDefaults.itemShape(index, contentFilters.size),
                        selected = filterState.contentFilter == contentFilter,
                        onClick = { onContentFilterChanged(contentFilter) },
                        selectedLeadingIcon = { Icon(Icons.Default.Check, null) },
                        trailingIcon = { Icon(contentFilter.imageVector, null) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))

            val toggles =
                listOf(
                    Triple(
                        MR.strings.monitored,
                        Pair(filterState.showMonitoredOnly, onToggleFilterMonitored),
                        Icons.Default.Bookmark,
                    ),
                    Triple(
                        MR.strings.premiers_only,
                        Pair(filterState.showPremiersOnly, onToggleFilterPremiersOnly),
                        Icons.Default.Celebration,
                    ),
                    Triple(
                        MR.strings.finales_only,
                        Pair(filterState.showFinalesOnly, onToggleFilterFinalesOnly),
                        Icons.Default.CurtainsClosed,
                    ),
                )

            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(1, 2),
            ) {
                toggles.forEachIndexed { index, (resource, pair, icon) ->
                    val (isChecked, action) = pair
                    DropdownMenuItem(
                        text = { Text(mokoString(resource)) },
                        shapes = MenuDefaults.itemShape(index, toggles.size),
                        selected = isChecked,
                        onClick = { action() },
                        selectedLeadingIcon = { Icon(Icons.Default.Check, null) },
                        trailingIcon = { Icon(icon, null) },
                    )
                }
            }
        }
    }
}
