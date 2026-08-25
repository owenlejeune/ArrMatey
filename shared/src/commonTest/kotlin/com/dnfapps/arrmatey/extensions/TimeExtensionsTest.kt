package com.dnfapps.arrmatey.extensions

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock

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
}
