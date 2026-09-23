package com.dnfapps.arrmatey.ui.screens.tracearr

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats
import com.dnfapps.arrmatey.ui.screens.dashboard.CompactStatCard
import com.dnfapps.arrmatey.ui.screens.dashboard.CountStatItem
import com.dnfapps.arrmatey.ui.screens.dashboard.SplitStatCard
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrPurple
import com.dnfapps.arrmatey.ui.theme.TracearrBlue
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun DashboardTracearrSection(
    state: CombinedDashboardState.Success,
    isExpanded: Boolean,
    isEditing: Boolean,
    modifier: Modifier = Modifier,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAllUsers: () -> Unit = {},
    onNavigateToViolations: () -> Unit = {},
    onNavigateToActivity: () -> Unit = {},
) {
    val tracearrStats = state.tracearrStats
    val statsList = tracearrStats.mapNotNull { it.stats }

    val containerColor by animateColorAsState(
        targetValue =
            if (isEditing || tracearrStats.isEmpty()) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                Color.Transparent
            },
        label = "TracearrCardBackgroundAnimation",
    )

    val internalPadding by animateDpAsState(
        targetValue = if (isEditing || tracearrStats.isEmpty()) 16.dp else 0.dp,
        label = "TracearrCardPaddingAnimation",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = containerColor,
            ),
    ) {
        Column(
            modifier = Modifier.padding(internalPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(
                visible = isEditing || tracearrStats.isEmpty(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(
                        painter = painterResource(InstanceType.Tracearr.icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = mokoString(MR.strings.dashboard_tracearr_overview),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (tracearrStats.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.no_type_instances_message, InstanceType.Tracearr.name),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                statsList.forEach { stats ->
                    TracearrDashboardStatsSection(
                        stats = stats,
                        isExpanded = isExpanded,
                        showTodayHeader = false,
                        onNavigateToHistory = onNavigateToHistory,
                        onNavigateToAllUsers = onNavigateToAllUsers,
                        onNavigateToViolations = onNavigateToViolations,
                        onNavigateToActivity = onNavigateToActivity,
                    )
                }
            }
        }
    }
}

@Composable
fun TracearrDashboardStatsSection(
    stats: TracearrTodayStats,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    showTodayHeader: Boolean = true,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAllUsers: () -> Unit = {},
    onNavigateToViolations: () -> Unit = {},
    onNavigateToActivity: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showTodayHeader) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = mokoString(MR.strings.today),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = if (isExpanded) 4 else 2,
        ) {
            val hasAlerts = stats.alertsLast24h > 0
            CountStatItem(
                icon = Icons.Default.Warning,
                count = stats.alertsLast24h,
                label = mokoString(MR.strings.alerts),
                iconColor = if (hasAlerts) MaterialTheme.colorScheme.error else ArrGreen,
                containerColor =
                    if (hasAlerts) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                modifier = Modifier.weight(1f),
                onClick = onNavigateToViolations,
            )
            SplitStatCard(
                icon = Icons.Default.PlayArrow,
                firstValue = stats.todayPlays.toString(),
                firstLabel = mokoString(MR.strings.plays),
                secondValue = stats.todaySessions.toString(),
                secondLabel = mokoString(MR.strings.sessions),
                iconColor = TracearrBlue,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToHistory,
            )
            CompactStatCard(
                icon = Icons.Default.Schedule,
                value = stats.formattedWatchTime,
                label = mokoString(MR.strings.watch_time),
                iconColor = ArrPurple,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToActivity,
            )
            CountStatItem(
                icon = Icons.Default.Group,
                count = stats.activeUsersToday,
                label = mokoString(MR.strings.active_users),
                iconColor = ArrOrange,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToAllUsers,
            )
        }
    }
}

@Composable
private fun TracearrStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = TracearrBlue.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TracearrBlue,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
