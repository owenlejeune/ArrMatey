package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlin.time.Instant

@Serializable
data class QueueSeasonResource(
    val seasonNumber: Int,
    val monitored: Boolean = true,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SonarrQueueItem(
    override val id: Int,
    override var instanceId: Long? = null,
    override var instanceName: String? = null,
    override var instanceType: InstanceType? = null,
    override val downloadId: String? = null,
    override val downloadClient: String? = null,
    override val title: String? = null,
    override val indexer: String? = null,
    override val protocol: ReleaseProtocol,
    override val size: Float,
    @JsonNames("sizeLeft") override var sizeleft: Float = 0f,
    @JsonNames("timeLeft") override val timeleft: String? = null,
    override val languages: List<Language> = emptyList(),
    override val quality: QualityInfo,
    override val customFormats: List<CustomFormat> = emptyList(),
    override val customFormatScore: Int? = null,
    @Contextual override val added: Instant? = null,
    @Contextual override var estimatedCompletionTime: Instant? = null,
    override val status: QueueItemStatus? = null,
    override val statusMessages: List<QueueStatusMessage> = emptyList(),
    override val errorMessage: String? = null,
    override val trackedDownloadStatus: QueueDownloadStatus = QueueDownloadStatus.Ok,
    override val trackedDownloadState: QueueDownloadState = QueueDownloadState.Downloading,
    override val outputPath: String? = null,
    override val downloadClientHasPostImportCategory: Boolean = false,
    override var taskGroupCount: Int? = null,

    // Series
    val seriesId: Long? = null,
    val series: ArrSeries? = null,

    // V5 Multi-episode / season pack fields
    val episodeIds: List<Long> = emptyList(),
    val episodes: List<Episode> = emptyList(),
    val seasons: List<QueueSeasonResource> = emptyList(),
    val episodesWithFilesCount: Int? = null,
    val isFullSeason: Boolean = false,

    // V3 Legacy single-episode fields (for backward compatibility & named constructor args)
    @SerialName("episodeId") val episodeId: Long? = null,
    @SerialName("episode") val episode: Episode? = null,
    val episodeHasFile: Boolean? = null,
    @SerialName("seasonNumber") val seasonNumber: Int? = null,
) : QueueItem {
    override val type: InstanceType
        get() = instanceType ?: InstanceType.Sonarr

    val effectiveEpisodeIds: List<Long>
        get() = episodeIds.ifEmpty { listOfNotNull(episodeId ?: episode?.id) }

    val primaryEpisodeId: Long?
        get() = effectiveEpisodeIds.firstOrNull()

    val effectiveEpisodes: List<Episode>
        get() = episodes.ifEmpty { listOfNotNull(episode) }

    val primaryEpisode: Episode?
        get() = effectiveEpisodes.firstOrNull()

    val primarySeasonNumber: Int?
        get() = seasons.firstOrNull()?.seasonNumber ?: seasonNumber

    val calcSeriesId: Long?
        get() = seriesId ?: series?.id

    val calcEpisodeId: Long?
        get() = primaryEpisodeId

    override val taskGroup: String
        get() = super.taskGroup + (primarySeasonNumber ?: "")

    override val titleLabel: String
        get() {
            series?.title?.let { seriesTitle ->
                if (isFullSeason || seasons.isNotEmpty()) {
                    primarySeasonNumber?.let { season ->
                        return "$seriesTitle (Season $season)"
                    }
                }
                taskGroupCount?.let { count ->
                    if (count > 1 && primarySeasonNumber != null) {
                        val seasonText = "Season $primarySeasonNumber"
                        return "$seriesTitle ($seasonText)"
                    }
                }
                primaryEpisode?.episodeLabel?.let { episodeLabel ->
                    return "$seriesTitle $episodeLabel"
                }
            }
            return series?.title ?: "Unknown"
        }

    override val mediaId: Long?
        get() = seriesId ?: series?.id ?: primaryEpisode?.seriesId
}
