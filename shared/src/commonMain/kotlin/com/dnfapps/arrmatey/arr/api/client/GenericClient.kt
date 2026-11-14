package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeGet
import io.ktor.client.HttpClient
import io.ktor.client.request.header

class GenericClient(
    httpClientFactory: HttpClientFactory,
) {
    private val httpClient: HttpClient = httpClientFactory.createGeneric()

    suspend fun test(
        endpoint: String,
        apiKey: String,
        type: InstanceType,
        headers: List<InstanceHeader> = emptyList(),
        noApiKeyRequired: Boolean = false,
        sessionToken: String = "",
    ): Boolean {
        try {
            val cleanEndpoint = endpoint.trim().trimEnd('/')
            val cleanApiBase = type.apiBase.trim().trim('/')
            val testEndpoint = type.testEndpoint.trim()
            val fullUrl = if (testEndpoint.startsWith("?")) {
                "$cleanEndpoint/$cleanApiBase$testEndpoint"
            } else {
                "$cleanEndpoint/$cleanApiBase/${testEndpoint.trimStart('/')}"
            }
            val response =
                httpClient.safeGet<Any>(fullUrl) {
                    if (type == InstanceType.Seerr && sessionToken.isNotEmpty()) {
                        header("Cookie", "connect.sid=$sessionToken")
                    } else if (!noApiKeyRequired && apiKey.isNotEmpty()) {
                        when (type) {
                            InstanceType.Tracearr ->
                                header("Authorization", "Bearer $apiKey")
                            InstanceType.Kapowarr ->
                                url.parameters.append("api_key", apiKey)
                            else ->
                                header("X-Api-Key", apiKey)
                        }
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
