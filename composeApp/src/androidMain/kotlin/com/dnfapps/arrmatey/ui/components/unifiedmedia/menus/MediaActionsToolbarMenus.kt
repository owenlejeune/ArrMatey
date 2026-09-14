package com.dnfapps.arrmatey.ui.components.unifiedmedia.menus

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.seerr.state.MediaButtonState
import com.dnfapps.arrmatey.seerr.state.MediaProvider
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MediaActionsToolbarMenus(
    buttonState: MediaButtonState,
    canAddDirectly: Boolean,
    onWatchClicked: (String, MediaProvider) -> Unit,
    onWatchTrailerClicked: (String) -> Unit,
    onViewRequestClicked: (Long) -> Unit,
    onApproveRequestClicked: (Long) -> Unit,
    onDeclineRequestClicked: (Long) -> Unit,
    onRequestClicked: () -> Unit,
    onRequest4kClicked: () -> Unit,
    onAddDirectlyClicked: () -> Unit,
) {
    if (buttonState.showWatchButton || buttonState.showWatchTrailerOption) {
        var showWatchMenu by remember { mutableStateOf(false) }
        val serviceIconRes: Any =
            when (buttonState.mediaProvider) {
                MediaProvider.Plex -> MR.images.plex
                MediaProvider.Jellyfin -> MR.images.jellyfin
                MediaProvider.None -> Icons.Default.PlayArrow
            }

        Box {
            IconButton(
                onClick = {
                    if (buttonState.showWatchButton && !buttonState.showWatchTrailerOption) {
                        buttonState.watchButtonUrl?.let { onWatchClicked(it, buttonState.mediaProvider) }
                    } else {
                        showWatchMenu = true
                    }
                },
                colors = IconButtonDefaults.headerBarColors(),
            ) {
                Icon(Icons.Default.PlayArrow, mokoString(buttonState.watchButtonLabel))
            }

            DropdownMenuPopup(
                expanded = showWatchMenu,
                onDismissRequest = { showWatchMenu = false },
            ) {
                DropdownMenuGroup(shapes = MenuDefaults.groupShape(0, 1)) {
                    if (buttonState.showWatchButton) {
                        DropdownMenuItem(
                            text = { Text(mokoString(buttonState.watchButtonLabel)) },
                            onClick = {
                                buttonState.watchButtonUrl?.let { onWatchClicked(it, buttonState.mediaProvider) }
                                showWatchMenu = false
                            },
                            leadingIcon = {
                                if (serviceIconRes is ImageResource) {
                                    Image(
                                        painter = painterResource(serviceIconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                } else if (serviceIconRes is ImageVector) {
                                    Icon(serviceIconRes, null)
                                }
                            },
                        )
                    }
                    if (buttonState.showWatchTrailerOption) {
                        DropdownMenuItem(
                            text = { Text(mokoString(MR.strings.watch_trailer)) },
                            onClick = {
                                buttonState.trailerUrl?.let(onWatchTrailerClicked)
                                showWatchMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, null) },
                        )
                    }
                }
            }
        }
    }

    // Approval Menu
    if (buttonState.showViewRequestButton) {
        var showApprovalMenu by remember { mutableStateOf(false) }
        Box {
            IconButton(
                onClick = {
                    if (!buttonState.showApproveRequestButton && !buttonState.showDeclineRequestButton) {
                        buttonState.pendingRequestId?.let(onViewRequestClicked)
                    } else {
                        showApprovalMenu = true
                    }
                },
                colors = IconButtonDefaults.headerBarColors(),
            ) {
                Icon(Icons.Default.Approval, mokoString(MR.strings.view_request))
            }

            DropdownMenuPopup(
                expanded = showApprovalMenu,
                onDismissRequest = { showApprovalMenu = false },
            ) {
                DropdownMenuGroup(shapes = MenuDefaults.groupShape(0, 1)) {
                    DropdownMenuItem(
                        text = { Text(mokoString(MR.strings.view_request)) },
                        onClick = {
                            buttonState.pendingRequestId?.let(onViewRequestClicked)
                            showApprovalMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Visibility, null) },
                    )
                    if (buttonState.showApproveRequestButton) {
                        DropdownMenuItem(
                            text = { Text(mokoString(MR.strings.approve_request)) },
                            onClick = {
                                buttonState.pendingRequestId?.let(onApproveRequestClicked)
                                showApprovalMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Check, null) },
                        )
                    }
                    if (buttonState.showDeclineRequestButton) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = mokoString(MR.strings.decline_request),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                buttonState.pendingRequestId?.let(onDeclineRequestClicked)
                                showApprovalMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    // Add / Request Menu
    val showAddMenu =
        canAddDirectly || buttonState.showRequestButton || buttonState.showRequest4kButton || buttonState.showRequestMoreButton
    if (showAddMenu) {
        var showAddMenuState by remember { mutableStateOf(false) }
        Box {
            IconButton(
                onClick = {
                    if (canAddDirectly && !buttonState.showRequestButton && !buttonState.showRequest4kButton) {
                        onAddDirectlyClicked()
                    } else {
                        showAddMenuState = true
                    }
                },
                colors = IconButtonDefaults.headerBarColors(),
            ) {
                Icon(Icons.Default.Add, mokoString(MR.strings.add))
            }

            DropdownMenuPopup(
                expanded = showAddMenuState,
                onDismissRequest = { showAddMenuState = false },
            ) {
                DropdownMenuGroup(shapes = MenuDefaults.groupShape(0, 1)) {
                    if (canAddDirectly) {
                        DropdownMenuItem(
                            text = { Text(mokoString(MR.strings.add)) },
                            onClick = {
                                onAddDirectlyClicked()
                                showAddMenuState = false
                            },
                            leadingIcon = { Icon(Icons.Default.Add, null) },
                        )
                    }
                    if (buttonState.showRequestButton || buttonState.showRequestMoreButton) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    mokoString(if (buttonState.showRequestMoreButton) MR.strings.request_more else MR.strings.request),
                                )
                            },
                            onClick = {
                                onRequestClicked()
                                showAddMenuState = false
                            },
                            leadingIcon = { Icon(Icons.Default.FileDownload, null) },
                        )
                    }
                    if (buttonState.showRequest4kButton) {
                        DropdownMenuItem(
                            text = { Text(mokoString(MR.strings.request_in_4k)) },
                            onClick = {
                                onRequest4kClicked()
                                showAddMenuState = false
                            },
                            leadingIcon = { Icon(Icons.Default.HighQuality, null) },
                        )
                    }
                }
            }
        }
    }
}
