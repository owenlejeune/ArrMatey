package com.dnfapps.arrmatey.ui.screens.tracearr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.dnfapps.arrmatey.ui.theme.ArrYellow
import com.dnfapps.arrmatey.ui.theme.TracearrBlue
import com.dnfapps.arrmatey.ui.theme.getTracearrServerColor
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun TracearrHistoryCard(
    item: TracearrHistoryItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val edgeColor = getTracearrServerColor(item.serverType, item.serverName)

    val percent = item.percentComplete?.toFloat() ?: run {
        val total = item.totalDurationMs ?: item.durationMs ?: 0L
        val prog = item.progressMs ?: 0L
        if (total > 0) (prog.toFloat() / total.toFloat() * 100f) else 0f
    }

    val progressFraction = (percent / 100f).coerceIn(0f, 1f)

    val isWatched = item.watched == true || percent >= 90f
    val isAbandoned = !isWatched && percent < 10f
    val isSampled = !isWatched && !isAbandoned

    var cardHeight by remember { mutableIntStateOf(0) }

    Card(
        onClick = { onClick?.invoke() },
        modifier = modifier.fillMaxWidth().onGloballyPositioned {
            cardHeight = it.size.height
        },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(cardHeight.pxToDp())
                    .background(edgeColor),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .aspectRatio(AspectRatio.Poster.ratio)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        val imageUrl = item.posterUrl ?: item.thumbPath
                        if (!imageUrl.isNullOrRelative()) {
                            AsyncImage(
                                model = rememberRemoteImageData(imageUrl, trim = false),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val username =
                                    item.effectiveUsername.ifEmpty { mokoString(MR.strings.user) }
                                val avatarUrl = item.effectiveUserAvatar

                                if (!avatarUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = rememberRemoteImageData(avatarUrl, trim = false),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                    )
                                } else {
                                    Surface(
                                        shape = CircleShape,
                                        color = ArrOrange,
                                        modifier = Modifier.size(18.dp),
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
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                            }

                            StatusChip(
                                isWatched = isWatched,
                                isSampled = isSampled,
                                isAbandoned = isAbandoned,
                            )
                        }

                        val displayTitle = item.grandparentTitle ?: item.showTitle ?: item.mediaTitle ?: mokoString(MR.strings.unknown)
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        val subtitle = when {
                            item.mediaType == TracearrMediaType.Episode || (item.seasonNumber != null && item.episodeNumber != null) -> {
                                val s = item.seasonNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                                val e = item.episodeNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                                "S$s E$e · ${item.mediaTitle ?: ""}"
                            }
                            item.mediaType == TracearrMediaType.Movie -> {
                                "${item.year ?: ""} · ${item.mediaTitle ?: ""}"
                            }
                            else -> listOfNotNull(item.artistName, item.albumName, item.mediaTitle).joinToString(" · ")
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
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = TracearrBlue,
                                trackColor = MaterialTheme.colorScheme.surface,
                            )

                            item.durationMs?.let { duration ->
                                Text(
                                    text = formatDurationMs(duration),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val serverName = item.effectiveServerName.ifEmpty { mokoString(MR.strings.server) }
                    val isTranscoding = item.isTranscode == true || item.videoDecision == TracearrStreamDecision.Transcode || item.audioDecision == TracearrStreamDecision.Transcode
                    val decisionText = if (isTranscoding) mokoString(MR.strings.transcode) else mokoString(MR.strings.direct_play)

                    Text(
                        text = "$serverName · $decisionText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    val resolution = item.resolution ?: "1080p"
                    val dateText = item.startedAt?.format("MMM d, yyyy, HH:mm")
                    val rightText = if (dateText != null) "$dateText · $resolution" else resolution

                    Text(
                        text = rightText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    isWatched: Boolean,
    isSampled: Boolean,
    isAbandoned: Boolean,
) {
    val (label, containerColor, contentColor) = when {
        isWatched -> Triple(
            mokoString(MR.strings.watched),
            Color(0xFF4CAF50).copy(alpha = 0.2f),
            Color(0xFF4CAF50),
        )
        isSampled -> Triple(
            mokoString(MR.strings.sampled),
            ArrYellow.copy(alpha = 0.2f),
            ArrYellow,
        )
        isAbandoned -> Triple(
            mokoString(MR.strings.abandoned),
            ArrRed.copy(alpha = 0.2f),
            ArrRed,
        )
        else -> Triple(
            mokoString(MR.strings.unknown),
            TracearrBlue.copy(alpha = 0.2f),
            TracearrBlue
        )
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = containerColor,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

private fun String?.isNullOrRelative(): Boolean {
    if (this.isNullOrEmpty()) return true
    return !this.startsWith("http://") && !this.startsWith("https://")
}

private fun formatDurationMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}
