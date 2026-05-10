package com.dnfapps.arrmatey.compose.utils

import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow

fun Long.bytesAsFileSizeString(): String {
    if (this < 1024L) return "$this B"

    val units = arrayOf("KB", "MB", "GB", "TB", "PB", "EB")
    val bytes = this.toDouble()
    val exp = (ln(bytes) / ln(1024.0)).toInt().coerceAtMost(units.lastIndex)
    val value = bytes / 1024.0.pow(exp)

    // Format to 1 decimal: 9.5 instead of 9.48274
    val decimal = (value * 10.0).let { floor(it) / 10.0 }

    return "${decimal.toInt()}.${(decimal * 10).toInt() % 10} ${units[max(exp-1,0)]}"
}

fun Long.toFormattedDuration(): String {
    val qbInfinity = 8_640_000L

    if (this >= qbInfinity) return "∞"
    if (this <= 0) return "0s"

    // Constants in seconds
    val secondsInYear = 31_536_000L
    val secondsInMonth = 2_592_000L
    val secondsInDay = 86_400L
    val secondsInHour = 3_600L
    val secondsInMinute = 60L

    var remainingSeconds = this

    val years = remainingSeconds / secondsInYear
    remainingSeconds %= secondsInYear

    val months = remainingSeconds / secondsInMonth
    remainingSeconds %= secondsInMonth

    val days = remainingSeconds / secondsInDay
    remainingSeconds %= secondsInDay

    val hours = remainingSeconds / secondsInHour
    remainingSeconds %= secondsInHour

    val minutes = remainingSeconds / secondsInMinute
    val seconds = remainingSeconds % secondsInMinute

    return buildString {
        if (years > 0) append("${years}y ")
        if (months > 0) append("${months}mo ")
        if (days > 0) append("${days}d ")
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}m ")
        if (seconds > 0 || isEmpty()) append("${seconds}s")
    }.trim()
}

fun Long.formatWithCommas(symbol: String = "$"): String {
    val formatted = this.toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
    return "$symbol$formatted"
}

fun String.toSeconds(): Long {
    val parts = this.split(":").filter { it.isNotBlank() }

    val reversedParts = parts.reversed()

    var totalSeconds = 0L

    reversedParts.forEachIndexed { index, part ->
        val value = part.toLongOrNull() ?: 0L
        when (index) {
            0 -> totalSeconds += value
            1 -> totalSeconds += value * 60
            2 -> totalSeconds += value * 3600
        }
    }

    return totalSeconds
}
