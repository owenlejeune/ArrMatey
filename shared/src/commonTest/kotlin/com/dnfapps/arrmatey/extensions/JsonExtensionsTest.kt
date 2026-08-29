package com.dnfapps.arrmatey.extensions

import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonExtensionsTest {
    @Test
    fun testToJsonArrayEmpty() {
        val array = emptyList<String>().toJsonArray()
        assertEquals(0, array.size)
    }

    @Test
    fun testToJsonArrayWrapsEachStringAsPrimitive() {
        val array = listOf("a", "b", "c").toJsonArray()
        assertEquals(3, array.size)
        array.forEachIndexed { index, element ->
            assertTrue(element is JsonPrimitive)
            assertEquals(listOf("a", "b", "c")[index], element.content)
        }
    }

    @Test
    fun testToJsonArrayPreservesOrderAndDuplicates() {
        val array = listOf("x", "x", "y").toJsonArray()
        assertEquals("x", (array[0] as JsonPrimitive).content)
        assertEquals("x", (array[1] as JsonPrimitive).content)
        assertEquals("y", (array[2] as JsonPrimitive).content)
    }
}
