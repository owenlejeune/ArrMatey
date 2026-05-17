package com.dnfapps.arrmatey.extensions

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun LocalDate.isToday(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val today = Clock.System.todayIn(timeZone)
    return this == today
}

fun LocalDate.isToday() = isToday(TimeZone.currentSystemDefault())

fun LocalDate.isAfterToday(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val today = Clock.System.todayIn(timeZone)
    return this > today
}

fun LocalDate.isAfterToday() = isAfterToday(TimeZone.currentSystemDefault())

fun LocalDate.isTodayOrAfter(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val today = Clock.System.todayIn(timeZone)
    return this >= today
}

fun LocalDate.isTodayOrAfter(): Boolean = isTodayOrAfter(timeZone = TimeZone.currentSystemDefault())

fun LocalDate.ifTodayOrAfter(): LocalDate? = if (isTodayOrAfter()) this else null

fun LocalDate.isTodayOrBefore(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val today = Clock.System.todayIn(timeZone)
    return this <= today
}

fun LocalDate.isTodayOrBefore(): Boolean = isTodayOrBefore(TimeZone.currentSystemDefault())

fun LocalDate.isBeforeToday(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val today = Clock.System.todayIn(timeZone)
    return this < today
}

fun LocalDate.isBeforeToday(): Boolean = isBeforeToday(TimeZone.currentSystemDefault())

@OptIn(ExperimentalTime::class)
fun Clock.Companion.localToday(): LocalDate {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
}

fun Instant.isTodayOrAfter(): Boolean {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val instantDate = this.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return instantDate >= today
}

fun Instant.ifTodayOrAfter(): Instant? {
    return if (isTodayOrAfter()) this else null
}

fun Instant.isToday(): Boolean {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val instantDate = this.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return instantDate == today
}

fun Instant.isEqual(date: LocalDate): Boolean {
    val timeZone: TimeZone = TimeZone.currentSystemDefault()
    return this.toLocalDateTime(timeZone).date == date
}

fun Instant?.isBetween(start: LocalDate, end: LocalDate): Boolean {
    if (this == null) return false

    val timeZone = TimeZone.currentSystemDefault()
    val startInstant = start.atStartOfDayIn(timeZone)
    val nextDay = LocalDate(end.year, end.month, end.day).run {
        val instantOfEndDay = atStartOfDayIn(timeZone)
        instantOfEndDay.plus(1.days)
    }
    return this in startInstant..<nextDay
}
