package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.entensions.bullet
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaType
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.ui.theme.getTracearrServerColor
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun DashboardActiveStreamsSection(
    state: CombinedDashboardState.Success,
    isEditing: Boolean,
    enabled: Boolean = true,
    onItemClick: (TracearrStreamSession) -> Unit = {},
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
        val streams = state.activeStreams
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.PlayArrow,
                    null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = mokoString(MR.strings.active_streams),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (streams.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.no_active_streams),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            streams.take(5).forEach { session ->
                val displayTitle =
                    session.grandparentTitle
                        ?: session.showTitle
                        ?: session.mediaTitle
                        ?: session.channelTitle
                        ?: mokoString(MR.strings.unknown)

                val isEpisode =
                    session.mediaType == TracearrMediaType.Episode ||
                        (session.seasonNumber != null && session.episodeNumber != null)
                val episodeInfo =
                    if (isEpisode) {
                        val seasonStr = session.seasonNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                        val episodeStr = session.episodeNumber?.let { if (it < 10) "0$it" else "$it" } ?: "00"
                        val epTitle = session.mediaTitle
                        if (!epTitle.isNullOrBlank() && epTitle != displayTitle) {
                            "S${seasonStr}E$episodeStr • $epTitle"
                        } else {
                            "S${seasonStr}E$episodeStr"
                        }
                    } else {
                        null
                    }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.clickable(enabled = !isEditing && enabled) { onItemClick(session) },
                ) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                getTracearrServerColor(
                                    session.server?.type ?: session.serverType,
                                    session.server?.name ?: session.serverName,
                                ),
                            ),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            displayTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        if (episodeInfo != null) {
                            Text(
                                episodeInfo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        val statusRow =
                            buildString {
                                if (session.effectiveUsername.isNotBlank()) {
                                    append(session.effectiveUsername)
                                }
                                val stateStr =
                                    when {
                                        session.state?.equals("paused", ignoreCase = true) == true -> mokoString(MR.strings.paused)
                                        session.state?.equals("playing", ignoreCase = true) == true -> mokoString(MR.strings.playing)
                                        else -> session.state?.replaceFirstChar { it.uppercase() }
                                    }
                                if (!stateStr.isNullOrBlank()) {
                                    if (isNotEmpty()) bullet()
                                    append(stateStr)
                                }
                                val quality = session.quality ?: session.resolution
                                if (!quality.isNullOrBlank()) {
                                    if (isNotEmpty()) bullet()
                                    append(quality)
                                }
                            }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            session.effectiveServerName.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    "$it • ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Text(
                                statusRow,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    val totalMs = session.totalDurationMs ?: session.durationMs ?: 0L
                    val progressMs = session.progressMs ?: 0L
                    if (totalMs > 0 && progressMs > 0) {
                        val percent = ((progressMs.toFloat() / totalMs.toFloat()) * 100).toInt().coerceIn(0, 100)
                        Text(
                            "$percent%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            if (streams.size > 5) {
                Text(
                    mokoString(
                        MR.strings.additional_items_count,
                        streams.size - 5,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}
