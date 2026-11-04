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
    val audioChannels: Int? = null,
    val videoRange: String? = null,
    val episodesWatchedCount: Int? = null,
    val totalEpisodesCount: Int? = null,
    val episodeWatchStatus: Map<Pair<Int, Int>, Boolean> = emptyMap(),
    val episodeDeepLinks: Map<Pair<Int, Int>, String> = emptyMap(),
    val episodeItemIds: Map<Pair<Int, Int>, String> = emptyMap(),
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

    val audioLayoutString: String?
        get() = when (audioChannels) {
            1 -> "Mono"
            2 -> "Stereo"
            6 -> "5.1"
            8 -> "7.1"
            null -> null
            else -> "${audioChannels}ch"
        }

    val hdrString: String?
        get() = when {
            videoRange?.contains("dovi", ignoreCase = true) == true || videoRange?.contains("dv", ignoreCase = true) == true -> "Dolby Vision"
            videoRange?.contains("hdr10+", ignoreCase = true) == true -> "HDR10+"
            videoRange?.contains("hdr", ignoreCase = true) == true -> "HDR"
            videoRange?.equals("sdr", ignoreCase = true) == true -> null
            else -> videoRange?.uppercase()
        }
}
