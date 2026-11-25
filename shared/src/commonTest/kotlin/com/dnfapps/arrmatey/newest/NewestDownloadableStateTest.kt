package com.dnfapps.arrmatey.newest

import kotlin.test.Test
import kotlin.test.assertEquals

class NewestDownloadableStateTest {
    private fun item(
        title: String,
        kind: ReleaseKind,
        publishDate: String,
    ) = NewestDownloadableItem(
        NewestRelease(ParsedRelease(title, 2025), kind, 10, publishDate, 1.0, emptyList(), 1),
    )

    private fun titles(state: NewestDownloadableState) = (state as NewestDownloadableState.Success).items.map { it.release.parsed.title }

    @Test
    fun testMergeSortsNewestFirst() {
        val movies = NewestDownloadableState.Success(listOf(item("Movie A", ReleaseKind.Movie, "2026-11-20"), item("Movie B", ReleaseKind.Movie, "2026-11-10")))
        val tv = NewestDownloadableState.Success(listOf(item("Show C", ReleaseKind.Tv, "2026-11-15")))
        assertEquals(listOf("Movie A", "Show C", "Movie B"), titles(NewestDownloadableState.merge(movies, tv)))
    }

    @Test
    fun testMergeShowsWhatLoadedWhileTheOtherLoads() {
        val movies = NewestDownloadableState.Success(listOf(item("Movie A", ReleaseKind.Movie, "2026-11-20")))
        assertEquals(listOf("Movie A"), titles(NewestDownloadableState.merge(movies, NewestDownloadableState.Loading)))
        assertEquals(listOf("Movie A"), titles(NewestDownloadableState.merge(NewestDownloadableState.Error("x"), movies)))
    }

    @Test
    fun testMergeWithoutResults() {
        assertEquals(NewestDownloadableState.Error("x"), NewestDownloadableState.merge(NewestDownloadableState.Error("x"), NewestDownloadableState.Loading))
        assertEquals(NewestDownloadableState.Loading, NewestDownloadableState.merge(NewestDownloadableState.Initial, NewestDownloadableState.Loading))
        assertEquals(NewestDownloadableState.Initial, NewestDownloadableState.merge(NewestDownloadableState.Initial, NewestDownloadableState.Initial))
    }
}
