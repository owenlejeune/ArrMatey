package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MovieEditorBody(
    val movieIds: List<Long>,
    val monitored: Boolean,
    val qualityProfileId: Int,
    val minimumAvailability: MediaStatus,
    val rootFolderPath: String,
    val tags: List<Int>,
    val applyTags: ApplyTags,
    val moveFiles: Boolean,
)
