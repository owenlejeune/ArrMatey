package com.dnfapps.arrmatey.ui.screens.tracearr

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.extensions.pxToDp
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaType
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamDecision
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrRed
import com.dnfapps.arrmatey.ui.theme.TracearrBlue
import com.dnfapps.arrmatey.ui.theme.TracearrDarkBlue
import com.dnfapps.arrmatey.ui.theme.TracearrLightBlue
import com.dnfapps.arrmatey.ui.theme.getTracearrServerColor
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun TracearrHistoryTable(
    items: List<TracearrHistoryItem>,
    modifier: Modifier = Modifier,
    onClickItem: (TracearrHistoryItem) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items.forEach { item ->
            TracearrHistoryTableRow(
                item = item,
                onClick = { onClickItem(item) },
            )
        }
    }
}

@Composable
fun TracearrHistoryTableRow(
    item: TracearrHistoryItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val edgeColor = getTracearrServerColor(item.serverType, item.serverName)

    val percent =
        item.percentComplete?.toFloat() ?: run {
            val total = item.totalDurationMs ?: item.durationMs ?: 0L
            val prog = item.progressMs ?: 0L
            if (total > 0) (prog.toFloat() / total.toFloat() * 100f) else 0f
        }
    val percentInt = percent.toInt().coerceIn(0, 100)
    val progressFraction = (percent / 100f).coerceIn(0f, 1f)

    val isWatched = item.watched == true || percent >= 90f
    val isAbandoned = !isWatched && percent < 10f
    val isSampled = !isWatched && !isAbandoned

    var rowHeight by remember { mutableIntStateOf(0) }

    Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .onGloballyPositioned { rowHeight = it.size.height },
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(4.dp)
                        .height(rowHeight.pxToDp())
                        .background(edgeColor),
            )

            Row(
                modifier =
                    Modifier
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1.3f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val username = item.effectiveUsername.ifEmpty { mokoString(MR.strings.user) }
                        val avatarUrl = item.effectiveUserAvatar

                        if (!avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = rememberRemoteImageData(avatarUrl, trim = false),
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .size(20.dp)
                                        .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = ArrOrange,
                                modifier = Modifier.size(20.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = username.take(1).uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }

                        Text(
                            text = username,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    val dateTimeStr = item.startedAt?.format("MMM d, yyyy, HH:mm") ?: "-"
                    Text(
                        text = dateTimeStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier.weight(2.5f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val mediaIcon =
                        when (item.mediaType) {
                            TracearrMediaType.Episode -> Icons.Default.Tv
                            TracearrMediaType.Movie -> Icons.Default.Movie
                            else -> Icons.Default.MusicNote
                        }
                    Icon(
                        imageVector = mediaIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        TableStatusChip(
                            isWatched = isWatched,
                            isSampled = isSampled,
                            isAbandoned = isAbandoned,
                        )

                        val displayTitle =
                            item.grandparentTitle ?: item.showTitle
                                ?: item.mediaTitle ?: mokoString(MR.strings.unknown)
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        val subtitle =
                            when {
                                item.mediaType == TracearrMediaType.Episode ||
                                    (item.seasonNumber != null && item.episodeNumber != null) -> {
                                    val s = item.seasonNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                                    val e = item.episodeNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                                    "S$s E$e · ${item.mediaTitle ?: ""}"
                                }
                                item.mediaType == TracearrMediaType.Movie -> {
                                    "${item.year ?: ""} · ${item.mediaTitle ?: ""}"
                                }
                                else ->
                                    listOfNotNull(item.artistName, item.albumName, item.mediaTitle)
                                        .joinToString(" · ")
                            }

                        if (subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = formatTableDurationMs(item.durationMs ?: item.totalDurationMs),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                color = TracearrBlue,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            )
                            Text(
                                text = "$percentInt%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(edgeColor),
                        )
                        Text(
                            text = item.effectiveServerName.ifEmpty { mokoString(MR.strings.server) },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = mokoString(MR.strings.local_network),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1.4f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val isTranscoding =
                        item.isTranscode == true ||
                            item.videoDecision == TracearrStreamDecision.Transcode ||
                            item.audioDecision == TracearrStreamDecision.Transcode
                    val isDark = isSystemInDarkTheme()
                    val (chipColor, textColor, icon, label) =
                        if (isTranscoding) {
                            Quadruple(
                                Color(0xFFF59E0B).copy(alpha = if (isDark) 0.25f else 0.15f),
                                if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                                Icons.Default.Bolt,
                                mokoString(MR.strings.transcode),
                            )
                        } else {
                            Quadruple(
                                Color(0xFF4CAF50).copy(alpha = if (isDark) 0.25f else 0.15f),
                                if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
                                Icons.Default.PlayArrow,
                                mokoString(MR.strings.direct_play),
                            )
                        }

                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = chipColor,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = textColor,
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                maxLines = 1,
                            )
                        }
                    }

                    val platformName = item.platform ?: item.device ?: mokoString(MR.strings.unknown)
                    Text(
                        text = platformName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    val productText = item.product ?: item.player ?: ""
                    if (productText.isNotEmpty()) {
                        Text(
                            text = productText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableStatusChip(
    isWatched: Boolean,
    isSampled: Boolean,
    isAbandoned: Boolean,
) {
    val isDark = isSystemInDarkTheme()
    val (label, containerColor, contentColor) =
        when {
            isWatched ->
                Triple(
                    mokoString(MR.strings.watched),
                    Color(0xFF4CAF50).copy(alpha = if (isDark) 0.25f else 0.15f),
                    if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
                )
            isSampled ->
                Triple(
                    mokoString(MR.strings.sampled),
                    Color(0xFFF59E0B).copy(alpha = if (isDark) 0.25f else 0.15f),
                    if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                )
            isAbandoned ->
                Triple(
                    mokoString(MR.strings.abandoned),
                    ArrRed.copy(alpha = if (isDark) 0.25f else 0.15f),
                    if (isDark) Color(0xFFEF5350) else Color(0xFFC62828),
                )
            else ->
                Triple(
                    mokoString(MR.strings.unknown),
                    TracearrBlue.copy(alpha = if (isDark) 0.25f else 0.15f),
                    if (isDark) TracearrLightBlue else TracearrDarkBlue,
                )
        }

    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = containerColor,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
        )
    }
}

private fun formatTableDurationMs(ms: Long?): String {
    if (ms == null || ms <= 0) return "-"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
