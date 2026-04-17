package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.instances.model.HeaderRestrictionType
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.utils.getNetworkUtils
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private const val HEADER_X_API_KEY = "X-Api-Key"
const val DEFAULT_SLOW_TIMEOUT = 300

fun createInstanceClient(
    instance: Instance?,
    json: Json,
    customLogger: Logger
) =
    HttpClient {
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

        instance?.let { instance ->
            defaultRequest {
                header(HEADER_X_API_KEY, instance.apiKey)
                instance.headers.forEach { header ->
                    val shouldSend = when (header.restrictionType) {
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

class HttpClientFactory(private val json: Json, private val logger: Logger) {
    fun create(instance: Instance): HttpClient =
        createInstanceClient(instance, json, logger)

    fun createDownloadClient(downloadClient: DownloadClient): HttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }

            install(HttpRequestRetry) {
                retryOnExceptionOrServerErrors(maxRetries = 3)
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
                url(downloadClient.url.trimEnd('/') + "/")
                downloadClient.headers.forEach { (key, value) ->
                    header(key, value)
                }
            }
        }

    fun createGeneric(): HttpClient =
        createInstanceClient(null, json, logger)
}

enum class LoggerLevel(
    internal val ktorValue: LogLevel
) {
    All(LogLevel.ALL),
    Headers(LogLevel.HEADERS),
    Body(LogLevel.BODY),
    Info(LogLevel.INFO),
    None(LogLevel.NONE)
}

class DynamicLogger(
    private val preferencesStore: PreferencesStore,
    private val logger: dev.shivathapaa.logger.api.Logger
): Logger {
    private var currentLogLevel = LogLevel.HEADERS

    init {
        CoroutineScope(Dispatchers.Default).launch {
            preferencesStore.httpLogLevel
                .collect { level ->
                    currentLogLevel = level.ktorValue
                }
        }
    }

    override fun log(message: String) {
        if (currentLogLevel == LogLevel.NONE) return
        if (currentLogLevel == LogLevel.ALL) {
            logger.info { message }
            return
        }

        val lines = message.split("\n")
        val filteredOutput = StringBuilder()

        lines.forEach { line ->
            val shouldInclude = when (currentLogLevel) {
                LogLevel.INFO -> {
                    line.startsWith("REQUEST:") ||
                            line.startsWith("RESPONSE:") ||
                            line.startsWith("METHOD:")
                }
                LogLevel.HEADERS -> {
                    // Include everything except the body sections
                    !isBodyLine(line) &&
                            !line.contains("X-Api-Key", ignoreCase = true)
                }
                LogLevel.BODY -> {
                    // Include Request/Response lines and the JSON body, skip headers
                    line.startsWith("REQUEST:") ||
                            line.startsWith("RESPONSE:") ||
                            line.startsWith("METHOD:") ||
                            isBodyLine(line)
                }
                else -> false
            }

            if (shouldInclude) {
                filteredOutput.append(line).append("\n")
            }
        }

        val result = filteredOutput.toString().trim()
        if (result.isNotEmpty()) {
            logger.info { result }
        }
    }
}

private fun isBodyLine(line: String): Boolean {
    val trimmed = line.trim()
    return trimmed.startsWith("BODY") ||
            trimmed.startsWith("{") ||
            trimmed.startsWith("}") ||
            trimmed.startsWith("[") ||
            trimmed.startsWith("]") ||
            trimmed.startsWith("\"")
}
