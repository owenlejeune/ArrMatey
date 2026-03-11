package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransmissionRpcResponse<T>(
    @SerialName("result") val result: String = "",
    @SerialName("arguments") val arguments: T? = null
)
