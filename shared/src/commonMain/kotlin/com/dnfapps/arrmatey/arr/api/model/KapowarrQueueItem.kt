package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class KapowarrQueueResponse(
    val error: String? = null,
    val result: List<KapowarrQueueItem>? = emptyList(),
)

@Serializable
data class KapowarrQueueItem(
    override val id: Int,
    @SerialName("volume_id") val volumeId: Long? = null,
    @SerialName("issue_id") val issueId: Long? = null,
    @SerialName("web_link") val webLink: String? = null,
    @SerialName("web_title") val webTitle: String? = null,
    @SerialName("web_sub_title") val webSubTitle: String? = null,
    @SerialName("download_link") val downloadLink: String? = null,
    @SerialName("pure_link") val pureLink: String? = null,
    @SerialName("download_service") val downloadService: String? = null,
    @SerialName("source_name") val sourceName: String? = null,
    @SerialName("type") val downloadServiceType: String? = null,
    val file: String? = null,
    override val title: String? = null,
    @SerialName("download_folder") val downloadFolder: String? = null,
    override val size: Float = 0f,
    @SerialName("status") val queueStatusString: String? = null,
    val progress: Float = 0f,
    val speed: Float = 0f,
    override var instanceId: Long? = null,
    override var instanceName: String? = null,
    override var instanceType: InstanceType? = null,
) : QueueItem {
    override val status: QueueItemStatus?
        get() = when (queueStatusString?.lowercase()) {
            "completed", "importing" -> QueueItemStatus.Completed
            "downloading" -> QueueItemStatus.Downloading
            "warning", "error" -> QueueItemStatus.Warning
            else -> QueueItemStatus.Completed
        }

    override val downloadId: String = id.toString()
    override val downloadClient: String? = downloadService ?: sourceName
    override val indexer: String? = downloadService ?: sourceName
    override val protocol: ReleaseProtocol = ReleaseProtocol.Usenet
    override var sizeleft: Float = size * (1f - (progress / 100f).coerceIn(0f, 1f))
    override val timeleft: String? = null
    override val languages: List<Language> = emptyList()
    override val quality: QualityInfo = QualityInfo(
        quality = Quality(id = 1, name = "CBR/CBZ"),
        revision = Revision(version = 1, real = 0, isRepack = false),
    )
    override val customFormats: List<CustomFormat> = emptyList()
    override val customFormatScore: Int? = null
    override val added: Instant? = null
    override var estimatedCompletionTime: Instant? = null
    override val statusMessages: List<QueueStatusMessage> = emptyList()
    override val errorMessage: String? = null
    override val trackedDownloadStatus: QueueDownloadStatus = QueueDownloadStatus.Ok
    override val trackedDownloadState: QueueDownloadState = QueueDownloadState.Downloading
    override val outputPath: String? = file
    override val downloadClientHasPostImportCategory: Boolean = false
    override var taskGroupCount: Int? = null

    override val titleLabel: String
        get() = title ?: webTitle ?: "Unknown Queue Item"

    override val mediaId: Long?
        get() = issueId ?: volumeId

    override val type: InstanceType
        get() = InstanceType.Kapowarr
}
