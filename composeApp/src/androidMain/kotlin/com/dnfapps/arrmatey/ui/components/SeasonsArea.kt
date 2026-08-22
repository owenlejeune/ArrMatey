package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.model.EpisodeWrapper
import com.dnfapps.arrmatey.model.SeasonWrapper
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.arr.api.model.Episode as ArrEpisode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SeasonsArea(
    seasons: List<SeasonWrapper>,
    modifier: Modifier = Modifier,
    seriesId: Long? = null,
    searchIds: Set<Long> = emptySet(),
    onToggleSeasonMonitor: (Int) -> Unit = {},
    onToggleEpisodeMonitor: (ArrEpisode) -> Unit = {},
    onEpisodeAutomaticSearch: (Long) -> Unit = {},
    onSeasonAutomaticSearch: (Int) -> Unit = {},
    deleteSeasonFiles: (Int) -> Unit = {},
    seasonDeleteInProgress: Boolean = false,
    onNavigateToEpisodeDetails: (ArrEpisode) -> Unit = {},
    onNavigateToSeriesRelease: (Long?, Int) -> Unit = { _, _ -> }
) {
    if (seasons.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Text(
            text = mokoString(MR.strings.seasons_header),
            style = MaterialTheme.typography.titleLarge
        )

        seasons.forEach { season ->
            var expanded by rememberSaveable { mutableStateOf(false) }
            val iconRotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = tween(durationMillis = 200),
                label = "iconRotation"
            )

            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                ContainerCard(
                    modifier = Modifier.clickable { expanded = !expanded }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (season.seasonNumber == 0) {
                                        mokoString(MR.strings.specials)
                                    } else {
                                        mokoString(MR.strings.season_label, season.seasonNumber)
                                    },
                                    style = MaterialTheme.typography.titleLarge
                                )
                                val statsText = season.episodeFileCount?.let {
                                    "$it/${season.totalEpisodeCount}"
                                } ?: mokoPlural(MR.plurals.episodes_count, season.totalEpisodeCount)

                                AnimatedContent(
                                    targetState = statsText,
                                    transitionSpec = {
                                        (fadeIn() + slideInVertically { it }).togetherWith(fadeOut() + slideOutVertically { -it })
                                    },
                                    label = "SeasonStatsTextAnimation"
                                ) { text ->
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            if (season.infoString.isNotBlank()) {
                                Text(
                                    text = season.infoString,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = season.monitored != null && seriesId != null && seriesId > 0,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            AnimatedContent(
                                targetState = season.isMonitored,
                                transitionSpec = {
                                    (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
                                },
                                label = "SeasonBookmarkIconAnimation"
                            ) { isMonitored ->
                                Icon(
                                    imageVector = if (isMonitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = if (isMonitored) {
                                        mokoString(MR.strings.monitored)
                                    } else {
                                        mokoString(MR.strings.unmonitored)
                                    },
                                    modifier = Modifier.clickable {
                                        onToggleSeasonMonitor(season.seasonNumber)
                                    }
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ExpandCircleDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(iconRotation)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        if (seriesId != null && season.arrSeason != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            SeasonHeader(
                                season = season,
                                seriesId = seriesId,
                                onPerformAutomaticSearch = onSeasonAutomaticSearch,
                                searchInProgress = { searchIds.contains(it.toLong()) },
                                onDeleteSeason = { deleteSeasonFiles(season.seasonNumber) },
                                deleteInProgress = seasonDeleteInProgress,
                                onNavigateToSeriesRelease = onNavigateToSeriesRelease
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        season.episodes.forEachIndexed { index, episode ->
                            val arrEp = episode.arrEpisode

                            EpisodeRow(
                                episode = episode,
                                onClick = arrEp?.let { ep -> { onNavigateToEpisodeDetails(ep) } },
                                onAutomaticSearch = onEpisodeAutomaticSearch,
                                onToggleMonitor = onToggleEpisodeMonitor,
                                onNavigateToSeriesRelease = { onNavigateToSeriesRelease(seriesId, episode.episodeNumber) },
                                searchInProgress = { searchIds.contains(it) }
                            )

                            if (index < season.episodes.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeasonsArea(
    series: ArrSeries,
    episodes: List<ArrEpisode>,
    searchIds: Set<Long>,
    onToggleSeasonMonitor: (Int) -> Unit,
    onToggleEpisodeMonitor: (ArrEpisode) -> Unit,
    onEpisodeAutomaticSearch: (Long) -> Unit,
    onSeasonAutomaticSearch: (Int) -> Unit,
    deleteSeasonFiles: (Int) -> Unit,
    seasonDeleteInProgress: Boolean,
    onNavigateToEpisodeDetails: (ArrSeries, ArrEpisode) -> Unit,
    onNavigateToSeriesRelease: (Long?, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val arrEpMap = episodes.groupBy { it.seasonNumber }
    val wrappedSeasons = series.seasons.sortedByDescending { it.seasonNumber }.map { season ->
        val seasonEpisodes = (arrEpMap[season.seasonNumber] ?: emptyList())
            .sortedByDescending { it.episodeNumber }
            .map { EpisodeWrapper(arrEpisode = it) }
        SeasonWrapper(
            seasonNumber = season.seasonNumber,
            arrSeason = season,
            episodes = seasonEpisodes
        )
    }

    SeasonsArea(
        seasons = wrappedSeasons,
        seriesId = series.id,
        searchIds = searchIds,
        onToggleSeasonMonitor = onToggleSeasonMonitor,
        onToggleEpisodeMonitor = onToggleEpisodeMonitor,
        onEpisodeAutomaticSearch = onEpisodeAutomaticSearch,
        onSeasonAutomaticSearch = onSeasonAutomaticSearch,
        deleteSeasonFiles = deleteSeasonFiles,
        seasonDeleteInProgress = seasonDeleteInProgress,
        onNavigateToEpisodeDetails = { onNavigateToEpisodeDetails(series, it) },
        onNavigateToSeriesRelease = onNavigateToSeriesRelease,
        modifier = modifier
    )
}