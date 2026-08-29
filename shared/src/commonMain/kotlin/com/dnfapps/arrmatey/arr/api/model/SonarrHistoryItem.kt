package com.dnfapps.arrmatey.arr.api.model

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
    val seriesId: Long,
    val episodeId: Long? = null,
) : HistoryItem
