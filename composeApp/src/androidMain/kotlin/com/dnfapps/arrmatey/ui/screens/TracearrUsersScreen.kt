package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserDetail
import com.dnfapps.arrmatey.tracearr.api.model.TracearrUserStats
import com.dnfapps.arrmatey.tracearr.state.TracearrUsersState
import com.dnfapps.arrmatey.tracearr.viewmodel.TracearrUsersViewModel
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.theme.TracearrBlue
import com.dnfapps.arrmatey.ui.theme.getTracearrServerColor
import com.dnfapps.arrmatey.utils.formatWatchTimeMs
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TracearrUsersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUser: (ref: String) -> Unit = {},
    viewModel: TracearrUsersViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState.text) {
        viewModel.updateSearchQuery(textFieldState.text.toString())
    }

    Scaffold(
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
            onRefresh = { viewModel.refresh() },
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
        ) {
            when (val currentState = state) {
                is TracearrUsersState.Initial,
                is TracearrUsersState.Loading,
                -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(96.dp))
                    }
                }
                is TracearrUsersState.NoInstance -> {
                    NoInstanceView(
                        type = InstanceType.Tracearr,
                        modifier = Modifier.fillMaxSize().wrapContentSize(),
                    )
                }
                is TracearrUsersState.Error -> {
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
                is TracearrUsersState.Success -> {
                    val listState = rememberLazyListState()

                    LaunchedEffect(listState) {
                        snapshotFlow {
                            listState.layoutInfo.visibleItemsInfo
                                .lastOrNull()
                                ?.index
                        }.collect { lastVisibleIndex ->
                            val userCount = currentState.users.size
                            if (lastVisibleIndex != null &&
                                userCount > 0 &&
                                lastVisibleIndex >= userCount - 2 &&
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
                        if (currentState.filteredUsers.isEmpty()) {
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text =
                                            if (currentState.searchQuery.isBlank()) {
                                                mokoString(
                                                    MR.strings.no_history,
                                                )
                                            } else {
                                                mokoString(MR.strings.no_results_found)
                                            },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } else {
                            items(
                                items = currentState.filteredUsers,
                                key = { "user_${it.id}" },
                            ) { userDetail ->
                                TracearrUserCard(
                                    detail = userDetail,
                                    stats = currentState.userStatsMap[userDetail.id],
                                    onClick = { onNavigateToUser(userDetail.id) },
                                )
                            }
                        }

                        if (currentState.isLoadingMore) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TracearrUserCard(
    detail: TracearrUserDetail,
    stats: TracearrUserStats?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = detail.username?.take(1)?.uppercase() ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
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
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            stats?.windows?.allTime?.let { allTime ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AssistChip(
                        onClick = onClick,
                        label = {
                            Text(
                                text = mokoPlural(MR.plurals.plays_count, allTime.plays),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TracearrBlue,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = TracearrBlue,
                                modifier = Modifier.size(12.dp),
                            )
                        },
                        border =
                            AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = TracearrBlue.copy(alpha = 0.5f),
                            ),
                        colors =
                            AssistChipDefaults.assistChipColors(
                                containerColor = TracearrBlue.copy(alpha = 0.12f),
                                labelColor = TracearrBlue,
                            ),
                    )
                    AssistChip(
                        onClick = onClick,
                        label = {
                            Text(
                                text = formatWatchTimeMs(allTime.watchTimeMs),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TracearrBlue,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = TracearrBlue,
                                modifier = Modifier.size(12.dp),
                            )
                        },
                        border =
                            AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = TracearrBlue.copy(alpha = 0.5f),
                            ),
                        colors =
                            AssistChipDefaults.assistChipColors(
                                containerColor = TracearrBlue.copy(alpha = 0.12f),
                                labelColor = TracearrBlue,
                            ),
                    )
                }
            }

            if (stats?.topGenres?.isNotEmpty() == true) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    stats.topGenres.forEach { genre ->
                        val genreText = genre.genre ?: mokoString(MR.strings.unknown)
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ) {
                            Text(
                                text = "$genreText (${genre.plays})",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }

            if (detail.accounts.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    detail.accounts.forEach { acc ->
                        val serverColor = getTracearrServerColor(acc.serverType, acc.username)
                        val typeName = acc.serverType?.name ?: "Server"
                        val userLabel = acc.username ?: acc.serverUserId ?: acc.externalUserId ?: ""
                        val label = if (userLabel.isNotBlank()) "$typeName: $userLabel" else typeName

                        AssistChip(
                            onClick = onClick,
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
                                    modifier = Modifier.size(12.dp),
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
                        )
                    }
                }
            }
        }
    }
}
