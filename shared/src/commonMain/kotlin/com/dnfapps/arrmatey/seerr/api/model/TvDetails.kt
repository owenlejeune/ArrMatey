package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class TvDetails(
    val id: Long,
    val backdropPath: String? = null,
    val posterPath: String? = null,
    val contentRatings: ContentRatings? = null,
    val createdBy: List<Creator> = emptyList(),
    val episodeRunTime: List<Int> = emptyList(),
    @Contextual val firstAirDate: LocalDate? = null,
    val genres: List<Genre> = emptyList(),
    val homepage: String? = null,
    val inProduction: Boolean = false,
    val languages: List<String> = emptyList(),
    @Contextual val lastAirDate: LocalDate? = null,
    val lastEpisodeToAir: Episode? = null,
    val name: String,
    val nextEpisodeToAir: Episode? = null,
    val networks: List<Network> = emptyList(),
    val numberOfEpisodes: Int = 0,
    val numberOfSeasons: Int = 0,
    val originCountry: List<String> = emptyList(),
    val originalLanguage: String,
    val originalName: String,
    val overview: String? = null,
    val popularity: Double = 0.0,
    val productionCompanies: List<ProductionCompany> = emptyList(),
    val productionCountries: List<ProductionCountry> = emptyList(),
    val spokenLanguages: List<SpokenLanguage> = emptyList(),
    val seasons: List<Season> = emptyList(),
    val status: String,
    val tagline: String? = null,
    val type: String,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val credits: Credits? = null,
    val externalIds: ExternalIds? = null,
    val keywords: List<Keyword> = emptyList(),
    val mediaInfo: MediaInfo? = null,
    val watchProviders: List<WatchProvider> = emptyList()
)