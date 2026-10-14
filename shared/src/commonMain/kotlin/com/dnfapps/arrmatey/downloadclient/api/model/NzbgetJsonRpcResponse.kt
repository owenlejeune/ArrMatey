package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NzbgetJsonRpcResponse<T>(
    @SerialName("version") val version: String? = null,
    @SerialName("id") val id: Int? = null,
    @SerialName("result") val result: T? = null,
    @SerialName("error") val error: NzbgetError? = null,
)
