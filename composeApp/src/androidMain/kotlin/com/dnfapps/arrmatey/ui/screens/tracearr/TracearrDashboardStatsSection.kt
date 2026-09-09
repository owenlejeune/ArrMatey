package com.dnfapps.arrmatey.ui.screens.tracearr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats
import com.dnfapps.arrmatey.ui.screens.dashboard.CompactStatCard
import com.dnfapps.arrmatey.ui.screens.dashboard.CountStatItem
import com.dnfapps.arrmatey.ui.screens.dashboard.SplitStatCard
import com.dnfapps.arrmatey.ui.theme.TracearrBlue
import com.dnfapps.arrmatey.ui.theme.TracearrDarkBlue
import com.dnfapps.arrmatey.ui.theme.TracearrLightBlue
import com.dnfapps.arrmatey.ui.theme.TracearrNavy
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun TracearrDashboardStatsSection(
    stats: TracearrTodayStats,
    isExpanded: Boolean,
    onNavigateToHistory: () -> Unit,
    onNavigateToAllUsers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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

        FlowRow (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = if (isExpanded) 4 else 2
        ) {
            CountStatItem(
                icon = Icons.Default.Warning,
                count = stats.alertsLast24h,
                label = mokoString(MR.strings.alerts),
                containerColor = if (stats.alertsLast24h > 0) MaterialTheme.colorScheme.errorContainer else TracearrDarkBlue,
                modifier = Modifier.weight(1f)
            )
            SplitStatCard(
                icon = Icons.Default.PlayArrow,
                firstValue = stats.todayPlays.toString(),
                firstLabel = mokoString(MR.strings.plays),
                secondValue = stats.todaySessions.toString(),
                secondLabel = mokoString(MR.strings.sessions),
                color = TracearrBlue,
                contentColor = TracearrNavy,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToHistory
            )
            CompactStatCard(
                icon = Icons.Default.Schedule,
                value = stats.formattedWatchTime,
                label = mokoString(MR.strings.watch_time),
                containerColor = TracearrLightBlue,
                contentColor = TracearrDarkBlue,
                modifier = Modifier.weight(1f)
            )
            CountStatItem(
                icon = Icons.Default.Group,
                count = stats.activeUsersToday,
                label = mokoString(MR.strings.active_users),
                containerColor = TracearrNavy,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToAllUsers
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
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
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
