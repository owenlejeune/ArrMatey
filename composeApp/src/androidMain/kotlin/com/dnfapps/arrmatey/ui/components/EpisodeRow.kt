package com.dnfapps.arrmatey.ui.components

import com.dnfapps.arrmatey.shared.MR
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.entensions.bullet
import com.dnfapps.arrmatey.extensions.isToday
import com.dnfapps.arrmatey.extensions.isTodayOrAfter
import com.dnfapps.arrmatey.ui.theme.ArrLightPurple
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun EpisodeRow(
    episode: Episode,
    isActive: Boolean,
    onAutomaticSearch: (Long) -> Unit,
    onToggleMonitor: (Episode) -> Unit,
    searchInProgress: (Long) -> Boolean,
    onNavigateToSeriesRelease: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    progressLabel: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clickable(
            enabled = onClick != null,
            onClick = onClick ?: {}
        )
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
                        append(episode.displayTitle)
                    }
                    episode.finaleType?.let { finalType ->
                        withStyle(SpanStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )) {
                            bullet()
                            append(mokoString(finalType.resource))
                        }
                    }
                }
            }
            Text(
                text = titleString,
                lineHeight = 1.5.em,
                overflow = TextOverflow.MiddleEllipsis,
                maxLines = 2
            )

            val airDate = episode.airDate?.takeIf { it.isTodayOrAfter() }
            val (statusText, statusColor) = when {
                isActive && progressLabel != null -> progressLabel to ArrLightPurple
                episode.fileQualityName != null -> episode.fileQualityName!! to MaterialTheme.colorScheme.tertiary
                airDate != null -> mokoString(MR.strings.unaired) to Color.Unspecified
                else -> mokoString(MR.strings.missing) to MaterialTheme.colorScheme.error
            }


            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    color = statusColor,
                    fontStyle = if (statusColor != Color.Unspecified) FontStyle.Italic else FontStyle.Normal
                )

                val (weight, color) = if (episode.airDate?.isToday() == true)
                    FontWeight.Medium to MaterialTheme.colorScheme.primary
                else
                    FontWeight.Normal to Color.Unspecified
                Text(
                    text = "$Bullet${episode.formatAirDateUtc()}",
                    color = color,
                    fontWeight = weight,
                    fontSize = 14.sp
                )
            }
        }
        IconButton(
            onClick = {
                onNavigateToSeriesRelease(episode.id)
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
                onAutomaticSearch(episode.id)
            },
            enabled = episode.monitored && !searchInProgress(episode.id),
            modifier = Modifier.size(24.dp)
        ) {
            if (searchInProgress(episode.id)) {
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
                onToggleMonitor(episode)
            },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (episode.monitored) {
                    Icons.Default.Bookmark
                } else {
                    Icons.Default.BookmarkBorder
                },
                contentDescription = null,
            )
        }
    }
}