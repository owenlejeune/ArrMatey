package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DelugeJsonRpcResponse<T>(
    @SerialName("id") val id: Int? = null,
    @SerialName("result") val result: T? = null,
    @SerialName("error") val error: JsonElement? = null
)
