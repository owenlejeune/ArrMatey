package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SonarrHistoryItem(
    override val id: Long,
    override val eventType: HistoryEventType,
    @Contextual override val date: Instant,
    override val sourceTitle: String? = null,
    override val quality: QualityInfo,
    override val languages: List<Language> = emptyList(),
    override val customFormats: List<CustomFormat> = emptyList(),
    override val customFormatScore: Int? = null,
    override val data: Map<String, String?> = emptyMap(),
    override val instanceId: Long? = null,
    override val instanceName: String? = null,
    override val instanceType: InstanceType? = null,
    val seriesId: Long,
    val episodeId: Long? = null,
    val series: ArrSeries? = null,
    val episode: Episode? = null,
) : HistoryItem {
    override val displayTitle: String?
        get() =
            when {
                series != null && episode != null ->
                    "${series.title} - ${episode.seasonEpLabel}${if (!episode.title.isNullOrBlank()) " - ${episode.title}" else ""}"
                series != null -> series.title
                episode != null -> episode.title
                else -> super.displayTitle
            }
}
