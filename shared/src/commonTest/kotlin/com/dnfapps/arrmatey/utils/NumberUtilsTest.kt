package com.dnfapps.arrmatey.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberUtilsTest {

    @Test
    fun testFormatToOneDecimalAppendsZeroForIntegers() {
        assertEquals("5.0", 5f.formatToOneDecimal())
        assertEquals("0.0", 0f.formatToOneDecimal())
    }

    @Test
    fun testFormatToOneDecimalTruncatesExtraDigits() {
        assertEquals("1.2", 1.234f.formatToOneDecimal())
        assertEquals("9.8", 9.87f.formatToOneDecimal())
    }

    @Test
    fun testFormatToOneDecimalKeepsSingleDecimal() {
        assertEquals("3.1", 3.1f.formatToOneDecimal())
    }

    @Test
    fun testFormatToOneDecimalHandlesNegative() {
        assertEquals("-2.5", (-2.5f).formatToOneDecimal())
    }
}
