package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TautulliMetadata(
    @SerialName("rating_key") val ratingKey: String,
    @SerialName("media_type") val mediaType: TautulliMediaType? = null,
    val title: String? = null,
    @SerialName("parent_title") val parentTitle: String? = null,
    @SerialName("grandparent_title") val grandparentTitle: String? = null,
    val year: String? = null,
    val duration: String? = null,
    val thumb: String? = null,
    val art: String? = null,
    val guid: String? = null,
    val guids: List<String> = emptyList(),
)
