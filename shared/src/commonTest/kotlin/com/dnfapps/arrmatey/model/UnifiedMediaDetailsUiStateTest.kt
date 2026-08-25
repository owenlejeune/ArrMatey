package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.arr.api.model.MockMedia
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UnifiedMediaDetailsUiStateTest {

    @Test
    fun testHasArrId() {
        val successState = UnifiedMediaDetailsUiState.Success(
            arrMedia = MockMedia.Default
        )
        assertTrue(successState.hasArrId)

        val noMediaState = UnifiedMediaDetailsUiState.Success(
            arrMedia = null
        )
        assertFalse(noMediaState.hasArrId)
    }

    @Test
    fun testDisplayTitle() {
        val state = UnifiedMediaDetailsUiState.Success(
            arrMedia = MockMedia.Sonarr
        )
        assertEquals("A Totally Awesome Series", state.displayTitle)
    }
}
