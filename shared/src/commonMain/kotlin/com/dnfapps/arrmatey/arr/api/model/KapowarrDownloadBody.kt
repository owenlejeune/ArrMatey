package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KapowarrDownloadBody(
    val link: String,
    @SerialName("indexer_id") val indexerId: Int,
    @SerialName("force_match") val forceMatch: Boolean = false,
)
