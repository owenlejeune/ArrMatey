package com.dnfapps.arrmatey.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class OperationStatusTest {
    @Test
    fun testIdleIsSingleton() {
        assertSame(OperationStatus.Idle, OperationStatus.Idle)
    }

    @Test
    fun testInProgressIsSingleton() {
        assertSame(OperationStatus.InProgress, OperationStatus.InProgress)
    }

    @Test
    fun testSuccessDefaults() {
        val success = OperationStatus.Success()
        assertNull(success.message)
        assertNull(success.result)
    }

    @Test
    fun testSuccessCarriesMessageAndResult() {
        val success = OperationStatus.Success(message = "ok", result = 42)
        assertEquals("ok", success.message)
        assertEquals(42, success.result)
    }

    @Test
    fun testSuccessEquality() {
        val a = OperationStatus.Success(message = "done", result = "payload")
        val b = OperationStatus.Success(message = "done", result = "payload")
        val c = OperationStatus.Success(message = "different")
        assertEquals(a, b)
        assertNotEquals(a, c)
    }

    @Test
    fun testErrorDefaults() {
        val error = OperationStatus.Error()
        assertNull(error.code)
        assertNull(error.message)
        assertNull(error.cause)
    }

    @Test
    fun testErrorCarriesFields() {
        val cause = IllegalStateException("boom")
        val error = OperationStatus.Error(code = 500, message = "failure", cause = cause)
        assertEquals(500, error.code)
        assertEquals("failure", error.message)
        assertSame(cause, error.cause)
    }

    @Test
    fun testDistinctSubtypesAreNotEqual() {
        assertNotEquals<OperationStatus>(OperationStatus.Idle, OperationStatus.InProgress)
        assertNotEquals<OperationStatus>(OperationStatus.Success(), OperationStatus.Error())
    }

    @Test
    fun testSealedInterfaceHierarchy() {
        val statuses: List<OperationStatus> =
            listOf(
                OperationStatus.Idle,
                OperationStatus.InProgress,
                OperationStatus.Success(),
                OperationStatus.Error(),
            )
        assertEquals(4, statuses.size)
    }
}
