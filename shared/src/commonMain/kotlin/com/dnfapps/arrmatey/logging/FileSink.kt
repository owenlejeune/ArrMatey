package com.dnfapps.arrmatey.logging

import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.sink.LogSink

expect class FileSink(filename: String) : LogSink {
    val maxFileSizeBytes: Long// = 5 * 1024 * 1024, // 5MB default
    val maxBackupFiles: Int// = 3 // Keep 3 backup files

    override fun emit(event: LogEvent)
    override fun flush()
    fun getLogFilePath(): String
    fun clearLogs()
}

expect object LogFileManager {
    fun getLogDirectory(): String
    fun getLogFilePath(filename: String): String
}