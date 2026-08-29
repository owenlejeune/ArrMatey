package com.dnfapps.arrmatey.extensions

import kotlin.test.Test
import kotlin.test.assertEquals

class FloatExtensionsTest {
    @Test
    fun testFormatAgeMinutesJustNowForZero() {
        assertEquals("Just now", 0f.formatAgeMinutes())
    }

    @Test
    fun testFormatAgeMinutesJustNowForNegative() {
        assertEquals("Just now", (-1f).formatAgeMinutes())
        assertEquals("Just now", (-9999f).formatAgeMinutes())
    }

    @Test
    fun testFormatAgeMinutesMinutesRange() {
        assertEquals("1 minutes", 1f.formatAgeMinutes())
        assertEquals("59 minutes", 59f.formatAgeMinutes())
        assertEquals("118 minutes", 118f.formatAgeMinutes())
        assertEquals("119 minutes", 119f.formatAgeMinutes())
    }

    @Test
    fun testFormatAgeMinutesHoursRange() {
        assertEquals("2 hours", 120f.formatAgeMinutes())
        assertEquals("47 hours", 2879f.formatAgeMinutes())
    }

    @Test
    fun testFormatAgeMinutesDaysRange() {
        assertEquals("2 days", 2880f.formatAgeMinutes())
        assertEquals("89 days", 129599f.formatAgeMinutes())
    }

    @Test
    fun testFormatAgeMinutesMonthsRange() {
        assertEquals("3 months", 129600f.formatAgeMinutes())
    }

    @Test
    fun testFormatAgeMinutesYearsRange() {
        assertEquals("1 years", 525600f.formatAgeMinutes())
    }
}
