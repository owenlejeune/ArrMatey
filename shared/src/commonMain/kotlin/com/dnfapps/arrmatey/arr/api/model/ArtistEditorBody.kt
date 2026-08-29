package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ArtistEditorBody(
    val artistIds: List<Long>,
    val monitored: Boolean,
    val monitorNewItems: ArtistMonitorType,
    val qualityProfileId: Int,
    val rootFolderPath: String?,
    val tags: List<Int>,
    val applyTags: ApplyTags,
    val moveFiles: Boolean,
)
