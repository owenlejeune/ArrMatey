package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class TvDetails(
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
    override val requestType: RequestType = RequestType.Tv,

    val contentRatings: ContentRatings? = null,
    val createdBy: List<Creator> = emptyList(),
    val episodeRunTime: List<Int> = emptyList(),
    @Contextual val firstAirDate: LocalDate? = null,
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
    val originalName: String,
    val seasons: List<Season> = emptyList(),
    val type: String,
    val keywords: List<Keyword> = emptyList()
): RequestMediaDetails {

    override fun getCertification(localeCode: String): String? =
        contentRatings?.results?.firstOrNull {
            it.iso_3166_1 == localeCode
        }?.rating
}