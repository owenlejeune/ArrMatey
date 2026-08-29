@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.dnfapps.arrmatey.logging

import kotlinx.cinterop.*
import platform.Foundation.*

actual object LogReader {
    @OptIn(BetaInteropApi::class)
    actual fun readLogs(): String =
        autoreleasepool {
            try {
                val logPath = LogFileManager.getLogFilePath("arrmatey.log")
                val fileManager = NSFileManager.defaultManager

                if (fileManager.fileExistsAtPath(logPath)) {
                    val data = NSData.dataWithContentsOfFile(logPath)
                    if (data != null) {
                        NSString.create(data, NSUTF8StringEncoding)?.toString() ?: "No logs found"
                    } else {
                        "No logs found"
                    }
                } else {
                    "No logs found"
                }
            } catch (e: Exception) {
                "Error reading logs: ${e.message}"
            }
        }

    @OptIn(BetaInteropApi::class)
    actual fun clearLogs() {
        autoreleasepool {
            try {
                val logPath = LogFileManager.getLogFilePath("arrmatey.log")
                val fileManager = NSFileManager.defaultManager

                if (fileManager.fileExistsAtPath(logPath)) {
                    val emptyData = NSData.create(bytes = null, length = 0u)
                    emptyData.writeToFile(logPath, atomically = true)
                }
            } catch (e: Exception) {
                NSLog("Failed to clear logs: ${e.message}")
            }
        }
    }

    actual fun getLogFilePath(): String = LogFileManager.getLogFilePath("arrmatey.log")
}
