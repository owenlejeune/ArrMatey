package com.dnfapps.arrmatey.utils

import dev.shivathapaa.logger.api.Logger
import dev.shivathapaa.logger.api.LoggerFactory

actual fun testLogger(): Logger = LoggerFactory.get("test")
