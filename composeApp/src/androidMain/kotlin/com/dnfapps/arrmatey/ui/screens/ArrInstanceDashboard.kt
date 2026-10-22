package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.state.ArrDashboardState
import com.dnfapps.arrmatey.arr.viewmodel.ArrInstanceDashboardViewModel
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.model.InfoItem
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrHealthCard
import com.dnfapps.arrmatey.ui.components.DiskSpaceSection
import com.dnfapps.arrmatey.ui.components.ErrorView
import com.dnfapps.arrmatey.ui.components.InfoArea
import com.dnfapps.arrmatey.ui.components.InstanceOptionsMenu
import com.dnfapps.arrmatey.ui.components.UpdateCard
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.sheets.DirectoryManualImportSheet
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArrInstanceDashboard(
    id: Long,
    windowSizeClass: WindowSizeClass,
    viewModel: ArrInstanceDashboardViewModel = koinViewModel(key = id.toString(), parameters = { parametersOf(id) }),
    moko: MokoStrings = koinInject(),
    onBack: () -> Unit = {},
    onNavigateToEditInstance: (Long) -> Unit = {},
) {
    val context = LocalContext.current
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val instance by viewModel.instance.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val lastCommandResult by viewModel.lastCommandResult.collectAsStateWithLifecycle()

    var confirmDelete by remember { mutableStateOf(false) }
    var showManualImportSheet by remember { mutableStateOf(false) }

    val commandQueuedMessage = mokoString(MR.strings.command_queued)
    val commandErrorMessage = mokoString(MR.strings.error_generic_title)

    LaunchedEffect(lastCommandResult) {
        when (lastCommandResult) {
            true -> Toast.makeText(context, commandQueuedMessage, Toast.LENGTH_SHORT).show()
            false -> Toast.makeText(context, commandErrorMessage, Toast.LENGTH_SHORT).show()
            null -> {}
        }
    }

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
                    instance?.let { instance ->
                        Text(instance.label)
                    }
                },
                navigationIcon = {
                    BackButton(onClick = onBack)
                },
                actions = {
                    IconButton(
                        onClick = {
                            onNavigateToEditInstance(id)
                        },
                    ) {
                        Icon(Icons.Default.Edit, null)
                    }

                    InstanceOptionsMenu(
                        onViewWebGui = {
                            instance?.url?.let { context.openLink(it) }
                        },
                        onRunRssSync = { viewModel.runRssSync() },
                        onSearchAllMissing = { viewModel.searchAllMissing() },
                        onUpdateLibrary = { viewModel.updateLibrary() },
                        onBackupDatabase = { viewModel.backupDatabase() },
                        onManualImport = if (instance?.supportsManualImport == true) {
                            { showManualImportSheet = true }
                        } else {
                            null
                        },
                        onDelete = { confirmDelete = true },
                    )
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
                            viewModel.deleteInstance()
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
                    Text(mokoString(MR.strings.confirm_delete_instance, instance?.label ?: ""))
                },
            )
        }

        if (showManualImportSheet && instance != null) {
            DirectoryManualImportSheet(
                instances = listOf(instance!!),
                initialInstanceId = instance!!.id,
                onDismiss = { showManualImportSheet = false },
                onSuccess = { viewModel.refresh() },
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
                is ArrDashboardState.Initial -> {}
                is ArrDashboardState.Loading -> {
                    LoadingIndicator()
                }
                is ArrDashboardState.Error -> {
                    ErrorView(
                        errorType = state.type,
                        message = state.message ?: "",
                        onRetry = { viewModel.refresh() },
                        onOpenSettings = {
                            instance?.let {
                                onNavigateToEditInstance(it.id)
                            }
                        },
                    )
                }
                is ArrDashboardState.Success -> {
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
                        state.updateInfo?.let { update ->
                            if (update.isAvailable) {
                                UpdateCard(update)
                            }
                        }

                        if (state.softwareStatus != null || state.prowlarrOverview != null || state.healthItems.isNotEmpty()) {
                            Text(
                                text = mokoString(MR.strings.health),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            state.healthItems.forEach {
                                ArrHealthCard(it)
                            }
                            if (state.healthItems.isEmpty()) {
                                Text(
                                    text = mokoString(MR.strings.no_issues),
                                    modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        if (state.disks.isNotEmpty()) {
                            Text(
                                text = mokoString(MR.strings.disk_space),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            DiskSpaceSection(state.disks)
                        }

                        state.prowlarrOverview?.let { prowlarr ->
                            val indexerItems = listOf(
                                InfoItem(moko.getString(MR.strings.total), prowlarr.totalIndexers.toString()),
                                InfoItem(moko.getString(MR.strings.healthy_indexers), prowlarr.healthyIndexers.toString()),
                                InfoItem(moko.getString(MR.strings.failing_indexers), prowlarr.failingIndexers.toString()),
                            )
                            InfoArea(indexerItems, title = MR.strings.indexers)
                        }

                        state.bazarrOverview?.let { bazarr ->
                            val subtitleItems = listOf(
                                InfoItem(moko.getString(MR.strings.wanted_episodes), bazarr.wantedEpisodes.toString()),
                                InfoItem(moko.getString(MR.strings.wanted_movies), bazarr.wantedMovies.toString()),
                                InfoItem(moko.getString(MR.strings.provider_issues), bazarr.providerIssues.toString()),
                            )
                            InfoArea(subtitleItems, title = MR.strings.wanted_subtitles)

                            val bazarrSystemItems = buildList {
                                add(InfoItem(moko.getString(MR.strings.host_endpoint), instance?.url ?: ""))
                                bazarr.version?.let { add(InfoItem(moko.getString(MR.strings.version), it)) }
                                bazarr.packageVersion?.let { add(InfoItem(moko.getString(MR.strings.package_version), it)) }
                                bazarr.operatingSystem?.let { add(InfoItem(moko.getString(MR.strings.operating_system), it)) }
                                bazarr.pythonVersion?.let { add(InfoItem(moko.getString(MR.strings.python_version), it)) }
                                bazarr.cpuCores?.let { add(InfoItem(moko.getString(MR.strings.cpu_cores), it.toString())) }
                                bazarr.timezone?.let { add(InfoItem(moko.getString(MR.strings.timezone), it)) }
                                bazarr.bazarrDirectory?.let { add(InfoItem(moko.getString(MR.strings.startup_path), it)) }
                                bazarr.configDirectory?.let { add(InfoItem(moko.getString(MR.strings.app_data_path), it)) }
                            }
                            InfoArea(bazarrSystemItems, title = MR.strings.system_info)
                        }

                        state.tautulliOverview?.let { tautulli ->
                            val streamItems = buildList {
                                add(InfoItem(moko.getString(MR.strings.active_streams), tautulli.streamCount.toString()))
                                tautulli.totalBandwidth?.let { add(InfoItem(moko.getString(MR.strings.total_bandwidth), "${it / 1000} Mbps")) }
                                tautulli.lanBandwidth?.let { add(InfoItem(moko.getString(MR.strings.lan_bandwidth), "${it / 1000} Mbps")) }
                                tautulli.wanBandwidth?.let { add(InfoItem(moko.getString(MR.strings.wan_bandwidth), "${it / 1000} Mbps")) }
                            }
                            InfoArea(streamItems, title = MR.strings.active_streams)

                            val tautulliSystemItems = buildList {
                                add(InfoItem(moko.getString(MR.strings.host_endpoint), instance?.url ?: ""))
                                tautulli.tautulliVersion?.let { add(InfoItem(moko.getString(MR.strings.version), it)) }
                                tautulli.installType?.let { add(InfoItem(moko.getString(MR.strings.install_type), it)) }
                                tautulli.pmsName?.let { add(InfoItem(moko.getString(MR.strings.plex_server), it)) }
                                tautulli.pmsVersion?.let { add(InfoItem(moko.getString(MR.strings.plex_server_version), it)) }
                                add(InfoItem(moko.getString(MR.strings.update_available), if (tautulli.updateAvailable) moko.getString(MR.strings.yes) else moko.getString(MR.strings.no)))
                            }
                            InfoArea(tautulliSystemItems, title = MR.strings.system_info)
                        }

                        state.tracearrOverview?.let { tracearr ->
                            val overviewItems = listOf(
                                InfoItem(moko.getString(MR.strings.connected_servers), tracearr.servers.size.toString()),
                                InfoItem(moko.getString(MR.strings.active_streams), tracearr.activeStreams.size.toString()),
                            )
                            InfoArea(overviewItems, title = MR.strings.dashboard_tracearr_overview)

                            tracearr.todayStats?.let { stats ->
                                val todayItems = listOf(
                                    InfoItem(moko.getString(MR.strings.today_plays), stats.todayPlays.toString()),
                                    InfoItem(moko.getString(MR.strings.watch_time), stats.formattedWatchTime),
                                    InfoItem(moko.getString(MR.strings.active_users), stats.activeUsersToday.toString()),
                                )
                                InfoArea(todayItems, title = MR.strings.today_stats)
                            }

                            val tracearrSystemItems = buildList {
                                add(InfoItem(moko.getString(MR.strings.host_endpoint), instance?.url ?: ""))
                                tracearr.version?.let { add(InfoItem(moko.getString(MR.strings.version), it)) }
                                tracearr.commitTag?.let { add(InfoItem(moko.getString(MR.strings.commit_tag), it)) }
                                add(InfoItem(moko.getString(MR.strings.update_available), if (tracearr.updateAvailable) moko.getString(MR.strings.yes) else moko.getString(MR.strings.no)))
                            }
                            InfoArea(tracearrSystemItems, title = MR.strings.system_info)
                        }

                        state.seerrOverview?.let { seerr ->
                            val requestItems = listOf(
                                InfoItem(moko.getString(MR.strings.total), seerr.totalRequests.toString()),
                                InfoItem(moko.getString(MR.strings.pending_requests), seerr.pendingRequests.toString()),
                                InfoItem(moko.getString(MR.strings.approved_requests), seerr.approvedRequests.toString()),
                                InfoItem(moko.getString(MR.strings.processing), seerr.processingRequests.toString()),
                                InfoItem(moko.getString(MR.strings.available), seerr.availableRequests.toString()),
                            )
                            InfoArea(requestItems, title = MR.strings.requests_label)

                            val issueItems = listOf(
                                InfoItem(moko.getString(MR.strings.total), seerr.totalIssues.toString()),
                                InfoItem(moko.getString(MR.strings.open_issues), seerr.openIssues.toString()),
                            )
                            InfoArea(issueItems, title = MR.strings.issues)

                            val seerrSystemItems = buildList {
                                add(InfoItem(moko.getString(MR.strings.host_endpoint), instance?.url ?: ""))
                                seerr.version?.let { add(InfoItem(moko.getString(MR.strings.version), it)) }
                                seerr.commitTag?.let { add(InfoItem(moko.getString(MR.strings.commit_tag), it)) }
                                add(InfoItem(moko.getString(MR.strings.update_available), if (seerr.updateAvailable) moko.getString(MR.strings.yes) else moko.getString(MR.strings.no)))
                            }
                            InfoArea(seerrSystemItems, title = MR.strings.system_info)
                        }

                        state.softwareStatus?.let { ss ->
                            val infoItems =
                                buildList {
                                    add(InfoItem(moko.getString(MR.strings.host_endpoint), instance?.url ?: ""))
                                    add(
                                        InfoItem(
                                            moko.getString(MR.strings.version),
                                            ss.version ?: moko.getString(MR.strings.unknown),
                                        ),
                                    )
                                    add(
                                        InfoItem(
                                            moko.getString(MR.strings.startup_path),
                                            ss.startupPath ?: moko.getString(MR.strings.unknown),
                                        ),
                                    )
                                    add(
                                        InfoItem(
                                            moko.getString(MR.strings.app_data_path),
                                            ss.appData ?: moko.getString(MR.strings.unknown),
                                        ),
                                    )
                                    add(
                                        InfoItem(
                                            moko.getString(MR.strings.host_platform),
                                            moko.getString(ss.hostPlatform),
                                        ),
                                    )
                                    add(
                                        InfoItem(
                                            moko.getString(MR.strings.host_os),
                                            ss.hostOs ?: moko.getString(MR.strings.unknown),
                                        ),
                                    )
                                    state.updateInfo?.let { update ->
                                        add(
                                            InfoItem(
                                                moko.getString(MR.strings.update_available),
                                                if (update.isAvailable) moko.getString(MR.strings.yes) else moko.getString(MR.strings.no),
                                            ),
                                        )
                                    }
                                }
                            InfoArea(infoItems, title = MR.strings.system_info)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
