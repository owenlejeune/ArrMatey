package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import com.dnfapps.arrmatey.shared.MR
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.ui.components.bazarr.EpisodeSubtitlesRow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.api.model.Episode as ArrEpisode
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.entensions.bullet
import com.dnfapps.arrmatey.extensions.isToday
import com.dnfapps.arrmatey.extensions.isTodayOrAfter
import com.dnfapps.arrmatey.model.EpisodeWrapper
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.ArrLightPurple
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun EpisodeRow(
    episode: EpisodeWrapper,
    modifier: Modifier = Modifier,
    searchInProgress: (Long) -> Boolean = { false },
    onAutomaticSearch: (Long) -> Unit = {},
    onToggleMonitor: (ArrEpisode) -> Unit = {},
    onNavigateToSeriesRelease: (Long?) -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    val arrEp = episode.arrEpisode

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.clickable(
            enabled = onClick != null,
            onClick = onClick ?: {}
        )
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                val titleString = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 16.sp)) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("${episode.episodeNumber}. ")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                            append(episode.title ?: "")
                        }
                        episode.finaleType?.let { finalType ->
                            withStyle(
                                SpanStyle(
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                bullet()
                                append(mokoString(finalType.resource))
                            }
                        }
                    }
                }
                Text(
                    text = titleString,
                    lineHeight = 1.4.em,
                    overflow = TextOverflow.MiddleEllipsis,
                    maxLines = 2
                )

                val airDate = episode.airDate?.takeIf { it.isTodayOrAfter() }
                val (statusText, statusColor) = when {
                    episode.isActive && episode.activityProgress != null -> episode.activityProgress!! to ArrLightPurple
                    episode.fileQualityName != null -> episode.fileQualityName!! to MaterialTheme.colorScheme.tertiary
                    airDate != null -> mokoString(MR.strings.unaired) to MaterialTheme.colorScheme.secondary
                    arrEp != null -> mokoString(MR.strings.missing) to MaterialTheme.colorScheme.error
                    else -> null to Color.Unspecified
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (statusText != null) {
                        Text(
                            text = statusText,
                            fontSize = 14.sp,
                            color = statusColor,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    val formattedDate = episode.formatAirDateUtc() ?: episode.airDate?.format()
                    if (formattedDate != null) {
                        val (weight, color) = if (episode.airDate?.isToday() == true)
                            FontWeight.Medium to MaterialTheme.colorScheme.primary
                        else
                            FontWeight.Normal to Color.Unspecified
                        val prefix = if (statusText != null) Bullet else ""
                        Text(
                            text = "$prefix$formattedDate",
                            color = color,
                            fontWeight = weight,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = arrEp != null,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                if (arrEp != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                onNavigateToSeriesRelease(arrEp.id)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                            )
                        }
                        IconButton(
                            onClick = {
                                onAutomaticSearch(arrEp.id)
                            },
                            enabled = arrEp.monitored && !searchInProgress(arrEp.id),
                            modifier = Modifier.size(24.dp)
                        ) {
                            if (searchInProgress(arrEp.id)) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                onToggleMonitor(arrEp)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            AnimatedContent(
                                targetState = arrEp.monitored,
                                transitionSpec = {
                                    (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
                                },
                                label = "EpisodeBookmarkIconAnimation"
                            ) { monitored ->
                                Icon(
                                    imageVector = if (monitored) {
                                        Icons.Default.Bookmark
                                    } else {
                                        Icons.Default.BookmarkBorder
                                    },
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
        }

        val stillUrl = episode.stillPath
        val epOverview = episode.overview?.takeUnless { it.isBlank() }
        if (stillUrl != null || epOverview != null) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = rememberRemoteImageData(stillUrl),
                    modifier = Modifier
                        .height(70.dp)
                        .aspectRatio(1.77f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentDescription = null
                )

                Column(
                    modifier = Modifier.height(70.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (epOverview != null) {
                        Text(
                            text = epOverview,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    episode.bazarrEpisode?.let { bazarrEp ->
                        EpisodeSubtitlesRow(
                            bazarrEpisode = bazarrEp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}