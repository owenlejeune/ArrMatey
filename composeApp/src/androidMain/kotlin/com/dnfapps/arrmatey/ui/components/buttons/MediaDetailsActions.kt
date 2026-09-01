package com.dnfapps.arrmatey.ui.components.buttons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.seerr.state.MediaButtonState
import com.dnfapps.arrmatey.seerr.state.MediaProvider
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
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
    modifier: Modifier = Modifier,
) {
    if (
        buttonState.showWatchButton ||
        buttonState.showWatchTrailerOption ||
        buttonState.showViewRequestButton ||
        buttonState.showRequestMoreButton ||
        buttonState.showRequestButton ||
        buttonState.showRequest4kButton
    ) {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (buttonState.showWatchButton || buttonState.showWatchTrailerOption) {
                WatchButton(buttonState, onWatchClicked, onWatchTrailerClicked)
            }
            if (buttonState.showViewRequestButton) {
                ViewRequestButton(
                    buttonState,
                    onViewRequestClicked,
                    onApproveRequestClicked,
                    onDeclineRequestClicked,
                )
            }
            if (buttonState.showRequestMoreButton) {
                RequestButton(
                    label = mokoString(MR.strings.request_more),
                    onClick = onRequestClicked,
                )
            }
            if (buttonState.showRequestButton && buttonState.showRequest4kButton) {
                var showRequestMenu by remember { mutableStateOf(false) }
                SplitButtonLayout(
                    modifier = Modifier,
                    leadingButton = {
                        SplitButtonDefaults.LeadingButton(
                            onClick = onRequestClicked,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text(mokoString(MR.strings.request))
                            }
                        }
                    },
                    trailingButton = {
                        Box {
                            SplitButtonDefaults.TrailingButton(
                                onClick = { showRequestMenu = true },
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                    ),
                            ) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenuPopup(
                                expanded = showRequestMenu,
                                onDismissRequest = { showRequestMenu = false },
                            ) {
                                DropdownMenuGroup(
                                    shapes = MenuDefaults.groupShape(0, 1),
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
                                                contentDescription = null,
                                            )
                                        },
                                        shapes = MenuDefaults.itemShape(0, 1),
                                    )
                                }
                            }
                        }
                    },
                )
            } else if (buttonState.showRequestButton) {
                RequestButton(
                    label = mokoString(MR.strings.request),
                    onClick = onRequestClicked,
                )
            } else if (buttonState.showRequest4kButton) {
                RequestButton(
                    label = mokoString(MR.strings.request_in_4k),
                    onClick = onRequest4kClicked,
                )
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
    modifier: Modifier = Modifier,
) {
    val (serviceButtonColor, serviceIconRes) =
        when (buttonState.mediaProvider) {
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
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = serviceButtonColor,
                        ),
                ) {
                    if (serviceIconRes is ImageResource) {
                        Image(
                            painter = painterResource(serviceIconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    } else if (serviceIconRes is ImageVector) {
                        Icon(serviceIconRes, null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(mokoString(buttonState.watchButtonLabel))
                }
            },
            trailingButton = {
                Box {
                    SplitButtonDefaults.TrailingButton(
                        onClick = { showWatchMenu = true },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = serviceButtonColor,
                            ),
                    ) {
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                    DropdownMenuPopup(
                        expanded = showWatchMenu,
                        onDismissRequest = { showWatchMenu = false },
                    ) {
                        DropdownMenuGroup(
                            shapes = MenuDefaults.groupShape(0, 1),
                        ) {
                            DropdownMenuItem(
                                text = { Text(mokoString(MR.strings.watch_trailer)) },
                                onClick = {
                                    buttonState.trailerUrl?.let(onWatchTrailerClicked)
                                    showWatchMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.PlayArrow, null)
                                },
                            )
                        }
                    }
                }
            },
        )
    } else {
        val containerColor = if (buttonState.showWatchButton) serviceButtonColor else MaterialTheme.colorScheme.primary
        val iconRes = if (buttonState.showWatchButton) serviceIconRes else Icons.Default.PlayArrow

        Button(
            onClick = {
                if (buttonState.showWatchButton) {
                    buttonState.watchButtonUrl?.let { url ->
                        onWatchClicked(url, buttonState.mediaProvider)
                    }
                } else {
                    buttonState.trailerUrl?.let(onWatchTrailerClicked)
                }
            },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                ),
            modifier = modifier,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (iconRes is ImageResource) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
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
    modifier: Modifier = Modifier,
) {
    var showRequestMenu by remember { mutableStateOf(false) }

    SplitButtonLayout(
        modifier = modifier,
        leadingButton = {
            SplitButtonDefaults.LeadingButton(
                onClick = {
                    buttonState.pendingRequestId?.let(onViewRequestClicked)
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    ),
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
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                        ),
                ) {
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                DropdownMenuPopup(
                    expanded = showRequestMenu,
                    onDismissRequest = { showRequestMenu = false },
                ) {
                    DropdownMenuGroup(
                        shapes = MenuDefaults.groupShape(0, 1),
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
        },
    )
}

@Composable
private fun RequestButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(Icons.Default.Add, null)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}
