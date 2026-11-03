package com.dnfapps.arrmatey.ui.components.mediaserver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.mediaserver.model.MediaServerMediaDetails
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.seerr.state.MediaProvider
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun MediaServerSourceChips(
    details: List<MediaServerMediaDetails>,
    onWatchClick: ((String, MediaProvider, String?) -> Unit)? = null,
    onToggleWatched: ((MediaServerType, String, Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (details.isEmpty()) return

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        details.forEach { detail ->
            val provider = when (detail.serverType) {
                MediaServerType.Plex -> MediaProvider.Plex
                MediaServerType.Jellyfin -> MediaProvider.Jellyfin
                MediaServerType.Emby -> MediaProvider.Emby
            }

            val isMovie = detail.totalEpisodesCount == null
            val (statusText, statusColor) = when {
                detail.played -> mokoString(MR.strings.watched) to ArrGreen
                detail.progressPercentage != null && detail.progressPercentage!! > 0f -> {
                    val percent = (detail.progressPercentage!! * 100).toInt()
                    "$percent%" to MaterialTheme.colorScheme.tertiary
                }
                detail.totalEpisodesCount != null && (detail.episodesWatchedCount ?: 0) > 0 -> {
                    val watched = detail.episodesWatchedCount ?: 0
                    val total = detail.totalEpisodesCount ?: 0
                    "$watched/$total" to ArrGreen
                }
                else -> mokoString(MR.strings.available) to MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            ) {
                Row(
                    modifier = Modifier.padding(start = 10.dp, end = if (onToggleWatched != null && isMovie) 4.dp else 10.dp, top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.clickable(enabled = onWatchClick != null) {
                            onWatchClick?.invoke(detail.deepLinkUrl, provider, detail.webFallbackUrl)
                        },
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(detail.serverType.icon),
                            contentDescription = detail.serverType.name,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = detail.serverLabel,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = "• $statusText",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                        )
                    }

                    if (onToggleWatched != null && isMovie) {
                        IconButton(
                            onClick = { onToggleWatched(detail.serverType, detail.itemId, !detail.played) },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = if (detail.played) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                contentDescription = if (detail.played) mokoString(MR.strings.watched) else mokoString(MR.strings.unwatched),
                                tint = if (detail.played) ArrGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
