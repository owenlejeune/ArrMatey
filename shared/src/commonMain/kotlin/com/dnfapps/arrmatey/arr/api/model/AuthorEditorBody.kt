package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthorEditorBody(
    val authorIds: List<Long>,
    val monitored: Boolean,
    val monitorNewItems: AuthorMonitorType,
    val qualityProfileId: Int,
    val rootFolderPath: String?,
    val tags: List<Int>,
    val applyTags: ApplyTags,
    val moveFiles: Boolean
)