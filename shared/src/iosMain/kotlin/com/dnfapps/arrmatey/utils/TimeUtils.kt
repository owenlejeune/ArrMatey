package com.dnfapps.arrmatey.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.timeZoneWithName
import kotlin.time.Instant

actual fun getCurrentSystemTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun is24Hour(): Boolean {
    val formatter = NSDateFormatter()
    formatter.dateStyle = NSDateFormatterNoStyle
    formatter.timeStyle = NSDateFormatterMediumStyle
    val testDate = NSDate()
    val formatted = formatter.stringFromDate(testDate)

    // Check if output contains AM/PM or similar 12h indicator
    return !formatted.contains("AM", true) && !formatted.contains("PM", true)
}

actual fun Instant.format(pattern: String): String {
    val cleanPattern = if (is24Hour()) pattern else pattern.replace("HH:mm", "h:mm a")
    val date = NSDate.dateWithTimeIntervalSince1970(toEpochMilliseconds().toDouble() / 1000.0)
    val formatter = NSDateFormatter()
    formatter.dateStyle = NSDateFormatterMediumStyle
    formatter.timeStyle = NSDateFormatterMediumStyle
    formatter.dateFormat = cleanPattern
    return formatter.stringFromDate(date)
}

actual fun LocalDate.format(pattern: String): String {
    val date = NSDate.dateWithTimeIntervalSince1970(toEpochDays().toDouble() * 86_400)
    val formatter = NSDateFormatter()
    formatter.dateStyle = NSDateFormatterMediumStyle
    formatter.dateFormat = pattern
    return formatter.stringFromDate(date)
}

actual fun formatLocalDateTime(
    localDateTime: LocalDateTime,
    pattern: String,
    timeZone: TimeZone
): String {
    val comps = NSDateComponents().apply {
        year = localDateTime.year.toLong()
        month = localDateTime.monthNumber.toLong()
        day = localDateTime.dayOfMonth.toLong()
        hour = localDateTime.hour.toLong()
        minute = localDateTime.minute.toLong()
        second = localDateTime.second.toLong()
    }

    val nsTimeZone = NSTimeZone.timeZoneWithName(timeZone.id, null)!!
    val calendar = NSCalendar.currentCalendar
    calendar.timeZone = nsTimeZone

    val date = calendar.dateFromComponents(comps)!!

    val formatter = NSDateFormatter().apply {
        dateFormat = pattern
        this.timeZone = nsTimeZone
        locale = NSLocale.currentLocale
    }

    return formatter.stringFromDate(date)
}