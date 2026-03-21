package com.dnfapps.arrmatey.logging

import dev.shivathapaa.logger.api.LogLevel
import dev.shivathapaa.logger.api.LoggerFactory
import dev.shivathapaa.logger.core.LoggerConfig
import dev.shivathapaa.logger.sink.DefaultLogSink

fun initLogging() {
    try {
        val config = LoggerConfig.Builder()
            .minLevel(LogLevel.DEBUG)
            .addSink(DefaultLogSink())
            .addSink(FileSink("arrmatey.log"))
            .build()

        LoggerFactory.install(config)
    } catch (e: Exception) {
        //
    }
}