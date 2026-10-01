package com.dnfapps.arrmatey.arr.api.client.logging

import com.dnfapps.arrmatey.datastore.PreferencesStore
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DynamicLogger(
    private val preferencesStore: PreferencesStore,
    private val logger: dev.shivathapaa.logger.api.Logger,
) : Logger {
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

        val sanitizedMessage = sanitizeLogMessage(message)

        if (isExceptionMessage(sanitizedMessage)) {
            logger.error { sanitizedMessage }
            return
        }

        if (currentLogLevel == LogLevel.ALL) {
            logger.info { sanitizedMessage }
            return
        }

        val lines = sanitizedMessage.split("\n")
        val filteredOutput = StringBuilder()

        lines.forEach { line ->
            val shouldInclude =
                when (currentLogLevel) {
                    LogLevel.INFO -> {
                        line.startsWith("REQUEST:") ||
                            line.startsWith("RESPONSE:") ||
                            line.startsWith("METHOD:")
                    }
                    LogLevel.HEADERS -> {
                        // Include everything except the body sections
                        !isBodyLine(line)
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
