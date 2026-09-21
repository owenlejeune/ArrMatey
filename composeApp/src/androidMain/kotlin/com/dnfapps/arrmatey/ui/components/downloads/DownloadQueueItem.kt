package com.dnfapps.arrmatey.ui.components.downloads

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.utils.breakable
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.entensions.ARROW_DOWN
import com.dnfapps.arrmatey.entensions.ARROW_UP
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrGrey
import com.dnfapps.arrmatey.ui.theme.ArrPurple
import com.dnfapps.arrmatey.ui.theme.ArrRed
import com.dnfapps.arrmatey.ui.theme.associatedColor
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun TorrentActionsCard(
    item: DownloadItem,
    showClientInfo: Boolean,
    isInSelectionMode: Boolean,
    isSelected: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState()

    LaunchedEffect(state.currentValue) {
        if (!isInSelectionMode) {
            when (state.currentValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (item.status.isPaused) onResume() else onPause()
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                }

                else -> {}
            }
        }
        state.snapTo(SwipeToDismissBoxValue.Settled)
    }

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = !isInSelectionMode,
        enableDismissFromEndToStart = !isInSelectionMode,
        backgroundContent = {
            val color =
                when (state.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> Color.Unspecified
                }
            val icon =
                when (state.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd ->
                        if (item.status.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause

                    else -> Icons.Default.Delete
                }
            val alignment =
                when (state.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    else -> Alignment.CenterEnd
                }

            when (state.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd,
                SwipeToDismissBoxValue.EndToStart,
                -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    drawRoundRect(
                                        color = color,
                                        cornerRadius = CornerRadius(10.dp.toPx()),
                                    )
                                }.wrapContentSize(alignment)
                                .padding(12.dp),
                    )
                }

                else -> {}
            }
        },
        onDismiss = {},
    ) {
        DownloadQueueItem(
            item = item,
            showClientInfo = showClientInfo,
            isInSelectionMode = isInSelectionMode,
            isSelected = isSelected,
            onLongClick = onLongClick,
            onClick = onClick,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadQueueItem(
    item: DownloadItem,
    showClientInfo: Boolean,
    isInSelectionMode: Boolean,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
) {
    val statusColor =
        remember(item.status) {
            when (item.status) {
                DownloadItemStatus.Downloading,
                DownloadItemStatus.DownloadingForced,
                DownloadItemStatus.DownloadingMetadataForced,
                DownloadItemStatus.Checking,
                DownloadItemStatus.CheckingResumeData,
                DownloadItemStatus.Moving,
                DownloadItemStatus.DownloadingStalled,
                -> ArrGreen

                DownloadItemStatus.Uploading,
                DownloadItemStatus.UploadingForced,
                -> ArrBlue

                DownloadItemStatus.DownloadingPaused,
                DownloadItemStatus.UploadingPaused,
                -> ArrPurple

                DownloadItemStatus.Queued,
                DownloadItemStatus.Allocating,
                DownloadItemStatus.Propagating,
                DownloadItemStatus.Fetching,
                -> ArrGrey

                DownloadItemStatus.Error,
                DownloadItemStatus.MissingFiles,
                DownloadItemStatus.Unknown,
                -> ArrRed
            }
        }

    ContainerCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onLongClick = onLongClick,
                    onClick = onClick,
                ),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showClientInfo) {
                Box(
                    modifier =
                        Modifier
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(item.client.type.associatedColor),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                if (isInSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = item.name.breakable(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (showClientInfo) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Image(
                                        painter = painterResource(item.client.type.icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = item.client.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = statusColor.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = mokoString(item.status.resource),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }

                        if (item.downloadSpeed > 0L) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Text(
                                    text = "$ARROW_DOWN ${item.downloadSpeed.bytesAsFileSizeString()}/s",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }

                        if (item.uploadSpeed > 0L) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                            ) {
                                Text(
                                    text = "$ARROW_UP ${item.uploadSpeed.bytesAsFileSizeString()}/s",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }

                        if (item.etaString.isNotBlank()) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ) {
                                Text(
                                    text = "ETA: ${item.etaString}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { item.progress.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        trackColor = statusColor.copy(alpha = 0.2f),
                        color = statusColor,
                        strokeCap = StrokeCap.Round,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${item.downloaded.bytesAsFileSizeString()} / ${item.size.bytesAsFileSizeString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${(item.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (item.category.isNotEmpty() || item.tags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            item.category.takeUnless { it.isEmpty() }?.let { category ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(category, style = MaterialTheme.typography.labelSmall) },
                                    border = null,
                                    colors =
                                        AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        ),
                                )
                            }
                            item.tags.forEach { tag ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                                    border = null,
                                    shape = CircleShape,
                                    colors =
                                        AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
