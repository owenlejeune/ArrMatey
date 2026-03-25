package com.dnfapps.arrmatey.logging

import android.content.Context
import android.util.Log
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

actual class FileSink actual constructor(private val filename: String) : LogSink {
    private var file: File = File(LogFileManager.getLogFilePath(filename))
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val logQueue = ConcurrentLinkedQueue<String>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    actual val maxFileSizeBytes: Long = 5 * 1024 * 1024 // 5MB
    actual val maxBackupFiles: Int = 3

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
            if (shouldRotate()) {
                rotateLogFile()
            }

            BufferedWriter(FileWriter(file, true)).use { writer ->
                while (logQueue.isNotEmpty()) {
                    logQueue.poll()?.let { line ->
                        writer.write(line)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FileSink", "Failed to write queued logs", e)
        }
    }

    private fun shouldRotate(): Boolean {
        return file.exists() && file.length() >= maxFileSizeBytes
    }

    private fun rotateLogFile() {
        try {
            for (i in maxBackupFiles - 1 downTo 1) {
                val oldFile = File(LogFileManager.getLogFilePath("$filename.$i"))
                val newFile = File(LogFileManager.getLogFilePath("$filename.${i + 1}"))

                if (oldFile.exists()) {
                    if (newFile.exists()) {
                        newFile.delete()
                    }
                    oldFile.renameTo(newFile)
                }
            }

            val backupFile = File(LogFileManager.getLogFilePath("$filename.1"))
            if (backupFile.exists()) {
                backupFile.delete()
            }
            file.renameTo(backupFile)

            file = File(LogFileManager.getLogFilePath(filename))
            file.createNewFile()

            Log.d("FileSink", "Rotated log file: $filename")
        } catch (e: Exception) {
            Log.e("FileSink", "Failed to rotate log file", e)
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

        for (i in 1..maxBackupFiles) {
            val backupFile = File(LogFileManager.getLogFilePath("$filename.$i"))
            if (backupFile.exists()) {
                backupFile.delete()
            }
        }
    }

    fun getAllLogFiles(): List<File> {
        return buildList {
            if (file.exists()) add(file)
            for (i in 1..maxBackupFiles) {
                val backupFile = File(LogFileManager.getLogFilePath("$filename.$i"))
                if (backupFile.exists()) {
                    add(backupFile)
                }
            }
        }
    }

    fun getTotalLogSize(): Long {
        return getAllLogFiles().sumOf { it.length() }
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

    fun getAllLogFiles(): List<File> {
        val logDir = File(getLogDirectory())
        return logDir.listFiles()?.toList() ?: emptyList()
    }

    fun getTotalLogDirectorySize(): Long {
        return getAllLogFiles().sumOf { it.length() }
    }

    fun clearAllLogs() {
        getAllLogFiles().forEach { it.delete() }
    }
}