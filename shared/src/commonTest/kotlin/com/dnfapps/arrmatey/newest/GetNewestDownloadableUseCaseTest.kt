package com.dnfapps.arrmatey.newest

import com.dnfapps.arrmatey.arr.api.model.ProwlarrSearchResult
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetNewestDownloadableUseCaseTest {
    private fun release(title: String) = ProwlarrSearchResult(
        title = title,
        indexerId = 1,
        size = 1000,
        age = 0,
        ageHours = 1.0,
        ageMinutes = 60.0,
        publishDate = "2026-11-18T10:00:00Z",
        seeders = 10,
    )

    private fun match(id: Long, title: String) = DiscoverResult(id = id, mediaType = RequestType.Movie, title = title, releaseDate = "2025-01-01")

    private fun List<NetworkResult<List<NewestDownloadableItem>>>.items() = map { (it as NetworkResult.Success).data }

    @Test
    fun testEmitsReleasesBeforeSeerrMatches() = runTest {
        val useCase = GetNewestDownloadableUseCase()
        val emissions = useCase.newest(
            kind = ReleaseKind.Movie,
            search = { NetworkResult.Success(listOf(release("Good.Movie.2025.1080p"))) },
            lookup = { GetNewestDownloadableUseCase.LookupResult.Found(match(7, "Good Movie")) },
        ).toList().items()

        assertEquals(2, emissions.size)
        assertNull(emissions[0].single().match)
        assertEquals(7L, emissions[1].single().match?.id)
    }

    @Test
    fun testWithoutSeerrEmitsOnce() = runTest {
        val emissions = GetNewestDownloadableUseCase().newest(
            kind = ReleaseKind.Movie,
            search = { NetworkResult.Success(listOf(release("Good.Movie.2025.1080p"))) },
            lookup = null,
        ).toList().items()

        assertEquals(1, emissions.size)
    }

    @Test
    fun testSlowLookupTimesOutAndKeepsPlainItem() = runTest {
        val emissions = GetNewestDownloadableUseCase().newest(
            kind = ReleaseKind.Movie,
            search = { NetworkResult.Success(listOf(release("Good.Movie.2025.1080p"))) },
            lookup = { awaitCancellation() },
            lookupTimeoutMillis = 1_000,
        ).toList().items()

        assertTrue(emissions.all { list -> list.single().match == null })
    }

    @Test
    fun testSearchErrorIsEmitted() = runTest {
        val emissions = GetNewestDownloadableUseCase().newest(
            kind = ReleaseKind.Movie,
            search = { NetworkResult.Error(message = "Request timed out") },
            lookup = null,
        ).toList()

        assertEquals("Request timed out", (emissions.single() as NetworkResult.Error).message)
    }

    @Test
    fun testCachedMatchIsUsedInFirstEmission() = runTest {
        val useCase = GetNewestDownloadableUseCase()
        val search: suspend () -> NetworkResult<List<ProwlarrSearchResult>> = { NetworkResult.Success(listOf(release("Good.Movie.2025.1080p"))) }
        useCase.newest(ReleaseKind.Movie, search, { GetNewestDownloadableUseCase.LookupResult.Found(match(7, "Good Movie")) }).toList()

        val second = useCase.newest(ReleaseKind.Movie, search, { error("should not look up again") }).toList().items()

        assertEquals(1, second.size)
        assertEquals(7L, second.single().single().match?.id)
    }
}
