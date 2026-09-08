package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class TracearrStreamSession(
    val id: String,
    @SerialName("server_id") val serverId: String? = null,
    @SerialName("server_name") val serverName: String? = null,
    @SerialName("server_type") val serverType: TracearrServerType? = null,
    @SerialName("server_user_id") val serverUserId: String? = null,
    @SerialName("session_key") val sessionKey: String? = null,
    val state: String? = null,
    @SerialName("media_type") val mediaType: TracearrMediaType? = null,
    @SerialName("media_title") val mediaTitle: String? = null,
    @SerialName("grandparent_title") val grandparentTitle: String? = null,
    @SerialName("show_title") val showTitle: String? = null,
    @SerialName("season_number") val seasonNumber: Int? = null,
    @SerialName("episode_number") val episodeNumber: Int? = null,
    val year: Int? = null,
    @SerialName("artist_name") val artistName: String? = null,
    @SerialName("album_name") val albumName: String? = null,
    @SerialName("track_number") val trackNumber: Int? = null,
    @SerialName("disc_number") val discNumber: Int? = null,
    @SerialName("thumb_path") val thumbPath: String? = null,
    @SerialName("poster_url") val posterUrl: String? = null,
    @SerialName("rating_key") val ratingKey: String? = null,
    @SerialName("server_version_key") val serverVersionKey: String? = null,
    @SerialName("parent_rating_key") val parentRatingKey: String? = null,
    @SerialName("grandparent_rating_key") val grandparentRatingKey: String? = null,
    @SerialName("media_id") val mediaId: String? = null,
    @SerialName("show_media_id") val showMediaId: String? = null,
    @SerialName("imdb_id") val imdbId: String? = null,
    @SerialName("tmdb_id") val tmdbId: Long? = null,
    @SerialName("tvdb_id") val tvdbId: Long? = null,
    @SerialName("external_session_id") val externalSessionId: String? = null,
    @SerialName("started_at") @Contextual val startedAt: Instant? = null,
    @SerialName("stopped_at") @Contextual val stoppedAt: Instant? = null,
    @SerialName("duration_ms") val durationMs: Long? = null,
    @SerialName("total_duration_ms") val totalDurationMs: Long? = null,
    @SerialName("progress_ms") val progressMs: Long? = null,
    @SerialName("last_paused_at") @Contextual val lastPausedAt: Instant? = null,
    @SerialName("paused_duration_ms") val pausedDurationMs: Long? = null,
    @SerialName("reference_id") val referenceId: String? = null,
    val watched: Boolean? = null,
    @SerialName("ip_address") val ipAddress: String? = null,
    @SerialName("geo_city") val geoCity: String? = null,
    @SerialName("geo_region") val geoRegion: String? = null,
    @SerialName("geo_country") val geoCountry: String? = null,
    @SerialName("geo_continent") val geoContinent: String? = null,
    @SerialName("geo_postal") val geoPostal: String? = null,
    @SerialName("geo_lat") val geoLat: Double? = null,
    @SerialName("geo_lon") val geoLon: Double? = null,
    @SerialName("geo_asn_number") val geoAsnNumber: String? = null,
    @SerialName("geo_asn_organization") val geoAsnOrganization: String? = null,
    @SerialName("player_name") val playerName: String? = null,
    @SerialName("device_id") val deviceId: String? = null,
    val product: String? = null,
    val device: String? = null,
    val platform: String? = null,
    val quality: String? = null,
    @SerialName("is_transcode") val isTranscode: Boolean? = null,
    @SerialName("video_decision") val videoDecision: TracearrStreamDecision? = null,
    @SerialName("audio_decision") val audioDecision: TracearrStreamDecision? = null,
    val bitrate: Int? = null,
    @SerialName("source_video_codec") val sourceVideoCodec: String? = null,
    @SerialName("source_audio_codec") val sourceAudioCodec: String? = null,
    @SerialName("source_audio_channels") val sourceAudioChannels: Int? = null,
    @SerialName("source_video_width") val sourceVideoWidth: Int? = null,
    @SerialName("source_video_height") val sourceVideoHeight: Int? = null,
    @SerialName("source_video_details") val sourceVideoDetails: TracearrSourceVideoDetails? = null,
    @SerialName("source_audio_details") val sourceAudioDetails: TracearrSourceAudioDetails? = null,
    @SerialName("stream_video_codec") val streamVideoCodec: String? = null,
    @SerialName("stream_audio_codec") val streamAudioCodec: String? = null,
    @SerialName("stream_video_details") val streamVideoDetails: TracearrStreamVideoDetails? = null,
    @SerialName("stream_audio_details") val streamAudioDetails: TracearrStreamAudioDetails? = null,
    @SerialName("transcode_info") val transcodeInfo: TracearrTranscodeInfo? = null,
    @SerialName("subtitle_info") val subtitleInfo: TracearrSubtitleInfo? = null,
    @SerialName("channel_title") val channelTitle: String? = null,
    @SerialName("channel_identifier") val channelIdentifier: String? = null,
    @SerialName("channel_thumb") val channelThumb: String? = null,
    val username: String? = null,
    @SerialName("user_thumb") val userThumb: String? = null,
    @SerialName("user_avatar_url") val userAvatarUrl: String? = null,
    val user: TracearrUser? = null,
    val server: TracearrServer? = null,
    @SerialName("can_terminate") val canTerminate: Boolean? = null,
    @SerialName("audio_channels_display") val audioChannelsDisplay: String? = null,
    val genres: List<String> = emptyList(),
    @SerialName("library_id") val libraryId: String? = null,
    val player: String? = null,
    val resolution: String? = null,
    @SerialName("source_audio_codec_display") val sourceAudioCodecDisplay: String? = null,
    @SerialName("source_video_codec_display") val sourceVideoCodecDisplay: String? = null,
    @SerialName("stream_audio_codec_display") val streamAudioCodecDisplay: String? = null,
    @SerialName("stream_video_codec_display") val streamVideoCodecDisplay: String? = null,
    @SerialName("media_details") val mediaDetails: TracearrMediaDetails? = null,
) {
    val effectiveUsername: String
        get() = user?.username ?: username ?: ""

    val effectiveUserAvatar: String?
        get() = user?.avatarUrl ?: user?.thumbUrl ?: userAvatarUrl ?: userThumb

    val effectiveServerName: String
        get() = server?.name ?: serverName ?: ""

    fun rebuildWithInstanceBaseUrl(instanceBaseUrl: String): TracearrStreamSession {
        val cleanBaseUrl = instanceBaseUrl.trimEnd('/')
        fun fixUrl(path: String?): String? {
            if (path.isNullOrEmpty()) return null
            return if (path.startsWith("/")) "$cleanBaseUrl$path" else path
        }

        return copy(
            thumbPath = fixUrl(thumbPath),
            posterUrl = fixUrl(posterUrl),
            userThumb = fixUrl(userThumb),
            userAvatarUrl = fixUrl(userAvatarUrl),
            user = user?.copy(
                thumbUrl = fixUrl(user.thumbUrl),
                avatarUrl = fixUrl(user.avatarUrl),
            ),
        )
    }
}
