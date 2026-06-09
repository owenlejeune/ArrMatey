package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrHealthType
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun OverviewHeader(state: CombinedDashboardState.Success) {
    val totalSize = state.instances.sumOf { it.sizeOnDisk }
    val totalIssues = state.instances.sumOf { it.healthItems.size }
    val criticalIssues = state.instances.sumOf { it.healthItems.count { h -> h.type == ArrHealthType.Error } }

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
                totalIssues > 0 -> Color(0xffffc653).copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    }
}