package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TracearrServerSummary(
    @SerialName("server_id") val serverId: String? = null,
    @SerialName("server_name") val serverName: String? = null,
    val total: Int = 0,
    val transcodes: Int = 0,
    @SerialName("direct_streams") val directStreams: Int = 0,
    @SerialName("direct_plays") val directPlays: Int = 0,
    @SerialName("total_bitrate") val totalBitrate: String? = null,
)
