package com.dnfapps.arrmatey.tracearr.api.client

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeGet
import io.ktor.client.HttpClient

class TracearrClient(
    val instance: Instance,
    private val httpClient: HttpClient
) {

    private val baseUrl: String
        get() = "${instance.getEffectiveBaseUrl()}/${instance.type.apiBase}"

    suspend fun testConnection(): NetworkResult<Unit> {
        val url = "$baseUrl/${instance.type.testEndpoint}"
        return httpClient.safeGet(url)
    }

}
