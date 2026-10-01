package com.dnfapps.arrmatey.arr.api.client.logging

import io.ktor.client.plugins.logging.LogLevel

enum class LoggerLevel(
    internal val ktorValue: LogLevel,
) {
    All(LogLevel.ALL),
    Headers(LogLevel.HEADERS),
    Body(LogLevel.BODY),
    Info(LogLevel.INFO),
    None(LogLevel.NONE),
}
