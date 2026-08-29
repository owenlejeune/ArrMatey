package com.dnfapps.arrmatey.ui.menu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.utils.QueueSortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.icons.Hard_drive
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActivityFilterMenu(
    instances: List<Instance>,
    selectedInstanceId: Long?,
    onInstanceChange: (Long?) -> Unit,
    sortBy: QueueSortBy,
    onSortByChanged: (QueueSortBy) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var instanceMenuExpanded by remember { mutableStateOf(false) }
    val groupInteractionSource = remember { MutableInteractionSource() }

    Box {
        IconButton(onClick = {
            menuExpanded = true
        }) {
            Icon(Icons.Default.FilterList, null)
        }
        DropdownMenuPopup(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            if (instances.size > 1) {
                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShape(0, 2),
                    interactionSource = groupInteractionSource,
                ) {
                    Box {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text =
                                        instances.firstOrNull { it.id == selectedInstanceId }?.label
                                            ?: mokoString(MR.strings.instances),
                                )
                            },
                            onClick = { instanceMenuExpanded = true },
                            trailingIcon = { Icon(Icons.Default.ChevronRight, null) },
                            leadingIcon = { Icon(Hard_drive, null) },
                        )

                        DropdownMenuPopup(
                            expanded = instanceMenuExpanded,
                            onDismissRequest = { instanceMenuExpanded = false },
                            offset = DpOffset(x = 350.dp, y = 0.dp),
                        ) {
                            DropdownMenuGroup(
                                shapes = MenuDefaults.groupShape(0, 1),
                                interactionSource = groupInteractionSource,
                                containerColor = MenuDefaults.groupVibrantContainerColor,
                            ) {
                                DropdownMenuItem(
                                    text = { Text(mokoString(MR.strings.all)) },
                                    selected = selectedInstanceId == null,
                                    onClick = { onInstanceChange(null) },
                                    selectedLeadingIcon = { Icon(Icons.Default.Check, null) },
                                    shapes = MenuDefaults.itemShape(0, instances.size + 1),
                                    colors = MenuDefaults.selectableItemVibrantColors(),
                                )
                                HorizontalDivider(Modifier.padding(MenuDefaults.HorizontalDividerPadding))
                                instances.forEachIndexed { index, instance ->
                                    DropdownMenuItem(
                                        text = { Text(instance.label) },
                                        selected = selectedInstanceId == instance.id,
                                        onClick = { onInstanceChange(instance.id) },
                                        selectedLeadingIcon = { Icon(Icons.Default.Check, null) },
                                        shapes =
                                            MenuDefaults.itemShape(
                                                index + 1,
                                                instances.size + 1,
                                            ),
                                        colors = MenuDefaults.selectableItemVibrantColors(),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))
            }

            DropdownMenuGroup(
                shapes =
                    if (instances.size > 1) {
                        MenuDefaults.groupShape(1, 2)
                    } else {
                        MenuDefaults.groupShape(0, 1)
                    },
                interactionSource = groupInteractionSource,
            ) {
                QueueSortBy.entries.forEachIndexed { index, sort ->
                    DropdownMenuItem(
                        text = { Text(mokoString(sort.resource)) },
                        shapes = MenuDefaults.itemShape(index, QueueSortBy.entries.size),
                        selected = sortBy == sort,
                        onClick = {
                            if (sortBy == sort) {
                                onSortOrderChanged(
                                    when (sortOrder) {
                                        SortOrder.Asc -> SortOrder.Desc
                                        SortOrder.Desc -> SortOrder.Asc
                                    },
                                )
                            } else {
                                onSortByChanged(sort)
                            }
                        },
                        selectedLeadingIcon = {
                            if (sortOrder == SortOrder.Asc) {
                                Icon(Icons.Default.ArrowDropUp, null)
                            } else {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        },
                    )
                }
            }
        }
    }
}
