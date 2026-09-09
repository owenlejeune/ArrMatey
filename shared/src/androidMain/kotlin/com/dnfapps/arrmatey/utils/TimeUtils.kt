package com.dnfapps.arrmatey.utils

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import java.util.Date
import java.util.Locale
import kotlin.time.Instant
import android.icu.util.TimeZone as JavaTimeZone

actual fun getCurrentSystemTimeMillis(): Long = System.currentTimeMillis()

actual fun is24Hour(): Boolean = DateHelper.is24Hour()

actual fun Instant.format(pattern: String): String {
    val cleanPattern = if (is24Hour()) pattern else pattern.replace("HH:mm", "h:mm a")
    val date = Date(toEpochMilliseconds())
    val sdf = SimpleDateFormat(cleanPattern, Locale.getDefault())
    return sdf.format(date)
}

actual fun LocalDate.format(pattern: String): String {
    val sdf =
        SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = JavaTimeZone.getTimeZone("UTC")
        }
    return sdf.format(Date(toEpochDays() * 86_400_000))
}

actual fun formatLocalDateTime(
    localDateTime: LocalDateTime,
    pattern: String,
    timeZone: TimeZone,
): String {
    val cal =
        Calendar.getInstance(JavaTimeZone.getTimeZone(timeZone.id), Locale.getDefault()).apply {
            set(Calendar.YEAR, localDateTime.year)
            set(Calendar.MONTH, localDateTime.month.number - 1) // Calendar months are 0-based
            set(Calendar.DAY_OF_MONTH, localDateTime.day)
            set(Calendar.HOUR_OF_DAY, localDateTime.hour)
            set(Calendar.MINUTE, localDateTime.minute)
            set(Calendar.SECOND, localDateTime.second)
            set(Calendar.MILLISECOND, localDateTime.nanosecond / 1_000_000)
        }

    val sdf =
        SimpleDateFormat(pattern, Locale.getDefault()).apply {
            this.timeZone = cal.timeZone
        }

    return sdf.format(cal.time)
}

actual fun formatWatchTimeMs(ms: Long): String {
    if (ms <= 0) return "0m"
    val totalSeconds = ms / 1000
    val totalMinutes = totalSeconds / 60
    val days = totalMinutes / (24 * 60)
    val hours = (totalMinutes % (24 * 60)) / 60
    val minutes = totalMinutes % 60

    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}
