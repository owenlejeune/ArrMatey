package com.dnfapps.arrmatey.bazarr.api.model

import com.dnfapps.arrmatey.arr.api.client.HasArrImages
import com.dnfapps.arrmatey.arr.api.model.ArrImage
import com.dnfapps.arrmatey.arr.api.model.CoverType
import com.dnfapps.arrmatey.arr.api.model.joinInstanceUrl
import com.dnfapps.arrmatey.instances.model.Instance
import kotlinx.serialization.Transient

sealed interface BazarrMedia : HasArrImages<BazarrMedia> {
    val serviceId: Long
    val alternativeTitles: List<String>
    val fanart: String?
    val imdbId: String?
    val monitored: Boolean
    val overview: String
    val path: String
    val poster: String?
    val profileId: Int?
    val tags: List<String>
    val title: String
    val year: String
    val audioLanguage: List<BazarrAudioLanguage>

    val mediaType: BazarrMediaType
        get() =
            when (this) {
                is BazarrMovie -> BazarrMediaType.Movie
                is BazarrSeries -> BazarrMediaType.Series
            }

    @Transient
    override val images: List<ArrImage>
        get() =
            listOfNotNull(
                poster?.let { ArrImage(CoverType.Poster, it, it) },
                fanart?.let { ArrImage(CoverType.FanArt, it, it) },
            )

    override fun withLocalImages(instance: Instance): BazarrMedia = when (this) {
        is BazarrMovie ->
            copy(
                poster = poster?.let { if (it.startsWith("/")) joinInstanceUrl(instance.url, it) else it },
                fanart = fanart?.let { if (it.startsWith("/")) joinInstanceUrl(instance.url, it) else it },
            )
        is BazarrSeries ->
            copy(
                poster = poster?.let { if (it.startsWith("/")) joinInstanceUrl(instance.url, it) else it },
                fanart = fanart?.let { if (it.startsWith("/")) joinInstanceUrl(instance.url, it) else it },
            )
    }
}

enum class BazarrMediaType {
    Movie,
    Series,
}
