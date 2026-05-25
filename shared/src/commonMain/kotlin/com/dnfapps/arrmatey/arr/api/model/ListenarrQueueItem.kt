package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Serializable
data class ListenarrQueueItem(
    @SerialName("id")
    val idStr: String,
    override val title: String,
    val author: String? = null,
    @SerialName("quality")
    val qualityStr: String,
    @SerialName("status")
    val statusStr: String,
    val progress: Double,
    @SerialName("size")
    val sizeLong: Long,
    val downloaded: Long,
    val downloadSpeed: Long,
    val eta: Long,
    override val downloadClient: String? = null,
    val downloadClientId: String? = null,
    val downloadClientType: String? = null,
    @Serializable(with = ListenarrInstantSerializer::class)
    val addedAt: Instant? = null,
    val isStaleSnapshot: Boolean = false,
    val snapshotState: String? = null,
    @Serializable(with = ListenarrInstantSerializer::class)
    val snapshotRefreshedAt: Instant? = null,
    val canPause: Boolean = false,
    val canRemove: Boolean = false,
    val seeders: Int = 0,
    val leechers: Int = 0,
    val ratio: Float = 0f,
    val audiobookId: Long? = null,
    val remotePath: String? = null,
    val localPath: String? = null,

    override var instanceId: Long? = null,
    override var instanceName: String? = null
) : QueueItem {

    override val id: Int
        get() = idStr.hashCode()

    override val downloadId: String?
        get() = downloadClientId

    override val indexer: String?
        get() = null

    override val protocol: ReleaseProtocol
        get() = when (downloadClientType?.lowercase()) {
            "qbittorrent", "transmission", "deluge" -> ReleaseProtocol.Torrent
            "sabnzbd" -> ReleaseProtocol.Usenet
            else -> ReleaseProtocol.Unknown
        }

    override val size: Float
        get() = sizeLong.toFloat()

    override var sizeleft: Float
        get() = (sizeLong - downloaded).toFloat()
        set(_) {}

    override val timeleft: String?
        get() = if (eta > 0) {
            val h = eta / 3600
            val m = (eta % 3600) / 60
            val s = eta % 60
            buildString {
                if (h > 0) append("${h}h ")
                if (m > 0 || h > 0) append("${m}m ")
                append("${s}s")
            }
        } else null

    override val languages: List<Language>
        get() = emptyList()

    override val quality: QualityInfo
        get() = QualityInfo(
            quality = Quality(id = 0, name = qualityStr),
            revision = Revision(version = 1, real = 0, isRepack = false)
        )

    override val customFormats: List<CustomFormat>
        get() = emptyList()

    override val customFormatScore: Int?
        get() = null

    override val added: Instant?
        get() = addedAt

    override var estimatedCompletionTime: Instant?
        get() = if (eta > 0) Clock.System.now().plus(eta.seconds) else null
        set(_) {}

    override val status: QueueItemStatus?
        get() = when (statusStr.lowercase()) {
            "queued" -> QueueItemStatus.Queued
            "paused" -> QueueItemStatus.Paused
            "downloading" -> QueueItemStatus.Downloading
            "completed" -> QueueItemStatus.Completed
            "failed" -> QueueItemStatus.Failed
            "warning" -> QueueItemStatus.Warning
            "delay" -> QueueItemStatus.Delay
            else -> QueueItemStatus.Unknown
        }

    override val statusMessages: List<QueueStatusMessage>
        get() = emptyList()

    override val errorMessage: String?
        get() = null

    override val trackedDownloadStatus: QueueDownloadStatus
        get() = when (status) {
            QueueItemStatus.Warning -> QueueDownloadStatus.Warning
            QueueItemStatus.Failed -> QueueDownloadStatus.Error
            else -> QueueDownloadStatus.Ok
        }

    override val trackedDownloadState: QueueDownloadState
        get() = when (statusStr.lowercase()) {
            "downloading" -> QueueDownloadState.Downloading
            "completed" -> QueueDownloadState.Imported
            "failed" -> QueueDownloadState.Failed
            else -> QueueDownloadState.Downloading
        }

    override val outputPath: String?
        get() = remotePath

    override val downloadClientHasPostImportCategory: Boolean
        get() = false

    override var taskGroupCount: Int?
        get() = null
        set(_) {}

    override val titleLabel: String
        get() = title

    override val mediaId: Long?
        get() = audiobookId
}
