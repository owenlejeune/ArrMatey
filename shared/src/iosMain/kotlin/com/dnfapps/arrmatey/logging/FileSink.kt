package com.dnfapps.arrmatey.logging

import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.sink.LogSink
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.autoreleasepool
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSLog
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.closeFile
import platform.Foundation.dataUsingEncoding
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.fileHandleForUpdatingAtPath
import platform.Foundation.seekToEndOfFile
import platform.Foundation.writeData

actual class FileSink actual constructor(private val filename: String) : LogSink {
    private val fileManager = NSFileManager.defaultManager
    private val filePath = LogFileManager.getLogFilePath(filename)
    private val dateFormatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    }

    init {
        val logDir = LogFileManager.getLogDirectory()
        if (!fileManager.fileExistsAtPath(logDir)) {
            fileManager.createDirectoryAtPath(
                path = logDir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }

        if (!fileManager.fileExistsAtPath(filePath)) {
            fileManager.createFileAtPath(
                path = filePath,
                contents = null,
                attributes = null
            )
        }
    }

    @OptIn(BetaInteropApi::class)
    actual override fun emit(event: LogEvent) {
        autoreleasepool {
            try {
                val timestamp = dateFormatter.stringFromDate(
                    NSDate.dateWithTimeIntervalSince1970((event.timestamp ?: 0L) / 1000.0)
                )
                val line = "[$timestamp] [${event.level}] ${event.loggerName}: ${event.message}\n"

                val fileHandle = NSFileHandle.fileHandleForUpdatingAtPath(filePath)

                if (fileHandle != null) {
                    fileHandle.seekToEndOfFile()

                    val data = (line as NSString).dataUsingEncoding(NSUTF8StringEncoding)
                    if (data != null) {
                        fileHandle.writeData(data)
                    }

                    fileHandle.closeFile()
                } else {
                    NSLog("Failed to open log file: $filePath")
                }
            } catch (e: Exception) {
                NSLog("Failed to write log: ${e.message}")
            }
        }
    }

    actual override fun flush() {
        // left empty
    }

    actual fun getLogFilePath(): String = filePath

    actual fun clearLogs() {
        try {
            fileManager.removeItemAtPath(filePath, error = null)
            fileManager.createFileAtPath(
                path = filePath,
                contents = null,
                attributes = null
            )
        } catch (e: Exception) {
            NSLog("Failed to clear logs: ${e.message}")
        }
    }
}

actual object LogFileManager {
    actual fun getLogDirectory(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            directory = NSDocumentDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true
        )
        val documentsDirectory = paths.first() as String
        return "$documentsDirectory/logs"
    }

    actual fun getLogFilePath(filename: String): String {
        return "${getLogDirectory()}/$filename"
    }
}