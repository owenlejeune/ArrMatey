package com.dnfapps.arrmatey.mediaserver.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaBrowserAuthRequest(
    @SerialName("Username") val username: String,
    @SerialName("Pw") val password: String,
)

@Serializable
data class MediaBrowserAuthResult(
    @SerialName("AccessToken") val accessToken: String,
    @SerialName("User") val user: MediaBrowserUser,
    @SerialName("ServerId") val serverId: String? = null,
)

@Serializable
data class MediaBrowserUser(
    @SerialName("Id") val id: String,
    @SerialName("Name") val name: String,
)

@Serializable
data class MediaBrowserItemsResult(
    @SerialName("Items") val items: List<MediaBrowserBaseItem> = emptyList(),
    @SerialName("TotalRecordCount") val totalRecordCount: Int = 0,
    @SerialName("StartIndex") val startIndex: Int = 0,
)

@Serializable
data class MediaBrowserStudio(
    @SerialName("Name") val name: String? = null,
    @SerialName("Id") val id: String? = null,
)

@Serializable
data class MediaBrowserBaseItem(
    @SerialName("Id") val id: String,
    @SerialName("Name") val name: String,
    @SerialName("Type") val type: String,
    @SerialName("CollectionType") val collectionType: String? = null,
    @SerialName("ProductionYear") val productionYear: Int? = null,
    @SerialName("Overview") val overview: String? = null,
    @SerialName("RunTimeTicks") val runTimeTicks: Long? = null,
    @SerialName("UserData") val userData: MediaBrowserUserData? = null,
    @SerialName("ImageTags") val imageTags: Map<String, String>? = null,
    @SerialName("BackdropImageTags") val backdropImageTags: List<String> = emptyList(),
    @SerialName("CommunityRating") val communityRating: Double? = null,
    @SerialName("SeriesName") val seriesName: String? = null,
    @SerialName("SeriesId") val seriesId: String? = null,
    @SerialName("SeasonId") val seasonId: String? = null,
    @SerialName("IndexNumber") val indexNumber: Int? = null,
    @SerialName("ParentIndexNumber") val parentIndexNumber: Int? = null,
    @SerialName("PremiereDate") val premiereDate: String? = null,
    @SerialName("SeriesStudio") val seriesStudio: String? = null,
    @SerialName("Studios") val studios: List<MediaBrowserStudio>? = null,
    @SerialName("SeriesPrimaryImageTag") val seriesPrimaryImageTag: String? = null,
    @SerialName("ProviderIds") val providerIds: Map<String, String>? = null,
    @SerialName("MediaSources") val mediaSources: List<MediaBrowserMediaSource> = emptyList(),
) {
    fun extractTmdbId(): Long? = providerIds?.get("Tmdb")?.toLongOrNull()
        ?: providerIds?.get("tmdb")?.toLongOrNull()

    fun extractTvdbId(): Long? = providerIds?.get("Tvdb")?.toLongOrNull()
        ?: providerIds?.get("tvdb")?.toLongOrNull()

    fun extractImdbId(): String? = providerIds?.get("Imdb") ?: providerIds?.get("imdb")
}

@Serializable
data class MediaBrowserUserData(
    @SerialName("PlaybackPositionTicks") val playbackPositionTicks: Long = 0,
    @SerialName("Played") val played: Boolean = false,
    @SerialName("PlayCount") val playCount: Int = 0,
    @SerialName("IsFavorite") val isFavorite: Boolean = false,
    @SerialName("LastPlayedDate") val lastPlayedDate: String? = null,
)

@Serializable
data class MediaBrowserMediaSource(
    @SerialName("Id") val id: String? = null,
    @SerialName("Container") val container: String? = null,
    @SerialName("Bitrate") val bitrate: Long? = null,
    @SerialName("MediaStreams") val mediaStreams: List<MediaBrowserMediaStream> = emptyList(),
)

@Serializable
data class MediaBrowserMediaStream(
    @SerialName("Type") val type: String? = null,
    @SerialName("Codec") val codec: String? = null,
    @SerialName("DisplayTitle") val displayTitle: String? = null,
    @SerialName("Width") val width: Int? = null,
    @SerialName("Height") val height: Int? = null,
    @SerialName("Channels") val channels: Int? = null,
    @SerialName("ChannelLayout") val channelLayout: String? = null,
    @SerialName("VideoRange") val videoRange: String? = null,
)

@Serializable
data class MediaBrowserSystemInfo(
    @SerialName("Id") val id: String? = null,
    @SerialName("ServerName") val serverName: String? = null,
    @SerialName("Version") val version: String? = null,
)

@Serializable
data class JellyfinQuickConnectInitiateResult(
    @SerialName("Secret") val secret: String,
    @SerialName("Code") val code: String,
    @SerialName("Authentication") val authentication: String? = null,
)

@Serializable
data class JellyfinQuickConnectCheckResult(
    @SerialName("Secret") val secret: String? = null,
    @SerialName("Authenticated") val authenticated: Boolean = false,
)

@Serializable
data class JellyfinQuickConnectAuthBody(
    @SerialName("Secret") val secret: String,
)
