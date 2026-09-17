package com.dnfapps.arrmatey.discover

import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.arr.api.model.SeriesType
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.model.SearchResultWeaver
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchResultWeaverTest {
    private fun createMockMovie(
        title: String,
        tmdbId: Long = 1L,
        popularity: Double = 10.0,
        inLibrary: Boolean = false,
    ): ArrMovie =
        ArrMovie(
            id = if (inLibrary) 100L else null,
            title = title,
            cleanTitle = title.lowercase().replace("[^a-z0-9]".toRegex(), ""),
            originalLanguage = Language(1, "English"),
            year = 2000,
            qualityProfileId = 1,
            monitored = false,
            runtime = 100,
            status = MediaStatus.Released,
            tmdbId = tmdbId,
            secondaryYearSourceId = 0,
            minimumAvailability = MediaStatus.Released,
            popularity = popularity,
        )

    private fun createMockSeries(
        title: String,
        tvdbId: Long = 2L,
    ): ArrSeries =
        ArrSeries(
            id = null,
            title = title,
            cleanTitle = title.lowercase().replace("[^a-z0-9]".toRegex(), ""),
            originalLanguage = Language(1, "English"),
            year = 2011,
            qualityProfileId = 1,
            monitored = false,
            runtime = 30,
            status = MediaStatus.Ended,
            seriesType = SeriesType.Standard,
            ended = true,
            seasonFolder = true,
            monitorNewItems = com.dnfapps.arrmatey.arr.api.model.MonitorNewItems.None,
            useSceneNumbering = false,
            tvdbId = tvdbId,
        )

    @Test
    fun testExactMatchScoresHigherThanPrefixMatch() {
        val exactMovie = SearchResult.ArrMediaResult(createMockMovie("X-Men", tmdbId = 1L))
        val prefixMovie = SearchResult.ArrMediaResult(createMockMovie("X-Men: Apocalypse", tmdbId = 2L))

        val weaved = SearchResultWeaver.weave("x-men", listOf(prefixMovie, exactMovie))
        assertEquals("X-Men", weaved.first().title)
    }

    @Test
    fun testTieBreakingIsDeterministicRegardlessOfInputOrder() {
        val movie = SearchResult.ArrMediaResult(createMockMovie("X-Men", tmdbId = 1L, popularity = 50.0))
        val series = SearchResult.ArrMediaResult(createMockSeries("X-Men", tvdbId = 2L))

        val weaved1 = SearchResultWeaver.weave("x-men", listOf(movie, series))
        val weaved2 = SearchResultWeaver.weave("x-men", listOf(series, movie))

        assertEquals(weaved1.map { it.id }, weaved2.map { it.id })
    }

    @Test
    fun testLibraryItemBoostedOverUnadded() {
        val unaddedMovie = SearchResult.ArrMediaResult(createMockMovie("X-Men", tmdbId = 1L, inLibrary = false))
        val libraryMovie = SearchResult.ArrMediaResult(createMockMovie("X-Men", tmdbId = 2L, inLibrary = true))

        val weaved = SearchResultWeaver.weave("x-men", listOf(unaddedMovie, libraryMovie))
        assertEquals(true, (weaved.first() as SearchResult.ArrMediaResult).media.id != null)
    }
}
