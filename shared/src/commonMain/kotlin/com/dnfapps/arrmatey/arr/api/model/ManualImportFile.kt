package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import kotlinx.serialization.Serializable

@Serializable
data class ManualImportFile(
    val id: Long? = null,
    val path: String? = null,
    val relativePath: String? = null,
    val name: String? = null,
    val size: Long = 0L,
    val quality: QualityInfo? = null,
    val languages: List<Language> = emptyList(),
    val releaseGroup: String? = null,
    val qualityWeight: Int = 0,
    val downloadId: String? = null,
    val customFormats: List<CustomFormat> = emptyList(),
    val customFormatScore: Int = 0,
    val rejections: List<ManualImportRejection> = emptyList(),
    val seriesId: Long? = null,
    val episodeIds: List<Long> = emptyList(),
    val seasonNumber: Int? = null,
    val series: ArrSeries? = null,
    val episodes: List<Episode> = emptyList(),
    val movieId: Long? = null,
    val movie: ArrMovie? = null,
    val artistId: Long? = null,
    val albumId: Long? = null,
    val authorId: Long? = null,
    val bookId: Long? = null,
) {
    val reasons: List<String>
        get() = rejections.mapNotNull { it.reason }

    val effectiveSeriesId: Long?
        get() = seriesId ?: series?.id

    val effectiveMovieId: Long?
        get() = movieId ?: movie?.id

    val effectiveEpisodeIds: List<Long>
        get() = if (episodeIds.isNotEmpty()) episodeIds else episodes.map { it.id }

    val effectiveSeasonNumber: Int?
        get() = seasonNumber ?: episodes.firstOrNull()?.seasonNumber

    fun withSeriesId(newSeriesId: Long): ManualImportFile = copy(seriesId = newSeriesId)
    fun withMovieId(newMovieId: Long): ManualImportFile = copy(movieId = newMovieId)
    fun withArtistId(newArtistId: Long): ManualImportFile = copy(artistId = newArtistId)
    fun withBookId(newBookId: Long): ManualImportFile = copy(bookId = newBookId)
    fun withEpisodes(newEpisodes: List<Episode>): ManualImportFile = copy(
        episodes = newEpisodes,
        episodeIds = newEpisodes.map { it.id },
        seasonNumber = newEpisodes.firstOrNull()?.seasonNumber ?: seasonNumber,
    )
    fun withSeasonAndEpisodeIds(season: Int?, newEpisodeIds: List<Long>): ManualImportFile = copy(
        seasonNumber = season ?: seasonNumber,
        episodeIds = newEpisodeIds,
    )

    val isAcceptable: Boolean
        get() = rejections.isEmpty()

    val qualityLabel: String
        get() = quality?.qualityLabel ?: "Unknown"

    val sizeLabel: String
        get() = size.bytesAsFileSizeString()

    val languageLabel: String
        get() = languages.takeUnless { it.isEmpty() }?.mapNotNull { it.name }?.joinToString(", ") ?: "Unknown"

    val displayName: String
        get() = relativePath ?: name ?: path ?: "Unknown"

    val stableId: String
        get() = id?.toString() ?: path ?: relativePath ?: name ?: ""
}

@Serializable
data class ManualImportRejection(
    val reason: String? = null,
    val type: String? = null,
)
