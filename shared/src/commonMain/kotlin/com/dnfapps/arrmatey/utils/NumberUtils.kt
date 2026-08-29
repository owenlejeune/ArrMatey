package com.dnfapps.arrmatey.utils

fun Float.formatToOneDecimal(): String {
    val s = this.toString()
    val dotIndex = s.indexOf('.')
    if (dotIndex == -1) return "$s.0"

    // Take everything before the dot, the dot, and one digit after
    val end = (dotIndex + 2).coerceAtMost(s.length)
    return s.substring(0, end)
}
