package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerManager
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerRepository
import com.dnfapps.arrmatey.seerr.state.MediaProvider
import com.dnfapps.networking.NetworkResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsMediaServerHandlerTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        // Keep Dispatchers.Main set so background continuations don't fail when resetting
    }

    private fun createMockRepo(
        serverType: MediaServerType,
        serverLabel: String,
        serverId: String = "srv-123",
    ): MediaServerRepository {
        val repo = mockk<MediaServerRepository>()
        val integration = MediaServerIntegration(
            id = 1L,
            serverType = serverType,
            label = serverLabel,
            serverId = serverId,
            url = "http://localhost:8096",
            token = com.dnfapps.arrmatey.database.EncryptedString("mock-token"),
            clientIdentifier = "mock-client",
            enabled = true,
        )
        every { repo.serverType } returns serverType
        every { repo.integration } returns integration
        every { repo.buildDeepLinkUrl(any()) } answers { "${serverType.name.lowercase()}://item/${(firstArg() as UnifiedMediaItem).id}" }
        every { repo.buildWebUrl(any()) } answers { "http://localhost:8096/web/${(firstArg() as UnifiedMediaItem).id}" }
        return repo
    }

    @Test
    fun testFindMediaMatchesByTmdbId() = runTest(UnconfinedTestDispatcher()) {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(testJob + UnconfinedTestDispatcher())
        try {
            val plexRepo = createMockRepo(MediaServerType.Plex, "My Plex")
            val mediaItem = UnifiedMediaItem(
                id = "plex-101",
                title = "Inception",
                type = MediaType.MOVIE,
                tmdbId = 27205L,
                serverType = MediaServerType.Plex,
                played = true,
                durationTicks = 88800000000L,
                playbackPositionTicks = 88800000000L,
                videoResolution = "1080p",
                videoCodec = "h264",
                audioCodec = "dts",
                container = "mkv",
                bitrate = 12000000L,
            )

            coEvery { plexRepo.search("Inception", 15) } returns NetworkResult.Success(listOf(mediaItem))

            val manager = mockk<MediaServerManager>()
            every { manager.activeRepositories } returns MutableStateFlow(listOf(plexRepo))

            val handler = UnifiedMediaDetailsMediaServerHandler(testScope, manager)

            handler.findMedia(
                tmdbId = 27205L,
                tvdbId = null,
                title = "Inception",
                year = 2010,
                isShow = false,
            )

            val details = handler.mediaServerDetails.value
            assertEquals(1, details.size)
            val detail = details.first()
            assertEquals("plex-101", detail.itemId)
            assertEquals("Inception", detail.title)
            assertEquals(MediaServerType.Plex, detail.serverType)
            assertTrue(detail.played)
            assertEquals("1080p", detail.videoResolution)
            assertEquals("mkv", detail.container)
            assertEquals(12000000L, detail.bitrate)

            val options = handler.watchOptions.value
            assertEquals(1, options.size)
            assertEquals(MediaProvider.Plex, options.first().provider)
            assertEquals("My Plex", options.first().serverName)
            assertEquals("plex://item/plex-101", options.first().url)
            assertEquals("http://localhost:8096/web/plex-101", options.first().webFallbackUrl)
        } finally {
            testJob.cancel()
        }
    }

    @Test
    fun testFindMediaMatchesTvShowWithEpisodes() = runTest(UnconfinedTestDispatcher()) {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(testJob + UnconfinedTestDispatcher())
        try {
            val jfRepo = createMockRepo(MediaServerType.Jellyfin, "My Jellyfin")
            val showItem = UnifiedMediaItem(
                id = "jf-202",
                title = "Breaking Bad",
                type = MediaType.SHOW,
                tvdbId = 81189L,
                serverType = MediaServerType.Jellyfin,
                played = false,
            )
            val ep1 = UnifiedMediaItem(
                id = "jf-ep-1",
                title = "Pilot",
                type = MediaType.EPISODE,
                serverType = MediaServerType.Jellyfin,
                seasonNumber = 1,
                episodeNumber = 1,
                played = true,
            )
            val ep2 = UnifiedMediaItem(
                id = "jf-ep-2",
                title = "Cat's in the Bag...",
                type = MediaType.EPISODE,
                serverType = MediaServerType.Jellyfin,
                seasonNumber = 1,
                episodeNumber = 2,
                played = false,
            )

            coEvery { jfRepo.search("Breaking Bad", 15) } returns NetworkResult.Success(listOf(showItem))
            coEvery { jfRepo.getShowEpisodes("jf-202") } returns NetworkResult.Success(listOf(ep1, ep2))

            val manager = mockk<MediaServerManager>()
            every { manager.activeRepositories } returns MutableStateFlow(listOf(jfRepo))

            val handler = UnifiedMediaDetailsMediaServerHandler(testScope, manager)

            handler.findMedia(
                tmdbId = 1396L,
                tvdbId = 81189L,
                title = "Breaking Bad",
                year = 2008,
                isShow = true,
            )

            val details = handler.mediaServerDetails.value
            assertEquals(1, details.size)
            val detail = details.first()
            assertEquals("jf-202", detail.itemId)
            assertEquals(1, detail.episodesWatchedCount)
            assertEquals(2, detail.totalEpisodesCount)
            assertEquals(true, detail.episodeWatchStatus[1 to 1])
            assertEquals(false, detail.episodeWatchStatus[1 to 2])
            assertEquals("jellyfin://item/jf-ep-1", detail.episodeDeepLinks[1 to 1])
            assertEquals("jellyfin://item/jf-ep-2", detail.episodeDeepLinks[1 to 2])

            val options = handler.watchOptions.value
            assertEquals(1, options.size)
            assertEquals(MediaProvider.Jellyfin, options.first().provider)
        } finally {
            testJob.cancel()
        }
    }

    @Test
    fun testFindMediaMultipleServersGeneratesMultipleOptions() = runTest(UnconfinedTestDispatcher()) {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(testJob + UnconfinedTestDispatcher())
        try {
            val plexRepo = createMockRepo(MediaServerType.Plex, "Home Plex")
            val embyRepo = createMockRepo(MediaServerType.Emby, "Home Emby")

            val plexMovie = UnifiedMediaItem(
                id = "plex-303",
                title = "Gladiator",
                type = MediaType.MOVIE,
                tmdbId = 98L,
                serverType = MediaServerType.Plex,
            )
            val embyMovie = UnifiedMediaItem(
                id = "emby-404",
                title = "Gladiator",
                type = MediaType.MOVIE,
                tmdbId = 98L,
                serverType = MediaServerType.Emby,
            )

            coEvery { plexRepo.search("Gladiator", 15) } returns NetworkResult.Success(listOf(plexMovie))
            coEvery { embyRepo.search("Gladiator", 15) } returns NetworkResult.Success(listOf(embyMovie))

            val manager = mockk<MediaServerManager>()
            every { manager.activeRepositories } returns MutableStateFlow(listOf(plexRepo, embyRepo))

            val handler = UnifiedMediaDetailsMediaServerHandler(testScope, manager)

            handler.findMedia(
                tmdbId = 98L,
                tvdbId = null,
                title = "Gladiator",
                year = 2000,
                isShow = false,
            )

            assertEquals(2, handler.mediaServerDetails.value.size)
            assertEquals(2, handler.watchOptions.value.size)
            val providers = handler.watchOptions.value.map { it.provider }
            assertTrue(providers.contains(MediaProvider.Plex))
            assertTrue(providers.contains(MediaProvider.Emby))
        } finally {
            testJob.cancel()
        }
    }

    @Test
    fun testMarkWatchedUpdatesState() = runTest(UnconfinedTestDispatcher()) {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(testJob + UnconfinedTestDispatcher())
        try {
            val plexRepo = createMockRepo(MediaServerType.Plex, "Plex")
            val movie = UnifiedMediaItem(
                id = "plex-505",
                title = "Dune",
                type = MediaType.MOVIE,
                tmdbId = 438631L,
                serverType = MediaServerType.Plex,
                played = false,
                durationTicks = 90000000000L,
            )

            coEvery { plexRepo.search("Dune", 15) } returns NetworkResult.Success(listOf(movie))
            coEvery { plexRepo.markWatched("plex-505", true) } returns NetworkResult.Success(Unit)

            val manager = mockk<MediaServerManager>()
            every { manager.activeRepositories } returns MutableStateFlow(listOf(plexRepo))
            every { manager.getRepository(MediaServerType.Plex) } returns plexRepo

            val handler = UnifiedMediaDetailsMediaServerHandler(testScope, manager)

            handler.findMedia(
                tmdbId = 438631L,
                tvdbId = null,
                title = "Dune",
                year = 2021,
                isShow = false,
            )

            assertFalse(handler.mediaServerDetails.value.first().played)

            handler.markWatched(MediaServerType.Plex, "plex-505", true)

            coVerify { plexRepo.markWatched("plex-505", true) }
            assertTrue(handler.mediaServerDetails.value.first().played)
            assertEquals(90000000000L, handler.mediaServerDetails.value.first().playbackPositionTicks)
        } finally {
            testJob.cancel()
        }
    }

    @Test
    fun testFindEpisodeMatchesAndPopulatesDetails() = runTest(UnconfinedTestDispatcher()) {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(testJob + UnconfinedTestDispatcher())
        try {
            val plexRepo = createMockRepo(MediaServerType.Plex, "My Plex")
            val show = UnifiedMediaItem(
                id = "show-1",
                title = "Breaking Bad",
                type = MediaType.SHOW,
                tvdbId = 81189L,
                year = 2008,
                serverType = MediaServerType.Plex,
            )
            val ep1 = UnifiedMediaItem(
                id = "ep-1",
                title = "Pilot",
                type = MediaType.EPISODE,
                seasonNumber = 1,
                episodeNumber = 1,
                serverType = MediaServerType.Plex,
                played = true,
                videoResolution = "1080p",
                videoCodec = "h264",
                audioCodec = "aac",
                container = "mkv",
                bitrate = 8_000_000L,
            )
            val ep2 = UnifiedMediaItem(
                id = "ep-2",
                title = "Cat's in the Bag...",
                type = MediaType.EPISODE,
                seasonNumber = 1,
                episodeNumber = 2,
                serverType = MediaServerType.Plex,
                played = false,
            )

            coEvery { plexRepo.search("Breaking Bad", 15) } returns NetworkResult.Success(listOf(show))
            coEvery { plexRepo.getShowEpisodes("show-1") } returns NetworkResult.Success(listOf(ep1, ep2))

            val manager = mockk<MediaServerManager>()
            every { manager.activeRepositories } returns MutableStateFlow(listOf(plexRepo))

            val handler = UnifiedMediaDetailsMediaServerHandler(testScope, manager)
            handler.findEpisode(
                tmdbId = null,
                tvdbId = 81189L,
                title = "Breaking Bad",
                year = 2008,
                seasonNumber = 1,
                episodeNumber = 1,
            )

            val details = handler.mediaServerDetails.value
            assertEquals(1, details.size)
            val epDetail = details.first()
            assertEquals("ep-1", epDetail.itemId)
            assertEquals("Pilot", epDetail.title)
            assertEquals(MediaServerType.Plex, epDetail.serverType)
            assertEquals("My Plex", epDetail.serverLabel)
            assertTrue(epDetail.played)
            assertEquals("1080p", epDetail.videoResolution)
            assertEquals("h264", epDetail.videoCodec)
            assertEquals("aac", epDetail.audioCodec)
            assertEquals("mkv", epDetail.container)
            assertEquals(8_000_000L, epDetail.bitrate)
            assertEquals("plex://item/ep-1", epDetail.deepLinkUrl)
            assertEquals("http://localhost:8096/web/ep-1", epDetail.webFallbackUrl)

            assertEquals(1, handler.watchOptions.value.size)
            assertEquals(MediaProvider.Plex, handler.watchOptions.value.first().provider)
        } finally {
            testJob.cancel()
        }
    }
}
