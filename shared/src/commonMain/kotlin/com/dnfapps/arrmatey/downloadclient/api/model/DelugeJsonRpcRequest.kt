package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DelugeJsonRpcRequest(
    @SerialName("method") val method: String,
    @SerialName("params") val params: List<JsonElement> = emptyList(),
    @SerialName("id") val id: Int
)
