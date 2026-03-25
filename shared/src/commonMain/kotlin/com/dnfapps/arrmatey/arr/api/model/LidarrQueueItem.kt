package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class LidarrQueueItem (
    override val id: Int,
    override var instanceId: Long? = null,
    override var instanceName: String? = null,
    override val downloadId: String? = null,
    override val downloadClient: String? = null,
    override val title: String? = null,
    override val indexer: String? = null,
    override val protocol: ReleaseProtocol,
    override val size: Float,
    override var sizeleft: Float,
    override val timeleft: String? = null,
    override val languages: List<Language> = emptyList(),
    override val quality: QualityInfo,
    override val customFormats: List<CustomFormat> = emptyList(),
    override val customFormatScore: Int? = null,
    @Contextual override val added: Instant? = null,
    @Contextual override var estimatedCompletionTime: Instant? = null,
    override val status: QueueItemStatus? = null,
    override val statusMessages: List<QueueStatusMessage> = emptyList(),
    override val errorMessage: String? = null,
    override val trackedDownloadStatus: QueueDownloadStatus,
    override val trackedDownloadState: QueueDownloadState,
    override val outputPath: String? = null,
    override val downloadClientHasPostImportCategory: Boolean,
    override var taskGroupCount: Int? = null,

    val albumId: Long? = null,
    val album: ArrAlbum? = null,
    val artistId: Long? = null,
    val artist: Arrtist? = null
): QueueItem {
    override val taskGroup: String
        get() = super.taskGroup + id

    override val titleLabel: String
        get() = album?.title ?: title ?: "Unknown"

    override val mediaId: Long?
        get() = albumId ?: artistId
}