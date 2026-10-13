package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.monitoring.model.PlaybackMediaType
import com.dnfapps.arrmatey.monitoring.state.MonitoringState
import com.dnfapps.arrmatey.monitoring.viewmodel.MonitoringViewModel
import com.dnfapps.arrmatey.navigation.navigationManager
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ErrorView
import com.dnfapps.arrmatey.ui.components.InstancePicker
import com.dnfapps.arrmatey.ui.components.NoMonitoringInstanceView
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.screens.monitoring.MonitoringStatsSection
import com.dnfapps.arrmatey.ui.screens.monitoring.PlaybackHistoryCard
import com.dnfapps.arrmatey.ui.screens.monitoring.PlaybackStreamCard
import com.dnfapps.arrmatey.ui.screens.monitoring.PlaybackStreamDetailsSheet
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MonitoringHomeScreen(
    wideRailIsVisible: Boolean,
    onNavigateToDetails: (type: PlaybackMediaType?, tmdbId: Long?) -> Unit,
    onNavigateToUser: (ref: String) -> Unit,
    onNavigateToAllUsers: () -> Unit,
    onNavigateToViolations: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToStats: () -> Unit = {},
    isLargeScreen: Boolean = false,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: MonitoringViewModel = koinViewModel(),
    instancesViewModel: InstancesViewModel =
        koinViewModel(
            key = "monitoring_instances",
            parameters = { parametersOf(listOf(InstanceType.Tracearr, InstanceType.Tautulli)) },
        ),
    globalPreferencesStore: PreferencesStore = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val instancesState by instancesViewModel.instancesState.collectAsStateWithLifecycle()
    val hideInstancePicker by globalPreferencesStore.hideInstanceSwitcher.collectAsStateWithLifecycle(false)

    val selectedSession by viewModel.selectedSession.collectAsStateWithLifecycle()
    val navManager = navigationManager

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = instancesState.selectedInstance?.label ?: mokoString(MR.strings.monitoring),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        BackButton(onClick = onNavigateBack)
                    } else if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                },
                actions = {
                    if (instancesState.instances.isNotEmpty() && (!hideInstancePicker || instancesState.instances.size > 1)) {
                        InstancePicker(
                            type = instancesState.selectedInstance?.type ?: InstanceType.Tautulli,
                            currentInstance = instancesState.selectedInstance,
                            typeInstances = instancesState.instances,
                            onInstanceSelected = { instancesViewModel.setInstanceActive(it) },
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier =
            Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            when (val currentState = state) {
                is MonitoringState.Initial,
                is MonitoringState.Loading,
                -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(96.dp))
                    }
                }
                is MonitoringState.NoInstance -> {
                    NoMonitoringInstanceView(
                        modifier = Modifier.fillMaxSize().wrapContentSize(),
                    )
                }
                is MonitoringState.Error -> {
                    ErrorView(
                        errorType = currentState.type,
                        message = currentState.message,
                        onOpenSettings = {
                            instancesState.selectedInstance?.let {
                                navManager.openEditInstanceScreen(it.id)
                            }
                        },
                        onRetry = {
                            viewModel.refresh()
                        },
                        modifier = Modifier.fillMaxSize().wrapContentSize(),
                    )
                }
                is MonitoringState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding =
                        PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom =
                            16.dp +
                                if (LocalFloatingBarBottomPadding.current >
                                    0.dp
                                ) {
                                    LocalFloatingBarBottomPadding.current
                                } else {
                                    navigationBarBottomInset()
                                },
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val hasStats = when (currentState.instance.type) {
                            InstanceType.Tracearr -> currentState.tracearrStats != null
                            InstanceType.Tautulli -> currentState.stats != null
                            else -> false
                        }

                        if (hasStats) {
                            item {
                                MonitoringStatsSection(
                                    instanceType = currentState.instance.type,
                                    summaryStats = currentState.stats,
                                    tracearrStats = currentState.tracearrStats,
                                    isExpanded = isLargeScreen,
                                    onNavigateToHistory = onNavigateToHistory,
                                    onNavigateToAllUsers = onNavigateToAllUsers,
                                    onNavigateToViolations = onNavigateToViolations,
                                    onNavigateToActivity = onNavigateToActivity,
                                    onNavigateToStats = onNavigateToStats,
                                )
                            }
                        }

                        item {
                            Row(
                                modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = if (hasStats) 12.dp else 0.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = mokoString(MR.strings.now_playing),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                val count = currentState.streams.size
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    modifier = Modifier.padding(start = 4.dp),
                                ) {
                                    Text(
                                        text = mokoPlural(MR.plurals.streams, count),
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    )
                                }
                            }
                        }

                        if (currentState.streams.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        modifier = Modifier.size(72.dp).padding(top = 24.dp),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = mokoString(MR.strings.no_active_streams),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } else {
                            items(
                                items = currentState.streams,
                                key = { it.sessionId },
                            ) { session ->
                                PlaybackStreamCard(
                                    session = session,
                                    onClick = { viewModel.setSelectedStream(session) },
                                    onTerminate = { viewModel.terminateStream(session) },
                                )
                            }
                        }

                        if (currentState.history.isNotEmpty()) {
                            item {
                                Row(
                                    modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Text(
                                            text = mokoString(MR.strings.history),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    TextButton(onClick = onNavigateToHistory) {
                                        Text(
                                            text = mokoString(MR.strings.all),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }

                            items(
                                items = currentState.history,
                                key = { "history_${it.id}" },
                            ) { historyItem ->
                                PlaybackHistoryCard(
                                    item = historyItem,
                                    onClick = { viewModel.setSelectedHistoryStream(historyItem) },
                                )
                            }
                        }
                    }

                    selectedSession?.let { session ->
                        PlaybackStreamDetailsSheet(
                            session = session,
                            onDismissRequest = { viewModel.clearSelected() },
                            onTerminateStream = { message ->
                                viewModel.terminateStream(session, message)
                            },
                            onNavigateToDetails = { type, tmdbId ->
                                viewModel.clearSelected()
                                onNavigateToDetails(type, tmdbId)
                            },
                            onNavigateToUser = { userRef ->
                                viewModel.clearSelected()
                                onNavigateToUser(userRef)
                            },
                        )
                    }
                }
            }
        }
    }
}
