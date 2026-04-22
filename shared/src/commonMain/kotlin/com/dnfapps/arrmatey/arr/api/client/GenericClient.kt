package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.safeGet
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType
import io.ktor.client.HttpClient
import io.ktor.client.request.header

class GenericClient(
    httpClientFactory: HttpClientFactory
) {

    private val httpClient: HttpClient = httpClientFactory.createGeneric()

    suspend fun test(
        endpoint: String,
        apiKey: String,
        type: InstanceType,
        headers: List<InstanceHeader> = emptyList(),
        basicAuthEnabled: Boolean = false
    ): Boolean {
        try {
            val response = httpClient.safeGet<Any>("${endpoint.trimEnd('/')}/${type.apiBase}/${type.testEndpoint}") {
                if (!basicAuthEnabled) {
                    header("X-Api-Key", apiKey)
                }
                headers.forEach { h ->
                    header(h.key, h.value)
                }
            }
            return response is NetworkResult.Success
        } catch (e: Exception) {
            return false
        }
    }
}
