package com.dnfapps.arrmatey.newest

import com.dnfapps.arrmatey.arr.api.model.ProwlarrSearchResult
import com.dnfapps.arrmatey.arr.api.model.ReleaseProtocol
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NewestReleaseGrouperTest {
    private fun release(
        title: String,
        seeders: Int? = 10,
        publishDate: String = "2026-11-18T10:00:00Z",
        protocol: ReleaseProtocol = ReleaseProtocol.Torrent,
        ageHours: Double = 5.0,
    ) = ProwlarrSearchResult(
        title = title,
        indexerId = 1,
        size = 1000,
        age = 0,
        ageHours = ageHours,
        ageMinutes = ageHours * 60,
        publishDate = publishDate,
        protocol = protocol,
        seeders = seeders,
    )

    @Test
    fun testDropsLowQualityAndPoorlySeeded() {
        val grouped = NewestReleaseGrouper.group(
            listOf(
                release("Some.Movie.2025.HDCAM.x264-GRP"),
                release("Other.Movie.2025.1080p.WEB-DL", seeders = 2),
                release("Good.Movie.2025.1080p.WEB-DL", seeders = 3),
            ),
            ReleaseKind.Movie,
        )
        assertEquals(listOf("Good Movie"), grouped.map { it.parsed.title })
    }

    @Test
    fun testKeepsUsenetWithoutSeeders() {
        val grouped = NewestReleaseGrouper.group(
            listOf(release("Good.Movie.2025.1080p.WEB-DL", seeders = null, protocol = ReleaseProtocol.Usenet)),
            ReleaseKind.Movie,
        )
        assertEquals(1, grouped.size)
    }

    @Test
    fun testMergesMovieReleasesByTitleAndYear() {
        val grouped = NewestReleaseGrouper.group(
            listOf(
                release("Good.Movie.2025.1080p.WEB-DL-A", seeders = 5, publishDate = "2026-11-17T10:00:00Z", ageHours = 30.0),
                release("Good Movie (2025) 2160p WEB-DL", seeders = 50, publishDate = "2026-11-18T09:00:00Z", ageHours = 6.0),
                release("Good.Movie.2025.1080p.BluRay-B", seeders = 8, publishDate = "2026-11-16T10:00:00Z", ageHours = 50.0),
                release("Good.Movie.1999.1080p.BluRay"),
            ),
            ReleaseKind.Movie,
        )
        assertEquals(2, grouped.size)
        val merged = grouped.first { it.parsed.year == 2025 }
        assertEquals(50, merged.seeders)
        assertEquals("2026-11-18T09:00:00Z", merged.publishDate)
        assertEquals(6.0, merged.ageHours)
        assertEquals(3, merged.releaseCount)
        assertEquals(listOf("1080p", "2160p"), merged.qualities)
    }

    @Test
    fun testMergesTvReleasesBySeason() {
        val grouped = NewestReleaseGrouper.group(
            listOf(
                release("Some.Show.S02E01.1080p.WEB"),
                release("Some.Show.S02E02.720p.WEB"),
                release("Some.Show.S01E08.1080p.WEB"),
            ),
            ReleaseKind.Tv,
        )
        assertEquals(listOf(2, 1).sorted(), grouped.map { it.parsed.season }.sortedBy { it })
        assertEquals(2, grouped.first { it.parsed.season == 2 }.releaseCount)
    }

    @Test
    fun testSortsByNewestPublishDate() {
        val grouped = NewestReleaseGrouper.group(
            listOf(
                release("Older.Movie.2024.1080p", publishDate = "2026-11-10T00:00:00Z"),
                release("Newest.Movie.2025.1080p", publishDate = "2026-11-19T00:00:00Z"),
                release("Middle.Movie.2025.1080p", publishDate = "2026-11-15T00:00:00Z"),
            ),
            ReleaseKind.Movie,
        )
        assertEquals(listOf("Newest Movie", "Middle Movie", "Older Movie"), grouped.map { it.parsed.title })
    }

    @Test
    fun testQualityOf() {
        assertEquals("2160p", NewestReleaseGrouper.qualityOf("Movie.2025.2160p.WEB"))
        assertEquals(null, NewestReleaseGrouper.qualityOf("Movie.2025.WEB"))
        assertTrue(NewestReleaseGrouper.group(emptyList(), ReleaseKind.Tv).isEmpty())
    }
}
