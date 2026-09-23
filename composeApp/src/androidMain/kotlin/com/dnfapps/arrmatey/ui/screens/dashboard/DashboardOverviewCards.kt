package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
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
    isEditing: Boolean,
    onHealthClick: () -> Unit = {},
) {
    val instances = state.instances
    val totalSize = instances.sumOf { it.sizeOnDisk }
    val totalIssues = instances.sumOf { it.healthItems.size }
    val criticalIssues =
        instances.sumOf { it.healthItems.count { h -> h.type == ArrHealthType.Error } }

    val containerColor by animateColorAsState(
        targetValue =
            if (isEditing || instances.isEmpty()) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                Color.Transparent
            },
        label = "ArrOverviewCardBackgroundAnimation",
    )

    val internalPadding by animateDpAsState(
        targetValue = if (isEditing || instances.isEmpty()) 16.dp else 0.dp,
        label = "ArrOverviewCardPaddingAnimation",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = containerColor,
            ),
        border = if (isEditing || instances.isEmpty()) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null,
    ) {
        Column(
            modifier = Modifier.padding(internalPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(
                visible = isEditing || instances.isEmpty(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Hard_drive,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = mokoString(MR.strings.dashboard_arr_overview),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            if (instances.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.no_type_instances_message, "Arr"),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Storage,
                        label = mokoString(MR.strings.total_space),
                        value = totalSize.bytesAsFileSizeString(),
                        iconColor = MaterialTheme.colorScheme.primary,
                    )

                    val hasErrors = criticalIssues > 0
                    val hasWarnings = totalIssues > 0
                    val healthColor =
                        when {
                            hasErrors -> MaterialTheme.colorScheme.error
                            hasWarnings -> ArrYellow
                            else -> MaterialTheme.colorScheme.primary
                        }

                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = if (hasWarnings) Icons.Default.Warning else Icons.Default.CheckCircle,
                        label = mokoString(MR.strings.health),
                        value = if (totalIssues == 0) mokoString(MR.strings.no_issues) else "$totalIssues Issues",
                        iconColor = healthColor,
                        containerColor =
                            if (hasErrors) {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                        onClick = if (!isEditing) onHealthClick else null,
                    )
                }
            }
        }
    }
}
