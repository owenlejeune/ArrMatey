package com.dnfapps.arrmatey.ui.screens.tracearr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaType
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamDecision
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrYellow
import com.dnfapps.arrmatey.ui.theme.TracearrBlue
import com.dnfapps.arrmatey.utils.AspectRatio

@Composable
fun TracearrStreamCard(
    session: TracearrStreamSession,
    modifier: Modifier = Modifier,
) {
    val isPaused = session.state?.equals("paused", ignoreCase = true) == true
    val isPlaying = session.state?.equals("playing", ignoreCase = true) == true
    val stateColor =
        when {
            isPaused -> ArrYellow
            isPlaying -> Color(0xFF4CAF50)
            else -> Color(0xFF2196F3)
        }

    val stateText =
        when {
            isPaused -> "Paused"
            isPlaying -> "Playing"
            else -> session.state?.replaceFirstChar { it.uppercase() } ?: "Active"
        }

    val totalMs = session.totalDurationMs ?: session.durationMs ?: 0L
    val progressMs = session.progressMs ?: 0L
    val progressFraction =
        if (totalMs > 0) {
            (progressMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Accent Strip
            Box(
                modifier =
                    Modifier
                        .width(4.dp)
                        .height(180.dp)
                        .background(stateColor),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Poster Thumbnail
                    Box(
                        modifier =
                            Modifier
                                .width(80.dp)
                                .aspectRatio(AspectRatio.Poster.ratio)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        val imageUrl = session.posterUrl ?: session.thumbPath
                        if (!imageUrl.isNullOrRelative()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop,
                            )
                        }

                        // State overlay icon
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector =
                                        if (isPaused) {
                                            Icons.Default.Pause
                                        } else {
                                            Icons.Default.PlayArrow
                                        },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }

                    // Content Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        // User & Action Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            val username = session.user?.username ?: "User"
                            val avatarUrl = session.user?.avatarUrl ?: session.user?.thumbUrl

                            if (!avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = null,
                                    modifier =
                                        Modifier
                                            .size(22.dp)
                                            .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Surface(
                                    shape = CircleShape,
                                    color = ArrOrange,
                                    modifier = Modifier.size(22.dp),
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
                                modifier = Modifier.weight(1f, fill = false),
                            )

                            Spacer(Modifier.weight(1f))

                            // Transcode indicator
                            val isTranscoding =
                                session.isTranscode == true ||
                                    session.videoDecision == TracearrStreamDecision.Transcode ||
                                    session.audioDecision == TracearrStreamDecision.Transcode

                            if (isTranscoding) {
                                Surface(
                                    shape = CircleShape,
                                    color = ArrYellow.copy(alpha = 0.2f),
                                    modifier = Modifier.size(24.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ElectricBolt,
                                            contentDescription = "Transcoding",
                                            tint = ArrYellow,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                }
                            }

                            // Device / Platform icon
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier.size(24.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector =
                                            if (session.platform?.contains("TV", ignoreCase = true) == true ||
                                                session.device?.contains("TV", ignoreCase = true) == true
                                            ) {
                                                Icons.Default.Tv
                                            } else {
                                                Icons.Default.Smartphone
                                            },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }

                            if (session.canTerminate == true) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Terminate",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }

                        // Title
                        val displayTitle =
                            session.grandparentTitle
                                ?: session.showTitle
                                ?: session.mediaTitle
                                ?: "Unknown Title"

                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        // Subtitle
                        val subtitle =
                            when {
                                session.mediaType == TracearrMediaType.Episode || (session.seasonNumber != null && session.episodeNumber != null) -> {
                                    val seasonStr = session.seasonNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                                    val episodeStr = session.episodeNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                                    "S$seasonStr E$episodeStr · ${session.mediaTitle ?: ""}"
                                }
                                session.mediaType == TracearrMediaType.Movie -> {
                                    "${session.year ?: ""} · ${session.mediaTitle ?: ""}"
                                }
                                else -> {
                                    listOfNotNull(session.artistName, session.albumName, session.mediaTitle)
                                        .joinToString(" · ")
                                }
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

                        Spacer(Modifier.height(4.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                            color = TracearrBlue,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        )

                        // Progress Time & Status Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = formatTimeMs(progressMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Text(
                                text = stateText,
                                style = MaterialTheme.typography.labelSmall,
                                color = stateColor,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Footer Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val serverName = session.server?.name ?: "Server"
                    val location = session.geoCountry ?: session.ipAddress ?: "Local Network"

                    Text(
                        text = "$serverName · $location",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    val quality = session.quality ?: session.resolution ?: "1080p"
                    Text(
                        text = quality,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun String?.isNullOrRelative(): Boolean {
    if (this.isNullOrEmpty()) return true
    return !this.startsWith("http://") && !this.startsWith("https://")
}

private fun formatTimeMs(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    val secondsStr = if (seconds < 10) "0$seconds" else "$seconds"
    return if (hours > 0) {
        val minutesStr = if (minutes < 10) "0$minutes" else "$minutes"
        "$hours:$minutesStr:$secondsStr"
    } else {
        "$minutes:$secondsStr"
    }
}
