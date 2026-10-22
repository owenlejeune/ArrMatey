package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientDashboardUiState
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadClientDashboardViewModel
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.model.InfoItem
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DiskSpaceSection
import com.dnfapps.arrmatey.ui.components.ErrorView
import com.dnfapps.arrmatey.ui.components.InfoArea
import com.dnfapps.arrmatey.ui.components.UpdateCard
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DownloadClientDashboard(
    id: Long,
    windowSizeClass: WindowSizeClass,
    viewModel: DownloadClientDashboardViewModel = koinViewModel(key = id.toString(), parameters = { parametersOf(id) }),
    moko: MokoStrings = koinInject(),
    onBack: () -> Unit = {},
    onNavigateToEditDownloadClient: (Long) -> Unit = {},
) {
    val context = LocalContext.current
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val client by viewModel.client.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    var confirmDelete by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val groupInteractionSource = remember { MutableInteractionSource() }

    Scaffold(
        modifier =
        if (isCompact) {
            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        } else {
            Modifier
        },
        topBar = {
            TopAppBar(
                title = {
                    client?.let { client ->
                        Text(client.label)
                    }
                },
                navigationIcon = {
                    BackButton(onClick = onBack)
                },
                actions = {
                    IconButton(
                        onClick = {
                            onNavigateToEditDownloadClient(id)
                        },
                    ) {
                        Icon(Icons.Default.Edit, null)
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }

                        DropdownMenuPopup(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuGroup(
                                shapes = MenuDefaults.groupShape(0, 1),
                                interactionSource = groupInteractionSource,
                            ) {
                                DropdownMenuItem(
                                    shapes = MenuDefaults.itemShape(0, 2),
                                    selected = false,
                                    interactionSource = groupInteractionSource,
                                    text = { Text(mokoString(MR.strings.view_web_gui)) },
                                    leadingIcon = { Icon(Icons.Default.Language, null) },
                                    onClick = {
                                        menuExpanded = false
                                        client?.url?.let { context.openLink(it) }
                                    },
                                )
                                DropdownMenuItem(
                                    shapes = MenuDefaults.itemShape(1, 2),
                                    selected = false,
                                    interactionSource = groupInteractionSource,
                                    text = { Text(mokoString(MR.strings.delete)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    },
                                    colors = MenuDefaults.itemColors(textColor = MaterialTheme.colorScheme.error),
                                    onClick = {
                                        menuExpanded = false
                                        confirmDelete = true
                                    },
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { contentPadding ->
        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteClient()
                            confirmDelete = false
                            onBack()
                        },
                    ) { Text(mokoString(MR.strings.yes)) }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            confirmDelete = false
                        },
                    ) { Text(mokoString(MR.strings.no)) }
                },
                title = { Text(mokoString(MR.strings.confirm)) },
                text = {
                    Text(mokoString(MR.strings.confirm_delete_download_client, client?.label ?: ""))
                },
            )
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier =
            Modifier
                .padding(top = contentPadding.calculateTopPadding())
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when (val state = state) {
                is DownloadClientDashboardUiState.Initial -> {}
                is DownloadClientDashboardUiState.Loading -> {
                    LoadingIndicator()
                }
                is DownloadClientDashboardUiState.Error -> {
                    ErrorView(
                        errorType = state.type,
                        message = state.message ?: "",
                        onRetry = { viewModel.refresh() },
                        onOpenSettings = {
                            client?.let {
                                onNavigateToEditDownloadClient(it.id)
                            }
                        },
                    )
                }
                is DownloadClientDashboardUiState.Success -> {
                    val overview = state.overview
                    Column(
                        modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(
                                bottom =
                                (
                                    if (LocalFloatingBarBottomPadding.current >
                                        0.dp
                                    ) {
                                        LocalFloatingBarBottomPadding.current
                                    } else {
                                        navigationBarBottomInset()
                                    }
                                    ) +
                                    12.dp,
                            ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        overview.updateInfo?.let { update ->
                            if (update.isAvailable) {
                                UpdateCard(update)
                            }
                        }

                        // Activity & Transfer Speeds
                        val activityItems = buildList {
                            add(InfoItem(moko.getString(MR.strings.active_downloads), overview.activeDownloadsCount.toString()))
                            overview.transferInfo?.let { transfer ->
                                add(InfoItem(moko.getString(MR.strings.download_speed), "${transfer.downloadSpeed.bytesAsFileSizeString()}/s"))
                                add(InfoItem(moko.getString(MR.strings.upload_speed), "${transfer.uploadSpeed.bytesAsFileSizeString()}/s"))
                            }
                        }
                        InfoArea(activityItems, title = MR.strings.activity)

                        // Disk Spaces
                        if (overview.diskSpaces.isNotEmpty()) {
                            Text(
                                text = mokoString(MR.strings.disk_space),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            DiskSpaceSection(overview.diskSpaces)
                        }

                        // System Information
                        val systemItems = buildList {
                            add(InfoItem(moko.getString(MR.strings.host_endpoint), client?.url ?: ""))
                            overview.version?.let {
                                add(InfoItem(moko.getString(MR.strings.version), it))
                            }
                            overview.webApiVersion?.let {
                                add(InfoItem(moko.getString(MR.strings.web_api_version), it))
                            }
                            overview.downloadDir?.let {
                                add(InfoItem(moko.getString(MR.strings.download_directory), it))
                            }
                            overview.connectionStatus?.let {
                                add(InfoItem(moko.getString(MR.strings.connection_status), it))
                            }
                            overview.listenPort?.let {
                                add(InfoItem(moko.getString(MR.strings.listen_port), it.toString()))
                            }
                            overview.dhtNodes?.let {
                                add(InfoItem(moko.getString(MR.strings.dht_nodes), it.toString()))
                            }
                            overview.totalPeers?.let {
                                add(InfoItem(moko.getString(MR.strings.active_peers), it.toString()))
                            }
                            overview.alltimeDownloaded?.let {
                                add(InfoItem(moko.getString(MR.strings.alltime_downloaded), it.bytesAsFileSizeString()))
                            }
                            overview.alltimeUploaded?.let {
                                add(InfoItem(moko.getString(MR.strings.alltime_uploaded), it.bytesAsFileSizeString()))
                            }
                            overview.extraInfo.forEach { (label, value) ->
                                add(InfoItem(label, value))
                            }
                            overview.updateInfo?.let { update ->
                                add(
                                    InfoItem(
                                        moko.getString(MR.strings.update_available),
                                        if (update.isAvailable) moko.getString(MR.strings.yes) else moko.getString(MR.strings.no),
                                    ),
                                )
                            }
                        }
                        InfoArea(systemItems, title = MR.strings.system_info)

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
