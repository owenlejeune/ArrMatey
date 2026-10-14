package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NzbgetJsonRpcRequest<T>(
    @SerialName("method") val method: String,
    @SerialName("params") val params: T,
    @SerialName("id") val id: Int = 1,
) {
    companion object {
        fun empty(method: String, id: Int = 1): NzbgetJsonRpcRequest<List<String>> = NzbgetJsonRpcRequest(method = method, params = emptyList(), id = id)
    }
}
