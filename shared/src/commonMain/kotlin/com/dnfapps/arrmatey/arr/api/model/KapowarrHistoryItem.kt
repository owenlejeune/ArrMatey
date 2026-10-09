package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class KapowarrHistoryItem(
    @SerialName("web_link") val webLink: String? = null,
    @SerialName("web_title") val webTitle: String? = null,
    @SerialName("web_sub_title") val webSubTitle: String? = null,
    @SerialName("file_title") val fileTitle: String? = null,
    @SerialName("volume_id") val volumeId: Long? = null,
    @SerialName("issue_id") val issueId: Long? = null,
    val source: String? = null,
    @SerialName("downloaded_at") val downloadedAt: Long? = null,
    val success: Boolean? = null,
    override val instanceId: Long? = null,
    override val instanceName: String? = null,
    override val instanceType: InstanceType? = InstanceType.Kapowarr,
) : HistoryItem {

    override val id: Long
        get() = (issueId ?: volumeId ?: 0L) * 100_000_000L + (downloadedAt ?: 0L)

    override val eventType: HistoryEventType
        get() = when (success) {
            true -> HistoryEventType.DownloadImported
            else -> HistoryEventType.Unknown
        }

    override val date: Instant
        get() = downloadedAt?.let { Instant.fromEpochSeconds(it) } ?: Instant.DISTANT_PAST

    override val sourceTitle: String?
        get() = fileTitle ?: webTitle ?: webSubTitle

    override val quality: QualityInfo?
        get() = null

    override val languages: List<Language>
        get() = emptyList()

    override val customFormats: List<CustomFormat>
        get() = emptyList()

    override val customFormatScore: Int?
        get() = null

    override val data: Map<String, String?>
        get() = buildMap {
            source?.let { put("indexer", it) }
            webLink?.let { put("webLink", it) }
            webTitle?.let { put("webTitle", it) }
        }

    override val displayTitle: String?
        get() = fileTitle ?: webTitle ?: webSubTitle
}
