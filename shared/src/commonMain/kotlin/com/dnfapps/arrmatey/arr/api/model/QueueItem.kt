package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.formatToOneDecimal
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable(with = QueueItemSerializer::class)
sealed interface QueueItem {
    /**
     * JSON properties
     */
    val id: Int
    var instanceId: Long?
    var instanceName: String?
    val downloadId: String?
    val downloadClient: String?
    val title: String?
    val indexer: String?
    val protocol: ReleaseProtocol
    val size: Float
    var sizeleft: Float
    val timeleft: String?
    val languages: List<Language>
    val quality: QualityInfo
    val customFormats: List<CustomFormat>
    val customFormatScore: Int?
    val added: Instant?
    var estimatedCompletionTime: Instant?
    val status: QueueItemStatus?
    val statusMessages: List<QueueStatusMessage>
    val errorMessage: String?
    val trackedDownloadStatus: QueueDownloadStatus
    val trackedDownloadState: QueueDownloadState
    val outputPath: String?
    val downloadClientHasPostImportCategory: Boolean
    var taskGroupCount: Int?

    /**
     * Computed properties
     */
    val hasIssue: Boolean
        get() = trackedDownloadStatus != QueueDownloadStatus.Ok || status == QueueItemStatus.Warning

    val needsManualImport: Boolean
        get() = downloadId != null &&
                trackedDownloadStatus != QueueDownloadStatus.Warning &&
                trackedDownloadState.isManualImport()

    val taskGroup: String
        get() = (downloadId ?: "") + (title ?: "") + size

    val statusLabel: String
        get() = when (status) {
            null -> "Unknown"
            QueueItemStatus.Completed -> when (trackedDownloadState) {
                QueueDownloadState.ImportPending -> "Import Pending"
                QueueDownloadState.ImportBlocked -> "Import Blocked"
                QueueDownloadState.Imported -> "Importing"
                QueueDownloadState.FailedPending -> "Waiting"
                else -> "Downloading"
            }
            else -> status!!.name
        }

    val progressPercent: Float
        get() = if (sizeleft > 0f) {
            (((size - sizeleft) / size) * 100)
        } else {
            0f
        }

    val progressLabel: String
        get() = "${progressPercent.formatToOneDecimal()}%"

    val remainingTimeLabel: String?
        get() {
            if (trackedDownloadState != QueueDownloadState.Downloading) return null
            val time = estimatedCompletionTime ?: return null
            val timeDiff = time - Clock.System.now()
            if (timeDiff.isNegative()) return null
            return timeDiff.toComponents { hours, minutes, seconds, _ ->
                buildString {
                    if (hours > 0) {
                        append("${hours}h ")
                    }
                    if (minutes > 0 || hours > 0) {
                        append("${minutes}m ")
                    }
                    append("${seconds}s")
                }
            }
        }

    val scoreLabel: String?
        get() {
            return customFormatScore?.let { score ->
                val symbol = if (score < 0) "" else "+"
                 "$symbol$score"
            }
        }

    val languageLabels: List<String>
        get() = languages.takeUnless { it.isEmpty() }?.mapNotNull { it.name } ?: listOf("Unknown")

    val titleLabel: String
    val mediaId: Long?
}

object QueueItemSerializer : JsonContentPolymorphicSerializer<QueueItem>(QueueItem::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<QueueItem> {
        val jsonObject = element.jsonObject

        return when {
            "movie" in jsonObject -> RadarrQueueItem.serializer()
            "series" in jsonObject -> SonarrQueueItem.serializer()
            "album" in jsonObject -> LidarrQueueItem.serializer()
            else -> throw SerializationException("Unknown MediaItem type")
        }
    }
}
