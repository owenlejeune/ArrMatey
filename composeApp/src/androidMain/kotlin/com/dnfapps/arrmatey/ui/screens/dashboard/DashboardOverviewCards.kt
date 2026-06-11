package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrHealthType
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.icons.Hard_drive
import com.dnfapps.arrmatey.ui.theme.ArrYellow
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun DashboardOverviewCards(
    state: CombinedDashboardState.Success,
    isEditing: Boolean
) {
    val totalSize = state.instances.sumOf { it.sizeOnDisk }
    val totalIssues = state.instances.sumOf { it.healthItems.size }
    val criticalIssues =
        state.instances.sumOf { it.healthItems.count { h -> h.type == ArrHealthType.Error } }

    val containerColor by animateColorAsState(
        targetValue = if (isEditing) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else Color.Transparent,
        label = "ArrOverviewCardBackgroundAnimation"
    )

    val internalPadding by animateDpAsState(
        targetValue = if (isEditing) 16.dp else 0.dp,
        label = "ArrOverviewCardPaddingAnimation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier.padding(internalPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = isEditing
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Hard_drive,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = mokoString(MR.strings.dashboard_arr_overview),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Storage,
                    label = mokoString(MR.strings.total_space),
                    value = totalSize.bytesAsFileSizeString(),
                    color = MaterialTheme.colorScheme.primaryContainer
                )

                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = if (totalIssues > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                    label = mokoString(MR.strings.health),
                    value = if (totalIssues == 0) mokoString(MR.strings.no_issues) else "$totalIssues Issues",
                    color = when {
                        criticalIssues > 0 -> MaterialTheme.colorScheme.errorContainer
                        totalIssues > 0 -> ArrYellow.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                )
            }
        }
    }
}