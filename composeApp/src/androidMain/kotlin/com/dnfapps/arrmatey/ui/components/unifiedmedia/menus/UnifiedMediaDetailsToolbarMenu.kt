package com.dnfapps.arrmatey.ui.components.unifiedmedia.menus

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.state.MediaButtonState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UnifiedMediaDetailsToolbarMenu(
    success: UnifiedMediaDetailsUiState.Success,
    buttonState: MediaButtonState,
    instanceType: InstanceType?,
    requestType: RequestType?,
    isArrConfigured: Boolean,
    isSeerrConfigured: Boolean,
    isMonitored: Boolean,
    onRefresh: () -> Unit,
    onAutomaticLookup: () -> Unit,
    onAddMissingInstance: (Instance) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkAsAvailable: () -> Unit,
    onRemoveFromService: () -> Unit,
    onClearData: () -> Unit,
    onReportIssue: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleteFile: (() -> Unit)? = null,
) {
    val showArrActions = success.hasArrId && isArrConfigured
    val showSeerrActions =
        isSeerrConfigured &&
            (
                buttonState.showReportIssueButton ||
                    buttonState.showRemoveFromServiceButton ||
                    buttonState.showClearDataButton ||
                    buttonState.showMarkAsAvailableButton
            )
    val showMissingInstances = success.missingInstances.isNotEmpty()
    val showMenuButton = showArrActions || showSeerrActions || showMissingInstances

    if (!showMenuButton) return

    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { showMenu = !showMenu },
            colors = IconButtonDefaults.headerBarColors(),
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
            )
        }

        DropdownMenuPopup(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            val totalGroups =
                (if (showArrActions) 2 else 0) +
                    (if (showMissingInstances && !showArrActions) 1 else 0) +
                    (if (showSeerrActions) 1 else 0)
            var currentGroup = 0

            if (showArrActions) {
                ArrPrimaryMenuGroup(
                    groupIndex = currentGroup++,
                    totalGroups = totalGroups,
                    isMonitored = isMonitored,
                    missingInstances = success.missingInstances,
                    onRefresh = onRefresh,
                    onAutomaticLookup = onAutomaticLookup,
                    onAddMissingInstance = onAddMissingInstance,
                    onEdit = onEdit,
                    onDismiss = { showMenu = false },
                )

                Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))

                ArrDestructiveMenuGroup(
                    groupIndex = currentGroup++,
                    totalGroups = totalGroups,
                    onDelete = onDelete,
                    onDeleteFile = onDeleteFile,
                    onDismiss = { showMenu = false },
                )

                Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))
            } else if (showMissingInstances) {
                MissingInstancesMenuGroup(
                    groupIndex = currentGroup++,
                    totalGroups = totalGroups,
                    missingInstances = success.missingInstances,
                    onAddMissingInstance = onAddMissingInstance,
                    onDismiss = { showMenu = false },
                )
                Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))
            }

            if (showSeerrActions) {
                SeerrActionsMenuGroup(
                    groupIndex = currentGroup++,
                    totalGroups = totalGroups,
                    buttonState = buttonState,
                    requestType = requestType,
                    onMarkAsAvailable = onMarkAsAvailable,
                    onRemoveFromService = onRemoveFromService,
                    onClearData = onClearData,
                    onReportIssue = onReportIssue,
                    onDismiss = { showMenu = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ArrPrimaryMenuGroup(
    groupIndex: Int,
    totalGroups: Int,
    isMonitored: Boolean,
    missingInstances: List<Instance>,
    onRefresh: () -> Unit,
    onAutomaticLookup: () -> Unit,
    onAddMissingInstance: (Instance) -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenuGroup(shapes = MenuDefaults.groupShape(groupIndex, totalGroups)) {
        DropdownMenuItem(
            text = { Text(mokoString(MR.strings.refresh)) },
            onClick = {
                onRefresh()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Refresh, null) },
        )
        DropdownMenuItem(
            text = { Text(mokoString(MR.strings.search_monitored)) },
            onClick = {
                onAutomaticLookup()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Search, null) },
        )
        DropdownMenuItem(
            text = {
                Text(
                    mokoString(if (isMonitored) MR.strings.unmonitored else MR.strings.monitored),
                )
            },
            onClick = {
                onRefresh()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    if (isMonitored) Icons.Default.BookmarkBorder else Icons.Default.Bookmark,
                    null,
                )
            },
        )
        DropdownMenuItem(
            text = { Text(mokoString(MR.strings.edit)) },
            onClick = {
                onEdit()
                onDismiss()
            },
            leadingIcon = { Icon(Icons.Default.Edit, null) },
        )
        if (missingInstances.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(MenuDefaults.HorizontalDividerPadding))
            missingInstances.forEach { instance ->
                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.add_to_arr, instance.label)) },
                    onClick = {
                        onAddMissingInstance(instance)
                        onDismiss()
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(instance.type.tabIcon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = instance.type.associatedColor,
                        )
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ArrDestructiveMenuGroup(
    groupIndex: Int,
    totalGroups: Int,
    onDelete: () -> Unit,
    onDeleteFile: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    DropdownMenuGroup(shapes = MenuDefaults.groupShape(groupIndex, totalGroups)) {
        if (onDeleteFile != null) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = mokoString(MR.strings.delete_files),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = {
                    onDeleteFile()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
            )
        }
        DropdownMenuItem(
            text = {
                Text(
                    text = mokoString(MR.strings.delete),
                    color = MaterialTheme.colorScheme.error,
                )
            },
            onClick = {
                onDelete()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MissingInstancesMenuGroup(
    groupIndex: Int,
    totalGroups: Int,
    missingInstances: List<Instance>,
    onAddMissingInstance: (Instance) -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenuGroup(shapes = MenuDefaults.groupShape(groupIndex, totalGroups)) {
        missingInstances.forEach { instance ->
            DropdownMenuItem(
                text = { Text(mokoString(MR.strings.add_to_arr, instance.label)) },
                onClick = {
                    onAddMissingInstance(instance)
                    onDismiss()
                },
                leadingIcon = {
                    Image(
                        painter = painterResource(instance.type.icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SeerrActionsMenuGroup(
    groupIndex: Int,
    totalGroups: Int,
    buttonState: MediaButtonState,
    requestType: RequestType?,
    onMarkAsAvailable: () -> Unit,
    onRemoveFromService: () -> Unit,
    onClearData: () -> Unit,
    onReportIssue: () -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenuGroup(shapes = MenuDefaults.groupShape(groupIndex, totalGroups)) {
        if (buttonState.showReportIssueButton) {
            DropdownMenuItem(
                text = { Text(mokoString(MR.strings.report_issue)) },
                onClick = {
                    onReportIssue()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ArrOrange,
                    )
                },
            )
        }
        if (buttonState.showMarkAsAvailableButton) {
            DropdownMenuItem(
                text = { Text(mokoString(MR.strings.mark_as_available)) },
                onClick = {
                    onMarkAsAvailable()
                    onDismiss()
                },
                leadingIcon = { Icon(Icons.Default.CheckCircle, null) },
            )
        }
        if (buttonState.showRemoveFromServiceButton) {
            val serviceName =
                buttonState.serviceName ?: if (requestType == RequestType.Movie) "Radarr" else "Sonarr"
            DropdownMenuItem(
                text = {
                    Text(
                        text = mokoString(MR.strings.remove_from_service, serviceName),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = {
                    onRemoveFromService()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
            )
        }
        if (buttonState.showClearDataButton) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = mokoString(MR.strings.clear_data),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = {
                    onClearData()
                    onDismiss()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
            )
        }
    }
}
