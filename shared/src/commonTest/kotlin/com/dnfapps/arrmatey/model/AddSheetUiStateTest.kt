package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddSheetUiStateTest {
    @Test
    fun testDefaultConstructorProducesEmptyState() {
        val state = AddSheetUiState()
        assertNull(state.targetInstance)
        assertTrue(state.qualityProfiles.isEmpty())
        assertTrue(state.rootFolders.isEmpty())
        assertTrue(state.tags.isEmpty())
        assertTrue(state.availableInstances.isEmpty())
    }

    @Test
    fun testSecondaryConstructorForIosMatchesDefaults() {
        val iosStyle = AddSheetUiState()
        val explicit = AddSheetUiState(targetInstance = null)
        assertEquals(explicit, iosStyle)
    }

    @Test
    fun testTargetInstanceRetained() {
        val instance =
            Instance(
                id = 1,
                label = "Radarr",
                url = "http://localhost:7878",
                apiKey = EncryptedString("k"),
                type = InstanceType.Radarr,
            )
        val state = AddSheetUiState(targetInstance = instance)
        assertEquals(instance, state.targetInstance)
    }
}
