package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import com.dnfapps.arrmatey.instances.model.HeaderRestrictionType
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.utils.getNetworkUtils
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val HEADER_X_API_KEY = "X-Api-Key"
const val DEFAULT_SLOW_TIMEOUT = 300

fun createInstanceClient(
    instance: Instance?,
    json: Json,
    customLogger: Logger,
) = HttpClient {
    expectSuccess = true
    install(ContentNegotiation) {
        json(json)
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 60_000
        connectTimeoutMillis = 60_000
        socketTimeoutMillis = 60_000

        if (instance?.slowInstance == true) {
            val timeoutMillis = (instance.customTimeout ?: DEFAULT_SLOW_TIMEOUT).toLong() * 1_000
            requestTimeoutMillis = timeoutMillis
            connectTimeoutMillis = timeoutMillis
            socketTimeoutMillis = timeoutMillis
        }
    }

    install(HttpRequestRetry) {
        retryOnExceptionOrServerErrors(maxRetries = 3)
        exponentialDelay()
    }

    install(Logging) {
        logger = customLogger
        level = LogLevel.ALL
    }

    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }

    defaultRequest {
        if (!url.user.isNullOrBlank() && !url.password.isNullOrBlank()) {
            basicAuth(url.user!!, url.password!!)
            url.user = null
            url.password = null
        }

        instance?.let { instance ->
            if (!instance.noApiKeyRequired) {
                when (instance.type) {
                    InstanceType.Tracearr ->
                        header(HttpHeaders.Authorization, "Bearer ${instance.apiKey.value}")
                    else ->
                        header(HEADER_X_API_KEY, instance.apiKey.value)
                }
            }
            instance.headers.forEach { header ->
                val shouldSend =
                    when (header.restrictionType) {
                        HeaderRestrictionType.Always -> true
                        HeaderRestrictionType.RemoteOnly -> !instance.isUsingLocalNetwork()
                        HeaderRestrictionType.SpecificSsids -> {
                            val currentSsid = getNetworkUtils().getCurrentWifiSsid()
                            currentSsid != null && header.restrictedSsids.contains(currentSsid)
                        }
                    }

                if (shouldSend) {
                    header(header.key, header.value)
                }
            }
        }
    }
}

open class HttpClientFactory(
    private val json: Json,
    private val logger: Logger,
) {
    open fun create(instance: Instance): HttpClient = createInstanceClient(instance, json, logger)

    fun createDownloadClient(downloadClient: DownloadClient): HttpClient = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }

        install(HttpRequestRetry) {
            // Never retry 4xx: repeated failed auth triggers qBittorrent's IP ban.
            retryOnServerErrors(maxRetries = 3)
            retryOnExceptionIf(maxRetries = 3) { _, cause ->
                cause !is ClientRequestException
            }
            exponentialDelay()
        }

        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }

        install(Logging) {
            this.logger = logger
            level = LogLevel.ALL
        }

        defaultRequest {
            url(downloadClient.getEffectiveBaseUrl().trimEnd('/') + "/")
            if (!url.user.isNullOrBlank() && !url.password.isNullOrBlank()) {
                basicAuth(url.user!!, url.password!!)
                url.user = null
                url.password = null
            }
            if (!downloadClient.noApiKeyRequired && downloadClient.apiKey.value.isNotEmpty()) {
                when (downloadClient.type) {
                    // qBittorrent >= 5.2 only accepts API keys via Authorization: Bearer.
                    DownloadClientType.QBittorrent ->
                        header(HttpHeaders.Authorization, "Bearer ${downloadClient.apiKey.value}")
                    else ->
                        header(HEADER_X_API_KEY, downloadClient.apiKey.value)
                }
            }
            downloadClient.headers.forEach { header ->
                val shouldSend =
                    when (header.restrictionType) {
                        HeaderRestrictionType.Always -> true
                        HeaderRestrictionType.RemoteOnly -> !downloadClient.isUsingLocalNetwork()
                        HeaderRestrictionType.SpecificSsids -> {
                            val currentSsid = getNetworkUtils().getCurrentWifiSsid()
                            currentSsid != null && header.restrictedSsids.contains(currentSsid)
                        }
                    }

                if (shouldSend) {
                    header(header.key, header.value)
                }
            }
        }
    }

    fun createGeneric(): HttpClient = createInstanceClient(null, json, logger)
}
