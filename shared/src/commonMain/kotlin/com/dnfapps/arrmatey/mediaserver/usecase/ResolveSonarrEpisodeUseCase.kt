package com.dnfapps.arrmatey.mediaserver.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.SonarrRepository
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.asSuccess

data class ResolvedSonarrEpisode(
    val series: ArrSeries,
    val episode: Episode,
    val instanceId: Long,
)

class ResolveSonarrEpisodeUseCase(
    private val instanceManager: InstanceManager,
) {
    suspend operator fun invoke(item: UnifiedMediaItem): ResolvedSonarrEpisode? {
        if (item.type != MediaType.EPISODE) return null

        val sonarrRepos = instanceManager.getRepositoriesByType(InstanceType.Sonarr)
            .filterIsInstance<SonarrRepository>()
        if (sonarrRepos.isEmpty()) return null

        val cleanSeriesTitle = item.seriesTitle?.replace(Regex("[^a-zA-Z0-9]"), "")?.lowercase()

        for (repo in sonarrRepos) {
            var library = repo.library.value?.asSuccess()?.data?.filterIsInstance<ArrSeries>()
            if (library.isNullOrEmpty()) {
                runCatching { repo.refreshLibrary() }
                library = repo.library.value?.asSuccess()?.data?.filterIsInstance<ArrSeries>()
            }
            if (library.isNullOrEmpty()) continue

            val matchedSeries = library.firstOrNull { series ->
                val tvdbMatches = item.tvdbId != null && item.tvdbId > 0 && series.tvdbId == item.tvdbId
                val tmdbMatches = item.tmdbId != null && item.tmdbId > 0 && series.tmdbId == item.tmdbId
                val rawTitleMatches = !item.seriesTitle.isNullOrBlank() &&
                    series.title?.equals(item.seriesTitle, ignoreCase = true) == true
                val cleanTitleMatches = !cleanSeriesTitle.isNullOrBlank() &&
                    (
                        series.cleanTitle?.equals(cleanSeriesTitle, ignoreCase = true) == true ||
                            series.title?.replace(Regex("[^a-zA-Z0-9]"), "")?.equals(cleanSeriesTitle, ignoreCase = true) == true
                        )
                tvdbMatches || tmdbMatches || rawTitleMatches || cleanTitleMatches
            } ?: continue

            val seriesId = matchedSeries.id ?: continue

            var episodes = repo.episodes.value[seriesId] ?: emptyList()
            var matchedEpisode = findEpisode(episodes, item)

            if (matchedEpisode == null) {
                val fetchResult = runCatching { repo.getEpisodes(seriesId, null) }.getOrNull()
                episodes = (fetchResult as? NetworkResult.Success)?.data ?: repo.episodes.value[seriesId] ?: emptyList()
                matchedEpisode = findEpisode(episodes, item)
            }

            if (matchedEpisode != null) {
                val effectiveSeries = matchedSeries.copy(instanceId = repo.instance.id)
                val effectiveEpisode = matchedEpisode.copy(series = effectiveSeries, instanceId = repo.instance.id)
                return ResolvedSonarrEpisode(
                    series = effectiveSeries,
                    episode = effectiveEpisode,
                    instanceId = repo.instance.id,
                )
            }
        }

        return null
    }

    private fun findEpisode(episodes: List<Episode>, item: UnifiedMediaItem): Episode? = episodes.firstOrNull { ep ->
        val numberMatches = item.seasonNumber != null &&
            item.episodeNumber != null &&
            ep.seasonNumber == item.seasonNumber &&
            ep.episodeNumber == item.episodeNumber
        val titleMatches = item.title.isNotBlank() && ep.title?.equals(item.title, ignoreCase = true) == true
        numberMatches || (item.episodeNumber == null && titleMatches)
    }
}
