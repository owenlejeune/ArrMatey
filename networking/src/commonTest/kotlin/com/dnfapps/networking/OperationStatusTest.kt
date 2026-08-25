package com.dnfapps.networking

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OperationStatusTest {

    @Test
    fun testStatusInheritance() {
        val idle = OperationStatus.Idle
        assertTrue(idle is OperationStatus)

        val inProgress = OperationStatus.InProgress
        assertTrue(inProgress is OperationStatus)

        val success = OperationStatus.Success("Done")
        assertTrue(success is OperationStatus)
        assertEquals("Done", success.message)

        val error = OperationStatus.Error(404, "Fail")
        assertTrue(error is OperationStatus)
        assertEquals(404, error.code)
    }
}
