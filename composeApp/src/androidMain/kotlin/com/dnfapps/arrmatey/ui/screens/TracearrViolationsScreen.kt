package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.androidModule
import com.dnfapps.arrmatey.di.appModules
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUser
import com.dnfapps.arrmatey.tracearr.api.model.TracearrViolation
import com.dnfapps.arrmatey.tracearr.api.model.TracearrViolationRule
import com.dnfapps.arrmatey.tracearr.api.model.ViolationSeverity
import com.dnfapps.arrmatey.tracearr.state.TracearrViolationsState
import com.dnfapps.arrmatey.tracearr.viewmodel.TracearrViolationsViewModel
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrViolationCard
import com.dnfapps.arrmatey.ui.theme.ArrMateyTheme
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.android.ext.koin.androidContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TracearrViolationsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TracearrViolationsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState.text) {
        viewModel.updateSearchQuery(textFieldState.text.toString())
    }

    TracearrViolationsContent(
        state = state,
        isRefreshing = isRefreshing,
        textFieldState = textFieldState,
        onRefresh = { viewModel.refresh() },
        onLoadMore = { viewModel.loadMore() },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TracearrViolationsContent(
    state: TracearrViolationsState,
    isRefreshing: Boolean,
    textFieldState: TextFieldState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ArrAppBarWithSearch(
                textFieldState = textFieldState,
                searchPlaceholder = mokoString(MR.strings.search),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = mokoString(MR.strings.back),
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
        ) {
            when (state) {
                is TracearrViolationsState.Initial,
                is TracearrViolationsState.Loading,
                -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(96.dp))
                    }
                }
                is TracearrViolationsState.NoInstance -> {
                    NoInstanceView(
                        type = InstanceType.Tracearr,
                        modifier = Modifier.fillMaxSize().wrapContentSize(),
                    )
                }
                is TracearrViolationsState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Button(onClick = onRefresh) {
                                Text(mokoString(MR.strings.retry))
                            }
                        }
                    }
                }
                is TracearrViolationsState.Success -> {
                    val listState = rememberLazyListState()

                    LaunchedEffect(listState) {
                        snapshotFlow {
                            listState.layoutInfo.visibleItemsInfo
                                .lastOrNull()
                                ?.index
                        }.collect { lastVisibleIndex ->
                            val violationCount = state.violations.size
                            if (lastVisibleIndex != null &&
                                violationCount > 0 &&
                                lastVisibleIndex >= violationCount - 2 &&
                                state.hasMore &&
                                !state.isLoadingMore
                            ) {
                                onLoadMore()
                            }
                        }
                    }

                    if (state.filteredViolations.isEmpty()) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = if (state.searchQuery.isBlank()) Icons.Default.Shield else Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                            Text(
                                text =
                                    if (state.searchQuery.isBlank()) {
                                        mokoString(MR.strings.no_violations_found)
                                    } else {
                                        mokoString(MR.strings.no_results_found)
                                    },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp),
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding =
                                PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp,
                                    bottom = 16.dp + if (LocalFloatingBarBottomPadding.current > 0.dp) LocalFloatingBarBottomPadding.current else navigationBarBottomInset(),
                                ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(
                                items = state.filteredViolations,
                                key = { "violation_${it.id}" },
                            ) { violation ->
                                TracearrViolationCard(violation = violation)
                            }

                            if (state.isLoadingMore) {
                                item {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        LoadingIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
fun TracearrViolationsScreenPreview() {
    val mockViolations =
        listOf(
            TracearrViolation(
                id = "1",
                serverId = "server-1",
                serverName = "Main Plex Server",
                severity = ViolationSeverity.High,
                acknowledged = false,
                createdAt = "2026-09-09T10:00:00.000Z",
                rule =
                    TracearrViolationRule(
                        id = "rule-1",
                        type = "stream_limit",
                        name = "Max 2 concurrent streams exceeded",
                    ),
                user =
                    TracearrUser(
                        id = "user-1",
                        username = "john_doe",
                    ),
            ),
            TracearrViolation(
                id = "2",
                serverId = "server-1",
                serverName = "Main Plex Server",
                severity = ViolationSeverity.Warning,
                acknowledged = true,
                createdAt = "2026-09-08T18:30:00.000Z",
                rule =
                    TracearrViolationRule(
                        id = "rule-2",
                        type = "location_mismatch",
                        name = "New login from unknown location",
                    ),
                user =
                    TracearrUser(
                        id = "user-2",
                        username = "jane_smith",
                    ),
            ),
            TracearrViolation(
                id = "3",
                serverId = "server-2",
                serverName = "Jellyfin Server",
                severity = ViolationSeverity.Low,
                acknowledged = false,
                createdAt = "2026-09-07T14:15:00.000Z",
                rule =
                    TracearrViolationRule(
                        id = "rule-3",
                        type = "bandwidth_limit",
                        name = "Bandwidth limit threshold reached",
                    ),
                user =
                    TracearrUser(
                        id = "user-3",
                        username = "alex_v",
                    ),
            ),
        )

    val context = LocalContext.current
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            androidContext(context)
            modules(appModules() + listOf(androidModule))
        }
    }

    ArrMateyTheme {
        TracearrViolationsContent(
            state =
                TracearrViolationsState.Success(
                    violations = mockViolations,
                    filteredViolations = mockViolations,
                    total = mockViolations.size,
                ),
            isRefreshing = false,
            textFieldState = rememberTextFieldState(),
            onRefresh = {},
            onLoadMore = {},
            onNavigateBack = {},
        )
    }
}
