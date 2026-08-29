package com.dnfapps.arrmatey.extensions

import kotlin.test.Test
import kotlin.test.assertEquals

class IntExtensionsTest {
    @Test
    fun testFormatMinutesAsRuntimeZero() {
        assertEquals("0m", 0.formatMinutesAsRuntime())
    }

    @Test
    fun testFormatMinutesAsRuntimeMinutesOnly() {
        assertEquals("1m", 1.formatMinutesAsRuntime())
        assertEquals("59m", 59.formatMinutesAsRuntime())
    }

    @Test
    fun testFormatMinutesAsRuntimeHoursOnly() {
        assertEquals("1h", 60.formatMinutesAsRuntime())
        assertEquals("2h", 120.formatMinutesAsRuntime())
    }

    @Test
    fun testFormatMinutesAsRuntimeHoursAndMinutes() {
        assertEquals("1h 1m", 61.formatMinutesAsRuntime())
        assertEquals("2h 5m", 125.formatMinutesAsRuntime())
    }

    @Test
    fun testFormatSecondsAsRuntimeZero() {
        assertEquals("0s", 0.formatSecondsAsRuntime())
    }

    @Test
    fun testFormatSecondsAsRuntimeSecondsOnly() {
        assertEquals("30s", 30.formatSecondsAsRuntime())
    }

    @Test
    fun testFormatSecondsAsRuntimeMinutesAndSeconds() {
        assertEquals("1m 30s", 90.formatSecondsAsRuntime())
    }

    @Test
    fun testFormatSecondsAsRuntimeHoursOnly() {
        assertEquals("1h", 3600.formatSecondsAsRuntime())
    }

    @Test
    fun testFormatSecondsAsRuntimeHoursMinutesSeconds() {
        assertEquals("1h 1m 1s", 3661.formatSecondsAsRuntime())
    }

    @Test
    fun testFormatSecondsAsRuntimeHoursAndMinutes() {
        assertEquals("1h 1m", 3660.formatSecondsAsRuntime())
    }

    @Test
    fun testFormatAsDurationZero() {
        assertEquals("00:00:00", 0.formatAsDuration())
    }

    @Test
    fun testFormatAsDurationSingleSecond() {
        assertEquals("00:00:01", 1000.formatAsDuration())
    }

    @Test
    fun testFormatAsDurationOneHourOneMinuteOneSecond() {
        assertEquals("01:01:01", 3_661_000.formatAsDuration())
    }

    @Test
    fun testFormatAsDurationTruncatesSubSecondMillis() {
        assertEquals("00:00:00", 999.formatAsDuration())
    }

    @Test
    fun testPadStart() {
        assertEquals("07", 7.padStart(2, '0'))
        assertEquals("123", 123.padStart(2, '0'))
        assertEquals("---9", 9.padStart(4, '-'))
    }

    @Test
    fun testDoubleToOneDecimalIntegerAppendsZero() {
        assertEquals("3.0", 3.0.toOneDecimal())
        assertEquals("0.0", 0.0.toOneDecimal())
    }

    @Test
    fun testDoubleToOneDecimalSingleDecimalKept() {
        assertEquals("1.5", 1.5.toOneDecimal())
    }
}
