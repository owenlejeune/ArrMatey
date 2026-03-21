package com.dnfapps.arrmatey.logging

expect object LogReader {
    fun readLogs(): String
    fun clearLogs()
    fun getLogFilePath(): String
}