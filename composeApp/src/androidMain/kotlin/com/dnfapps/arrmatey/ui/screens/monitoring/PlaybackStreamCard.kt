package com.dnfapps.arrmatey.ui.screens.monitoring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tablet
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamDecision
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamSession
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrDevicePlatform
import com.dnfapps.arrmatey.tracearr.api.model.TracearrServerType
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.TracearrBlue
import com.dnfapps.arrmatey.ui.theme.getTracearrServerColor
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

@Composable
fun PlaybackStreamCard(
    session: PlaybackStreamSession,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onTerminate: (() -> Unit)? = null,
) {
    val isPaused = session.state?.equals("paused", ignoreCase = true) == true
    val isPlaying = session.state?.equals("playing", ignoreCase = true) == true
    val stateColor =
        when {
            isPaused -> MaterialTheme.colorScheme.tertiary
            isPlaying -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.secondary
        }

    val edgeColor =
        getTracearrServerColor(
            type = session.serverType ?: if (session.instanceType == InstanceType.Tautulli) TracearrServerType.Plex else null,
            name = session.serverName,
        )

    val stateText =
        when {
            isPaused -> mokoString(MR.strings.paused)
            isPlaying -> mokoString(MR.strings.playing)
            else -> session.state?.replaceFirstChar { it.uppercase() } ?: mokoString(MR.strings.active)
        }

    val totalMs = session.totalDurationMs ?: 0L
    val initialProgressMs = session.progressMs ?: (if (totalMs > 0) (session.progressPercent * totalMs).toLong() else 0L)
    var currentProgressMs by remember(session.sessionId, session.progressMs) { mutableLongStateOf(initialProgressMs) }

    LaunchedEffect(session.sessionId, session.progressMs, isPlaying) {
        currentProgressMs = session.progressMs ?: (if (totalMs > 0) (session.progressPercent * totalMs).toLong() else 0L)
        if (isPlaying) {
            while (isActive) {
                delay(1.seconds)
                if (currentProgressMs < totalMs) {
                    currentProgressMs += 1000L
                }
            }
        }
    }

    val remainingMs = (totalMs - currentProgressMs).coerceAtLeast(0L)
    val progressFraction =
        if (totalMs > 0) {
            (currentProgressMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
        } else {
            session.progressPercent.coerceIn(0f, 1f)
        }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier =
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(edgeColor),
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
                    Box(
                        modifier =
                        Modifier
                            .width(80.dp)
                            .aspectRatio(AspectRatio.Poster.ratio)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        val imageUrl = session.posterUrl
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
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f, fill = false),
                            ) {
                                val username =
                                    session.userDisplayName.ifEmpty { session.username.ifEmpty { mokoString(MR.strings.user) } }
                                val avatarUrl = session.userAvatarUrl

                                if (!avatarUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = rememberRemoteImageData(avatarUrl, trim = false),
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
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                val isTranscoding =
                                    session.isTranscoding || session.streamDecision == PlaybackStreamDecision.Transcode

                                if (isTranscoding) {
                                    val isDark = isSystemInDarkTheme()
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFF59E0B).copy(alpha = if (isDark) 0.25f else 0.15f),
                                        modifier = Modifier.size(24.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.ElectricBolt,
                                                contentDescription = mokoString(MR.strings.transcoding),
                                                tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    }
                                }

                                PlaybackDeviceIcon(session)

                                if (session.canTerminate) {
                                    Box(
                                        modifier =
                                        Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (onTerminate != null) {
                                                    Modifier.clickable(onClick = onTerminate)
                                                } else {
                                                    Modifier
                                                },
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = mokoString(MR.strings.terminate),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }

                        val displayTitle = session.displayTitle.ifEmpty { mokoString(MR.strings.unknown) }

                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        val subtitle = session.subtitle
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        val playerDevice =
                            listOfNotNull(session.player, session.product ?: session.device, session.platform)
                                .filter { it.isNotBlank() }
                                .distinct()
                                .joinToString(" • ")
                        if (playerDevice.isNotBlank()) {
                            Text(
                                text = playerDevice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        Text(
                            text = stateText,
                            style = MaterialTheme.typography.labelSmall,
                            color = stateColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = TracearrBlue,
                            trackColor = MaterialTheme.colorScheme.surface,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = formatTimeMs(currentProgressMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Text(
                                text = "-${formatTimeMs(remainingMs)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val serverName = (session.serverName ?: session.instanceLabel).ifEmpty { mokoString(MR.strings.server) }
                    val location = session.location ?: session.ipAddress ?: mokoString(MR.strings.local_network)

                    Text(
                        text = "$serverName · $location",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    val quality = session.videoResolution ?: "1080p"
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

@Composable
private fun PlaybackDeviceIcon(session: PlaybackStreamSession) {
    val devicePlatform =
        remember(session.platform, session.player, session.device) {
            TracearrDevicePlatform.fromSession(
                platform = session.platform,
                product = session.player,
                device = session.device,
            )
        }
    val icon =
        when (devicePlatform) {
            TracearrDevicePlatform.PHONE -> Icons.Default.PhoneIphone
            TracearrDevicePlatform.TABLET -> Icons.Default.Tablet
            TracearrDevicePlatform.TV -> Icons.Default.Tv
            TracearrDevicePlatform.DESKTOP -> Icons.Default.DesktopWindows
            TracearrDevicePlatform.CONSOLE -> Icons.Default.SportsEsports
            TracearrDevicePlatform.UNKNOWN -> Icons.Default.SmartDisplay
        }

    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.size(24.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = devicePlatform.name,
                modifier = Modifier.size(14.dp),
            )
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
