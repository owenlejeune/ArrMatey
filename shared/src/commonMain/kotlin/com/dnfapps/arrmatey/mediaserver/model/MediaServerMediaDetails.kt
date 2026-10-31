package com.dnfapps.arrmatey.mediaserver.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaServerMediaDetails(
    val serverType: MediaServerType,
    val serverId: String?,
    val serverLabel: String,
    val itemId: String,
    val title: String,
    val deepLinkUrl: String,
    val webFallbackUrl: String? = null,
    val played: Boolean = false,
    val playbackPositionTicks: Long? = null,
    val durationTicks: Long? = null,
    val videoCodec: String? = null,
    val audioCodec: String? = null,
    val videoResolution: String? = null,
    val container: String? = null,
    val bitrate: Long? = null,
    val episodesWatchedCount: Int? = null,
    val totalEpisodesCount: Int? = null,
    val episodeWatchStatus: Map<Pair<Int, Int>, Boolean> = emptyMap(),
    val episodeDeepLinks: Map<Pair<Int, Int>, String> = emptyMap(),
) {
    val progressPercentage: Float?
        get() = if (durationTicks != null && durationTicks > 0 && playbackPositionTicks != null) {
            (playbackPositionTicks.toFloat() / durationTicks.toFloat()).coerceIn(0f, 1f)
        } else {
            null
        }

    val remainingMinutes: Long?
        get() = if (durationTicks != null && playbackPositionTicks != null) {
            val remainingTicks = (durationTicks - playbackPositionTicks).coerceAtLeast(0L)
            remainingTicks / 10_000_000L / 60L
        } else {
            null
        }
}
