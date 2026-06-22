package com.dnfapps.arrmatey.bazarr.api.model

import com.dnfapps.arrmatey.arr.api.client.HasArrImages
import com.dnfapps.arrmatey.arr.api.model.ArrImage
import com.dnfapps.arrmatey.arr.api.model.CoverType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class BazarrMovie(
    val alternativeTitles: List<String>,
    @SerialName("audio_langauge")
    val audioLanguage: List<BazarrAudioLanguage> = emptyList(),
    val fanart: String? = null,
    val imdbId: String,
    @SerialName("missing_subtitles")
    val missingSubtitles: List<BazarrMissingSubtitle> = emptyList(),
    val monitored: Boolean,
    val overview: String,
    val path: String,
    val poster: String? = null,
    val profileId: Int,
    val radarrId: Int,
    val sceneName: String? = null,
    val subtitles: List<BazarrSubtitle> = emptyList(),
    val tags: List<String> = emptyList(),
    val title: String,
    val year: String
) : HasArrImages<BazarrMovie> {
    @Transient
    override val images: List<ArrImage> = listOfNotNull(
        poster?.let { ArrImage(CoverType.Poster, it, it) },
        fanart?.let { ArrImage(CoverType.FanArt, it, it) }
    )

    override fun withLocalImages(instanceUrl: String): BazarrMovie {
        return copy(
            poster = if (poster?.startsWith("/") == true) "$instanceUrl$poster" else poster,
            fanart = if (fanart?.startsWith("/") == true) "$instanceUrl$fanart" else fanart
        )
    }
}
