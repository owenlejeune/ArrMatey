package com.dnfapps.arrmatey.logging

import dev.shivathapaa.logger.core.LogEvent
import dev.shivathapaa.logger.sink.LogSink
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.autoreleasepool
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.*


@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class FileSink actual constructor(private val filename: String) : LogSink {
    private val fileManager = NSFileManager.defaultManager
    private var filePath = LogFileManager.getLogFilePath(filename)
    private val dateFormatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
    }

    actual val maxFileSizeBytes: Long = 5 * 1024 * 1024 // 5MB
    actual val maxBackupFiles: Int = 3

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

    private fun shouldRotate(): Boolean {
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val attrs = fileManager.attributesOfItemAtPath(filePath, error = error.ptr)

            if (error.value != null) {
                NSLog("Error checking file size: ${error.value?.localizedDescription}")
                return false
            }

            val fileSize = attrs?.get(NSFileSize) as? NSNumber
            return (fileSize?.longLongValue ?: 0L) >= maxFileSizeBytes
        }
    }

    private fun rotateLogFile() {
        autoreleasepool {
            try {
                for (i in maxBackupFiles - 1 downTo 1) {
                    val oldPath = LogFileManager.getLogFilePath("$filename.$i")
                    val newPath = LogFileManager.getLogFilePath("$filename.${i + 1}")

                    if (fileManager.fileExistsAtPath(oldPath)) {
                        if (fileManager.fileExistsAtPath(newPath)) {
                            fileManager.removeItemAtPath(newPath, error = null)
                        }
                        fileManager.moveItemAtPath(
                            srcPath = oldPath,
                            toPath = newPath,
                            error = null
                        )
                    }
                }

                val backupPath = LogFileManager.getLogFilePath("$filename.1")
                if (fileManager.fileExistsAtPath(backupPath)) {
                    fileManager.removeItemAtPath(backupPath, error = null)
                }
                fileManager.moveItemAtPath(
                    srcPath = filePath,
                    toPath = backupPath,
                    error = null
                )

                filePath = LogFileManager.getLogFilePath(filename)
                fileManager.createFileAtPath(
                    path = filePath,
                    contents = null,
                    attributes = null
                )

                NSLog("Rotated log file: $filename")
            } catch (e: Exception) {
                NSLog("Failed to rotate log file: ${e.message}")
            }
        }
    }

    actual override fun emit(event: LogEvent) {
        autoreleasepool {
            try {
                if (shouldRotate()) {
                    rotateLogFile()
                }

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
        // iOS flushes automatically on closeFile()
    }

    actual fun getLogFilePath(): String = filePath

    actual fun clearLogs() {
        autoreleasepool {
            try {
                fileManager.removeItemAtPath(filePath, error = null)
                fileManager.createFileAtPath(
                    path = filePath,
                    contents = null,
                    attributes = null
                )

                for (i in 1..maxBackupFiles) {
                    val backupPath = LogFileManager.getLogFilePath("$filename.$i")
                    if (fileManager.fileExistsAtPath(backupPath)) {
                        fileManager.removeItemAtPath(backupPath, error = null)
                    }
                }
            } catch (e: Exception) {
                NSLog("Failed to clear logs: ${e.message}")
            }
        }
    }

    fun getAllLogFiles(): List<String> {
        return buildList {
            if (fileManager.fileExistsAtPath(filePath)) {
                add(filePath)
            }
            for (i in 1..maxBackupFiles) {
                val backupPath = LogFileManager.getLogFilePath("$filename.$i")
                if (fileManager.fileExistsAtPath(backupPath)) {
                    add(backupPath)
                }
            }
        }
    }

    fun getTotalLogSize(): Long {
        var totalSize = 0L
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            getAllLogFiles().forEach { path ->
                val attrs = fileManager.attributesOfItemAtPath(path, error = error.ptr)
                if (error.value == null) {
                    val fileSize = attrs?.get(NSFileSize) as? NSNumber
                    totalSize += fileSize?.longLongValue ?: 0L
                }
            }
        }
        return totalSize
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual object LogFileManager {
    private val fileManager = NSFileManager.defaultManager

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

    fun getAllLogFiles(): List<String> {
        val logDir = getLogDirectory()
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val contents = fileManager.contentsOfDirectoryAtPath(logDir, error = error.ptr)

            if (error.value != null || contents == null) {
                return emptyList()
            }

            return buildList {
                for (i in 0 until contents.size) {
                    val filename = contents[i] as String
                    add("$logDir/$filename")
                }
            }
        }
    }

    fun getTotalLogDirectorySize(): Long {
        var totalSize = 0L
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            getAllLogFiles().forEach { path ->
                val attrs = fileManager.attributesOfItemAtPath(path, error = error.ptr)
                if (error.value == null) {
                    val fileSize = attrs?.get(NSFileSize) as? NSNumber
                    totalSize += fileSize?.longLongValue ?: 0L
                }
            }
        }
        return totalSize
    }

    fun clearAllLogs() {
        getAllLogFiles().forEach { path ->
            fileManager.removeItemAtPath(path, error = null)
        }
    }
}