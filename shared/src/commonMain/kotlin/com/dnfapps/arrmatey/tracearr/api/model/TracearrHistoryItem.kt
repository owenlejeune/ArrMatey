package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class TracearrHistoryItem(
    val id: String,
    @SerialName("server_id") val serverId: String? = null,
    @SerialName("server_name") val serverName: String? = null,
    @SerialName("server_type") val serverType: TracearrServerType? = null,
    val state: String? = null,
    @SerialName("media_type") val mediaType: TracearrMediaType? = null,
    @SerialName("media_title") val mediaTitle: String? = null,
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
    @SerialName("duration_ms") val durationMs: Long? = null,
    @SerialName("progress_ms") val progressMs: Long? = null,
    @SerialName("total_duration_ms") val totalDurationMs: Long? = null,
    @SerialName("percent_complete") val percentComplete: Double? = null,
    @SerialName("started_at") @Contextual val startedAt: Instant? = null,
    @SerialName("stopped_at") @Contextual val stoppedAt: Instant? = null,
    val watched: Boolean? = null,
    @SerialName("segment_count") val segmentCount: Int? = null,
    val device: String? = null,
    val player: String? = null,
    val product: String? = null,
    val platform: String? = null,
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
    val resolution: String? = null,
    @SerialName("source_video_codec_display") val sourceVideoCodecDisplay: String? = null,
    @SerialName("source_audio_codec_display") val sourceAudioCodecDisplay: String? = null,
    @SerialName("audio_channels_display") val audioChannelsDisplay: String? = null,
    @SerialName("stream_video_codec_display") val streamVideoCodecDisplay: String? = null,
    @SerialName("stream_audio_codec_display") val streamAudioCodecDisplay: String? = null,
    @SerialName("media_id") val mediaId: String? = null,
    @SerialName("show_media_id") val showMediaId: String? = null,
    @SerialName("imdb_id") val imdbId: String? = null,
    @SerialName("tmdb_id") val tmdbId: Long? = null,
    @SerialName("tvdb_id") val tvdbId: Long? = null,
    @SerialName("rating_key") val ratingKey: String? = null,
    @SerialName("parent_rating_key") val parentRatingKey: String? = null,
    @SerialName("grandparent_rating_key") val grandparentRatingKey: String? = null,
    @SerialName("grandparent_title") val grandparentTitle: String? = null,
    @SerialName("library_id") val libraryId: String? = null,
    val genres: List<String> = emptyList(),
    @SerialName("reference_id") val referenceId: String? = null,
    val user: TracearrUser? = null,
    val username: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("user_thumb") val userThumb: String? = null,
    @SerialName("user_avatar_url") val userAvatarUrl: String? = null,
    @SerialName("media_details") val mediaDetails: TracearrMediaDetails? = null,
) {
    val effectiveUsername: String
        get() = user?.username ?: username ?: ""

    val effectiveUserRef: String?
        get() = userId ?: user?.id ?: user?.userId ?: username?.takeIf { it.isNotBlank() } ?: user?.username?.takeIf { it.isNotBlank() }

    val effectiveUserAvatar: String?
        get() = user?.avatarUrl ?: user?.thumbUrl ?: userAvatarUrl ?: userThumb

    val effectiveServerName: String
        get() = serverName ?: ""

    fun rebuildWithInstanceBaseUrl(instanceBaseUrl: String): TracearrHistoryItem {
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
            user =
                user?.copy(
                    thumbUrl = fixUrl(user.thumbUrl),
                    avatarUrl = fixUrl(user.avatarUrl),
                ),
        )
    }

    fun toStreamSession(): TracearrStreamSession =
        TracearrStreamSession(
            id = id,
            serverId = serverId,
            serverName = serverName,
            serverType = serverType,
            serverUserId = user?.userId,
            sessionKey = referenceId,
            state = state ?: "stopped",
            mediaType = mediaType,
            mediaTitle = mediaTitle,
            grandparentTitle = grandparentTitle,
            showTitle = showTitle,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            year = year,
            artistName = artistName,
            albumName = albumName,
            trackNumber = trackNumber,
            discNumber = discNumber,
            thumbPath = thumbPath,
            posterUrl = posterUrl,
            ratingKey = ratingKey,
            serverVersionKey = null,
            parentRatingKey = parentRatingKey,
            grandparentRatingKey = grandparentRatingKey,
            mediaId = mediaId,
            showMediaId = showMediaId,
            imdbId = imdbId,
            tmdbId = tmdbId,
            tvdbId = tvdbId,
            externalSessionId = null,
            startedAt = startedAt,
            stoppedAt = stoppedAt,
            durationMs = durationMs,
            totalDurationMs = totalDurationMs,
            progressMs = progressMs,
            lastPausedAt = null,
            pausedDurationMs = null,
            referenceId = referenceId,
            watched = watched,
            ipAddress = null,
            playerName = player ?: device,
            deviceId = null,
            product = product,
            device = device,
            platform = platform,
            quality = resolution,
            isTranscode = isTranscode,
            videoDecision = videoDecision,
            audioDecision = audioDecision,
            bitrate = bitrate,
            sourceVideoCodec = sourceVideoCodec,
            sourceAudioCodec = sourceAudioCodec,
            sourceAudioChannels = sourceAudioChannels,
            sourceVideoWidth = sourceVideoWidth,
            sourceVideoHeight = sourceVideoHeight,
            sourceVideoDetails = sourceVideoDetails,
            sourceAudioDetails = sourceAudioDetails,
            streamVideoCodec = streamVideoCodec,
            streamAudioCodec = streamAudioCodec,
            streamVideoDetails = streamVideoDetails,
            streamAudioDetails = streamAudioDetails,
            transcodeInfo = transcodeInfo,
            subtitleInfo = subtitleInfo,
            channelTitle = null,
            channelIdentifier = null,
            channelThumb = null,
            username = username,
            userId = userId ?: user?.id ?: user?.userId,
            userThumb = userThumb,
            userAvatarUrl = userAvatarUrl,
            user = user,
            server = null,
            canTerminate = false,
            audioChannelsDisplay = audioChannelsDisplay,
            genres = genres,
            libraryId = libraryId,
            player = player,
            resolution = resolution,
            sourceAudioCodecDisplay = sourceAudioCodecDisplay,
            sourceVideoCodecDisplay = sourceVideoCodecDisplay,
            streamAudioCodecDisplay = streamAudioCodecDisplay,
            streamVideoCodecDisplay = streamVideoCodecDisplay,
            mediaDetails = mediaDetails,
        )
}
