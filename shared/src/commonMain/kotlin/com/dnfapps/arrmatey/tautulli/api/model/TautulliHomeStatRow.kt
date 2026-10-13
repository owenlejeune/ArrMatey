package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TautulliHomeStatRow(
    val title: String? = null,
    @SerialName("user_id") val userId: Long? = null,
    val user: String? = null,
    @SerialName("friendly_name") val friendlyName: String? = null,
    @SerialName("user_thumb") val userThumb: String? = null,
    @SerialName("total_plays") val totalPlays: Long? = null,
    @SerialName("total_duration") val totalDuration: Long? = null,
    @SerialName("rating_key") val ratingKey: String? = null,
    val thumb: String? = null,
    @SerialName("grandparent_title") val grandparentTitle: String? = null,
    @SerialName("parent_title") val parentTitle: String? = null,
    @SerialName("media_type") val mediaType: TautulliMediaType? = null,
    val platform: String? = null,
)
