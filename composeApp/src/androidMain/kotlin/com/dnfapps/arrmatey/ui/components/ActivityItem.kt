package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.dnfapps.arrmatey.arr.api.model.QueueDownloadState
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.ui.theme.surfaceDark

@Composable
fun ActivityItem(
    item: QueueItem,
    useFullColorCards: Boolean = false,
    onClick: () -> Unit,
) {
    val containerColor =
        when {
            item.hasIssue -> MaterialTheme.colorScheme.errorContainer
            useFullColorCards -> item.type.associatedColor
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        }
    val contentColor =
        when {
            item.hasIssue -> MaterialTheme.colorScheme.onErrorContainer
            useFullColorCards -> surfaceDark
            else -> MaterialTheme.colorScheme.onSurface
        }
    val secondaryContentColor =
        when {
            item.hasIssue -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            useFullColorCards -> surfaceDark.copy(alpha = 0.8f)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
        shape = MaterialTheme.shapes.large,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (!useFullColorCards) {
                Box(
                    modifier = Modifier.matchParentSize(),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(6.dp)
                                .fillMaxHeight()
                                .background(item.type.associatedColor)
                                .align(Alignment.CenterStart),
                    )
                }
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = if (!useFullColorCards) 6.dp else 0.dp)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.titleLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        overflow = TextOverflow.Ellipsis,
                        color = contentColor,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (item.hasIssue) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (useFullColorCards) surfaceDark else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp).padding(start = 4.dp),
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    item.instanceName?.takeIf { it.isNotBlank() }?.let { instanceName ->
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color =
                                if (useFullColorCards) {
                                    surfaceDark.copy(
                                        alpha = 0.15f,
                                    )
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                },
                        ) {
                            Text(
                                text = instanceName,
                                style = MaterialTheme.typography.labelSmall,
                                color = secondaryContentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }

                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color =
                            if (item.hasIssue) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            } else if (useFullColorCards) {
                                surfaceDark.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                    ) {
                        Text(
                            text = item.statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color =
                                if (item.hasIssue) {
                                    MaterialTheme.colorScheme.error
                                } else if (useFullColorCards) {
                                    surfaceDark
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color =
                            if (useFullColorCards) {
                                surfaceDark.copy(
                                    alpha = 0.15f,
                                )
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                    ) {
                        Text(
                            text = item.quality.qualityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryContentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }

                    if (item.size > 0f) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color =
                                if (useFullColorCards) {
                                    surfaceDark.copy(
                                        alpha = 0.15f,
                                    )
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                },
                        ) {
                            Text(
                                text = item.size.toLong().bytesAsFileSizeString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = secondaryContentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }

                    if (item.trackedDownloadState == QueueDownloadState.Downloading) {
                        item.remainingTimeLabel?.let { remainingTimeLabel ->
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color =
                                    if (useFullColorCards) {
                                        surfaceDark.copy(
                                            alpha = 0.15f,
                                        )
                                    } else {
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    },
                            ) {
                                Text(
                                    text = "$remainingTimeLabel left",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (useFullColorCards) surfaceDark else MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                }

                if (item.trackedDownloadState == QueueDownloadState.Downloading && item.progressPercent > 0f) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { item.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            strokeCap = StrokeCap.Round,
                            color = if (useFullColorCards) surfaceDark else MaterialTheme.colorScheme.primary,
                            trackColor =
                                if (useFullColorCards) {
                                    surfaceDark.copy(
                                        alpha = 0.2f,
                                    )
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Text(
                                text = item.progressLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = secondaryContentColor,
                            )
                        }
                    }
                }
            }
        }
    }
}
