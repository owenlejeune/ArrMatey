package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InstanceMediaPresenceTest {
    private val instance =
        Instance(
            id = 1,
            label = "Test",
            url = "http://localhost",
            apiKey = EncryptedString("k"),
            type = InstanceType.Sonarr,
        )

    @Test
    fun testIsPresentWhenArrMediaHasNonZeroId() {
        val presence = InstanceMediaPresence(instance = instance, arrMedia = MockMedia.Sonarr)
        assertTrue(presence.isPresent)
    }

    @Test
    fun testIsNotPresentWhenArrMediaIsNull() {
        val presence = InstanceMediaPresence(instance = instance, arrMedia = null)
        assertFalse(presence.isPresent)
    }

    @Test
    fun testExplicitIsPresentOverride() {
        val presence = InstanceMediaPresence(instance = instance, arrMedia = null, isPresent = true)
        assertTrue(presence.isPresent)
    }

    @Test
    fun testInstanceReferenceRetained() {
        val presence = InstanceMediaPresence(instance = instance)
        assertEquals(instance, presence.instance)
    }
}
