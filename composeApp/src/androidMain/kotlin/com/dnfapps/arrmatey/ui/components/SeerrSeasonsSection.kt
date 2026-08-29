package com.dnfapps.arrmatey.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dnfapps.arrmatey.model.EpisodeWrapper
import com.dnfapps.arrmatey.model.SeasonWrapper
import com.dnfapps.arrmatey.seerr.api.model.Season

@Composable
fun SeerrSeasonsSection(
    seasons: List<Season>,
    modifier: Modifier = Modifier,
) {
    val wrappedSeasons =
        seasons.map { season ->
            SeasonWrapper(
                seasonNumber = season.seasonNumber,
                seerrSeason = season,
                episodes = season.episodes.map { ep -> EpisodeWrapper(seerrEpisode = ep) },
            )
        }
    SeasonsArea(
        seasons = wrappedSeasons,
        modifier = modifier,
    )
}
