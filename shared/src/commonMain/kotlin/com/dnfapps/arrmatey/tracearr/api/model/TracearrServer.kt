package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class TracearrServer(
    @JsonNames("id", "server_id")
    @SerialName("server_id")
    val serverId: String? = null,

    @JsonNames("name", "server_name")
    @SerialName("server_name")
    val serverName: String? = null,

    val status: String? = null,
    val reason: String? = null,

    @JsonNames("type", "server_type")
    @SerialName("server_type")
    val serverType: TracearrServerType? = null,

    val historical: Boolean = false,

    @SerialName("active_streams")
    val activeStreams: Int? = null,

    val version: String? = null,
) {
    val id: String? get() = serverId
    val name: String? get() = serverName
    val type: TracearrServerType? get() = serverType
}
