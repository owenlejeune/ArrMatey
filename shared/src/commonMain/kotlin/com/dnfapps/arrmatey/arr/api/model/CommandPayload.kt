package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
sealed class CommandPayload(
    val name: String,
) {
    @Serializable
    data class Movie(
        val movieIds: List<Long>,
    ) : CommandPayload("MoviesSearch")

    @Serializable
    data class Series(
        val seriesId: Long,
    ) : CommandPayload("SeriesSearch")

    @Serializable
    data class Season(
        val seriesId: Long,
        val seasonNumber: Int,
    ) : CommandPayload("SeasonSearch")

    @Serializable
    data class Episode(
        val episodeIds: List<Long>,
    ) : CommandPayload("EpisodeSearch")

    @Serializable
    data class Artist(
        val artistId: Long,
    ) : CommandPayload("ArtistSearch")

    @Serializable
    data class Album(
        val albumIds: List<Long>,
    ) : CommandPayload("AlbumSearch")

    @Serializable
    data class Author(
        val authorId: Long,
    ) : CommandPayload("AuthorSearch")

    @Serializable
    data class Book(
        val bookIds: List<Long>,
    ) : CommandPayload("BookSearch")

    @Serializable
    data class Audiobook(
        val audiobookId: Long,
    ) : CommandPayload("name_not_needed")

    @Serializable
    data class RefreshSeries(
        val seriesIds: List<Long>? = null,
    ) : CommandPayload("RefreshSeries")

    @Serializable
    data class RefreshMovie(
        val movieIds: List<Long>? = null,
    ) : CommandPayload("RefreshMovie")

    @Serializable
    data class RefreshAlbum(
        val albumIds: List<Long>? = null,
    ) : CommandPayload("RefreshAlbum")

    @Serializable
    data class RefreshArtist(
        val artistIds: List<Long>? = null,
    ) : CommandPayload("RefreshArtist")

    @Serializable
    data class RefreshAuthor(
        val authorIds: List<Long>? = null,
    ) : CommandPayload("RefreshAuthor")

    @Serializable
    data object RefreshMonitoredDownloads : CommandPayload("RefreshMonitoredDownloads")

    @Serializable
    data object RssSync : CommandPayload("RssSync")

    @Serializable
    data object MissingEpisodeSearch : CommandPayload("MissingEpisodeSearch")

    @Serializable
    data object MissingMoviesSearch : CommandPayload("MissingMoviesSearch")

    @Serializable
    data object MissingAlbumSearch : CommandPayload("MissingAlbumSearch")

    @Serializable
    data object MissingBookSearch : CommandPayload("MissingBookSearch")

    @Serializable
    data object Backup : CommandPayload("Backup")
}
