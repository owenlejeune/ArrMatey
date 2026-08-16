package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscoverResponse(
    val page: Int,
    val totalPages: Int,
    val totalResults: Int,
    val results: List<DiscoverResult>
)

@Serializable
data class DiscoverResult(
    val id: Long,
    val mediaType: RequestType,
    val adult: Boolean = false,
    val genreIds: List<Int> = emptyList(),
    val originalLanguage: String? = null,
    val originalTitle: String? = null,
    val originalName: String? = null,
    val overview: String? = null,
    val popularity: Double = 0.0,
    val releaseDate: String? = null,
    val firstAirDate: String? = null,
    val title: String? = null,
    val name: String? = null,
    val video: Boolean = false,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val backdropPath: String? = null,
    val posterPath: String? = null,
    val profilePath: String? = null,
    val character: String? = null,
    val job: String? = null,
    val knownFor: List<DiscoverResult> = emptyList(),
    val knownForDepartment: String? = null,
    val mediaInfo: MediaInfo? = null
) {
    val fullPosterPath: String?
        get() = (posterPath ?: profilePath)?.let { "https://image.tmdb.org/t/p/w500${it}" }

    val fullBackdropPath: String?
        get() = backdropPath?.let { "https://image.tmdb.org/t/p/original${it}" }
}
