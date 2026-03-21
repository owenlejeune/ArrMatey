package com.dnfapps.arrmatey.logging

import android.content.Context
import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.sink.LogSink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

actual class FileSink actual constructor(private val filename: String): LogSink {
    private val file: File = File(LogFileManager.getLogFilePath(filename))
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val logQueue = ConcurrentLinkedQueue<String>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        file.parentFile?.mkdirs()
        if (!file.exists()) {
            file.createNewFile()
        }

        startBackgroundWriter()
    }

    private fun startBackgroundWriter() {
        scope.launch {
            while (isActive) {
                writeQueuedLogs()
                delay(1000)
            }
        }
    }

    private fun writeQueuedLogs() {
        if (logQueue.isEmpty()) return

        try {
            BufferedWriter(FileWriter(file, true)).use { writer ->
                while (logQueue.isNotEmpty()) {
                    logQueue.poll()?.let { line ->
                        writer.write(line)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FileSink", "Failed to write queued logs", e)
        }
    }

    actual override fun emit(event: LogEvent) {
        val timestamp = dateFormat.format(Date(event.timestamp ?: 0L))
        val line = "[$timestamp] [${event.level}] ${event.loggerName}: ${event.message}\n"
        logQueue.offer(line)
    }

    actual override fun flush() {
        runBlocking {
            writeQueuedLogs()
        }
    }

    actual fun getLogFilePath(): String = file.absolutePath

    actual fun clearLogs() {
        logQueue.clear()
        file.delete()
        file.createNewFile()
    }

    fun shutdown() {
        flush()
        scope.cancel()
    }
}

actual object LogFileManager {
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    actual fun getLogDirectory(): String {
        return File(appContext.filesDir, "logs").apply { mkdirs() }.absolutePath
    }

    actual fun getLogFilePath(filename: String): String {
        return File(getLogDirectory(), filename).absolutePath
    }
}