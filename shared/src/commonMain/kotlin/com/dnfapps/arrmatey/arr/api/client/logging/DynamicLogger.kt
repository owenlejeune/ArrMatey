package com.dnfapps.arrmatey.arr.api.client.logging

import com.dnfapps.arrmatey.datastore.PreferencesStore
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dev.shivathapaa.logger.api.Logger as AppLogger

class DynamicLogger(
    private val preferencesStore: PreferencesStore,
    private val logger: AppLogger,
) : Logger {
    var currentLogLevel = LogLevel.HEADERS
        private set

    val level: LogLevel get() = currentLogLevel

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

        if (sanitizedMessage.isNotBlank()) {
            logger.info { sanitizedMessage }
        }
    }
}
