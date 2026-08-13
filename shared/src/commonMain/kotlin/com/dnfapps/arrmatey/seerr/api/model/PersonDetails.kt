package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class PersonDetails(
    override val id: Long,
    val name: String,
    @Contextual val birthday: LocalDate? = null,
    @Contextual val deathday: LocalDate? = null,
    val knownForDepartment: String? = null,
    val alsoKnownAs: List<String> = emptyList(),
    val gender: Int = 0,
    val biography: String? = null,
    override val popularity: Double = 0.0,
    val placeOfBirth: String? = null,
    val profilePath: String? = null,
    val adult: Boolean = false,
    val imdbId: String? = null,
    override val homepage: String? = null,

    // RequestMediaDetails implementation
    override val backdropPath: String? = null,
    override val posterPath: String? = profilePath,
    override val genres: List<Genre> = emptyList(),
    override val originalLanguage: String = "",
    override val overview: String? = biography,
    override val productionCompanies: List<ProductionCompany> = emptyList(),
    override val productionCountries: List<ProductionCountry> = emptyList(),
    override val spokenLanguages: List<SpokenLanguage> = emptyList(),
    override val status: String = "",
    override val tagline: String? = null,
    override val voteAverage: Double = 0.0,
    override val voteCount: Int = 0,
    override val credits: Credits? = null,
    override val externalIds: ExternalIds? = null,
    override val mediaInfo: MediaInfo? = null,
    override val watchProviders: List<WatchProvider> = emptyList(),
    override val relatedVideos: List<Video> = emptyList(),
    override val requestType: RequestType = RequestType.Movie // Using Movie as a fallback/not strictly applicable
) : RequestMediaDetails {
    override fun getCertification(localeCode: String): String? = null
}
