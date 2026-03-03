package com.dnfapps.arrmatey.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

expect fun getCurrentSystemTimeMillis(): Long

expect fun is24Hour(): Boolean

expect fun formatLocalDateTime(localDateTime: LocalDateTime, pattern: String, timeZone: TimeZone): String

expect fun Instant.format(pattern: String  = "HH:mm MMMM d, yyyy"): String

expect fun LocalDate.format(pattern: String = "MMM d, yyyy"): String