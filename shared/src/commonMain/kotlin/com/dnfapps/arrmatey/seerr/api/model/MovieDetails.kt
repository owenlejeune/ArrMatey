package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetails(
    override val id: Long,
    override val backdropPath: String? = null,
    override val posterPath: String? = null,
    override val genres: List<Genre> = emptyList(),
    override val homepage: String? = null,
    override val originalLanguage: String,
    override val overview: String? = null,
    override val popularity: Double = 0.0,
    override val productionCompanies: List<ProductionCompany> = emptyList(),
    override val productionCountries: List<ProductionCountry> = emptyList(),
    override val spokenLanguages: List<SpokenLanguage> = emptyList(),
    override val status: String,
    override val tagline: String? = null,
    override val voteAverage: Double = 0.0,
    override val voteCount: Int = 0,
    override val credits: Credits? = null,
    override val externalIds: ExternalIds? = null,
    override val mediaInfo: MediaInfo? = null,
    override val watchProviders: List<WatchProvider> = emptyList(),
    override val relatedVideos: List<Video> = emptyList(),
    override val requestType: RequestType = RequestType.Movie,

    val imdbId: String? = null,
    val adult: Boolean = false,
    val budget: Long = 0,
    val originalTitle: String,
    @Contextual val releaseDate: LocalDate? = null,
    val releases: Releases? = null,
    val revenue: Long = 0,
    val runtime: Int? = null,
    val title: String,
    val video: Boolean = false,
    val collection: Collection? = null
): RequestMediaDetails {

    override fun getCertification(localeCode: String): String? =
        releases?.results?.firstOrNull {
            it.iso_3166_1 == localeCode
        }?.rating
}