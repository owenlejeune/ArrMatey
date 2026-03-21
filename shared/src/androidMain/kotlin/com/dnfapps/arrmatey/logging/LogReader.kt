package com.dnfapps.arrmatey.logging

import java.io.File

actual object LogReader {
    actual fun readLogs(): String {
        return try {
            val logFile = File(LogFileManager.getLogFilePath("arrmatey.log"))
            if (logFile.exists()) {
                logFile.readText()
            } else {
                "No logs found"
            }
        } catch (e: Exception) {
            "Error reading logs: ${e.message}"
        }
    }

    actual fun clearLogs() {
        try {
            val logFile = File(LogFileManager.getLogFilePath("arrmatey.log"))
            if (logFile.exists()) {
                logFile.writeText("")
            }
        } catch (e: Exception) {
            android.util.Log.e("LogReader", "Failed to clear logs", e)
        }
    }

    actual fun getLogFilePath(): String {
        return LogFileManager.getLogFilePath("arrmatey.log")
    }
}