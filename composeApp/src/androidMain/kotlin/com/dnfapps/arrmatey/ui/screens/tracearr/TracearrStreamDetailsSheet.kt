package com.dnfapps.arrmatey.ui.screens.tracearr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrDevicePlatform
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaType
import com.dnfapps.arrmatey.tracearr.api.model.TracearrServerType
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamDecision
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrYellow
import com.dnfapps.arrmatey.ui.theme.TracearrBlue
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracearrStreamDetailsSheet(
    session: TracearrStreamSession,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            isPaused -> mokoString(MR.strings.paused)
            isPlaying -> mokoString(MR.strings.playing)
            else -> session.state?.replaceFirstChar { it.uppercase() } ?: mokoString(MR.strings.active)
        }

    val totalMs = session.totalDurationMs ?: session.durationMs ?: 0L
    val initialProgressMs = session.progressMs ?: 0L
    var currentProgressMs by remember(session.id, session.progressMs) { mutableLongStateOf(initialProgressMs) }

    val initialPausedMs = remember(session.pausedDurationMs, session.lastPausedAt, isPaused) {
        val base = session.pausedDurationMs ?: 0L
        val lastPaused = session.lastPausedAt?.toEpochMilliseconds()
        if (isPaused && lastPaused != null) {
            val now = System.currentTimeMillis()
            base + (now - lastPaused).coerceAtLeast(0L)
        } else {
            base
        }
    }
    var currentPausedMs by remember(session.id, initialPausedMs) { mutableLongStateOf(initialPausedMs) }

    val initialWatchTimeMs = remember(session.startedAt, initialPausedMs) {
        val startedMs = session.startedAt?.toEpochMilliseconds()
        if (startedMs != null) {
            val now = System.currentTimeMillis()
            val totalElapsed = (now - startedMs).coerceAtLeast(0L)
            (totalElapsed - initialPausedMs).coerceAtLeast(0L)
        } else {
            session.progressMs ?: 0L
        }
    }
    var currentWatchTimeMs by remember(session.id, initialWatchTimeMs) { mutableLongStateOf(initialWatchTimeMs) }

    LaunchedEffect(session.id, session.progressMs, isPlaying, isPaused) {
        currentProgressMs = session.progressMs ?: 0L
        while (isActive) {
            delay(1.seconds)
            if (isPlaying) {
                if (currentProgressMs < totalMs) {
                    currentProgressMs += 1000L
                }
                currentWatchTimeMs += 1000L
            } else if (isPaused) {
                currentPausedMs += 1000L
            }
        }
    }

    val progressFraction =
        if (totalMs > 0) {
            (currentProgressMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    val progressPercent = (progressFraction * 100).roundToInt()

    val serverType = session.server?.type ?: session.serverType
    val serverColor =
        when (serverType) {
            TracearrServerType.Plex -> Color(0xFFE5A00D)
            TracearrServerType.Jellyfin -> Color(0xFFAA5CC3)
            TracearrServerType.Emby -> Color(0xFF52B54B)
            null -> {
                val name = (session.server?.name ?: session.serverName ?: "").lowercase()
                when {
                    name.contains("plex") -> Color(0xFFE5A00D)
                    name.contains("jellyfin") -> Color(0xFFAA5CC3)
                    name.contains("emby") -> Color(0xFF52B54B)
                    else -> TracearrBlue
                }
            }
        }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = stateColor,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = mokoString(MR.strings.session_details),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    shape = CircleShape,
                    color = stateColor.copy(alpha = 0.2f),
                ) {
                    Text(
                        text = stateText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = stateColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                IconButton(onClick = onDismissRequest, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = mokoString(MR.strings.close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            ContainerCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(70.dp)
                                .aspectRatio(AspectRatio.Poster.ratio)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        val imageUrl = session.posterUrl ?: session.thumbPath
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
                        val typeLabel =
                            when (session.mediaType) {
                                TracearrMediaType.Episode -> mokoString(MR.strings.episode)
                                TracearrMediaType.Movie -> mokoString(MR.strings.movie_singular)
                                TracearrMediaType.Track -> mokoString(MR.strings.track)
                                else -> session.mediaType?.name ?: mokoString(MR.strings.media)
                            }
                        val yearText = session.year?.let { " · $it" } ?: ""
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = if (session.mediaType == TracearrMediaType.Movie) Icons.Default.Movie else Icons.Default.Tv,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "$typeLabel$yearText",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        val displayTitle =
                            session.grandparentTitle
                                ?: session.showTitle
                                ?: session.mediaTitle
                                ?: mokoString(MR.strings.unknown)
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        val subtitle =
                            when {
                                session.mediaType == TracearrMediaType.Episode || (session.seasonNumber != null && session.episodeNumber != null) -> {
                                    val seasonStr = session.seasonNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                                    val episodeStr = session.episodeNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                                    "S$seasonStr E$episodeStr · ${session.mediaTitle ?: ""}"
                                }
                                session.mediaType == TracearrMediaType.Movie -> session.mediaTitle ?: ""
                                else -> listOfNotNull(session.artistName, session.albumName, session.mediaTitle).joinToString(" · ")
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                color = TracearrBlue,
                                trackColor = MaterialTheme.colorScheme.surface,
                            )
                            Text(
                                text = "$progressPercent%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            val username = session.effectiveUsername.ifEmpty { mokoString(MR.strings.user) }
            ContainerCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val avatarUrl = session.effectiveUserAvatar
                    if (!avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = rememberRemoteImageData(avatarUrl, trim = false),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = ArrOrange,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = username.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = username,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = mokoString(MR.strings.view_profile),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            SectionCard(
                icon = Icons.Default.Dns,
                title = mokoString(MR.strings.server),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = mokoString(MR.strings.server),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val serverTypeName = serverType?.name ?: session.serverName ?: "Server"
                        Text(
                            text = serverTypeName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = serverColor,
                        )
                        val sName = session.effectiveServerName
                        if (sName.isNotEmpty()) {
                            Text(
                                text = "· $sName",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            SectionCard(
                icon = Icons.Default.Schedule,
                title = mokoString(MR.strings.playback),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val startedText = formatStartedAt(session.startedAt)
                    if (startedText.isNotBlank()) {
                        DetailRow(
                            label = mokoString(MR.strings.started),
                            value = startedText,
                        )
                    }
                    DetailRow(
                        label = mokoString(MR.strings.watch_time),
                        value = formatDetailedDuration(currentWatchTimeMs),
                    )
                    if (currentPausedMs > 0L || isPaused) {
                        DetailRow(
                            label = mokoString(MR.strings.paused),
                            value = formatDetailedDuration(currentPausedMs),
                        )
                    }
                    DetailRow(
                        label = mokoString(MR.strings.media_length),
                        value = formatDetailedDuration(totalMs, hideSecondsIfHours = true),
                    )
                }
            }

            SectionCard(
                icon = Icons.Default.LocationOn,
                title = mokoString(MR.strings.location),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val ip = session.ipAddress ?: mokoString(MR.strings.unknown)
                    DetailRow(
                        label = mokoString(MR.strings.ip_address),
                        value = ip,
                    )
                    val locationText = listOfNotNull(session.geoCity, session.geoRegion, session.geoCountry)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                        .ifEmpty { mokoString(MR.strings.local_network) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = locationText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            val devicePlatform = remember(session) {
                TracearrDevicePlatform.fromSession(
                    session.platform,
                    session.product,
                    session.device,
                )
            }
            val deviceCardIcon = when (devicePlatform) {
                TracearrDevicePlatform.PHONE -> Icons.Default.PhoneIphone
                TracearrDevicePlatform.TABLET -> Icons.Default.Tablet
                TracearrDevicePlatform.TV -> Icons.Default.Tv
                TracearrDevicePlatform.DESKTOP -> Icons.Default.DesktopWindows
                TracearrDevicePlatform.CONSOLE -> Icons.Default.SportsEsports
                TracearrDevicePlatform.UNKNOWN -> Icons.Default.SmartDisplay
            }

            SectionCard(
                icon = deviceCardIcon,
                title = mokoString(MR.strings.device),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    session.platform?.let { DetailRow(label = mokoString(MR.strings.platform), value = it) }
                    session.product?.let { DetailRow(label = mokoString(MR.strings.product), value = it) }
                    session.device?.let { DetailRow(label = mokoString(MR.strings.device), value = it) }
                    val player = session.player ?: session.playerName
                    player?.let { DetailRow(label = mokoString(MR.strings.player), value = it) }
                    session.deviceId?.let { DetailRow(label = mokoString(MR.strings.device_id), value = it, ellipsize = true) }
                }
            }

            val isStreamTranscode = session.isTranscode == true ||
                session.videoDecision == TracearrStreamDecision.Transcode ||
                session.audioDecision == TracearrStreamDecision.Transcode
            val streamBadge = if (isStreamTranscode) mokoString(MR.strings.transcode) else mokoString(MR.strings.direct_play)
            val streamBadgeColor = if (isStreamTranscode) ArrYellow else Color(0xFF4CAF50)

            SectionCard(
                icon = Icons.Default.Speed,
                title = mokoString(MR.strings.stream_details),
                trailingBadge = { DecisionBadge(text = streamBadge, color = streamBadgeColor) },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val srcContainer = session.transcodeInfo?.sourceContainer ?: "MKV"
                    val dstContainer = session.transcodeInfo?.streamContainer ?: srcContainer
                    DetailComparisonRow(
                        label = mokoString(MR.strings.container),
                        source = srcContainer,
                        stream = dstContainer,
                    )
                    val bitrateVal = session.bitrate ?: session.sourceVideoDetails?.bitrate?.toInt()
                    bitrateVal?.let {
                        DetailRow(
                            label = mokoString(MR.strings.bitrate),
                            value = formatBitrate(it.toDouble()),
                        )
                    }
                }
            }

            val isVideoTranscode = session.videoDecision == TracearrStreamDecision.Transcode
            val videoBadge = if (isVideoTranscode) mokoString(MR.strings.transcode) else mokoString(MR.strings.direct_play)
            val videoBadgeColor = if (isVideoTranscode) ArrYellow else Color(0xFF4CAF50)

            SectionCard(
                icon = Icons.Default.Videocam,
                title = mokoString(MR.strings.video),
                trailingBadge = { DecisionBadge(text = videoBadge, color = videoBadgeColor) },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailHeaderRow()

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    val srcCodec = session.sourceVideoCodecDisplay ?: session.sourceVideoCodec ?: "H.264"
                    val dstCodec = session.streamVideoCodecDisplay ?: session.streamVideoCodec ?: srcCodec
                    DetailComparisonRow(label = mokoString(MR.strings.codec), source = srcCodec, stream = dstCodec)

                    val srcRes = if (session.sourceVideoWidth != null && session.sourceVideoHeight != null) {
                        "${session.sourceVideoWidth}×${session.sourceVideoHeight} (${session.resolution ?: "1080p"})"
                    } else {
                        session.resolution ?: "1080p"
                    }
                    val streamVideoDetails = session.streamVideoDetails
                    val streamWidth = streamVideoDetails?.width?.toInt()
                    val streamHeight = streamVideoDetails?.height?.toInt()
                    val dstRes = if (streamWidth != null && streamHeight != null) {
                        "${streamWidth}×${streamHeight} (${session.resolution ?: "1080p"})"
                    } else {
                        srcRes
                    }
                    DetailComparisonRow(label = mokoString(MR.strings.resolution), source = srcRes, stream = dstRes)

                    val srcBitrate = session.sourceVideoDetails?.bitrate?.let { formatBitrate(it) } ?: ""
                    val dstBitrate = streamVideoDetails?.bitrate?.let { formatBitrate(it) } ?: srcBitrate
                    if (srcBitrate.isNotBlank()) {
                        DetailComparisonRow(label = mokoString(MR.strings.bitrate), source = srcBitrate, stream = dstBitrate)
                    }

                    val srcFramerate = session.sourceVideoDetails?.framerate ?: ""
                    val dstFramerate = streamVideoDetails?.framerate ?: srcFramerate
                    if (srcFramerate.isNotBlank()) {
                        DetailComparisonRow(label = mokoString(MR.strings.framerate), source = srcFramerate, stream = dstFramerate)
                    }

                    val srcHdr = session.sourceVideoDetails?.dynamicRange ?: "SDR"
                    val dstHdr = streamVideoDetails?.dynamicRange ?: srcHdr
                    DetailComparisonRow(label = mokoString(MR.strings.hdr), source = srcHdr, stream = dstHdr)

                    session.sourceVideoDetails?.profile?.let {
                        DetailRow(label = mokoString(MR.strings.profile), value = it)
                    }

                    val colorInfo = listOfNotNull(
                        session.sourceVideoDetails?.colorSpace,
                        session.sourceVideoDetails?.colorDepth?.let { "${it}bit" },
                    ).joinToString(" ")
                    if (colorInfo.isNotBlank()) {
                        DetailRow(label = mokoString(MR.strings.color), value = colorInfo)
                    }
                }
            }

            val isAudioTranscode = session.audioDecision == TracearrStreamDecision.Transcode
            val audioBadge = if (isAudioTranscode) mokoString(MR.strings.transcode) else mokoString(MR.strings.direct_play)
            val audioBadgeColor = if (isAudioTranscode) ArrYellow else Color(0xFF4CAF50)

            SectionCard(
                icon = Icons.Default.GraphicEq,
                title = mokoString(MR.strings.audio),
                trailingBadge = { DecisionBadge(text = audioBadge, color = audioBadgeColor) },
            ) {
                val streamAudioDetails = session.streamAudioDetails
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailHeaderRow()

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    val srcAudioCodec = session.sourceAudioCodecDisplay ?: session.sourceAudioCodec ?: "EAC3"
                    val dstAudioCodec = session.streamAudioCodecDisplay ?: session.streamAudioCodec ?: srcAudioCodec
                    DetailComparisonRow(label = mokoString(MR.strings.codec), source = srcAudioCodec, stream = dstAudioCodec)

                    val srcChannels = session.audioChannelsDisplay ?: (session.sourceAudioChannels?.let { if (it == 2) "Stereo" else "$it Channels" } ?: "Stereo")
                    val streamChannelsNum = streamAudioDetails?.channels?.toInt()
                    val dstChannels = if (streamChannelsNum != null) {
                        if (streamChannelsNum == 2) "Stereo" else "$streamChannelsNum Channels"
                    } else {
                        srcChannels
                    }
                    DetailComparisonRow(label = mokoString(MR.strings.channels), source = srcChannels, stream = dstChannels)

                    val srcAudioBitrate = session.sourceAudioDetails?.bitrate?.let { formatBitrate(it) } ?: ""
                    val dstAudioBitrate = streamAudioDetails?.bitrate?.let { formatBitrate(it) } ?: srcAudioBitrate
                    if (srcAudioBitrate.isNotBlank()) {
                        DetailComparisonRow(label = mokoString(MR.strings.bitrate), source = srcAudioBitrate, stream = dstAudioBitrate)
                    }

                    val srcLang = session.sourceAudioDetails?.language ?: ""
                    val dstLang = streamAudioDetails?.language ?: srcLang
                    if (srcLang.isNotBlank()) {
                        DetailComparisonRow(label = mokoString(MR.strings.language), source = srcLang, stream = dstLang)
                    }

                    session.sourceAudioDetails?.sampleRate?.let {
                        DetailRow(label = mokoString(MR.strings.sample_rate), value = "${(it / 1000).toInt()} kHz")
                    }
                }
            }

            session.subtitleInfo?.let { sub ->
                SectionCard(
                    icon = Icons.Default.Subtitles,
                    title = mokoString(MR.strings.subtitle),
                ) {
                    val subFormat = listOfNotNull(sub.codec, sub.language).joinToString(" · ")
                    DetailRow(label = mokoString(MR.strings.subtitle_format), value = subFormat)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    icon: ImageVector,
    title: String,
    trailingBadge: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    ContainerCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TracearrBlue,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                trailingBadge?.invoke()
            }
            content()
        }
    }
}

@Composable
private fun DecisionBadge(
    text: String,
    color: Color,
) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.2f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun DetailHeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.weight(1.2f))
        Text(
            text = mokoString(MR.strings.source_header),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier.width(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Spacer matching arrow width
        }
        Text(
            text = mokoString(MR.strings.stream_header),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    ellipsize: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            maxLines = if (ellipsize) 1 else Int.MAX_VALUE,
            overflow = if (ellipsize) TextOverflow.Ellipsis else TextOverflow.Clip,
            modifier = if (ellipsize) Modifier.weight(1f, fill = false).padding(start = 16.dp) else Modifier,
        )
    }
}

@Composable
private fun DetailComparisonRow(
    label: String,
    source: String,
    stream: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1.2f),
        )
        Text(
            text = source,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier.width(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "→",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = stream,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
    }
}

private fun String?.isNullOrRelative(): Boolean {
    if (this.isNullOrEmpty()) return true
    return !this.startsWith("http://") && !this.startsWith("https://")
}

@Composable
private fun formatStartedAt(instant: Instant?): String {
    if (instant == null) return ""

    val locale = LocalConfiguration.current.locales[0]
    val justNow = mokoString(MR.strings.just_now)
    val minuteAgo = mokoString(MR.strings.minute_ago)
    val hourAgo = mokoString(MR.strings.hour_ago)

    val ms = instant.toEpochMilliseconds()
    val nowMs = System.currentTimeMillis()
    val minutesAgo = (nowMs - ms) / 60000L

    val relative = when {
        minutesAgo < 1 -> justNow
        minutesAgo == 1L -> minuteAgo
        minutesAgo < 60 -> mokoString(MR.strings.minutes_ago, minutesAgo)
        minutesAgo < 120 -> hourAgo
        else -> mokoString(MR.strings.hours_ago, minutesAgo / 60)
    }

    val formattedDate = try {
        val date = Date(ms)
        val dateFormatter = SimpleDateFormat("MMM d, h:mm a", locale)
        dateFormatter.format(date)
    } catch (e: Exception) {
        ""
    }

    return if (formattedDate.isNotEmpty()) "$formattedDate ($relative)" else relative
}

private fun formatDetailedDuration(ms: Long, hideSecondsIfHours: Boolean = false): String {
    if (ms <= 0) return "0s"
    val totalSeconds = ms / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return when {
        hours > 0 -> if (hideSecondsIfHours) "${hours}h ${minutes}m" else "${hours}h ${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

private fun formatBitrate(kbps: Double?): String {
    if (kbps == null || kbps <= 0) return ""
    return if (kbps >= 1000) {
        val mbps = kbps / 1000.0
        val rounded = (mbps * 10.0).roundToInt() / 10.0
        "$rounded Mbps"
    } else {
        "${kbps.toInt()} kbps"
    }
}
