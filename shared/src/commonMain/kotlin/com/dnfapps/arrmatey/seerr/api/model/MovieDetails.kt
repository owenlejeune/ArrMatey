package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetails(
    val id: Long,
    val imdbId: String? = null,
    val adult: Boolean = false,
    val backdropPath: String? = null,
    val posterPath: String? = null,
    val budget: Long = 0,
    val genres: List<Genre> = emptyList(),
    val homepage: String? = null,
    val relatedVideos: List<Video> = emptyList(),
    val originalLanguage: String,
    val originalTitle: String,
    val overview: String? = null,
    val popularity: Double = 0.0,
    val productionCompanies: List<ProductionCompany> = emptyList(),
    val productionCountries: List<ProductionCountry> = emptyList(),
    @Contextual val releaseDate: LocalDate? = null,
    val releases: Releases? = null,
    val revenue: Long = 0,
    val runtime: Int? = null,
    val spokenLanguages: List<SpokenLanguage> = emptyList(),
    val status: String,
    val tagline: String? = null,
    val title: String,
    val video: Boolean = false,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val credits: Credits? = null,
    val collection: Collection? = null,
    val externalIds: ExternalIds? = null,
    val mediaInfo: MediaInfo? = null,
    val watchProviders: List<WatchProvider> = emptyList()
)