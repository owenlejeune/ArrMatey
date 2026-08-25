package com.dnfapps.networking

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NetworkResultTest {

    @Test
    fun testMapSuccess() {
        val result = NetworkResult.Success(10)
        val mapped = result.map { it * 2 }
        assertTrue(mapped is NetworkResult.Success)
        assertEquals(20, mapped.data)
    }

    @Test
    fun testMapError() {
        val result = NetworkResult.Error(code = 404, message = "Not Found")
        val mapped = result.map { it }
        assertTrue(mapped is NetworkResult.Error)
        assertEquals(404, mapped.code)
        assertEquals("Not Found", mapped.message)
    }

    @Test
    fun testMapValues() {
        val result = NetworkResult.Success(listOf(1, 2, 3))
        val mapped = result.mapValues { it * 2 }
        assertTrue(mapped is NetworkResult.Success)
        assertEquals(listOf(2, 4, 6), mapped.data)
    }
}
