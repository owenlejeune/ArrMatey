package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TautulliActivity(
    @SerialName("stream_count") val streamCount: String? = null,
    @SerialName("total_bandwidth") val totalBandwidth: Long? = null,
    @SerialName("lan_bandwidth") val lanBandwidth: Long? = null,
    @SerialName("wan_bandwidth") val wanBandwidth: Long? = null,
    val sessions: List<TautulliSession> = emptyList(),
) {
    val streamCountInt: Int
        get() = streamCount?.toIntOrNull() ?: sessions.size
}
