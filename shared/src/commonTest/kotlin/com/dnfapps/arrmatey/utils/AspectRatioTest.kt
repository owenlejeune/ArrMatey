package com.dnfapps.arrmatey.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class AspectRatioTest {

    @Test
    fun testPosterRatio() {
        assertEquals(0.675f, AspectRatio.Poster.ratio)
    }

    @Test
    fun testCoverRatio() {
        assertEquals(1f, AspectRatio.Cover.ratio)
    }

    @Test
    fun testAllValuesAccountedFor() {
        assertEquals(setOf(AspectRatio.Poster, AspectRatio.Cover), AspectRatio.entries.toSet())
    }
}
