package com.dnfapps.arrmatey.newest

import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.MediaInfo
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TitleMatcherTest {
    private fun movie(id: Long, title: String, date: String?) = DiscoverResult(id = id, mediaType = RequestType.Movie, title = title, releaseDate = date)

    private fun show(id: Long, name: String, date: String?) = DiscoverResult(id = id, mediaType = RequestType.Tv, name = name, firstAirDate = date)

    @Test
    fun testSimilarityIgnoresCaseAndPunctuation() {
        assertEquals(1.0, TitleMatcher.similarity("Spider-Man: No Way Home", "spider man no way home"))
        assertTrue(TitleMatcher.similarity("The Long Walk", "Long Walk") < 0.85)
    }

    @Test
    fun testPicksMovieWithMatchingYear() {
        val candidates = listOf(
            movie(1, "Dune", "1984-12-14"),
            movie(2, "Dune", "2021-09-15"),
            show(3, "Dune", "2021-01-01"),
        )
        val match = TitleMatcher.bestMatch(ParsedRelease("Dune", 2021), ReleaseKind.Movie, candidates)
        assertEquals(2L, match?.id)
    }

    @Test
    fun testAllowsOneYearDifference() {
        val match = TitleMatcher.bestMatch(
            ParsedRelease("Weapons", 2025),
            ReleaseKind.Movie,
            listOf(movie(7, "Weapons", "2024-12-30")),
        )
        assertEquals(7L, match?.id)
    }

    @Test
    fun testRejectsDistantTitle() {
        val match = TitleMatcher.bestMatch(
            ParsedRelease("Some Movie", 2025),
            ReleaseKind.Movie,
            listOf(movie(1, "Something Else Entirely", "2025-01-01")),
        )
        assertNull(match)
    }

    @Test
    fun testMatchesTvByName() {
        val match = TitleMatcher.bestMatch(
            ParsedRelease("Slow Horses", null, season = 5),
            ReleaseKind.Tv,
            listOf(movie(1, "Slow Horses", "2022-01-01"), show(2, "Slow Horses", "2022-04-01")),
        )
        assertEquals(2L, match?.id)
    }

    @Test
    fun testInLibraryUsesAvailableStatus() {
        val release = NewestRelease(ParsedRelease("Dune", 2021), ReleaseKind.Movie, 10, null, 1.0, emptyList(), 1)
        val available = movie(2, "Dune", "2021-09-15").copy(mediaInfo = MediaInfo(status = 5))
        assertTrue(NewestDownloadableItem(release, available).isInLibrary)
        assertFalse(NewestDownloadableItem(release, movie(2, "Dune", "2021-09-15")).isInLibrary)
        assertFalse(NewestDownloadableItem(release).isInLibrary)
        assertEquals("Dune", NewestDownloadableItem(release).displayTitle)
    }
}
