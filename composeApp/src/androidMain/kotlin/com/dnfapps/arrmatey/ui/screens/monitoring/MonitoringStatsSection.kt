package com.dnfapps.arrmatey.ui.screens.monitoring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.monitoring.model.PlaybackSummaryStats
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats
import com.dnfapps.arrmatey.ui.screens.dashboard.CountStatItem
import com.dnfapps.arrmatey.ui.screens.dashboard.SplitStatCard
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrDashboardStatsSection
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrRed
import com.dnfapps.arrmatey.ui.theme.TautulliOrange
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.math.roundToLong

@Composable
fun MonitoringStatsSection(
    instanceType: InstanceType,
    summaryStats: PlaybackSummaryStats?,
    tracearrStats: TracearrTodayStats?,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAllUsers: () -> Unit = {},
    onNavigateToViolations: () -> Unit = {},
    onNavigateToActivity: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
) {
    when (instanceType) {
        InstanceType.Tracearr -> {
            tracearrStats?.let { stats ->
                TracearrDashboardStatsSection(
                    stats = stats,
                    isExpanded = isExpanded,
                    modifier = modifier,
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToAllUsers = onNavigateToAllUsers,
                    onNavigateToViolations = onNavigateToViolations,
                    onNavigateToActivity = onNavigateToActivity,
                )
            }
        }

        InstanceType.Tautulli -> {
            summaryStats?.let { stats ->
                TautulliStatsSection(
                    stats = stats,
                    isExpanded = isExpanded,
                    modifier = modifier,
                    onNavigateToAllUsers = onNavigateToAllUsers,
                    onNavigateToStats = onNavigateToStats,
                )
            }
        }

        else -> {}
    }
}

@Composable
fun TautulliStatsSection(
    stats: PlaybackSummaryStats,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    onNavigateToAllUsers: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = mokoString(MR.strings.statistics),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            TextButton(onClick = onNavigateToStats) {
                Text(
                    text = mokoString(MR.strings.all),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = if (isExpanded) 4 else 2,
        ) {
            CountStatItem(
                icon = Icons.Default.PlayArrow,
                count = stats.activeStreamsCount,
                label = mokoString(MR.strings.active_streams),
                iconColor = TautulliOrange,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.weight(1f),
            )

            CountStatItem(
                icon = Icons.Default.Group,
                count = stats.activeUsersCount,
                label = mokoString(MR.strings.active_users),
                iconColor = ArrOrange,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToAllUsers,
            )

            SplitStatCard(
                icon = Icons.Default.Speed,
                firstValue = stats.directPlayCount.toString(),
                firstLabel = mokoString(MR.strings.direct),
                secondValue = stats.transcodeCount.toString(),
                secondLabel = mokoString(MR.strings.transcode),
                iconColor = if (stats.transcodeCount > 0) ArrRed else ArrGreen,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            SplitStatCard(
                icon = Icons.Default.Cloud,
                firstValue = formatBandwidth(stats.wanBandwidthKbps),
                firstLabel = mokoString(MR.strings.wan),
                secondValue = formatBandwidth(stats.lanBandwidthKbps),
                secondLabel = mokoString(MR.strings.lan),
                iconColor = ArrBlue,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun formatBandwidth(kbps: Long): String = if (kbps >= 1000L) {
    val mbps = (kbps / 100.0).roundToLong() / 10.0
    val mbpsStr = if (mbps % 1.0 == 0.0) "${mbps.toLong()}" else "$mbps"
    mokoString(MR.strings.bandwidth_mbps, mbpsStr)
} else {
    mokoString(MR.strings.bandwidth_kbps, kbps)
}
