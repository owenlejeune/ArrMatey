package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.arr.api.model.FinaleType
import com.dnfapps.arrmatey.bazarr.api.model.BazarrEpisode
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import com.dnfapps.arrmatey.arr.api.model.Episode as ArrEpisode
import com.dnfapps.arrmatey.seerr.api.model.Episode as SeerrEpisode

data class EpisodeWrapper(
    val arrEpisode: ArrEpisode? = null,
    val seerrEpisode: SeerrEpisode? = null,
    val bazarrEpisode: BazarrEpisode? = null,
    val mediaServerEpisode: UnifiedMediaItem? = null,
    val isActive: Boolean = false,
    val activityProgress: String? = null,
) {
    val seasonNumber: Int
        get() = arrEpisode?.seasonNumber ?: seerrEpisode?.seasonNumber ?: mediaServerEpisode?.seasonNumber ?: 0

    val episodeNumber: Int
        get() = arrEpisode?.episodeNumber ?: seerrEpisode?.episodeNumber ?: mediaServerEpisode?.episodeNumber ?: 0

    val title: String?
        get() = arrEpisode?.displayTitle ?: seerrEpisode?.name ?: mediaServerEpisode?.title

    val overview: String?
        get() = arrEpisode?.overview ?: seerrEpisode?.overview ?: mediaServerEpisode?.summary

    val stillPath: String?
        get() = seerrEpisode?.stillPath ?: arrEpisode?.images?.firstOrNull()?.remoteUrl ?: mediaServerEpisode?.stillUrl ?: mediaServerEpisode?.posterUrl

    val airDate: LocalDate?
        get() = arrEpisode?.airDate ?: seerrEpisode?.airDate ?: mediaServerEpisode?.releaseDate?.let {
            try {
                LocalDate.parse(it.take(10))
            } catch (_: Exception) {
                null
            }
        }

    val airDateUtc: Instant?
        get() = arrEpisode?.airDateUtc

    val monitored: Boolean?
        get() = arrEpisode?.monitored

    val isMonitored: Boolean
        get() = arrEpisode?.monitored == true

    val hasFile: Boolean
        get() = arrEpisode?.hasFile == true

    val fileQualityName: String?
        get() = arrEpisode?.fileQualityName

    val finaleType: FinaleType?
        get() = arrEpisode?.finaleType

    fun formatAirDateUtc(): String? = arrEpisode?.formatAirDateUtc()
}
