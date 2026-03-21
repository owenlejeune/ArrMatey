package com.dnfapps.arrmatey.utils

import com.dnfapps.arrmatey.logging.LogFileManager
import java.io.File

class AndroidCrashManager: CrashManager {
    private val logFile = File(LogFileManager.getLogFilePath("arrmatey.log"))

    override fun initialize() {
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logFile.appendText(throwable.stackTraceToString())
            oldHandler?.uncaughtException(thread, throwable)
        }
    }

}