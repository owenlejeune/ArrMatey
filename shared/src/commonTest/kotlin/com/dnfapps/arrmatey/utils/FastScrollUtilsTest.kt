package com.dnfapps.arrmatey.utils

import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.AuthorMonitorType
import com.dnfapps.arrmatey.arr.api.model.BookshelfRatings
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.compose.utils.alphabet
import com.dnfapps.arrmatey.compose.utils.isTitleSort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FastScrollUtilsTest {

    private fun createMovie(title: String, sortTitle: String = title): ArrMovie = ArrMovie(
        id = 1L,
        title = title,
        sortTitle = sortTitle,
        cleanTitle = title.lowercase(),
        originalLanguage = Language(1, "English"),
        year = 2024,
        qualityProfileId = 1,
        monitored = false,
        runtime = 120,
        status = MediaStatus.Released,
        tmdbId = 1L,
        secondaryYearSourceId = 0,
        minimumAvailability = MediaStatus.Announced,
    )

    @Test
    fun testIsTitleSort() {
        assertTrue(SortBy.Title.isTitleSort)
        assertTrue(SortBy.TitleLastFirst.isTitleSort)
        assertTrue(SortBy.Name.isTitleSort)
        assertFalse(SortBy.Year.isTitleSort)
        assertFalse(SortBy.Added.isTitleSort)
    }

    @Test
    fun testAlphabet() {
        val ascAlphabet = SortOrder.Asc.alphabet
        assertEquals("#", ascAlphabet.first())
        assertEquals("A", ascAlphabet[1])
        assertEquals("Z", ascAlphabet.last())

        val descAlphabet = SortOrder.Desc.alphabet
        assertEquals("Z", descAlphabet.first())
        assertEquals("A", descAlphabet[25])
        assertEquals("#", descAlphabet.last())
    }

    @Test
    fun testGetSortKeyAndSectionLetter() {
        val movie = createMovie(title = "The Matrix", sortTitle = "matrix")
        assertEquals("matrix", FastScrollUtils.getSortKey(movie, SortBy.Title))
        assertEquals("M", FastScrollUtils.getSectionLetter(movie, SortBy.Title))

        val symbolMovie = createMovie(title = "1917", sortTitle = "1917")
        assertEquals("#", FastScrollUtils.getSectionLetter(symbolMovie, SortBy.Title))

        val author = Author(
            id = 3,
            title = "Stephen King",
            sortTitle = "king stephen",
            sortNameLastFirst = "King, Stephen",
            qualityProfileId = 1,
            monitored = true,
            images = emptyList(),
            ratings = BookshelfRatings(votes = 10, value = 4.5f, popularity = 8.0f),
            status = MediaStatus.Continuing,
            metadataProfileId = 1,
            monitorNewItems = AuthorMonitorType.None,
        )
        assertEquals("King, Stephen", FastScrollUtils.getSortKey(author, SortBy.TitleLastFirst))
        assertEquals("K", FastScrollUtils.getSectionLetter(author, SortBy.TitleLastFirst))
        assertEquals("king stephen", FastScrollUtils.getSortKey(author, SortBy.Title))
        assertEquals("K", FastScrollUtils.getSectionLetter(author, SortBy.Title))
    }

    @Test
    fun testBuildLetterIndexMap() {
        val items = listOf(
            createMovie(title = "Avatar", sortTitle = "avatar"),
            createMovie(title = "Batman", sortTitle = "batman"),
            createMovie(title = "Beetlejuice", sortTitle = "beetlejuice"),
            createMovie(title = "Inception", sortTitle = "inception"),
        )

        val map = FastScrollUtils.buildLetterIndexMap(items, SortBy.Title)
        assertEquals(0, map["A"])
        assertEquals(1, map["B"])
        assertEquals(3, map["I"])
        assertEquals(null, map["C"])
    }

    @Test
    fun testFindTargetIndex() {
        val letterIndexMap = mapOf("A" to 0, "B" to 1, "I" to 3)
        val alphabet = SortOrder.Asc.alphabet

        assertEquals(0, FastScrollUtils.findTargetIndex("A", letterIndexMap, alphabet, 4))
        assertEquals(1, FastScrollUtils.findTargetIndex("B", letterIndexMap, alphabet, 4))
        // C is missing, so it should fall back to next available (I at index 3)
        assertEquals(3, FastScrollUtils.findTargetIndex("C", letterIndexMap, alphabet, 4))
        // Z is past I, so it should fall back to last item index (3)
        assertEquals(3, FastScrollUtils.findTargetIndex("Z", letterIndexMap, alphabet, 4))
        // Empty list
        assertEquals(0, FastScrollUtils.findTargetIndex("A", emptyMap(), alphabet, 0))
    }
}
