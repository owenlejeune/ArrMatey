package com.dnfapps.arrmatey.discover

import com.dnfapps.arrmatey.discover.model.DiscoverFilterState
import com.dnfapps.arrmatey.discover.model.DiscoverGenres
import com.dnfapps.arrmatey.discover.model.DiscoverLanguages
import com.dnfapps.arrmatey.discover.model.DiscoverSortBy
import com.dnfapps.arrmatey.discover.model.DiscoverSortOrder
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.MediaInfo
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiscoverFilterAndSortTest {

    private val item1 = DiscoverResult(
        id = 1L,
        mediaType = RequestType.Movie,
        title = "Alpha",
        popularity = 100.0,
        voteAverage = 8.5,
        voteCount = 1000,
        releaseDate = "2020-01-01",
        genreIds = listOf(28, 12), // Action, Adventure
        originalLanguage = "en",
    )

    private val item2 = DiscoverResult(
        id = 2L,
        mediaType = RequestType.Movie,
        title = "Beta",
        popularity = 200.0,
        voteAverage = 7.0,
        voteCount = 500,
        releaseDate = "2022-05-15",
        genreIds = listOf(35, 12), // Comedy, Adventure
        originalLanguage = "fr",
    )

    private val item3 = DiscoverResult(
        id = 3L,
        mediaType = RequestType.Movie,
        title = "Gamma",
        popularity = 50.0,
        voteAverage = 9.0,
        voteCount = 2000,
        releaseDate = "2018-11-20",
        genreIds = listOf(28, 35), // Action, Comedy
        originalLanguage = "ja",
        mediaInfo = MediaInfo(status = 5), // Available in library
    )

    private val allItems = listOf(item1, item2, item3)

    @Test
    fun testDefaultFilterStateIsNotActive() {
        val state = DiscoverFilterState()
        assertFalse(state.isActive)
    }

    @Test
    fun testFilterStateIsActiveWhenModified() {
        assertTrue(DiscoverFilterState(sortBy = DiscoverSortBy.RATING).isActive)
        assertTrue(DiscoverFilterState(sortOrder = DiscoverSortOrder.ASCENDING).isActive)
        assertTrue(DiscoverFilterState(selectedGenreId = 28).isActive)
        assertTrue(DiscoverFilterState(selectedLanguage = "en").isActive)
        assertTrue(DiscoverFilterState(hideInLibrary = true).isActive)
    }

    @Test
    fun testGenreAndLanguageLookups() {
        assertEquals("Action", DiscoverGenres.getGenreName(28))
        assertEquals("Comedy", DiscoverGenres.getGenreName(35))
        assertEquals("Genre 9999", DiscoverGenres.getGenreName(9999))

        assertEquals("English", DiscoverLanguages.getLanguageName("en"))
        assertEquals("French", DiscoverLanguages.getLanguageName("fr"))
        assertEquals("Japanese", DiscoverLanguages.getLanguageName("ja"))
        assertEquals("XYZ", DiscoverLanguages.getLanguageName("xyz"))
    }

    @Test
    fun testFilteringByGenre() {
        val actionItems = allItems.filter { it.genreIds.contains(28) }
        assertEquals(2, actionItems.size)
        assertTrue(actionItems.contains(item1))
        assertTrue(actionItems.contains(item3))

        val comedyItems = allItems.filter { it.genreIds.contains(35) }
        assertEquals(2, comedyItems.size)
        assertTrue(comedyItems.contains(item2))
        assertTrue(comedyItems.contains(item3))
    }

    @Test
    fun testFilteringByLanguage() {
        val englishItems = allItems.filter { it.originalLanguage.equals("en", ignoreCase = true) }
        assertEquals(1, englishItems.size)
        assertEquals(item1, englishItems.first())

        val frenchItems = allItems.filter { it.originalLanguage.equals("fr", ignoreCase = true) }
        assertEquals(1, frenchItems.size)
        assertEquals(item2, frenchItems.first())
    }

    @Test
    fun testFilteringHideInLibrary() {
        val notInLibraryItems = allItems.filter { !it.isAvailableInLibrary }
        assertEquals(2, notInLibraryItems.size)
        assertFalse(notInLibraryItems.contains(item3))
    }

    @Test
    fun testSortingByPopularity() {
        val desc = allItems.sortedByDescending { it.popularity }
        assertEquals(listOf(item2, item1, item3), desc)

        val asc = allItems.sortedBy { it.popularity }
        assertEquals(listOf(item3, item1, item2), asc)
    }

    @Test
    fun testSortingByRating() {
        val desc = allItems.sortedByDescending { it.voteAverage }
        assertEquals(listOf(item3, item1, item2), desc)

        val asc = allItems.sortedBy { it.voteAverage }
        assertEquals(listOf(item2, item1, item3), asc)
    }

    @Test
    fun testSortingByReleaseDate() {
        val desc = allItems.sortedByDescending { it.releaseDate ?: "" }
        assertEquals(listOf(item2, item1, item3), desc)

        val asc = allItems.sortedBy { it.releaseDate ?: "" }
        assertEquals(listOf(item3, item1, item2), asc)
    }
}
