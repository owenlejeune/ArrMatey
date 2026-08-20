package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.compose.stringResource as mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ToolbarAddButton(
    canAddDirectly: Boolean,
    isSeerrConfigured: Boolean,
    pendingRequestId: Long?,
    resolvedInstanceType: InstanceType?,
    onAddDirectlyClicked: () -> Unit,
    onViewRequestClicked: () -> Unit,
    onRequestClicked: () -> Unit,
    showRequest4kButton: Boolean = false,
    onRequest4kClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (canAddDirectly && isSeerrConfigured) {
        var showToolbarAddMenu by remember { mutableStateOf(false) }
        Box(modifier = modifier) {
            IconButton(
                onClick = { showToolbarAddMenu = true },
                colors = IconButtonDefaults.headerBarColors()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = mokoString(MR.strings.add)
                )
            }
            DropdownMenuPopup(
                expanded = showToolbarAddMenu,
                onDismissRequest = { showToolbarAddMenu = false }
            ) {
                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShape(0, 1)
                ) {
                    DropdownMenuItem(
                        selected = false,
                        text = { Text(mokoString(MR.strings.add_to_arr, resolvedInstanceType?.name ?: "Arr")) },
                        onClick = {
                            showToolbarAddMenu = false
                            onAddDirectlyClicked()
                        },
                        leadingIcon = { Icon(Icons.Default.Add, null) },
                        shapes = MenuDefaults.itemShape(1, 2)
                    )
                    if (pendingRequestId != null) {
                        DropdownMenuItem(
                            selected = false,
                            text = { Text(mokoString(MR.strings.view_request)) },
                            onClick = {
                                showToolbarAddMenu = false
                                onViewRequestClicked()
                            },
                            leadingIcon = { Icon(Icons.Default.Schedule, null) },
                            shapes = MenuDefaults.itemShape(0, 2)
                        )
                    } else {
                        DropdownMenuItem(
                            selected = false,
                            text = { Text(mokoString(MR.strings.request)) },
                            onClick = {
                                showToolbarAddMenu = false
                                onRequestClicked()
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Default.Send, null) },
                            shapes = MenuDefaults.itemShape(1, 2)
                        )
                    }
                }
            }
        }
    } else if (canAddDirectly) {
        IconButton(
            onClick = onAddDirectlyClicked,
            colors = IconButtonDefaults.headerBarColors(),
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = mokoString(MR.strings.add)
            )
        }
    } else if (isSeerrConfigured) {
        if (pendingRequestId != null) {
            IconButton(
                onClick = onViewRequestClicked,
                colors = IconButtonDefaults.headerBarColors(),
                modifier = modifier
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = mokoString(MR.strings.view_request)
                )
            }
        } else if (showRequest4kButton) {
            var showRequestMenu by remember { mutableStateOf(false) }
            SplitButtonLayout(
                modifier = modifier,
                leadingButton = {
                    SplitButtonDefaults.LeadingButton(
                        onClick = onRequestClicked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background.copy(alpha = .8f),
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = mokoString(MR.strings.request)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(mokoString(MR.strings.request))
                        }
                    }
                },
                trailingButton = {
                    Box {
                        SplitButtonDefaults.TrailingButton(
                            onClick = { showRequestMenu = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.background.copy(alpha = .8f),
                                contentColor = MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenuPopup(
                            expanded = showRequestMenu,
                            onDismissRequest = { showRequestMenu = false }
                        ) {
                            DropdownMenuGroup(
                                shapes = MenuDefaults.groupShape(0, 1)
                            ) {
                                DropdownMenuItem(
                                    selected = false,
                                    text = { Text(mokoString(MR.strings.request_in_4k)) },
                                    onClick = {
                                        showRequestMenu = false
                                        onRequest4kClicked()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.HighQuality,
                                            contentDescription = null
                                        )
                                    },
                                    shapes = MenuDefaults.itemShape(0, 1)
                                )
                            }
                        }
                    }
                }
            )
        } else {
            IconButton(
                onClick = onRequestClicked,
                colors = IconButtonDefaults.headerBarColors(),
                modifier = modifier
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = mokoString(MR.strings.add)
                )
            }
        }
    }
}
