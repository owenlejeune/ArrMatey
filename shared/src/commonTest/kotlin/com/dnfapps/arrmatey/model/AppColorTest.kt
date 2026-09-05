package com.dnfapps.arrmatey.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AppColorTest {
    @Test
    fun testAllEntriesExposeResource() {
        AppColor.entries.forEach { assertNotNull(it.resource) }
    }

    @Test
    fun testExpectedEntries() {
        val names = AppColor.entries.map { it.name }
        assertEquals(listOf("Dynamic", "ArrMatey", "Amoled"), names)
    }
}
