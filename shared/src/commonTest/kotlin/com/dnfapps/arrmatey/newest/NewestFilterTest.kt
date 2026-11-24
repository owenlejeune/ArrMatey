package com.dnfapps.arrmatey.newest

import com.dnfapps.arrmatey.discover.model.DiscoverFilterState
import com.dnfapps.arrmatey.discover.model.DiscoverSortBy
import com.dnfapps.arrmatey.discover.model.DiscoverSortOrder
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.MediaInfo
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import kotlin.test.Test
import kotlin.test.assertEquals

class NewestFilterTest {
    private fun item(
        title: String,
        publishDate: String = "2026-11-18T00:00:00Z",
        seeders: Int = 10,
        match: DiscoverResult? = null,
    ) = NewestDownloadableItem(
        release = NewestRelease(
            parsed = ParsedRelease(title, 2025),
            kind = ReleaseKind.Movie,
            seeders = seeders,
            publishDate = publishDate,
            ageHours = 1.0,
            qualities = emptyList(),
            releaseCount = 1,
        ),
        match = match,
    )

    private fun movie(
        id: Long,
        date: String = "2025-01-01",
        genres: List<Int> = emptyList(),
        language: String = "en",
        rating: Double = 5.0,
        popularity: Double = 1.0,
        status: Int = 0,
    ) = DiscoverResult(
        id = id,
        mediaType = RequestType.Movie,
        title = "Movie $id",
        releaseDate = date,
        genreIds = genres,
        originalLanguage = language,
        voteAverage = rating,
        popularity = popularity,
        mediaInfo = MediaInfo(status = status),
    )

    private fun titles(items: List<NewestDownloadableItem>) = items.map { it.release.parsed.title }

    private val items = listOf(
        item("A", publishDate = "2026-11-10T00:00:00Z", seeders = 50, match = movie(1, date = "2025-06-01", rating = 9.0, popularity = 1.0)),
        item("B", publishDate = "2026-11-19T00:00:00Z", seeders = 5, match = movie(2, date = "2024-01-01", rating = 6.0, popularity = 90.0)),
        item("C", publishDate = "2026-11-15T00:00:00Z", seeders = 20, match = movie(3, date = "2025-09-01", rating = 7.0, popularity = 10.0)),
    )

    @Test
    fun testDefaultIsNewestUploadFirst() {
        assertEquals(listOf("B", "C", "A"), titles(NewestFilter.apply(items, NewestFilter.DEFAULT, hasSeerr = true)))
    }

    @Test
    fun testSorts() {
        fun sortedBy(sort: DiscoverSortBy, order: DiscoverSortOrder = DiscoverSortOrder.DESCENDING) = titles(NewestFilter.sorted(items, sort, order))
        assertEquals(listOf("C", "A", "B"), sortedBy(DiscoverSortBy.RELEASE_DATE))
        assertEquals(listOf("A", "C", "B"), sortedBy(DiscoverSortBy.SEEDERS))
        assertEquals(listOf("A", "C", "B"), sortedBy(DiscoverSortBy.RATING))
        assertEquals(listOf("B", "C", "A"), sortedBy(DiscoverSortBy.POPULARITY))
        assertEquals(listOf("A", "C", "B"), sortedBy(DiscoverSortBy.UPLOAD_TIME, DiscoverSortOrder.ASCENDING))
    }

    @Test
    fun testSeerrSortsFallBackWithoutSeerr() {
        val filter = DiscoverFilterState(sortBy = DiscoverSortBy.RATING)
        assertEquals(listOf("B", "C", "A"), titles(NewestFilter.apply(items, filter, hasSeerr = false)))
        assertEquals(listOf(DiscoverSortBy.UPLOAD_TIME, DiscoverSortBy.RELEASE_DATE, DiscoverSortBy.SEEDERS), NewestFilter.sortOptions(hasSeerr = false))
    }

    @Test
    fun testGenreAndLanguageUseSeerrMatchAndDropUnmatched() {
        val list = listOf(
            item("Horror", match = movie(1, genres = listOf(27), language = "ko")),
            item("Comedy", match = movie(2, genres = listOf(35), language = "en")),
            item("Unmatched"),
        )
        assertEquals(listOf("Horror"), titles(NewestFilter.apply(list, NewestFilter.DEFAULT.toggleGenre(27), hasSeerr = true)))
        assertEquals(listOf("Horror"), titles(NewestFilter.apply(list, NewestFilter.DEFAULT.toggleLanguage("KO"), hasSeerr = true)))
        assertEquals(3, NewestFilter.apply(list, NewestFilter.DEFAULT.toggleGenre(27), hasSeerr = false).size)
    }

    @Test
    fun testHideInLibrary() {
        val list = listOf(
            item("Partly", match = movie(1, status = 4)),
            item("New", match = movie(2)),
            item("Unmatched"),
        )
        assertEquals(listOf("New", "Unmatched").sorted(), titles(NewestFilter.apply(list, NewestFilter.DEFAULT.copy(hideInLibrary = true), hasSeerr = true)).sorted())
        assertEquals(3, NewestFilter.apply(list, NewestFilter.DEFAULT, hasSeerr = true).size)
    }
}
