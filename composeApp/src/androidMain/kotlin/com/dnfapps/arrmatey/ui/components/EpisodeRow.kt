package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.BULLET
import com.dnfapps.arrmatey.extensions.isToday
import com.dnfapps.arrmatey.extensions.isTodayOrAfter
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.model.EpisodeWrapper
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.bazarr.EpisodeSubtitlesRow
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrLightPurple
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.arr.api.model.Episode as ArrEpisode

@Composable
fun EpisodeRow(
    episode: EpisodeWrapper,
    modifier: Modifier = Modifier,
    searchInProgress: (Long) -> Boolean = { false },
    onAutomaticSearch: (Long) -> Unit = {},
    onToggleMonitor: (ArrEpisode) -> Unit = {},
    onNavigateToSeriesRelease: (Long?) -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    bazarrDetailsIntegration: Boolean = true,
    isMediaServerWatched: Boolean = false,
    episodeDeepLink: String? = null,
    mediaServerType: MediaServerType? = null,
    onToggleMediaServerWatched: ((Boolean) -> Unit)? = null,
    onPlayEpisode: ((String) -> Unit)? = null,
) {
    val arrEp = episode.arrEpisode
    val effectiveMediaServerType = mediaServerType ?: episode.mediaServerEpisode?.serverType
    val stillUrl = episode.stillPath?.takeUnless { it.isBlank() }
    val epOverview = episode.overview?.takeUnless { it.isBlank() }

    val hasArrActions = arrEp != null
    val hasMediaServerActions = (episodeDeepLink != null && onPlayEpisode != null) || onToggleMediaServerWatched != null
    val hasActions = hasArrActions || hasMediaServerActions
    val hasSubtitles = bazarrDetailsIntegration && episode.bazarrEpisode != null
    val hasImage = stillUrl != null

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier =
        modifier
            .padding(vertical = 4.dp)
            .combinedClickable(
                enabled = onClick != null || onLongClick != null,
                onClick = onClick ?: {},
                onLongClick = onLongClick,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("${episode.episodeNumber}. ")
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append(episode.title ?: "")
                    }
                },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )

            episode.finaleType?.let { finalType ->
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Text(
                        text = mokoString(finalType.resource),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }

        val airDate = episode.airDate?.takeIf { it.isTodayOrAfter() }
        val (statusText, statusColor) =
            when {
                episode.isActive && episode.activityProgress != null -> episode.activityProgress!! to ArrLightPurple
                episode.fileQualityName != null -> episode.fileQualityName!! to MaterialTheme.colorScheme.tertiary
                airDate != null -> mokoString(MR.strings.unaired) to MaterialTheme.colorScheme.secondary
                arrEp != null -> mokoString(MR.strings.missing) to MaterialTheme.colorScheme.error
                else -> null to Color.Unspecified
            }

        val fileSizeString = arrEp?.episodeFile?.size?.bytesAsFileSizeString()
        val formattedDate = episode.formatAirDateUtc() ?: episode.airDate?.format()

        val metadataString =
            buildAnnotatedString {
                if (statusText != null) {
                    withStyle(SpanStyle(color = statusColor, fontStyle = FontStyle.Italic)) {
                        append(statusText)
                    }
                }
                if (fileSizeString != null) {
                    if (length > 0) append(BULLET)
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append(fileSizeString)
                    }
                }
                if (formattedDate != null) {
                    if (length > 0) append(BULLET)
                    val dateColor =
                        if (episode.airDate?.isToday() == true) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    val dateWeight = if (episode.airDate?.isToday() == true) FontWeight.Medium else FontWeight.Normal
                    withStyle(SpanStyle(color = dateColor, fontWeight = dateWeight)) {
                        append(formattedDate)
                    }
                }
                val runtime = arrEp?.runtimeString
                if (runtime != null) {
                    if (length > 0) append(BULLET)
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append(runtime)
                    }
                }
            }

        if (metadataString.isNotEmpty()) {
            Text(
                text = metadataString,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (hasImage) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier =
                    Modifier
                        .height(72.dp)
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    AsyncImage(
                        model = rememberRemoteImageData(stillUrl),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                    )
                    if (isMediaServerWatched) {
                        Box(
                            modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(18.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Watched",
                                tint = ArrGreen,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }

                if (epOverview != null) {
                    Text(
                        text = epOverview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        } else if (epOverview != null) {
            Text(
                text = epOverview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (hasSubtitles) {
            episode.bazarrEpisode?.let { bazarrEp ->
                EpisodeSubtitlesRow(
                    bazarrEpisode = bazarrEp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (hasActions) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                EpisodeActions(
                    arrEp = arrEp,
                    isMediaServerWatched = isMediaServerWatched,
                    effectiveMediaServerType = effectiveMediaServerType,
                    episodeDeepLink = episodeDeepLink,
                    searchInProgress = searchInProgress,
                    onPlayEpisode = onPlayEpisode,
                    onToggleMediaServerWatched = onToggleMediaServerWatched,
                    onNavigateToSeriesRelease = onNavigateToSeriesRelease,
                    onAutomaticSearch = onAutomaticSearch,
                    onToggleMonitor = onToggleMonitor,
                )
            }
        }
    }
}

@Composable
private fun RowScope.EpisodeActions(
    arrEp: ArrEpisode?,
    isMediaServerWatched: Boolean,
    effectiveMediaServerType: MediaServerType?,
    episodeDeepLink: String?,
    searchInProgress: (Long) -> Boolean,
    onPlayEpisode: ((String) -> Unit)?,
    onToggleMediaServerWatched: ((Boolean) -> Unit)?,
    onNavigateToSeriesRelease: (Long?) -> Unit,
    onAutomaticSearch: (Long) -> Unit,
    onToggleMonitor: (ArrEpisode) -> Unit,
) {
    if (episodeDeepLink != null && onPlayEpisode != null) {
        IconButton(
            onClick = { onPlayEpisode(episodeDeepLink) },
            modifier = Modifier.weight(1f),
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = effectiveMediaServerType?.brandColor ?: MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
    if (onToggleMediaServerWatched != null) {
        IconButton(
            onClick = { onToggleMediaServerWatched(!isMediaServerWatched) },
            modifier = Modifier.weight(1f),
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            Icon(
                imageVector = if (isMediaServerWatched) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                contentDescription = if (isMediaServerWatched) "Mark Unwatched" else "Mark Watched",
                tint = if (isMediaServerWatched) ArrGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp),
            )
        }
    }

    // Arr Actions
    if (arrEp != null) {
        IconButton(
            onClick = {
                onNavigateToSeriesRelease(arrEp.id)
            },
            modifier = Modifier.weight(1f),
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Releases",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(
            onClick = {
                onAutomaticSearch(arrEp.id)
            },
            enabled = arrEp.monitored && !searchInProgress(arrEp.id),
            modifier = Modifier.weight(1f),
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            if (searchInProgress(arrEp.id)) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint =
                    if (arrEp.monitored) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.38f,
                        )
                    },
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        IconButton(
            onClick = {
                onToggleMonitor(arrEp)
            },
            modifier = Modifier.weight(1f),
            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            AnimatedContent(
                targetState = arrEp.monitored,
                transitionSpec = {
                    (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
                },
                label = "EpisodeBookmarkIconAnimation",
            ) { monitored ->
                Icon(
                    imageVector =
                    if (monitored) {
                        Icons.Default.Bookmark
                    } else {
                        Icons.Default.BookmarkBorder
                    },
                    contentDescription = if (monitored) "Monitored" else "Unmonitored",
                    tint =
                    if (monitored) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.6f,
                        )
                    },
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
