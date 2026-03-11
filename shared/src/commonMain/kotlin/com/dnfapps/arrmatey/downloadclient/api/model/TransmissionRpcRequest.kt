package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class TransmissionRpcRequest(
    @SerialName("method") val method: String,
    @SerialName("arguments") val arguments: JsonObject? = null
)
