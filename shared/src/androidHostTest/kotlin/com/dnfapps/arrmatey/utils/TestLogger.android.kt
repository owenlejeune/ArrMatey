package com.dnfapps.arrmatey.utils

import dev.shivathapaa.logger.api.Logger
import io.mockk.mockk

// Android host tests cannot call the framework logger from background coroutines.
actual fun testLogger(): Logger = mockk(relaxed = true)
