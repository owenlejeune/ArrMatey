package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.arr.api.model.Season as ArrSeason
import com.dnfapps.arrmatey.seerr.api.model.Season as SeerrSeason

data class SeasonWrapper(
    val seasonNumber: Int,
    val arrSeason: ArrSeason? = null,
    val seerrSeason: SeerrSeason? = null,
    val episodes: List<EpisodeWrapper> = emptyList()
) {
    val totalEpisodeCount: Int
        get() = arrSeason?.statistics?.totalEpisodeCount ?: seerrSeason?.episodeCount ?: episodes.size

    val episodeFileCount: Int?
        get() = arrSeason?.statistics?.episodeFileCount

    val monitored: Boolean?
        get() = arrSeason?.monitored

    val isMonitored: Boolean
        get() = arrSeason?.monitored == true

    val hasArrSeason: Boolean
        get() = arrSeason != null

    val name: String?
        get() = seerrSeason?.name
}
