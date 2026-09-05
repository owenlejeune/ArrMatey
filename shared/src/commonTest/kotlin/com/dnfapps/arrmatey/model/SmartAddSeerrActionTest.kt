package com.dnfapps.arrmatey.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SmartAddSeerrActionTest {
    @Test
    fun testDefaultIsAlwaysAsk() {
        assertEquals(SmartAddSeerrAction.AlwaysAsk, SmartAddSeerrAction.default)
    }

    @Test
    fun testAllEntriesExposeResource() {
        SmartAddSeerrAction.entries.forEach { assertNotNull(it.resource) }
    }

    @Test
    fun testAllExpectedEntriesPresent() {
        val names = SmartAddSeerrAction.entries.map { it.name }.toSet()
        assertEquals(setOf("Approve", "Decline", "AlwaysAsk"), names)
    }
}
