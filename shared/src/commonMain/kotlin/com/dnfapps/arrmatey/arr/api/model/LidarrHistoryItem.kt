package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class LidarrHistoryItem(
    override val id: Long,
    override val eventType: HistoryEventType,
    @Contextual override val date: Instant,
    override val sourceTitle: String? = null,
    override val quality: QualityInfo,
    override val customFormats: List<CustomFormat> = emptyList(),
    override val customFormatScore: Int? = 0,
    override val data: Map<String, String?> = emptyMap(),
    val albumId: Long = 0,
    val artistId: Long = 0,
    val trackId: Long = 0,
    val downloadId: String? = null,
    val qualityCutoffNotMet: Boolean = false,
    val album: ArrAlbum? = null,
    val artist: Arrtist? = null,
    val track: LidarrTrack? = null,
) : HistoryItem {
    override val languages: List<Language>
        get() = emptyList()

    override val displayTitle: String?
        get() =
            when {
                track != null -> "${artist?.title} - ${track.title}"
                album != null -> "${artist?.title} - ${album.title}"
                else -> super.displayTitle
            }
}
