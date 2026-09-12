package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaType
import com.dnfapps.arrmatey.tracearr.state.TracearrHistoryState
import com.dnfapps.arrmatey.tracearr.viewmodel.TracearrHistoryViewModel
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrHistoryCard
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrHistoryTable
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrStreamCard
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrStreamDetailsSheet
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TracearrHistoryScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToDetails: (type: TracearrMediaType?, tmdbId: Long?) -> Unit = { _, _ -> },
    onNavigateToUser: (ref: String) -> Unit = { _ -> },
    isLargeScreen: Boolean = false,
    viewModel: TracearrHistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val selectedSession by viewModel.selectedSession.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = mokoString(MR.strings.sessions),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
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
            onRefresh = { viewModel.refresh() },
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
        ) {
            when (val currentState = state) {
                is TracearrHistoryState.Initial,
                is TracearrHistoryState.Loading,
                -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(96.dp))
                    }
                }
                is TracearrHistoryState.NoInstance -> {
                    NoInstanceView(
                        type = InstanceType.Tracearr,
                        modifier = Modifier.fillMaxSize().wrapContentSize(),
                    )
                }
                is TracearrHistoryState.Error -> {
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
                is TracearrHistoryState.Success -> {
                    val listState = rememberLazyListState()

                    LaunchedEffect(listState) {
                        snapshotFlow {
                            listState.layoutInfo.visibleItemsInfo
                                .lastOrNull()
                                ?.index
                        }.collect { lastVisibleIndex ->
                            if (lastVisibleIndex != null &&
                                lastVisibleIndex >= currentState.items.size - 3 &&
                                currentState.hasMore &&
                                !currentState.isLoadingMore
                            ) {
                                viewModel.loadMore()
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
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
                        if (currentState.activeStreams.isNotEmpty()) {
                            item(key = "active_header") {
                                Text(
                                    text = mokoString(MR.strings.now_playing),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            items(
                                items = currentState.activeStreams,
                                key = { "active_${it.id}" },
                            ) { session ->
                                TracearrStreamCard(
                                    session = session,
                                    onClick = { viewModel.setSelectedStreamSession(session) },
                                )
                            }
                        }

                        item(key = "history_header") {
                            Text(
                                text = mokoString(MR.strings.history),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        if (currentState.items.isEmpty()) {
                            item(key = "empty_history") {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = mokoString(MR.strings.no_history),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } else if (isLargeScreen) {
                            item(key = "history_table") {
                                TracearrHistoryTable(
                                    items = currentState.items,
                                    onClickItem = { viewModel.setSelectedHistoryStream(it) },
                                )
                            }
                        } else {
                            items(
                                items = currentState.items,
                                key = { "history_${it.id}" },
                            ) { historyItem ->
                                TracearrHistoryCard(
                                    item = historyItem,
                                    onClick = { viewModel.setSelectedHistoryStream(historyItem) },
                                )
                            }
                        }

                        if (currentState.isLoadingMore) {
                            item(key = "loading_more") {
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
