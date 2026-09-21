package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrHealthType
import com.dnfapps.arrmatey.arr.state.ArrInstanceDashboardState
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.theme.ArrYellow
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun InstanceDashboardSection(
    state: CombinedDashboardState.Success,
    onInstanceClicked: (Long) -> Unit,
    enabled: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Cloud,
                    null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = mokoString(MR.strings.overview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            state.instances.forEachIndexed { index, instanceState ->
                InstanceDashboardCard(
                    instanceState,
                    enabled = enabled,
                    onClick = {
                        onInstanceClicked(instanceState.instance.id)
                    },
                )
                if (index < state.instances.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}

@Composable
private fun InstanceDashboardCard(
    state: ArrInstanceDashboardState,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val completion =
        if (state.library.isNotEmpty()) {
            state.library
                .asSequence()
                .map { it.statusProgress }
                .average()
                .toFloat()
        } else {
            0f
        }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.clickable(onClick = onClick, enabled = enabled),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            ) {
                Box(modifier = Modifier.padding(6.dp)) {
                    Image(
                        painter = painterResource(state.instance.type.icon),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.instance.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text =
                        "${state.totalItems} Items • " +
                            "${state.sizeOnDisk.bytesAsFileSizeString()} • " +
                            "${(completion * 100).toInt()}% Downloaded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val hasErrors = state.healthItems.any { it.type == ArrHealthType.Error }
            val hasWarnings = state.healthItems.isNotEmpty()

            when {
                hasErrors -> {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "${state.healthItems.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
                hasWarnings -> {
                    Surface(
                        shape = CircleShape,
                        color = ArrYellow.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, ArrYellow.copy(alpha = 0.4f)),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = ArrYellow,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "${state.healthItems.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                else -> {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    ) {
                        Box(modifier = Modifier.padding(4.dp)) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }

        if (state.disks.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                state.disks.forEach { disk ->
                    val usedSpace = disk.totalSpace - disk.freeSpace
                    val diskProgressColor =
                        when {
                            disk.usedPercentage > 0.90f -> MaterialTheme.colorScheme.error
                            disk.usedPercentage > 0.75f -> ArrYellow
                            else -> MaterialTheme.colorScheme.primary
                        }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = disk.path ?: mokoString(MR.strings.unknown),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            Spacer(Modifier.width(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "${usedSpace.bytesAsFileSizeString()} / ${disk.totalSpace.bytesAsFileSizeString()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "(${(disk.usedPercentage * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color =
                                        if (disk.usedPercentage >
                                            0.90f
                                        ) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { disk.usedPercentage },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(5.dp),
                            color = diskProgressColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round,
                            drawStopIndicator = {},
                        )
                    }
                }
            }
        }
    }
}
