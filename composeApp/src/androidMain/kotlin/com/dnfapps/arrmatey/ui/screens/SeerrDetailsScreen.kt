package com.dnfapps.arrmatey.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.state.MediaDetailsUiState
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SeerrScreen
import com.dnfapps.arrmatey.seerr.api.model.MediaInfo
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import com.dnfapps.arrmatey.seerr.api.model.Video
import com.dnfapps.arrmatey.seerr.state.MediaProvider
import com.dnfapps.arrmatey.seerr.state.SeerrDetailsState
import com.dnfapps.arrmatey.seerr.state.toButtonState
import com.dnfapps.arrmatey.seerr.viewmodel.SeerrMediaDetailsViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DetailsHeader
import com.dnfapps.arrmatey.ui.components.ErrorView
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.buttons.MediaDetailsActions
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.koinInject
import androidx.core.net.toUri
import com.dnfapps.arrmatey.entensions.openLink

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SeerrDetailsScreen(
    tmdbId: Long,
    requestType: RequestType,
    viewModel: SeerrMediaDetailsViewModel = koinInjectParams(tmdbId, requestType),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<SeerrScreen> = navigationManager.requests()
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedInstance by viewModel.selectedInstance.collectAsStateWithLifecycle()
    val buttonState by viewModel.buttonState.collectAsStateWithLifecycle()
//    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var showViewRequestSheet by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues.copy(bottom = 0.dp, top = 0.dp))
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is SeerrDetailsState.Initial,
                is SeerrDetailsState.Loading -> {
                    LoadingIndicator(
                        modifier = Modifier
                            .size(96.dp)
                            .align(Alignment.Center)
                    )
                }
                is SeerrDetailsState.Error -> {
                    ErrorView(
                        errorType = state.errorType,
                        message = state.message ?: mokoString(MR.strings.unknown),
                        onOpenSettings = {
                            selectedInstance?.id?.let { id ->
                                navigationManager.openEditInstanceScreen(id)
                            }
                        },
                        onRetry = {
                            viewModel.refreshDetails()
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is SeerrDetailsState.Success -> {
                    val item = state.item
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = { viewModel.refreshDetails() }
                    ) {
                        Column(
                            modifier = Modifier.verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailsHeader(item)

                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                MediaDetailsActions(
                                    buttonState = buttonState,
                                    onWatchClicked = { url, provider -> handleWatchClick(url, provider, context) },
//                                    onReportIssueClicked = { },
//                                    onOpenInServiceClicked = { },
//                                    onClearDataClicked = { },
                                    onRequestClicked = { },
                                    onRequest4kClicked = { },
                                    onWatchTrailerClicked = { context.openLink(it) },
                                    onViewRequestClicked = { showViewRequestSheet = true },
                                    onApproveRequestClicked = { viewModel.approveRequest(it) },
                                    onDeclineRequestClicked = { viewModel.declineRequest(it) },
                                )

                                item.overview?.let { overview ->
                                    ItemDescriptionCard(overview)
                                }

                            }
                        }
                    }
                }
            }

            OverlayTopAppBar(
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.TopCenter),
                navigationIcon = {
                    IconButton(
                        onClick = { navigation.popBackStack() },
                        colors = IconButtonDefaults.headerBarColors()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(MR.strings.back)
                        )
                    }
                },
                actions = {
                    if (buttonState.showReportIssueButton) {
                        IconButton(
                            onClick = {  },
                            colors = IconButtonDefaults.headerBarColors()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Report issue"
                            )
                        }
                    }
                    if (buttonState.showManageMenu) {
                        IconButton(
                            onClick = {  },
                            colors = IconButtonDefaults.headerBarColors()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Manage"
                            )
                        }
                    }
                }
            )
        }
    }
}

fun handleWatchClick(url: String, provider: MediaProvider, context: Context) {
    when (provider) {
        MediaProvider.Plex -> {
            // Try to open Plex app first, fallback to web
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Plex app not installed, open in browser
                val webIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(webIntent)
            }
        }
        MediaProvider.Jellyfin -> {
            // Open Jellyfin app or web interface
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Jellyfin app not installed, show message or open web
                Toast.makeText(context, "Jellyfin app not installed", Toast.LENGTH_SHORT).show()
            }
        }
        MediaProvider.None -> {
            // Shouldn't happen, but handle gracefully
            Toast.makeText(context, "No media provider available", Toast.LENGTH_SHORT).show()
        }
    }
}

//@Composable
//fun MediaDetailsActions(
//    mediaDetails: RequestMediaDetails,
//    currentUser: SeerrUser?,
//    onWatchClicked: (String, MediaProvider) -> Unit,
//    onWatchTrailerClicked: (String) -> Unit,
//    onViewRequestClicked: (Long) -> Unit,
//    onApproveRequestClicked: (Long) -> Unit,
//    onDeclineRequestClicked: (Long) -> Unit,
//    onReportIssueClicked: () -> Unit,
//    onOpenInServiceClicked: (String) -> Unit,
//    onClearDataClicked: () -> Unit,
//    onRequestClicked: () -> Unit,
//    onRequest4kClicked: () -> Unit
//) {
//    val isAdmin = currentUser?.hasPermission(UserPermission.ADMIN) == true
//    val totalSeasonCount = (mediaDetails as? TvDetails)?.numberOfSeasons ?: 0
//    val buttonState = mediaDetails.mediaInfo.toButtonState(mediaDetails.relatedVideos, totalSeasonCount, currentUser?.id, isAdmin)
//
//    Column(
//        modifier = Modifier.fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        // Watch button with provider-specific icon and color
//        if (buttonState.showWatchButton || buttonState.showWatchTrailerOption) {
//            var showWatchMenu by remember { mutableStateOf(false) }
//
//            val (buttonColor, iconRes) = when (buttonState.mediaProvider) {
//                MediaProvider.Plex -> Color(0xFFE5A00D) to MR.images.plex
//                MediaProvider.Jellyfin -> Color(0xff4747ed) to MR.images.jellyfin
//                MediaProvider.None -> MaterialTheme.colorScheme.primary to Icons.Default.PlayArrow
//            }
//
//            Box {
//                Button(
//                    onClick = {
//                        if (buttonState.showWatchTrailerOption) {
//                            showWatchMenu = true
//                        } else {
//                            buttonState.watchButtonUrl?.let { url ->
//                                onWatchClicked(url, buttonState.mediaProvider)
//                            }
//                        }
//                    },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = buttonColor
//                    )
//                ) {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            if (iconRes is ImageResource) {
//                                Icon(
//                                    painter = painterResource(iconRes),
//                                    contentDescription = null
//                                )
//                            } else if (iconRes is ImageVector) {
//                                Icon(iconRes, null)
//                            }
//                            Spacer(Modifier.width(8.dp))
//                            Text(buttonState.watchButtonLabel)
//                        }
//
//                        if (buttonState.showWatchTrailerOption) {
//                            Icon(Icons.Default.ArrowDropDown, null)
//                        }
//                    }
//                }
//
//                if (buttonState.showWatchTrailerOption) {
//                    DropdownMenu(
//                        expanded = showWatchMenu,
//                        onDismissRequest = { showWatchMenu = false }
//                    ) {
//                        DropdownMenuItem(
//                            text = { Text(buttonState.watchButtonLabel) },
//                            onClick = {
//                                buttonState.watchButtonUrl?.let { url ->
//                                    onWatchClicked(url, buttonState.mediaProvider)
//                                }
//                                showWatchMenu = false
//                            },
//                            leadingIcon = {
//                                if (iconRes is ImageResource) {
//                                    Icon(
//                                        painter = painterResource(iconRes),
//                                        contentDescription = null
//                                    )
//                                } else if (iconRes is ImageVector) {
//                                    Icon(iconRes, null)
//                                }
//                            }
//                        )
//                        DropdownMenuItem(
//                            text = { Text("Watch Trailer") },
//                            onClick = {
//                                buttonState.trailerUrl?.let(onWatchTrailerClicked)
//                                showWatchMenu = false
//                            },
//                            leadingIcon = {
//                                Icon(Icons.Default.PlayArrow, null)
//                            }
//                        )
//                    }
//                }
//            }
//        }
//
//        // View Request button with approve/decline dropdown
//        if (buttonState.showViewRequestButton) {
//            var showRequestMenu by remember { mutableStateOf(false) }
//
//            Box {
//                Button(
//                    onClick = {
//                        if (buttonState.showApproveRequestButton || buttonState.showDeclineRequestButton) {
//                            showRequestMenu = true
//                        } else {
//                            buttonState.pendingRequestId?.let(onViewRequestClicked)
//                        }
//                    },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.tertiary
//                    )
//                ) {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Icon(Icons.Default.Schedule, null)
//                            Spacer(Modifier.width(8.dp))
//                            Text("View Request")
//                        }
//
//                        if (buttonState.showApproveRequestButton || buttonState.showDeclineRequestButton) {
//                            Icon(Icons.Default.ArrowDropDown, null)
//                        }
//                    }
//                }
//
//                DropdownMenu(
//                    expanded = showRequestMenu,
//                    onDismissRequest = { showRequestMenu = false }
//                ) {
//                    DropdownMenuItem(
//                        text = { Text("View Request") },
//                        onClick = {
//                            buttonState.pendingRequestId?.let(onViewRequestClicked)
//                            showRequestMenu = false
//                        },
//                        leadingIcon = {
//                            Icon(Icons.Default.Visibility, null)
//                        }
//                    )
//
//                    if (buttonState.showApproveRequestButton) {
//                        DropdownMenuItem(
//                            text = { Text("Approve Request") },
//                            onClick = {
//                                buttonState.pendingRequestId?.let(onApproveRequestClicked)
//                                showRequestMenu = false
//                            },
//                            leadingIcon = {
//                                Icon(Icons.Default.Check, null)
//                            }
//                        )
//                    }
//
//                    if (buttonState.showDeclineRequestButton) {
//                        DropdownMenuItem(
//                            text = { Text("Decline Request") },
//                            onClick = {
//                                buttonState.pendingRequestId?.let(onDeclineRequestClicked)
//                                showRequestMenu = false
//                            },
//                            leadingIcon = {
//                                Icon(Icons.Default.Close, null)
//                            }
//                        )
//                    }
//                }
//            }
//        }
//
//        // Request button
//        if (buttonState.showRequestButton) {
//            Button(
//                onClick = onRequestClicked,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Icon(Icons.Default.Add, null)
//                Spacer(Modifier.width(8.dp))
//                Text("Request")
//            }
//        }
//
//        // Report Issue & Manage Row
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            if (buttonState.showReportIssueButton) {
//                OutlinedButton(
//                    onClick = onReportIssueClicked,
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Icon(Icons.Default.Warning, null)
//                    Spacer(Modifier.width(4.dp))
//                    Text("Report Issue")
//                }
//            }
//
//            if (buttonState.showManageMenu) {
//                var showMenu by remember { mutableStateOf(false) }
//
//                Box(modifier = if (!buttonState.showReportIssueButton) Modifier.fillMaxWidth() else Modifier.weight(1f)) {
//                    OutlinedButton(
//                        onClick = { showMenu = true },
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Icon(Icons.Default.Settings, null)
//                        Spacer(Modifier.width(4.dp))
//                        Text("Manage")
//                    }
//
//                    DropdownMenu(
//                        expanded = showMenu,
//                        onDismissRequest = { showMenu = false }
//                    ) {
//                        if (buttonState.showOpenInServiceButton) {
//                            DropdownMenuItem(
//                                text = { Text("Open in ${buttonState.serviceName}") },
//                                onClick = {
//                                    buttonState.serviceUrl?.let(onOpenInServiceClicked)
//                                    showMenu = false
//                                },
//                                leadingIcon = {
//                                    Icon(Icons.Default.OpenInBrowser, null)
//                                }
//                            )
//                        }
//
//                        if (buttonState.showClearDataButton) {
//                            DropdownMenuItem(
//                                text = { Text("Clear Data") },
//                                onClick = {
//                                    onClearDataClicked()
//                                    showMenu = false
//                                },
//                                leadingIcon = {
//                                    Icon(Icons.Default.Delete, null)
//                                }
//                            )
//                        }
//
//                        if (buttonState.showRequest4kButton) {
//                            DropdownMenuItem(
//                                text = { Text("Request in 4K") },
//                                onClick = {
//                                    onRequest4kClicked()
//                                    showMenu = false
//                                },
//                                leadingIcon = {
//                                    Icon(Icons.Default.HighQuality, null)
//                                }
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}