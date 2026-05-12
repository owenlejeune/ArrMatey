package com.dnfapps.arrmatey.extensions

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.utils.screenDensity

fun Int.pxToDp(): Dp = (this / screenDensity).dp

fun Int.formatMinutesAsRuntime(): String {
    val hours = this / 60
    val minutes = this % 60

    return buildString {
        if (hours > 0) append("$hours${if (hours == 1) "h" else "h"}")
        if (minutes > 0) {
            if (hours > 0) append(" ")
            append("$minutes${if (minutes == 1) "m" else "m"}")
        }
        if (hours == 0 && minutes == 0) append("0m")
    }
}

fun Int.formatSecondsAsRuntime(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    return buildString {
        if (hours > 0) append("${hours}h")

        if (minutes > 0) {
            if (isNotEmpty()) append(" ")
            append("${minutes}m")
        }

        if (seconds > 0 || (hours == 0 && minutes == 0)) {
            if (isNotEmpty()) append(" ")
            append("${seconds}s")
        }
    }
}

fun Int.formatAsDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    fun pad(value: Int): String = if (value < 10) "0$value" else value.toString()

    return "${pad(hours)}:${pad(minutes)}:${pad(seconds)}"
}

fun Int.padStart(length: Int, char: Char): String = toString().padStart(length, char)

fun Double.toOneDecimal(): String {
    val rounded = (this * 10.0).let { kotlin.math.round(it) / 10.0 }
    val string = rounded.toString()

    return if (string.contains(".")) {
        val parts = string.split(".")
        val decimal = parts[1]
        if (decimal.length == 1) string else "${parts[0]}.${decimal.take(1)}"
    } else {
        "$string.0"
    }
}