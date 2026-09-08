package com.dnfapps.arrmatey.tracearr.api.client

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamsResponse
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeGet
import io.ktor.client.HttpClient

class TracearrClient(
    val instance: Instance,
    private val httpClient: HttpClient,
) {

    private val v2BaseUrl: String
        get() = "${instance.getEffectiveBaseUrl()}/${instance.type.apiBase}/v2"


    private val v1BaseUrl: String
        get() = "${instance.getEffectiveBaseUrl()}/${instance.type.apiBase}/v1"

    suspend fun testConnection(): NetworkResult<Unit> {
        val url = "$v2BaseUrl/${instance.type.testEndpoint}"
        return httpClient.safeGet(url)
    }

    suspend fun getPublicStreams(): NetworkResult<TracearrStreamsResponse> {
        val url = "$v2BaseUrl/public/streams"
        return httpClient.safeGet(url)
    }

    suspend fun getTodayStats(): NetworkResult<TracearrTodayStats> {
        val url = "$v1BaseUrl/public/stats/today"
        return httpClient.safeGet(url)
    }
}
