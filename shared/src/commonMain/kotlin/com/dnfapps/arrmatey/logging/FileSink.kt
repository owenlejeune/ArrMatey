package com.dnfapps.arrmatey.logging

import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.sink.LogSink

expect class FileSink(filename: String) : LogSink {
    override fun emit(event: LogEvent)
    override fun flush()
    fun getLogFilePath(): String
    fun clearLogs()
}

expect object LogFileManager {
    fun getLogDirectory(): String
    fun getLogFilePath(filename: String): String
}