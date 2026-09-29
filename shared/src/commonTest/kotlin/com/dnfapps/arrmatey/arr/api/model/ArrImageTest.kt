package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlin.test.Test
import kotlin.test.assertEquals

class ArrImageTest {
    private val tmdbUrl = "https://image.tmdb.org/t/p/original/abc.jpg"

    private fun instance(
        type: InstanceType,
        url: String,
    ) = Instance(
        type = type,
        label = "test",
        url = url,
        apiKey = EncryptedString("key"),
    )

    private fun rebuild(
        instance: Instance,
        url: String?,
        remoteUrl: String? = null,
    ) = ArrImage(CoverType.Poster, url, remoteUrl).rebuildWithLocalUrls(instance).remoteUrl

    @Test
    fun testDownloadedSonarrCoverUsesApiRouteWithoutDuplicatingUrlBase() {
        assertEquals(
            "https://host/sonarr/api/v3/mediacover/82/poster.jpg?lastWrite=1",
            rebuild(instance(InstanceType.Sonarr, "https://host/sonarr"), "/sonarr/MediaCover/82/poster.jpg?lastWrite=1", tmdbUrl),
        )
    }

    @Test
    fun testDownloadedRadarrCoverUsesApiRoute() {
        assertEquals(
            "http://10.0.0.2:7878/api/v3/mediacover/5/fanart.jpg?h=abc",
            rebuild(instance(InstanceType.Radarr, "http://10.0.0.2:7878/"), "/MediaCover/5/fanart.jpg?h=abc", tmdbUrl),
        )
    }

    @Test
    fun testProxyPrefixWithoutServerUrlBase() {
        assertEquals(
            "https://host/sonarr/api/v3/mediacover/1/poster.jpg?lastWrite=1",
            rebuild(instance(InstanceType.Sonarr, "https://host/sonarr"), "/MediaCover/1/poster.jpg?lastWrite=1"),
        )
    }

    @Test
    fun testCoverNotYetDownloadedFallsBackToPublicUrl() {
        assertEquals(tmdbUrl, rebuild(instance(InstanceType.Sonarr, "https://host"), "/MediaCover/1/poster.jpg", tmdbUrl))
    }

    @Test
    fun testCoverWithoutPublicUrlUsesApiRouteEvenWithoutMarker() {
        assertEquals(
            "https://host/api/v3/mediacover/1/poster.jpg",
            rebuild(instance(InstanceType.Sonarr, "https://host"), "/MediaCover/1/poster.jpg"),
        )
    }

    @Test
    fun testLidarrArtistAndAlbumRoutes() {
        val lidarr = instance(InstanceType.Lidarr, "https://host/lidarr")
        assertEquals(
            "https://host/lidarr/api/v1/mediacover/artist/62/poster.jpg?lastWrite=9",
            rebuild(lidarr, "/lidarr/MediaCover/62/poster.jpg?lastWrite=9", "/config/MediaCover/62/poster.jpg"),
        )
        assertEquals(
            "https://host/lidarr/api/v1/mediacover/album/7/cover.jpg?lastWrite=9",
            rebuild(lidarr, "/lidarr/MediaCover/Albums/7/cover.jpg?lastWrite=9"),
        )
    }

    @Test
    fun testBookshelfAuthorAndBookRoutes() {
        val bookshelf = instance(InstanceType.Bookshelf, "https://host")
        assertEquals(
            "https://host/api/v1/mediacover/author/3/poster.jpg?lastWrite=1",
            rebuild(bookshelf, "/MediaCover/3/poster.jpg?lastWrite=1"),
        )
        assertEquals(
            "https://host/api/v1/mediacover/book/4/cover.jpg?lastWrite=1",
            rebuild(bookshelf, "/MediaCover/Books/4/cover.jpg?lastWrite=1"),
        )
    }

    @Test
    fun testEpisodeRewritesOwnAndNestedSeriesImages() {
        val sonarr = instance(InstanceType.Sonarr, "https://host/sonarr")
        val series =
            ArrSeries(
                title = "Show",
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
                tvdbId = 1,
                seriesType = SeriesType.Standard,
                images = listOf(ArrImage(CoverType.Poster, "/sonarr/MediaCover/9/poster.jpg?lastWrite=1", tmdbUrl)),
            )
        val episode =
            Episode(
                id = 1,
                seriesId = 9,
                tvdbId = null,
                episodeFileId = null,
                seasonNumber = 1,
                episodeNumber = 1,
                runtime = null,
                hasFile = false,
                monitored = false,
                unverifiedSceneNumbering = false,
                images = listOf(ArrImage(CoverType.Screenshot, "https://artworks.thetvdb.com/still.jpg", "https://artworks.thetvdb.com/still.jpg")),
                series = series,
            ).withLocalImages(sonarr)

        assertEquals("https://artworks.thetvdb.com/still.jpg", episode.getPoster()?.remoteUrl)
        assertEquals("https://host/sonarr/api/v3/mediacover/9/poster.jpg?lastWrite=1", episode.series?.getPoster()?.remoteUrl)
    }

    @Test
    fun testAlbumCoverUsesAlbumRoute() {
        val album =
            ArrAlbum(
                id = 7,
                artistId = 1,
                foreignAlbumId = "abc",
                anyReleaseOk = false,
                profileId = 1,
                duration = 0,
                images = listOf(ArrImage(CoverType.Cover, "/lidarr/MediaCover/Albums/7/cover.jpg?lastWrite=1", "https://coverartarchive.org/x.jpg")),
            ).withLocalImages(instance(InstanceType.Lidarr, "https://host/lidarr"))

        assertEquals("https://host/lidarr/api/v1/mediacover/album/7/cover.jpg?lastWrite=1", album.getCover()?.remoteUrl)
    }

    @Test
    fun testBookCoverUsesBookRoute() {
        val book =
            Book(
                id = 4,
                title = "Book",
                images = listOf(ArrImage(CoverType.Cover, "/MediaCover/Books/4/cover.jpg?lastWrite=1", "https://images.example/b.jpg")),
            ).withLocalImages(instance(InstanceType.Bookshelf, "http://host:8787"))

        assertEquals("http://host:8787/api/v1/mediacover/book/4/cover.jpg?lastWrite=1", book.getCover()?.remoteUrl)
    }

    @Test
    fun testUnsavedItemProxyPathKeepsPublicUrl() {
        assertEquals(tmdbUrl, rebuild(instance(InstanceType.Radarr, "https://host"), "/MediaCoverProxy/abc/poster.jpg", tmdbUrl))
    }

    @Test
    fun testRebuildIsIdempotent() {
        val sonarr = instance(InstanceType.Sonarr, "https://host/sonarr")
        val once = ArrImage(CoverType.Poster, "/sonarr/MediaCover/1/poster.jpg?lastWrite=1", tmdbUrl).rebuildWithLocalUrls(sonarr)
        assertEquals(once, once.rebuildWithLocalUrls(sonarr))
    }

    @Test
    fun testListenarrRelativeImageIsJoinedWithoutDuplicatingUrlBase() {
        assertEquals(
            "https://host/listenarr/api/v1/images/B00TEST",
            rebuild(instance(InstanceType.Listenarr, "https://host/listenarr"), "/listenarr/api/v1/images/B00TEST", "/listenarr/api/v1/images/B00TEST"),
        )
        assertEquals(
            "https://host/listenarr/api/v1/images/B00TEST",
            rebuild(instance(InstanceType.Listenarr, "https://host/listenarr"), "/api/v1/images/B00TEST", "/api/v1/images/B00TEST"),
        )
    }

    @Test
    fun testJoinInstanceUrl() {
        assertEquals("https://host/bazarr/images/series/1/poster.jpg", joinInstanceUrl("https://host/bazarr", "/bazarr/images/series/1/poster.jpg"))
        assertEquals("https://host/bazarr/images/series/1/poster.jpg", joinInstanceUrl("https://host/Bazarr/", "/bazarr/images/series/1/poster.jpg"))
        assertEquals("https://host/sonarr/sonarr4k/x.jpg", joinInstanceUrl("https://host/sonarr", "/sonarr4k/x.jpg"))
        assertEquals("https://host:6767/images/series/1/poster.jpg", joinInstanceUrl("https://host:6767", "/images/series/1/poster.jpg"))
    }
}
