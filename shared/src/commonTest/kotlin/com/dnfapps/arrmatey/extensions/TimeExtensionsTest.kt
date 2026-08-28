package com.dnfapps.arrmatey.extensions

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class TimeExtensionsTest {

    private val timeZone = TimeZone.UTC

    @Test
    fun testLocalDateIsToday() {
        val today = Clock.System.todayIn(timeZone)
        assertTrue(today.isToday(timeZone))
        
        assertFalse(LocalDate(1999, 12, 31).isToday(timeZone))
    }

    @Test
    fun testLocalDateIsAfterToday() {
        val today = Clock.System.todayIn(timeZone)
        val future = LocalDate(today.year + 1, today.month, today.day)
        val past = LocalDate(today.year - 1, today.month, today.day)

        assertTrue(future.isAfterToday(timeZone))
        assertFalse(past.isAfterToday(timeZone))
        assertFalse(today.isAfterToday(timeZone))
    }

    @Test
    fun testLocalDateIsBeforeToday() {
        val today = Clock.System.todayIn(timeZone)
        val past = LocalDate(today.year - 1, today.month, today.day)
        val future = LocalDate(today.year + 1, today.month, today.day)

        assertTrue(past.isBeforeToday(timeZone))
        assertFalse(today.isBeforeToday(timeZone))
        assertFalse(future.isBeforeToday(timeZone))
    }

    @Test
    fun testLocalDateIsTodayOrAfter() {
        val today = Clock.System.todayIn(timeZone)
        val past = LocalDate(today.year - 1, today.month, today.day)
        val future = LocalDate(today.year + 1, today.month, today.day)

        assertTrue(today.isTodayOrAfter(timeZone))
        assertTrue(future.isTodayOrAfter(timeZone))
        assertFalse(past.isTodayOrAfter(timeZone))
    }

    @Test
    fun testLocalDateIsTodayOrBefore() {
        val today = Clock.System.todayIn(timeZone)
        val past = LocalDate(today.year - 1, today.month, today.day)
        val future = LocalDate(today.year + 1, today.month, today.day)

        assertTrue(today.isTodayOrBefore(timeZone))
        assertTrue(past.isTodayOrBefore(timeZone))
        assertFalse(future.isTodayOrBefore(timeZone))
    }

    @Test
    fun testInstantIsBetweenInclusiveStart() {
        val start = LocalDate(2024, 6, 1)
        val end = LocalDate(2024, 6, 10)
        val onStart = start.atStartOfDayIn(TimeZone.currentSystemDefault())
        assertTrue(onStart.isBetween(start, end))
    }

    @Test
    fun testInstantIsBetweenIncludesEndDay() {
        val start = LocalDate(2024, 6, 1)
        val end = LocalDate(2024, 6, 10)
        // last hour of the end day should still be included
        val lateOnEnd = end.atStartOfDayIn(TimeZone.currentSystemDefault()) + 23.hours
        assertTrue(lateOnEnd.isBetween(start, end))
    }

    @Test
    fun testInstantIsBetweenExcludesNextDayAfterEnd() {
        val start = LocalDate(2024, 6, 1)
        val end = LocalDate(2024, 6, 10)
        // start of day after end is excluded
        val nextDayStart = LocalDate(2024, 6, 11).atStartOfDayIn(TimeZone.currentSystemDefault())
        assertFalse(nextDayStart.isBetween(start, end))
    }

    @Test
    fun testInstantIsBetweenExcludesBeforeStart() {
        val start = LocalDate(2024, 6, 1)
        val end = LocalDate(2024, 6, 10)
        val before = LocalDate(2024, 5, 31).atStartOfDayIn(TimeZone.currentSystemDefault())
        assertFalse(before.isBetween(start, end))
    }

    @Test
    fun testNullableInstantIsBetweenReturnsFalse() {
        val start = LocalDate(2024, 6, 1)
        val end = LocalDate(2024, 6, 10)
        val nullInstant: kotlin.time.Instant? = null
        assertFalse(nullInstant.isBetween(start, end))
    }

    @Test
    fun testIfTodayOrAfterReturnsThisWhenTrue() {
        val future = LocalDate(9999, 12, 31)
        assertTrue(future.ifTodayOrAfter() == future)
    }

    @Test
    fun testIfTodayOrAfterReturnsNullWhenBefore() {
        val past = LocalDate(1900, 1, 1)
        assertNull(past.ifTodayOrAfter())
    }
}
