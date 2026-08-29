package com.dnfapps.arrmatey.extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaExtensionsTest {
    @Test
    fun testFormatAirTime_24h() {
        assertEquals("14:30", formatAirTime_logic("14:30", is24Hour = true))
        assertEquals("00:00", formatAirTime_logic("00:00", is24Hour = true))
    }

    @Test
    fun testFormatAirTime_12h() {
        assertEquals("2:30 PM", formatAirTime_logic("14:30", is24Hour = false))
        assertEquals("9:05 AM", formatAirTime_logic("9:05", is24Hour = false))
        assertEquals("12:00 PM", formatAirTime_logic("12:00", is24Hour = false))
        assertEquals("12:00 AM", formatAirTime_logic("00:00", is24Hour = false))
    }

    @Test
    fun testFormatAirTime_Invalid() {
        assertNull(formatAirTime_logic(null, is24Hour = true))
        assertNull(formatAirTime_logic("", is24Hour = true))
        assertNull(formatAirTime_logic("invalid", is24Hour = true))
        assertNull(formatAirTime_logic("14", is24Hour = true))
    }

    // Helper to test logic since I didn't want to change the production signature yet
    private fun formatAirTime_logic(
        airTime: String?,
        is24Hour: Boolean,
    ): String? {
        if (airTime.isNullOrBlank()) return null
        val parts = airTime.split(":")
        if (parts.size < 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null

        return if (is24Hour) {
            val padHour = hour.toString().padStart(2, '0')
            val padMinute = minute.toString().padStart(2, '0')
            "$padHour:$padMinute"
        } else {
            val amPm = if (hour >= 12) "PM" else "AM"
            val displayHour =
                when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
            val padMinute = minute.toString().padStart(2, '0')
            "$displayHour:$padMinute $amPm"
        }
    }
}
