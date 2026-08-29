package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SeriesEditorBody(
    val seriesIds: List<Long>,
    val monitored: Boolean,
    val monitorNewItems: MonitorNewItems,
    val seriesType: SeriesType,
    val seasonFolder: Boolean,
    val qualityProfileId: Int,
    val rootFolderPath: String?,
    val tags: List<Int>,
    val applyTags: ApplyTags,
    val moveFiles: Boolean,
)
