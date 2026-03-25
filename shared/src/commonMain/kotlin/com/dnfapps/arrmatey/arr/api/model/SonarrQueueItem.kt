package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SonarrQueueItem(
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
    override val downloadClientHasPostImportCategory: Boolean = false,
    override var taskGroupCount: Int? = null,

    val seriesId: Long? = null,
    val series: ArrSeries? = null,
    val episodeId: Long? = null,
    val episode: Episode? = null,
    val episodeHasFile: Boolean? = null,
    val seasonNumber: Int? = null
): QueueItem {
    val calcSeriesId: Long?
        get() = seriesId ?: series?.id

    val calcEpisodeId: Long?
        get() = episodeId ?: episode?.id

    override val taskGroup: String
        get() = super.taskGroup + seasonNumber

    override val titleLabel: String
        get() {
            series?.title?.let { seriesTitle ->
                taskGroupCount?.let { count ->
                    if (count > 1 && seasonNumber != null) {
                        val seasonText = "Season $seasonNumber"
                        return "$seriesTitle ($seasonText)"
                    }
                }
                episode?.episodeLabel?.let { episodeLabel ->
                    return "$seriesTitle $episodeLabel"
                }
            }
            return series?.title ?: "Unknown"
        }

    override val mediaId: Long?
        get() = seriesId ?: series?.id ?: episode?.seriesId
}
