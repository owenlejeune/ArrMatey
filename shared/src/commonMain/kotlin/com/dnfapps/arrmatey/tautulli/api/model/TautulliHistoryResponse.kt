package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TautulliHistoryResponse(
    @SerialName("recordsFiltered") val recordsFiltered: Int? = null,
    @SerialName("recordsTotal") val recordsTotal: Int? = null,
    @SerialName("draw") val draw: Int? = null,
    val data: List<TautulliHistoryItem> = emptyList(),
)
