package com.dnfapps.arrmatey.ui.screens.monitoring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.dnfapps.arrmatey.monitoring.model.PlaybackMediaType
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamDecision
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamSession
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.mokoString
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackStreamDetailsSheet(
    session: PlaybackStreamSession,
    onDismissRequest: () -> Unit,
    onTerminateStream: ((message: String?) -> Unit)? = null,
    onNavigateToDetails: ((type: PlaybackMediaType?, tmdbId: Long?) -> Unit)? = null,
    onNavigateToUser: ((userRef: String) -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showTerminateDialog by remember { mutableStateOf(false) }
    var terminateMessage by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    val isTranscoding = session.isTranscoding || session.streamDecision == PlaybackStreamDecision.Transcode
    val decisionBadgeText =
        when {
            isTranscoding -> mokoString(MR.strings.transcode)
            session.streamDecision == PlaybackStreamDecision.DirectPlay -> mokoString(MR.strings.direct_play)
            session.streamDecision == PlaybackStreamDecision.DirectStream -> mokoString(MR.strings.direct_stream)
            else -> session.streamDecision.name.replaceFirstChar { it.uppercase() }
        }

    val badgeContainerColor =
        if (isTranscoding) {
            Color(0xFFF59E0B).copy(alpha = if (isDark) 0.25f else 0.15f)
        } else {
            Color(0xFF4CAF50).copy(alpha = if (isDark) 0.25f else 0.15f)
        }
    val badgeContentColor =
        if (isTranscoding) {
            if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
        } else {
            if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
        }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(enabled = session.tmdbId != null && onNavigateToDetails != null) {
                        onDismissRequest()
                        onNavigateToDetails?.invoke(session.mediaType, session.tmdbId)
                    },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier =
                    Modifier
                        .width(72.dp)
                        .aspectRatio(AspectRatio.Poster.ratio)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center,
                ) {
                    val imageUrl = session.posterUrl
                    if (!imageUrl.isNullOrBlank()) {
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
                    Text(
                        text = session.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )

                    val yearText = session.year ?: session.subtitle
                    if (!yearText.isNullOrBlank()) {
                        Text(
                            text = yearText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = badgeContainerColor,
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Text(
                            text = decisionBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeContentColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = mokoString(MR.strings.session),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )

                val username = session.userDisplayName.ifEmpty { session.username.ifEmpty { mokoString(MR.strings.user) } }
                SheetDetailRow(
                    label = mokoString(MR.strings.user),
                    value = username,
                    onClick = if (onNavigateToUser != null && session.username.isNotBlank()) {
                        {
                            onDismissRequest()
                            onNavigateToUser(session.username)
                        }
                    } else {
                        null
                    },
                )

                val stateText =
                    when {
                        session.state?.equals("paused", ignoreCase = true) == true -> mokoString(MR.strings.paused)
                        session.state?.equals("playing", ignoreCase = true) == true -> mokoString(MR.strings.playing)
                        else -> session.state?.replaceFirstChar { it.uppercase() } ?: mokoString(MR.strings.active)
                    }
                SheetDetailRow(label = mokoString(MR.strings.state), value = stateText)

                val playerText =
                    listOfNotNull(session.player, session.product ?: session.device, session.platform)
                        .filter { it.isNotBlank() }
                        .distinct()
                        .joinToString(" • ")
                        .ifBlank { mokoString(MR.strings.unknown) }
                SheetDetailRow(label = mokoString(MR.strings.player), value = playerText)

                val qualityText = session.qualityProfile ?: session.videoResolution ?: mokoString(MR.strings.original)
                SheetDetailRow(label = mokoString(MR.strings.quality), value = qualityText)

                val locationText = session.location?.uppercase() ?: session.ipAddress ?: mokoString(MR.strings.local_network)
                SheetDetailRow(label = mokoString(MR.strings.location), value = locationText)

                val progressText = "${(session.progressPercent * 100).toInt()}%"
                SheetDetailRow(label = mokoString(MR.strings.progress), value = progressText)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = mokoString(MR.strings.stream),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )

                SheetDetailPillRow(
                    label = mokoString(MR.strings.decision),
                    pillText = decisionBadgeText,
                    containerColor = badgeContainerColor,
                    contentColor = badgeContentColor,
                )

                val videoDetails =
                    listOfNotNull(session.transcodeVideoCodec ?: session.videoCodec, session.videoResolution)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                val videoLabel =
                    if (videoDetails.isNotBlank()) {
                        "$decisionBadgeText ($videoDetails)"
                    } else {
                        decisionBadgeText
                    }
                SheetDetailPillRow(
                    label = mokoString(MR.strings.video),
                    pillText = videoLabel,
                    containerColor = badgeContainerColor,
                    contentColor = badgeContentColor,
                )

                val audioCodec = session.transcodeAudioCodec ?: session.audioCodec
                val audioLabel =
                    if (!audioCodec.isNullOrBlank()) {
                        "$decisionBadgeText ($audioCodec)"
                    } else {
                        decisionBadgeText
                    }
                SheetDetailPillRow(
                    label = mokoString(MR.strings.audio),
                    pillText = audioLabel,
                    containerColor = badgeContainerColor,
                    contentColor = badgeContentColor,
                )

                val containerText = session.container ?: "mp4"
                SheetDetailRow(label = mokoString(MR.strings.container), value = containerText)

                val bandwidthText =
                    session.bandwidthKbps?.let { bw ->
                        if (bw >= 1000) {
                            "${(bw / 1000f).let { String.format(Locale.US, "%.1f", it) }} Mbps"
                        } else {
                            "$bw Kbps"
                        }
                    } ?: "-"
                SheetDetailRow(label = mokoString(MR.strings.bandwidth), value = bandwidthText)
            }

            if (session.canTerminate && onTerminateStream != null) {
                Button(
                    onClick = { showTerminateDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = mokoString(MR.strings.terminate_stream),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    if (showTerminateDialog) {
        AlertDialog(
            onDismissRequest = { showTerminateDialog = false },
            title = {
                Text(
                    text = mokoString(MR.strings.terminate_stream_dialog_title),
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = mokoString(MR.strings.terminate_stream_dialog_message))
                    OutlinedTextField(
                        value = terminateMessage,
                        onValueChange = { terminateMessage = it },
                        label = { Text(mokoString(MR.strings.message_optional)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTerminateDialog = false
                        onTerminateStream?.invoke(terminateMessage.takeIf { it.isNotBlank() })
                        onDismissRequest()
                    },
                    colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(text = mokoString(MR.strings.terminate_stream_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTerminateDialog = false }) {
                    Text(text = mokoString(MR.strings.cancel))
                }
            },
        )
    }
}

@Composable
private fun SheetDetailRow(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SheetDetailPillRow(
    label: String,
    pillText: String,
    containerColor: Color,
    contentColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp),
        )
        Surface(
            shape = CircleShape,
            color = containerColor,
        ) {
            Text(
                text = pillText,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            )
        }
    }
}
