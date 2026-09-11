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
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaType
import com.dnfapps.arrmatey.tracearr.state.TracearrState
import com.dnfapps.arrmatey.tracearr.viewmodel.TracearrViewModel
import com.dnfapps.arrmatey.ui.components.InstancePicker
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrDashboardStatsSection
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrHistoryCard
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrHistoryTable
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrStreamCard
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrStreamDetailsSheet
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TracearrHomeScreen(
    wideRailIsVisible: Boolean,
    onNavigateToDetails: (type: TracearrMediaType?, tmdbId: Long?) -> Unit,
    onNavigateToUser: (ref: String) -> Unit,
    onNavigateToAllUsers: () -> Unit,
    onNavigateToViolations: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToActivity: () -> Unit,
    isLargeScreen: Boolean = false,
    viewModel: TracearrViewModel = koinViewModel(),
    instancesViewModel: InstancesViewModel =
        koinViewModel(
            key = InstanceType.Tracearr.name,
            parameters = { parametersOf(InstanceType.Tracearr) },
        ),
    globalPreferencesStore: PreferencesStore = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val instancesState by instancesViewModel.instancesState.collectAsStateWithLifecycle()
    val hideInstancePicker by globalPreferencesStore.hideInstanceSwitcher.collectAsStateWithLifecycle(false)

    val selectedSession by viewModel.selectedSession.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = mokoString(MR.strings.tracearr),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                },
                actions = {
                    if (!hideInstancePicker || instancesState.instances.size > 1) {
                        InstancePicker(
                            type = InstanceType.Tracearr,
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
                is TracearrState.Initial,
                is TracearrState.Loading,
                -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(96.dp))
                    }
                }
                is TracearrState.NoInstance -> {
                    NoInstanceView(
                        type = InstanceType.Tracearr,
                        modifier = Modifier.fillMaxSize().wrapContentSize(),
                    )
                }
                is TracearrState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = currentState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                is TracearrState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding =
                            PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 16.dp + navigationBarBottomInset(),
                            ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        currentState.stats?.let { stats ->
                            item {
                                TracearrDashboardStatsSection(
                                    stats = stats,
                                    isExpanded = isLargeScreen,
                                    onNavigateToHistory = onNavigateToHistory,
                                    onNavigateToAllUsers = onNavigateToAllUsers,
                                    onNavigateToViolations = onNavigateToViolations,
                                    onNavigateToActivity = onNavigateToActivity,
                                )
                            }
                        }

                        item {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = if (currentState.stats != null) 12.dp else 0.dp),
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
                                key = { it.id },
                            ) { session ->
                                TracearrStreamCard(
                                    session = session,
                                    onClick = { viewModel.setSelectedStream(session) },
                                )
                            }
                        }

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

                        val historyItems = currentState.history
                        if (historyItems.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = mokoString(MR.strings.no_history),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } else if (isLargeScreen) {
                            item {
                                TracearrHistoryTable(
                                    items = historyItems,
                                    onClickItem = { viewModel.setSelectedHistoryStream(it) },
                                )
                            }
                        } else {
                            items(
                                items = historyItems,
                                key = { "history_${it.id}" },
                            ) { historyItem ->
                                TracearrHistoryCard(
                                    item = historyItem,
                                    onClick = { viewModel.setSelectedHistoryStream(historyItem) },
                                )
                            }
                        }
                    }

                    selectedSession?.let { session ->
                        TracearrStreamDetailsSheet(
                            session = session,
                            onDismissRequest = { viewModel.clearSelected() },
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
