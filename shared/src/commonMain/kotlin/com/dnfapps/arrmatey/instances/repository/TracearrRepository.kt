package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.tracearr.api.client.TracearrClient
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamsResponse
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient

class TracearrRepository(
    override val instance: Instance,
    httpClient: HttpClient,
) : InstanceScopedRepository {

    private val tracearrClient = TracearrClient(instance, httpClient)

    override suspend fun testConnection(): NetworkResult<Unit> =
        tracearrClient.testConnection()

    suspend fun getPublicStreams(): NetworkResult<TracearrStreamsResponse> =
        tracearrClient.getPublicStreams()
}
