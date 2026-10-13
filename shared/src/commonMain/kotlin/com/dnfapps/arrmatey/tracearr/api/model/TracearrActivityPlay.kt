package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class TracearrActivityPlay(
    val date: String? = null,
    val serverId: String? = null,
    @JsonNames("serverName", "server_name")
    @SerialName("server_name")
    val serverName: String? = null,
    @JsonNames("serverType", "server_type")
    @SerialName("server_type")
    val serverType: TracearrServerType? = null,
    val count: Int = 0,
)
