package com.dnfapps.arrmatey.extensions

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
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

fun Instant.isToday(): Boolean {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val instantDate = this.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return instantDate == today
}
