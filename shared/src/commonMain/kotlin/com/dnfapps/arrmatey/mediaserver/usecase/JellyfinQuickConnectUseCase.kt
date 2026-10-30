package com.dnfapps.arrmatey.mediaserver.usecase

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.mediaserver.model.JellyfinQuickConnectAuthBody
import com.dnfapps.arrmatey.mediaserver.model.JellyfinQuickConnectCheckResult
import com.dnfapps.arrmatey.mediaserver.model.JellyfinQuickConnectInitiateResult
import com.dnfapps.arrmatey.mediaserver.model.MediaBrowserAuthResult
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeGet
import com.dnfapps.networking.safePost
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class JellyfinQuickConnectUseCase(
    httpClientFactory: HttpClientFactory,
) {
    private val httpClient: HttpClient = httpClientFactory.createGeneric()

    private fun buildAuthHeader(clientIdentifier: String): String = """MediaBrowser Client="ArrMatey", Device="ArrMatey", DeviceId="$clientIdentifier", Version="1.0.0""""

    suspend fun initiate(
        baseUrl: String,
        clientIdentifier: String,
        headers: List<InstanceHeader> = emptyList(),
    ): NetworkResult<JellyfinQuickConnectInitiateResult> {
        val url = baseUrl.trimEnd('/') + "/QuickConnect/Initiate"
        return httpClient.safePost<JellyfinQuickConnectInitiateResult>(url) {
            header("Authorization", buildAuthHeader(clientIdentifier))
            header("Accept", "application/json")
            headers.forEach { header(it.key, it.value) }
        }
    }

    suspend fun check(
        baseUrl: String,
        secret: String,
        clientIdentifier: String,
        headers: List<InstanceHeader> = emptyList(),
    ): NetworkResult<JellyfinQuickConnectCheckResult> {
        val url = baseUrl.trimEnd('/') + "/QuickConnect/Connect?secret=$secret"
        return httpClient.safeGet<JellyfinQuickConnectCheckResult>(url) {
            header("Authorization", buildAuthHeader(clientIdentifier))
            header("Accept", "application/json")
            headers.forEach { header(it.key, it.value) }
        }
    }

    suspend fun authenticate(
        baseUrl: String,
        secret: String,
        clientIdentifier: String,
        headers: List<InstanceHeader> = emptyList(),
    ): NetworkResult<MediaBrowserAuthResult> {
        val url = baseUrl.trimEnd('/') + "/Users/AuthenticateWithQuickConnect"
        return httpClient.safePost<MediaBrowserAuthResult>(url) {
            header("Authorization", buildAuthHeader(clientIdentifier))
            header("Accept", "application/json")
            contentType(ContentType.Application.Json)
            setBody(JellyfinQuickConnectAuthBody(secret))
            headers.forEach { header(it.key, it.value) }
        }
    }
}
