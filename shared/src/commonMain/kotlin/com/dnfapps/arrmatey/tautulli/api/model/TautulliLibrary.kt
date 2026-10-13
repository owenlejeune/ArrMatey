package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TautulliLibrary(
    @SerialName("section_id") val sectionId: String,
    @SerialName("section_name") val sectionName: String,
    @SerialName("section_type") val sectionType: TautulliSectionType? = null,
    val count: String? = null,
    @SerialName("parent_count") val parentCount: String? = null,
    @SerialName("child_count") val childCount: String? = null,
    val thumb: String? = null,
)
