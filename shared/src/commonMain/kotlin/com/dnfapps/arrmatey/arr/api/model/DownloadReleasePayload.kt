package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface DownloadReleasePayload {
    val guid: String
    val indexerId: Int

    @Serializable
    data class Movie(
        override val guid: String,
        override val indexerId: Int,
        val movieId: Int?
    ) : DownloadReleasePayload

    @Serializable
    data class Series(
        override val guid: String,
        override val indexerId: Int,
        val seriesId: Int? = null,
        val seasonNumber: Int? = null,
        val episodeId: Long? = null
    ) : DownloadReleasePayload

    @Serializable
    data class Album(
        override val guid: String,
        override val indexerId: Int,
        val albumId: Long? = null,
    ): DownloadReleasePayload

    @Serializable
    data class Book(
        override val guid: String,
        override val indexerId: Int
    ): DownloadReleasePayload

    @Serializable
    data class AudioBook(
        val audiobookId: Long,
        val searchResult: ListenarrRelease,
        override val guid: String = searchResult.torrentUrl ?: "",
        override val indexerId: Int = -1,
    ): DownloadReleasePayload
}