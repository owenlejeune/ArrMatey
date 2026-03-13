package com.dnfapps.arrmatey.ui.components.buttons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.isDebug
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import com.dnfapps.arrmatey.seerr.state.MediaButtonState
import com.dnfapps.arrmatey.seerr.state.MediaProvider
import com.dnfapps.arrmatey.seerr.state.toButtonState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun MediaDetailsActions(
    buttonState: MediaButtonState,
    onWatchClicked: (String, MediaProvider) -> Unit,
    onWatchTrailerClicked: (String) -> Unit,
    onViewRequestClicked: (Long) -> Unit,
    onApproveRequestClicked: (Long) -> Unit,
    onDeclineRequestClicked: (Long) -> Unit,
    onRequestClicked: () -> Unit,
    onRequest4kClicked: () -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier,
//        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (buttonState.showWatchButton || buttonState.showWatchTrailerOption) {
            WatchButton(buttonState, onWatchClicked, onWatchTrailerClicked)
        }
        if (isDebug()) {
            if (buttonState.showViewRequestButton) {
                ViewRequestButton(
                    buttonState,
                    onViewRequestClicked,
                    onApproveRequestClicked,
                    onDeclineRequestClicked
                )
            }
            if (buttonState.showRequestButton) {
                RequestButton(onRequestClicked)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WatchButton(
    buttonState: MediaButtonState,
    onWatchClicked: (String, MediaProvider) -> Unit,
    onWatchTrailerClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val (buttonColor, iconRes) = when (buttonState.mediaProvider) {
        MediaProvider.Plex -> Color(0xFFE5A00D) to MR.images.plex
        MediaProvider.Jellyfin -> Color(0xff4747ed) to MR.images.jellyfin
        MediaProvider.None -> MaterialTheme.colorScheme.primary to Icons.Default.PlayArrow
    }

    if (buttonState.showWatchButton && buttonState.showWatchTrailerOption) {
        var showWatchMenu by remember { mutableStateOf(false) }
        SplitButtonLayout(
            modifier = modifier,
            leadingButton = {
                SplitButtonDefaults.LeadingButton(
                    onClick = {
                        buttonState.watchButtonUrl?.let { url ->
                            onWatchClicked(url, buttonState.mediaProvider)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor
                    )
                ) {
                    if (iconRes is ImageResource) {
                        Image(
                            painter = painterResource(iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (iconRes is ImageVector) {
                        Icon(iconRes, null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(mokoString(buttonState.watchButtonLabel))
                }
            },
            trailingButton = {
                Box {
                    SplitButtonDefaults.TrailingButton(
                        onClick = { showWatchMenu = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor
                        )
                    ) {
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenuPopup(
                        expanded = showWatchMenu,
                        onDismissRequest = { showWatchMenu = false }
                    ) {
                        DropdownMenuGroup(
                            shapes = MenuDefaults.groupShape(0, 1)
                        ) {
                            DropdownMenuItem(
                                text = { Text(mokoString(MR.strings.watch_trailer)) },
                                onClick = {
                                    buttonState.trailerUrl?.let(onWatchTrailerClicked)
                                    showWatchMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.PlayArrow, null)
                                }
                            )
                        }
                    }
                }
            }
        )
    } else {
        Button(
            onClick = {
                if (buttonState.showWatchButton) {
                    buttonState.watchButtonUrl?.let { url ->
                        onWatchClicked(url, buttonState.mediaProvider)
                    }
                } else {
                    buttonState.trailerUrl?.let(onWatchTrailerClicked)
                }
            }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (iconRes is ImageResource) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null
                    )
                } else if (iconRes is ImageVector) {
                    Icon(iconRes, null)
                }
                Spacer(Modifier.width(8.dp))
                if (buttonState.showWatchButton) {
                    Text(mokoString(buttonState.watchButtonLabel))
                } else {
                    Text(mokoString(MR.strings.watch_trailer))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ViewRequestButton(
    buttonState: MediaButtonState,
    onViewRequestClicked: (Long) -> Unit,
    onApproveRequestClicked: (Long) -> Unit,
    onDeclineRequestClicked: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRequestMenu by remember { mutableStateOf(false) }

    SplitButtonLayout(
        modifier = modifier,
        leadingButton = {
            SplitButtonDefaults.LeadingButton(
                onClick = {
                    buttonState.pendingRequestId?.let(onViewRequestClicked)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null)
                    Spacer(Modifier.width(8.dp))
                    Text(mokoString(MR.strings.view_request))
                }
            }
        },
        trailingButton = {
            Box {
                SplitButtonDefaults.TrailingButton(
                    onClick = { showRequestMenu = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
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
                        if (buttonState.showApproveRequestButton) {
                            DropdownMenuItem(
                                selected = false,
                                text = { Text(mokoString(MR.strings.approve_request)) },
                                onClick = {
                                    buttonState.pendingRequestId?.let(onApproveRequestClicked)
                                    showRequestMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Check, null)
                                },
                                shapes = MenuDefaults.itemShape(0, ViewType.entries.size),
                            )
                        }
                        if (buttonState.showDeclineRequestButton) {
                            DropdownMenuItem(
                                selected = false,
                                text = { Text(mokoString(MR.strings.decline_request)) },
                                onClick = {
                                    buttonState.pendingRequestId?.let(onDeclineRequestClicked)
                                    showRequestMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Close, null)
                                },
                                shapes = MenuDefaults.itemShape(1, ViewType.entries.size),
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun RequestButton(
    onRequestClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onRequestClicked,
        modifier = modifier
    ) {
        Icon(Icons.Default.Add, null)
        Spacer(Modifier.width(8.dp))
        Text(mokoString(MR.strings.request))
    }
}