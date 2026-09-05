package com.dnfapps.arrmatey.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AppThemeTest {
    @Test
    fun testAllEntriesExposeResource() {
        AppTheme.entries.forEach { assertNotNull(it.resource) }
    }

    @Test
    fun testExpectedEntries() {
        val names = AppTheme.entries.map { it.name }
        assertEquals(listOf("System", "Light", "Dark"), names)
    }
}
