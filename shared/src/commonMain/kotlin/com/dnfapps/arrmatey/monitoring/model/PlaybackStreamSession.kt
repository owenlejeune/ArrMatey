package com.dnfapps.arrmatey.monitoring.model

import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.tracearr.api.model.TracearrServerType

data class PlaybackStreamSession(
    val sessionId: String,
    val sessionKey: String? = null,
    val instanceId: Long,
    val instanceLabel: String,
    val instanceType: InstanceType,
    val serverName: String? = null,
    val serverType: TracearrServerType? = null,
    val ratingKey: String? = null,
    val tmdbId: Long? = null,
    val mediaType: PlaybackMediaType = PlaybackMediaType.Other,
    val title: String,
    val parentTitle: String? = null,
    val grandparentTitle: String? = null,
    val displayTitle: String,
    val subtitle: String? = null,
    val year: String? = null,
    val username: String,
    val userDisplayName: String,
    val userAvatarUrl: String? = null,
    val player: String? = null,
    val device: String? = null,
    val platform: String? = null,
    val ipAddress: String? = null,
    val location: String? = null,
    val progressPercent: Float = 0f,
    val progressMs: Long? = null,
    val totalDurationMs: Long? = null,
    val state: String? = null,
    val streamDecision: PlaybackStreamDecision = PlaybackStreamDecision.DirectPlay,
    val isTranscoding: Boolean = false,
    val videoCodec: String? = null,
    val videoResolution: String? = null,
    val transcodeVideoCodec: String? = null,
    val audioCodec: String? = null,
    val transcodeAudioCodec: String? = null,
    val bandwidthKbps: Long? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val product: String? = null,
    val qualityProfile: String? = null,
    val container: String? = null,
    val videoDecision: PlaybackStreamDecision? = null,
    val audioDecision: PlaybackStreamDecision? = null,
    val canTerminate: Boolean = false,
) {
    val progressPercentInt: Int
        get() {
            val raw = progressPercent
            return if (raw <= 1f) (raw * 100f).toInt() else raw.toInt()
        }

    val progressPercentFormatted: String
        get() = "$progressPercentInt%"
}
