package com.dnfapps.arrmatey.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HelpersTest {
    @Test
    fun testThenGetTrueReturnsResult() {
        assertEquals("value", true thenGet "value")
        assertEquals(42, true thenGet 42)
    }

    @Test
    fun testThenGetFalseReturnsNull() {
        assertNull(false thenGet "value")
        assertNull(false thenGet 42)
    }

    @Test
    fun testThenGetPreservesNullResult() {
        val result: String? = true thenGet null
        assertNull(result)
    }
}
