package com.dnfapps.arrmatey.extensions

fun Float.formatAgeMinutes(): String {
    val minutes = toInt()
    val days = minutes / 60 / 24
    val years = days.toFloat() / 30f / 12f

    return when (minutes) {
        in -10_000..<1 -> {
            // less than 1 minute (or bad data)
            "Just now"
        }
        in 1..<120 -> {
            // less than 120 minutes
            "$minutes minutes"
        }
        in 120..<2_880 -> {
            // less than 48 hours
            "${minutes / 60} hours"
        }
        in 2_880..<129_600 -> {
            // less than 90 days
            "$days days"
        }
        in 129_600..<525_600 -> {
            // less than 365 days
            "${days / 30} months"
        }
        else -> {
            "${years.toInt()} years"
        }
    }
}
