package com.dnfapps.arrmatey.mediaserver.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlexRootContainer<T>(
    @SerialName("MediaContainer") val mediaContainer: T,
)

@Serializable
data class PlexDirectoryListing(
    @SerialName("size") val size: Int = 0,
    @SerialName("Directory") val directories: List<PlexDirectory>? = null,
)

@Serializable
data class PlexDirectory(
    @SerialName("key") val key: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("type") val type: String = "",
)

@Serializable
data class PlexMediaListing(
    @SerialName("size") val size: Int = 0,
    @SerialName("totalSize") val totalSize: Int? = null,
    @SerialName("offset") val offset: Int? = null,
    @SerialName("Metadata") val metadata: List<PlexMetadataItem>? = null,
)

@Serializable
data class PlexMetadataItem(
    @SerialName("ratingKey") val ratingKey: String? = null,
    @SerialName("key") val key: String? = null,
    @SerialName("guid") val guid: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("year") val year: Int? = null,
    @SerialName("summary") val summary: String? = null,
    @SerialName("thumb") val thumb: String? = null,
    @SerialName("art") val art: String? = null,
    @SerialName("viewOffset") val viewOffset: Long? = null,
    @SerialName("duration") val duration: Long? = null,
    @SerialName("viewCount") val viewCount: Int? = null,
    @SerialName("lastViewedAt") val lastViewedAt: Long? = null,
    @SerialName("rating") val rating: Double? = null,
    @SerialName("parentRatingKey") val parentRatingKey: String? = null,
    @SerialName("grandparentRatingKey") val grandparentRatingKey: String? = null,
    @SerialName("parentTitle") val parentTitle: String? = null,
    @SerialName("grandparentTitle") val grandparentTitle: String? = null,
    @SerialName("index") val index: Int? = null,
    @SerialName("parentIndex") val parentIndex: Int? = null,
    @SerialName("Guid") val guids: List<PlexGuid>? = null,
    @SerialName("Media") val media: List<PlexMediaItem>? = null,
) {
    fun extractTmdbId(): Long? {
        val tmdbGuid = guids?.firstOrNull { it.id.startsWith("tmdb://") }?.id
            ?: guid?.takeIf { it.startsWith("tmdb://") }
        if (tmdbGuid != null) {
            return tmdbGuid.removePrefix("tmdb://").toLongOrNull()
        }
        val legacy = guid?.takeIf { it.contains("themoviedb://") }
        return legacy?.substringAfter("themoviedb://")?.substringBefore("?")?.toLongOrNull()
    }

    fun extractTvdbId(): Long? {
        val tvdbGuid = guids?.firstOrNull { it.id.startsWith("tvdb://") }?.id
            ?: guid?.takeIf { it.startsWith("tvdb://") }
        if (tvdbGuid != null) {
            return tvdbGuid.removePrefix("tvdb://").toLongOrNull()
        }
        val legacy = guid?.takeIf { it.contains("thetvdb://") }
        return legacy?.substringAfter("thetvdb://")?.substringBefore("?")?.toLongOrNull()
    }

    fun extractImdbId(): String? {
        val imdbGuid = guids?.firstOrNull { it.id.startsWith("imdb://") }?.id
            ?: guid?.takeIf { it.startsWith("imdb://") }
        if (imdbGuid != null) {
            return imdbGuid.removePrefix("imdb://")
        }
        val legacy = guid?.takeIf { it.contains("imdb://") }
        return legacy?.substringAfter("imdb://")?.substringBefore("?")
    }
}

@Serializable
data class PlexGuid(
    @SerialName("id") val id: String = "",
)

@Serializable
data class PlexMediaItem(
    @SerialName("id") val id: Long? = null,
    @SerialName("videoResolution") val videoResolution: String? = null,
    @SerialName("videoCodec") val videoCodec: String? = null,
    @SerialName("audioCodec") val audioCodec: String? = null,
    @SerialName("audioChannels") val audioChannels: Int? = null,
    @SerialName("Part") val part: List<PlexPart>? = null,
)

@Serializable
data class PlexPart(
    @SerialName("id") val id: Long? = null,
    @SerialName("key") val key: String? = null,
    @SerialName("duration") val duration: Long? = null,
    @SerialName("file") val file: String? = null,
    @SerialName("size") val size: Long? = null,
    @SerialName("container") val container: String? = null,
)

@Serializable
data class PlexServerResource(
    @SerialName("name") val name: String,
    @SerialName("product") val product: String = "",
    @SerialName("productVersion") val productVersion: String? = null,
    @SerialName("platform") val platform: String? = null,
    @SerialName("device") val device: String? = null,
    @SerialName("clientIdentifier") val clientIdentifier: String,
    @SerialName("provides") val provides: String = "",
    @SerialName("connections") val connections: List<PlexServerConnection> = emptyList(),
)

@Serializable
data class PlexServerConnection(
    @SerialName("protocol") val protocol: String,
    @SerialName("address") val address: String,
    @SerialName("port") val port: Int,
    @SerialName("uri") val uri: String,
    @SerialName("local") val local: Boolean = false,
)
