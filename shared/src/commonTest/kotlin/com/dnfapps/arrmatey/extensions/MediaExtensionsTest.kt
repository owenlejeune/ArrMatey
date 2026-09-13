package com.dnfapps.arrmatey.extensions

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.arr.api.model.MonitorNewItems
import com.dnfapps.arrmatey.arr.api.model.SeriesType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MediaExtensionsTest {
    @Test
    fun testMergeWithLibrary_DoesNotMatchZeroIds() {
        val librarySeries = createSeries(title = "The Cult Behind the Killer", tvdbId = 12345, tmdbId = 0L)
        val searchSeries1 = createSeries(title = "Killer Stories", tvdbId = 465974, tmdbId = 0L)
        val searchSeries2 = createSeries(title = "Killer", tvdbId = 331242, tmdbId = 0L)

        val searchResults: List<ArrMedia> = listOf(searchSeries1, searchSeries2)
        val library: List<ArrMedia> = listOf(librarySeries)

        val merged = searchResults.mergeWithLibrary(library)

        assertEquals("Killer Stories", merged[0].title)
        assertEquals("Killer", merged[1].title)
    }

    @Test
    fun testMergeWithLibrary_MatchesValidIds() {
        val librarySeries = createSeries(title = "The Killer Closer", tvdbId = 351323, tmdbId = 120285L, id = 99L)
        val searchSeries = createSeries(title = "The Killer Closer (Search)", tvdbId = 351323, tmdbId = 120285L, id = null)

        val searchResults: List<ArrMedia> = listOf(searchSeries)
        val library: List<ArrMedia> = listOf(librarySeries)
        val merged = searchResults.mergeWithLibrary(library)

        assertEquals(1, merged.size)
        assertEquals(99L, merged[0].id)
    }

    private fun createSeries(
        title: String,
        tvdbId: Long,
        tmdbId: Long?,
        id: Long? = null,
    ): ArrSeries =
        ArrSeries(
            id = id,
            title = title,
            originalLanguage = Language(1, "English"),
            year = 2020,
            qualityProfileId = 1,
            monitored = true,
            runtime = 45,
            status = MediaStatus.Ended,
            ended = true,
            seasonFolder = false,
            monitorNewItems = MonitorNewItems.All,
            useSceneNumbering = false,
            tvdbId = tvdbId,
            tmdbId = tmdbId,
            seriesType = SeriesType.Standard,
        )

    @Test
    fun testFormatAirTime_24h() {
        assertEquals("14:30", formatAirTime("14:30", is24Hour = true))
        assertEquals("00:00", formatAirTime("00:00", is24Hour = true))
    }

    @Test
    fun testFormatAirTime_12h() {
        assertEquals("2:30 PM", formatAirTime("14:30", is24Hour = false))
        assertEquals("9:05 AM", formatAirTime("9:05", is24Hour = false))
        assertEquals("12:00 PM", formatAirTime("12:00", is24Hour = false))
        assertEquals("12:00 AM", formatAirTime("00:00", is24Hour = false))
    }

    @Test
    fun testFormatAirTime_Invalid() {
        assertNull(formatAirTime(null, is24Hour = true))
        assertNull(formatAirTime("", is24Hour = true))
        assertNull(formatAirTime("invalid", is24Hour = true))
        assertNull(formatAirTime("14", is24Hour = true))
    }
}
