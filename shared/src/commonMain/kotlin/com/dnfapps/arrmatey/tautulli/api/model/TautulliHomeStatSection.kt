package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TautulliHomeStatSection(
    @SerialName("stat_id") val statId: TautulliHomeStatId? = null,
    @SerialName("stat_type") val statType: String? = null,
    @SerialName("stat_title") val statTitle: String? = null,
    val rows: List<TautulliHomeStatRow> = emptyList(),
)
