package com.dnfapps.arrmatey.ui.components.mediaserver

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.mediaserver.model.MediaServerMediaDetails
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.seerr.state.MediaProvider
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.compose.painterResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaServerSpecsCard(
    details: MediaServerMediaDetails,
    onWatchClick: (String, MediaProvider, String?) -> Unit,
    onToggleWatched: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val serverColor: Color
    val iconRes: ImageResource
    when (details.serverType) {
        MediaServerType.Plex -> {
            serverColor = Color(0xFFE5A00D)
            iconRes = MR.images.plex
        }
        MediaServerType.Jellyfin -> {
            serverColor = Color(0xFF4747ED)
            iconRes = MR.images.jellyfin
        }
        MediaServerType.Emby -> {
            serverColor = Color(0xFF52B54B)
            iconRes = MR.images.emby
        }
    }
    val provider = when (details.serverType) {
        MediaServerType.Plex -> MediaProvider.Plex
        MediaServerType.Jellyfin -> MediaProvider.Jellyfin
        MediaServerType.Emby -> MediaProvider.Emby
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Server Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = details.serverType.name,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = details.serverLabel,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                // Play Button
                FilledTonalButton(
                    onClick = { onWatchClick(details.deepLinkUrl, provider, details.webFallbackUrl) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = serverColor.copy(alpha = 0.2f),
                        contentColor = serverColor,
                    ),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(mokoString(MR.strings.watch))
                }
            }

            // Playback Progress / Watched Status
            if (details.played) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ArrGreen,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = mokoString(MR.strings.watched),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ArrGreen,
                    )
                }
            } else {
                val progress = details.progressPercentage
                if (progress != null && progress > 0f) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "${(progress * 100).toInt()}% watched",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            details.remainingMinutes?.let { mins ->
                                if (mins > 0) {
                                    Text(
                                        text = "$mins min left",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(MaterialTheme.shapes.small),
                            color = serverColor,
                        )
                    }
                }
            }

            // Episode Completion Summary for Shows
            val epWatched = details.episodesWatchedCount
            val epTotal = details.totalEpisodesCount
            if (epWatched != null && epTotal != null && epTotal > 0) {
                val percent = (epWatched.toFloat() / epTotal.toFloat() * 100).toInt()
                Text(
                    text = "$epWatched / $epTotal episodes watched ($percent%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Media Specifications Chips
            val specBadges = buildList {
                details.videoResolution?.let { add(it.uppercase()) }
                details.videoCodec?.let { add(it.uppercase()) }
                details.audioCodec?.let { add(it.uppercase()) }
                details.container?.let { add(it.uppercase()) }
                details.bitrate?.let { br ->
                    if (br > 0) {
                        val mbps = br / 1_000_000f
                        add("${(mbps * 10).toInt() / 10f} Mbps")
                    }
                }
            }

            if (specBadges.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    specBadges.forEach { badge ->
                        ElevatedAssistChip(
                            onClick = {},
                            label = { Text(badge, style = MaterialTheme.typography.labelSmall) },
                            colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                        )
                    }
                }
            }

            // Toggle Watched Action
            OutlinedButton(
                onClick = { onToggleWatched(!details.played) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = if (details.played) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(if (details.played) "Mark as Unwatched" else "Mark as Watched")
            }
        }
    }
}
