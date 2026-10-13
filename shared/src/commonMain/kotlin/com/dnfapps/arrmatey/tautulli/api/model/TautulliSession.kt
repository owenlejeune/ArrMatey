package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TautulliSession(
    @SerialName("session_key") val sessionKey: String? = null,
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("rating_key") val ratingKey: String? = null,
    @SerialName("parent_rating_key") val parentRatingKey: String? = null,
    @SerialName("grandparent_rating_key") val grandparentRatingKey: String? = null,
    @SerialName("media_type") val mediaType: TautulliMediaType? = null,
    val title: String? = null,
    @SerialName("parent_title") val parentTitle: String? = null,
    @SerialName("grandparent_title") val grandparentTitle: String? = null,
    @SerialName("full_title") val fullTitle: String? = null,
    val year: String? = null,
    @SerialName("friendly_name") val friendlyName: String? = null,
    val user: String? = null,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("user_thumb") val userThumb: String? = null,
    val thumb: String? = null,
    @SerialName("parent_thumb") val parentThumb: String? = null,
    @SerialName("grandparent_thumb") val grandparentThumb: String? = null,
    val art: String? = null,
    val player: String? = null,
    val device: String? = null,
    val platform: String? = null,
    @SerialName("platform_name") val platformName: String? = null,
    val product: String? = null,
    @SerialName("quality_profile") val qualityProfile: String? = null,
    val container: String? = null,
    @SerialName("stream_container") val streamContainer: String? = null,
    @SerialName("transcode_video_codec") val transcodeVideoCodec: String? = null,
    @SerialName("transcode_audio_codec") val transcodeAudioCodec: String? = null,
    @SerialName("video_full_resolution") val videoFullResolution: String? = null,
    val state: TautulliStreamState? = null,
    @SerialName("progress_percent") val progressPercent: String? = null,
    @SerialName("view_offset") val viewOffset: String? = null,
    val duration: String? = null,
    val bandwidth: String? = null,
    @SerialName("ip_address") val ipAddress: String? = null,
    @SerialName("ip_address_public") val ipAddressPublic: String? = null,
    val location: TautulliStreamLocation? = null,
    @SerialName("transcode_decision") val transcodeDecision: TautulliStreamDecision? = null,
    @SerialName("stream_container_decision") val streamContainerDecision: TautulliStreamDecision? = null,
    @SerialName("video_decision") val videoDecision: TautulliStreamDecision? = null,
    @SerialName("audio_decision") val audioDecision: TautulliStreamDecision? = null,
    @SerialName("subtitle_decision") val subtitleDecision: TautulliStreamDecision? = null,
    @SerialName("video_codec") val videoCodec: String? = null,
    @SerialName("stream_video_codec") val streamVideoCodec: String? = null,
    @SerialName("video_resolution") val videoResolution: String? = null,
    @SerialName("stream_video_resolution") val streamVideoResolution: String? = null,
    @SerialName("stream_video_full_resolution") val streamVideoFullResolution: String? = null,
    @SerialName("video_bitrate") val videoBitrate: String? = null,
    @SerialName("stream_video_bitrate") val streamVideoBitrate: String? = null,
    @SerialName("audio_codec") val audioCodec: String? = null,
    @SerialName("stream_audio_codec") val streamAudioCodec: String? = null,
    @SerialName("audio_channels") val audioChannels: String? = null,
    @SerialName("stream_audio_channels") val streamAudioChannels: String? = null,
    @SerialName("audio_bitrate") val audioBitrate: String? = null,
    @SerialName("stream_audio_bitrate") val streamAudioBitrate: String? = null,
    @SerialName("transcode_speed") val transcodeSpeed: String? = null,
    @SerialName("transcode_hw_decoding") val transcodeHwDecoding: Int? = null,
    @SerialName("transcode_hw_encoding") val transcodeHwEncoding: Int? = null,
    val guid: String? = null,
    @SerialName("parent_guid") val parentGuid: String? = null,
    @SerialName("grandparent_guid") val grandparentGuid: String? = null,
    val guids: List<String> = emptyList(),
    @Serializable(with = NullableStringOrIntSerializer::class)
    @SerialName("media_index") val mediaIndex: String? = null,
    @Serializable(with = NullableStringOrIntSerializer::class)
    @SerialName("parent_media_index") val parentMediaIndex: String? = null,
    @Serializable(with = NullableStringOrIntSerializer::class)
    @SerialName("server_name") val serverName: String? = null,
    @Serializable(with = NullableStringOrIntSerializer::class)
    @SerialName("pms_name") val pmsName: String? = null,
) {
    val id: String
        get() = sessionId ?: sessionKey ?: ratingKey ?: ""

    val effectiveTitle: String
        get() = when (mediaType) {
            TautulliMediaType.Episode -> {
                val show = grandparentTitle ?: ""
                val ep = title ?: fullTitle ?: ""
                if (show.isNotBlank() && ep.isNotBlank()) "$show - $ep" else ep.ifBlank { show }
            }
            TautulliMediaType.Track -> {
                val artist = grandparentTitle ?: ""
                val track = title ?: ""
                if (artist.isNotBlank() && track.isNotBlank()) "$artist - $track" else track.ifBlank { artist }
            }
            else -> title ?: fullTitle ?: ""
        }

    val effectiveUser: String
        get() = friendlyName?.takeIf { it.isNotBlank() } ?: user?.takeIf { it.isNotBlank() } ?: "Unknown"

    val progressFloat: Float
        get() = progressPercent?.toFloatOrNull()?.let { (it / 100f).coerceIn(0f, 1f) } ?: 0f

    val isTranscoding: Boolean
        get() = transcodeDecision == TautulliStreamDecision.Transcode ||
            videoDecision == TautulliStreamDecision.Transcode ||
            audioDecision == TautulliStreamDecision.Transcode

    val isDirectPlay: Boolean
        get() = transcodeDecision == TautulliStreamDecision.DirectPlay &&
            (videoDecision == null || videoDecision == TautulliStreamDecision.DirectPlay) &&
            (audioDecision == null || audioDecision == TautulliStreamDecision.DirectPlay)
}
