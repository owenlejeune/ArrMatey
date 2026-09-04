package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.extensions.formatMinutesAsRuntime
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.MokoStrings
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.dnfapps.arrmatey.arr.api.model.Season as ArrSeason
import com.dnfapps.arrmatey.seerr.api.model.Season as SeerrSeason

data class SeasonWrapper(
    val seasonNumber: Int,
    val arrSeason: ArrSeason? = null,
    val seerrSeason: SeerrSeason? = null,
    val episodes: List<EpisodeWrapper> = emptyList(),
) {
    val totalEpisodeCount: Int
        get() = arrSeason?.statistics?.totalEpisodeCount ?: seerrSeason?.episodeCount ?: episodes.size

    val episodeFileCount: Int?
        get() = arrSeason?.statistics?.episodeFileCount

    val activeEpisodeCount: Int
        get() = episodes.count { it.isActive }

    val monitored: Boolean?
        get() = arrSeason?.monitored

    val isMonitored: Boolean
        get() = arrSeason?.monitored == true

    val hasArrSeason: Boolean
        get() = arrSeason != null

    val name: String?
        get() = seerrSeason?.name

    val customTitle: String?
        get() {
            val rawName = seerrSeason?.name?.trim() ?: return null
            if (rawName.isBlank()) return null
            if (rawName.equals("Season $seasonNumber", ignoreCase = true)) return null
            if (seasonNumber == 0 &&
                (rawName.equals("Specials", ignoreCase = true) || rawName.equals("Season 0", ignoreCase = true))
            ) {
                return null
            }
            return rawName
        }

    val year: String
        get() {
            val minUtcYear =
                episodes
                    .mapNotNull { it.airDateUtc }
                    .minOrNull()
                    ?.toLocalDateTime(TimeZone.UTC)
                    ?.date
                    ?.year
                    ?.toString()
            val minDateYear = episodes.mapNotNull { it.airDate?.year }.minOrNull()?.toString()
            val seerrYear = seerrSeason?.airDate?.year?.toString()
            return minUtcYear ?: minDateYear ?: seerrYear ?: MokoStrings().getString(MR.strings.tba)
        }

    val runtime: String?
        get() {
            val items = episodes.mapNotNull { it.arrEpisode?.runtime?.takeIf { r -> r > 0 } }
            return if (items.isEmpty()) {
                null
            } else {
                items.sorted()[items.size / 2].formatMinutesAsRuntime()
            }
        }

    val sizeOnDisk: String?
        get() = arrSeason?.statistics?.sizeOnDisk?.bytesAsFileSizeString()

    val infoString: String
        get() {
            val seasonInfo = listOfNotNull(customTitle, year, runtime, sizeOnDisk)
            return seasonInfo.joinToString(" • ")
        }
}
