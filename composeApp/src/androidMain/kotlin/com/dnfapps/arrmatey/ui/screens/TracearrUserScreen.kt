package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserAccount
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserDetail
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserStats
import com.dnfapps.arrmatey.tracearr.state.TracearrUserState
import com.dnfapps.arrmatey.tracearr.viewmodel.TracearrUserViewModel
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.screens.dashboard.CompactStatCard
import com.dnfapps.arrmatey.ui.screens.dashboard.CountStatItem
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrHistoryCard
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrHistoryTable
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrStreamDetailsSheet
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrPurple
import com.dnfapps.arrmatey.ui.theme.TracearrBlue
import com.dnfapps.arrmatey.ui.theme.getTracearrServerColor
import com.dnfapps.arrmatey.utils.formatWatchTimeMs
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TracearrUserScreen(
    userRef: String,
    onNavigateBack: () -> Unit,
    isLargeScreen: Boolean = false,
    onNavigateToDetails: (type: RequestType?, tmdbId: Long?) -> Unit = { _, _ -> },
    onNavigateToUser: (ref: String) -> Unit = {},
    viewModel: TracearrUserViewModel = koinViewModel { parametersOf(userRef) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val selectedSession by viewModel.selectedSession.collectAsStateWithLifecycle()

    val titleText =
        (state as? TracearrUserState.Success)
            ?.userDetail
            ?.username
            ?.takeIf { it.isNotBlank() } ?: mokoString(MR.strings.user_details)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titleText,
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
                is TracearrUserState.Initial,
                is TracearrUserState.Loading,
                -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(96.dp))
                    }
                }
                is TracearrUserState.NoInstance -> {
                    NoInstanceView(
                        type = InstanceType.Tracearr,
                        modifier = Modifier.fillMaxSize().wrapContentSize(),
                    )
                }
                is TracearrUserState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = currentState.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Button(onClick = { viewModel.refresh() }) {
                                Text(mokoString(MR.strings.retry))
                            }
                        }
                    }
                }
                is TracearrUserState.Success -> {
                    val listState = rememberLazyListState()

                    LaunchedEffect(listState) {
                        snapshotFlow {
                            listState.layoutInfo.visibleItemsInfo
                                .lastOrNull()
                                ?.index
                        }.collect { lastVisibleIndex ->
                            val historyCount = currentState.history.size
                            if (lastVisibleIndex != null &&
                                historyCount > 0 &&
                                lastVisibleIndex >= historyCount - 2 &&
                                currentState.hasMoreHistory &&
                                !currentState.isLoadingMoreHistory
                            ) {
                                viewModel.loadMoreHistory()
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
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        currentState.userDetail?.let { detail ->
                            item {
                                UserInfoCard(detail = detail)
                            }
                        }

                        currentState.userStats?.let { stats ->
                            item {
                                UserStatsCard(stats, isExpanded = isLargeScreen)
                            }
                        }

                        item {
                            Text(
                                text = mokoString(MR.strings.recent_sessions),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        if (currentState.history.isEmpty()) {
                            item {
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
                            item {
                                TracearrHistoryTable(
                                    items = currentState.history,
                                    onClickItem = { viewModel.setSelectedHistoryStream(it) },
                                )
                            }
                        } else {
                            items(
                                items = currentState.history,
                                key = { "user_history_${it.id}" },
                            ) { historyItem ->
                                TracearrHistoryCard(
                                    item = historyItem,
                                    onClick = { viewModel.setSelectedHistoryStream(historyItem) },
                                )
                            }
                        }

                        if (currentState.isLoadingMoreHistory) {
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

            selectedSession?.let { session ->
                TracearrStreamDetailsSheet(
                    session = session,
                    onDismissRequest = { viewModel.clearSelected() },
                    onNavigateToDetails = { type, tmdbId ->
                        viewModel.clearSelected()
                        onNavigateToDetails(type?.requestType, tmdbId)
                    },
                    onNavigateToUser = { ref ->
                        viewModel.clearSelected()
                        onNavigateToUser(ref)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserInfoCard(
    detail: TracearrUserDetail,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = mokoString(MR.strings.user_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = detail.username?.take(1)?.uppercase() ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = detail.username ?: mokoString(MR.strings.unknown),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    detail.email?.takeIf { it.isNotBlank() }?.let { email ->
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            if (detail.accounts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = mokoString(MR.strings.accounts),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        detail.accounts.forEach { acc ->
                            AccountChip(account = acc)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountChip(
    account: TracearrUserAccount,
    modifier: Modifier = Modifier,
) {
    val serverColor = getTracearrServerColor(account.serverType, account.username)
    val typeName = account.serverType?.name ?: "Server"
    val userLabel = account.username ?: account.serverUserId ?: account.externalUserId ?: ""
    val label = if (userLabel.isNotBlank()) "$typeName: $userLabel" else typeName

    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = serverColor,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Dns,
                contentDescription = null,
                tint = serverColor,
                modifier = Modifier.size(14.dp),
            )
        },
        border =
            AssistChipDefaults.assistChipBorder(
                enabled = true,
                borderColor = serverColor.copy(alpha = 0.5f),
            ),
        colors =
            AssistChipDefaults.assistChipColors(
                containerColor = serverColor.copy(alpha = 0.12f),
                labelColor = serverColor,
            ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserStatsCard(
    stats: TracearrUserStats,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = mokoString(MR.strings.statistics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            val windows = stats.windows
            val allTime = windows?.allTime
            val last30 = windows?.last30
            val last7 = windows?.last7

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = if (isExpanded) 4 else 2,
            ) {
                CountStatItem(
                    icon = Icons.Default.PlayArrow,
                    count = allTime?.plays?.toInt() ?: 0,
                    label = mokoString(MR.strings.plays),
                    iconColor = TracearrBlue,
                    modifier = Modifier.weight(1f),
                )

                CompactStatCard(
                    icon = Icons.Default.Schedule,
                    value = formatWatchTimeMs(allTime?.watchTimeMs ?: 0),
                    label = mokoString(MR.strings.watch_time),
                    iconColor = ArrPurple,
                    modifier = Modifier.weight(1f),
                )

                last30?.let { last30 ->
                    CompactStatCard(
                        icon = Icons.Default.PlayArrow,
                        value = last30.plays.toString(),
                        label = mokoString(MR.strings.last_30_days),
                        iconColor = ArrOrange,
                        modifier = Modifier.weight(1f),
                    )
                }

                last7?.let { last7 ->
                    CompactStatCard(
                        icon = Icons.Default.PlayArrow,
                        value = last7.plays.toString(),
                        label = mokoString(MR.strings.last_7_days),
                        iconColor = ArrGreen,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (stats.topGenres.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = mokoString(MR.strings.top_genres),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        stats.topGenres.forEach { genreStat ->
                            genreStat.genre?.let { g ->
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                ) {
                                    Text(
                                        text = "$g (${genreStat.plays})",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
