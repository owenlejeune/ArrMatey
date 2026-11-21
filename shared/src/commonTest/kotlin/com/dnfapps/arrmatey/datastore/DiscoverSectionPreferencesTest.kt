package com.dnfapps.arrmatey.datastore

import com.dnfapps.arrmatey.discover.model.DiscoverCategory
import com.dnfapps.arrmatey.discover.model.DiscoverCategory.POPULAR_MOVIES
import com.dnfapps.arrmatey.discover.model.DiscoverCategory.POPULAR_SERIES
import com.dnfapps.arrmatey.discover.model.DiscoverCategory.PROWLARR_MOVIES
import com.dnfapps.arrmatey.discover.model.DiscoverCategory.PROWLARR_SERIES
import com.dnfapps.arrmatey.discover.model.DiscoverCategory.TRENDING
import com.dnfapps.arrmatey.discover.model.DiscoverCategory.UPCOMING_MOVIES
import com.dnfapps.arrmatey.discover.model.DiscoverCategory.UPCOMING_SERIES
import kotlin.test.Test
import kotlin.test.assertEquals

class DiscoverSectionPreferencesTest {
    @Test
    fun testDefaultsEndWithProwlarrRows() {
        assertEquals(listOf(PROWLARR_MOVIES, PROWLARR_SERIES), DiscoverSectionPreferences().visibleCategories.takeLast(2))
    }

    @Test
    fun testOlderSavedPreferencesGainProwlarrRowsAtTheEnd() {
        // Saved by a build that had only the Seerr rows: Popular Series moved up, Upcoming Series hidden.
        val saved =
            """{"visibleCategories":["POPULAR_SERIES","TRENDING","POPULAR_MOVIES","UPCOMING_MOVIES"],""" +
                """"hiddenCategories":["UPCOMING_SERIES"],"hideInLibraryContent":true}"""

        val prefs = DiscoverSectionPreferencesSerializer.deserialize(saved)

        assertEquals(
            listOf(POPULAR_SERIES, TRENDING, POPULAR_MOVIES, UPCOMING_MOVIES, PROWLARR_MOVIES, PROWLARR_SERIES),
            prefs.visibleCategories,
        )
        assertEquals(listOf(UPCOMING_SERIES), prefs.hiddenCategories)
        assertEquals(true, prefs.hideInLibraryContent)
    }

    @Test
    fun testSavedProwlarrOrderAndHidingSurviveARoundTrip() {
        val prefs = DiscoverSectionPreferences(
            visibleCategories = listOf(PROWLARR_SERIES, TRENDING, POPULAR_MOVIES, UPCOMING_MOVIES, POPULAR_SERIES, UPCOMING_SERIES),
            hiddenCategories = listOf(PROWLARR_MOVIES),
        )

        val restored = DiscoverSectionPreferencesSerializer.deserialize(DiscoverSectionPreferencesSerializer.serialize(prefs))

        assertEquals(prefs, restored)
    }

    @Test
    fun testWithoutProwlarrAndRestore() {
        val original = DiscoverSectionPreferences(
            visibleCategories = listOf(TRENDING, PROWLARR_MOVIES, POPULAR_MOVIES, UPCOMING_MOVIES),
            hiddenCategories = listOf(POPULAR_SERIES, PROWLARR_SERIES, UPCOMING_SERIES),
        )

        val shown = original.withoutProwlarr()
        assertEquals(listOf(TRENDING, POPULAR_MOVIES, UPCOMING_MOVIES), shown.visibleCategories)
        assertEquals(listOf(POPULAR_SERIES, UPCOMING_SERIES), shown.hiddenCategories)

        // The user hides Trending in the sheet, which only showed the Seerr rows.
        val edited = shown.copy(
            visibleCategories = listOf(POPULAR_MOVIES, UPCOMING_MOVIES),
            hiddenCategories = listOf(POPULAR_SERIES, UPCOMING_SERIES, TRENDING),
        )
        val saved = edited.restoreProwlarrFrom(original)

        assertEquals(listOf(POPULAR_MOVIES, PROWLARR_MOVIES, UPCOMING_MOVIES), saved.visibleCategories)
        assertEquals(listOf(POPULAR_SERIES, UPCOMING_SERIES, TRENDING, PROWLARR_SERIES), saved.hiddenCategories)
        assertEquals(DiscoverCategory.entries.toSet(), (saved.visibleCategories + saved.hiddenCategories).toSet())
    }
}
