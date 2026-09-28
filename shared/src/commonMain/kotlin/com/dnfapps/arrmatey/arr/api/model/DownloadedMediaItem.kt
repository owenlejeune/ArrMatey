package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlin.time.Instant

data class DownloadedMediaItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val episodeInfo: String? = null,
    val episodeTitle: String? = null,
    val media: ArrMedia? = null,
    val date: Instant? = null,
    val quality: String? = null,
    val size: Long? = null,
    val languages: List<String> = emptyList(),
    val indexer: String? = null,
    val customFormats: List<String> = emptyList(),
    val instanceId: Long = 0L,
    val instanceName: String = "",
    val instanceType: InstanceType = InstanceType.Radarr,
) {
    val added: Instant? get() = date
    val uniqueKey: String get() = id
}
