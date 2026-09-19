package com.dnfapps.arrmatey.ui.components.tracearr

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.extensions.formatMinutesAsRuntime
import com.dnfapps.arrmatey.model.TracearrMediaUiState
import com.dnfapps.arrmatey.model.TracearrStatsWindowType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaWatcher
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrHistoryCard
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrHistoryTable
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun TracearrSummaryChipRow(
    uiState: TracearrMediaUiState,
    modifier: Modifier = Modifier,
) {
    if (!uiState.isTracearrConfigured) return
    val stats = uiState.stats ?: return
    val allTime = stats.windows?.allTime?.combined ?: return

    val scrollState = rememberScrollState()

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        SuggestionChip(
            onClick = {},
            label = { Text(mokoPlural(MR.plurals.plays_count, allTime.plays.toInt(), allTime.plays.toInt())) },
            icon = {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                )
            },
            colors =
                SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
        )

        SuggestionChip(
            onClick = {},
            label = { Text((allTime.watchTimeMs / 1000 / 60).toInt().formatMinutesAsRuntime()) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                )
            },
        )

        if (allTime.uniqueUsers > 0) {
            SuggestionChip(
                onClick = {},
                label = { Text(mokoPlural(MR.plurals.viewers_count, allTime.uniqueUsers, allTime.uniqueUsers)) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                    )
                },
            )
        }

        val serverCount =
            stats.windows
                ?.allTime
                ?.perServer
                ?.size ?: 0
        if (serverCount > 0) {
            SuggestionChip(
                onClick = {},
                label = { Text(mokoPlural(MR.plurals.servers_count, serverCount, serverCount)) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                    )
                },
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TracearrAnalyticsSection(
    uiState: TracearrMediaUiState,
    onWindowSelected: (TracearrStatsWindowType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            val windows =
                listOf(
                    TracearrStatsWindowType.AllTime to mokoString(MR.strings.all_time),
                    TracearrStatsWindowType.Last30 to mokoString(MR.strings.last_30_days),
                    TracearrStatsWindowType.Last7 to mokoString(MR.strings.last_7_days),
                )
            windows.forEachIndexed { index, (windowType, label) ->
                SegmentedButton(
                    selected = uiState.selectedStatsWindow == windowType,
                    onClick = { onWindowSelected(windowType) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = windows.size),
                ) {
                    Text(label)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnalyticsMetricCard(
                title = mokoString(MR.strings.total_plays),
                value = uiState.totalPlays.toString(),
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.weight(1f),
            )
            AnalyticsMetricCard(
                title = mokoString(MR.strings.watch_time),
                value = (uiState.totalWatchTimeMs / 1000 / 60).toInt().formatMinutesAsRuntime(),
                icon = Icons.Default.Schedule,
                modifier = Modifier.weight(1f),
            )
            AnalyticsMetricCard(
                title = mokoString(MR.strings.viewers),
                value = uiState.uniqueUsers.toString(),
                icon = Icons.Default.Group,
                modifier = Modifier.weight(1f),
            )
        }

        if (uiState.perServerStats.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = mokoString(MR.strings.per_server_breakdown),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    uiState.perServerStats.forEach { serverStat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = serverStat.serverName ?: serverStat.serverId ?: mokoString(MR.strings.server),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            Text(
                                text = "${mokoPlural(
                                    MR.plurals.plays_count,
                                    serverStat.plays.toInt(),
                                    serverStat.plays.toInt(),
                                )} • ${(serverStat.watchTimeMs / 1000 / 60).toInt().formatMinutesAsRuntime()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        val watchers = uiState.watchers?.watchers ?: emptyList()
        if (watchers.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = mokoString(MR.strings.top_watchers),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                watchers.forEach { watcher ->
                    WatcherCard(watcher = watcher)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WatcherCard(
    watcher: TracearrMediaWatcher,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val user = watcher.user
            val avatarUrl = user?.identityName ?: user?.username
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = user?.identityName ?: user?.username ?: mokoString(MR.strings.users),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${mokoPlural(
                        MR.plurals.plays_count,
                        watcher.plays.toInt(),
                        watcher.plays.toInt(),
                    )} • ${(watcher.watchTimeMs / 1000 / 60).toInt().formatMinutesAsRuntime()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                watcher.lastWatchedDay?.let { day ->
                    Text(
                        text = mokoString(MR.strings.last_watched, day),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            watcher.completionPct?.let { pct ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun TracearrHistorySection(
    uiState: TracearrMediaUiState,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    isLargeScreen: Boolean = false,
    onClickItem: ((TracearrHistoryItem) -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (uiState.historyItems.isEmpty() && !uiState.isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
            ) {
                Box(
                    modifier =
                        Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = mokoString(MR.strings.no_stream_history),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            if (isLargeScreen) {
                TracearrHistoryTable(
                    items = uiState.historyItems,
                    onClickItem = { item -> onClickItem?.invoke(item) },
                )
            } else {
                uiState.historyItems.forEach { item ->
                    TracearrHistoryCard(
                        item = item,
                        onClick = onClickItem?.let { { it(item) } },
                    )
                }
            }

            if (uiState.nextHistoryCursor != null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (uiState.isLoadingHistoryMore) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    } else {
                        OutlinedButton(onClick = onLoadMore) {
                            Text(mokoString(MR.strings.load_more_history))
                        }
                    }
                }
            }
        }
    }
}
