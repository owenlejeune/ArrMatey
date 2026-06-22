package com.dnfapps.arrmatey.bazarr.api.model

import com.dnfapps.arrmatey.arr.api.client.HasArrImages
import com.dnfapps.arrmatey.arr.api.model.ArrImage
import com.dnfapps.arrmatey.arr.api.model.CoverType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class BazarrSeries(
    val alternativeTitles: List<String> = emptyList(),
    val audio_language: List<String> = emptyList(),
    val episodeFileCount: Int,
    val ended: Boolean,
    val episodeMissingCount: Int,
    val fanart: String? = null,
    val imdbId: String,
    val lastAired: String? = null,
    val monitored: Boolean,
    val overview: String,
    val path: String,
    val poster: String? = null,
    val profileId: Int,
    val seriesType: String,
    val sonarrSeriesId: Int,
    val tags: List<String> = emptyList(),
    val title: String,
    val tvdbId: Int,
    val year: String
) : HasArrImages<BazarrSeries> {
    @Transient
    override val images: List<ArrImage> = listOfNotNull(
        poster?.let { ArrImage(CoverType.Poster, it, it) },
        fanart?.let { ArrImage(CoverType.FanArt, it, it) }
    )

    override fun withLocalImages(instanceUrl: String): BazarrSeries {
        return copy(
            poster = if (poster?.startsWith("/") == true) "$instanceUrl$poster" else poster,
            fanart = if (fanart?.startsWith("/") == true) "$instanceUrl$fanart" else fanart
        )
    }
}
