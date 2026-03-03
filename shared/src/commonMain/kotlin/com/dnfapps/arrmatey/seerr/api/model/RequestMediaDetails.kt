package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.datetime.LocalDate

sealed interface RequestMediaDetails {
    val id: Long
    val backdropPath: String?
    val posterPath: String?
    val genres: List<Genre>
    val homepage: String?
    val originalLanguage: String
    val overview: String?
    val popularity: Double
    val productionCompanies: List<ProductionCompany>
    val productionCountries: List<ProductionCountry>
    val spokenLanguages: List<SpokenLanguage>
    val status: String
    val tagline: String?
    val voteAverage: Double
    val voteCount: Int
    val credits: Credits?
    val externalIds: ExternalIds?
    val mediaInfo: MediaInfo?
    val watchProviders: List<WatchProvider>
    val relatedVideos: List<Video>

    val displayTitle: String
        get() = when (this) {
            is MovieDetails -> title
            is TvDetails -> name
        }

    val displayDate: LocalDate?
        get() = when (this) {
            is MovieDetails -> releaseDate
            is TvDetails -> firstAirDate
        }

    val fullBackdropPath: String?
        get() = backdropPath?.let {
            "https://image.tmdb.org/t/p/w1920_and_h800_multi_faces$it"
        }

    val fullPosterPath: String?
        get() = posterPath?.let {
            "https://image.tmdb.org/t/p/w600_and_h900_bestv2$it"
        }

    val requestType: RequestType

    fun getCertification(localeCode: String): String?
}