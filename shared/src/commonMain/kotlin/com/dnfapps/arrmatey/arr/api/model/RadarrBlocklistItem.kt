package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class RadarrBlocklistItem(
    override val id: Long,
    override val sourceTitle: String? = null,
    override val quality: QualityInfo,
    @Contextual override val date: Instant,
    override val protocol: ReleaseProtocol? = null,
    override val indexer: String? = null,
    override val message: String? = null,
    override val languages: List<Language> = emptyList(),
    override val customFormats: List<CustomFormat> = emptyList(),
    override val customFormatScore: Int? = null,
    override val data: Map<String, String?> = emptyMap(),
    override val instanceId: Long? = null,
    override val instanceName: String? = null,
    override val instanceType: InstanceType? = null,
    val movieId: Long,
    val movie: ArrMovie? = null,
) : BlocklistItem {
    override val displayTitle: String?
        get() = movie?.title ?: super.displayTitle
}
