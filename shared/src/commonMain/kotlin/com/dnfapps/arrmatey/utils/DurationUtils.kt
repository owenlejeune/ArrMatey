package com.dnfapps.arrmatey.utils

fun formatDurationSeconds(totalSeconds: Long): String {
    if (totalSeconds <= 0) return "0s"
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}

fun formatDurationMs(ms: Long): String = formatDurationSeconds(ms / 1000)
