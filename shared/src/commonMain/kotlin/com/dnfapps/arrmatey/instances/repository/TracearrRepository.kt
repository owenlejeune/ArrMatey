package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient

class TracearrRepository(
    override val instance: Instance,
    httpClient: HttpClient
): InstanceScopedRepository {

    override suspend fun testConnection(): NetworkResult<Unit> {
        TODO("Not yet implemented")
    }

}
