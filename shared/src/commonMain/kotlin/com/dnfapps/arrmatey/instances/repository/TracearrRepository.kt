package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.tracearr.api.client.TracearrClient
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamsResponse
import com.dnfapps.arrmatey.tracearr.api.model.TracearrTodayStats
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient

class TracearrRepository(
    override val instance: Instance,
    httpClient: HttpClient,
) : InstanceScopedRepository {

    private val tracearrClient = TracearrClient(instance, httpClient)

    override suspend fun testConnection(): NetworkResult<Unit> =
        tracearrClient.testConnection()

    suspend fun getPublicStreams(): NetworkResult<TracearrStreamsResponse> {
        val baseUrl = instance.getEffectiveBaseUrl()
        return tracearrClient.getPublicStreams().map { response ->
            response.copy(
                data = response.data.map { session ->
                    session.rebuildWithInstanceBaseUrl(baseUrl)
                },
            )
        }
    }

    suspend fun getTodayStats(): NetworkResult<TracearrTodayStats> =
        tracearrClient.getTodayStats()

    suspend fun getHistory(cursor: String? = null, pageSize: Int? = null): NetworkResult<TracearrHistoryResponse> {
        val baseUrl = instance.getEffectiveBaseUrl()
        return tracearrClient.getHistory(cursor = cursor, pageSize = pageSize).map { response ->
            response.copy(
                data = response.data.map { item ->
                    item.rebuildWithInstanceBaseUrl(baseUrl)
                },
            )
        }
    }
}
