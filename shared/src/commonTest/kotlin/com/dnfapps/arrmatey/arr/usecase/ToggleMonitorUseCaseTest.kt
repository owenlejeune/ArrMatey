package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.networking.NetworkResult
import dev.shivathapaa.logger.api.LoggerFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToggleMonitorUseCaseTest {
    private val logger = LoggerFactory.get("test")
    private val useCase = ToggleMonitorUseCase()

    private fun repository(type: InstanceType): ArrInstanceRepository {
        val instance =
            Instance(
                id = 1,
                label = "Test",
                url = "http://localhost",
                apiKey = EncryptedString("k"),
                type = type,
                enabled = true,
            )
        val mockEngine = MockEngine { respond("", HttpStatusCode.OK) }
        return ArrInstanceRepository(instance, HttpClient(mockEngine), logger)
    }

    @Test
    fun testToggleSeasonOnWrongInstanceReturnsError() =
        runTest {
            val result = useCase.toggleSeason(seriesId = 1, seasonNumber = 2, repository = repository(InstanceType.Radarr))
            assertTrue(result is NetworkResult.Error)
            assertEquals("Not a Sonarr instance", result.message)
        }

    @Test
    fun testToggleEpisodeOnWrongInstanceReturnsError() =
        runTest {
            val episode =
                Episode(
                    id = 1,
                    seriesId = 1,
                    tvdbId = null,
                    episodeFileId = null,
                    seasonNumber = 1,
                    episodeNumber = 1,
                    runtime = null,
                    hasFile = false,
                    monitored = false,
                    unverifiedSceneNumbering = false,
                )
            val result = useCase.toggleEpisode(episode = episode, repository = repository(InstanceType.Radarr))
            assertTrue(result is NetworkResult.Error)
            assertEquals("Not a Sonarr instance", result.message)
        }

    @Test
    fun testToggleAlbumOnWrongInstanceReturnsError() =
        runTest {
            val album =
                ArrAlbum(
                    id = 1,
                    artistId = 1,
                    foreignAlbumId = "abc",
                    anyReleaseOk = false,
                    profileId = 1,
                    duration = 0,
                )
            val result = useCase.toggleAlbum(album = album, repository = repository(InstanceType.Sonarr))
            assertTrue(result is NetworkResult.Error)
            assertEquals("Not a Lidarr instance", result.message)
        }

    @Test
    fun testToggleBookOnWrongInstanceReturnsError() =
        runTest {
            val book = Book(id = 1, title = "Book")
            val result = useCase.toggleBook(book = book, repository = repository(InstanceType.Sonarr))
            assertTrue(result is NetworkResult.Error)
            assertEquals("Not a Readarr instance", result.message)
        }

    @Test
    fun testToggleAudiobookOnWrongInstanceReturnsError() =
        runTest {
            val audiobook = Audiobook(id = 1, title = "Audio")
            val result = useCase.toggleAudiobook(audiobook = audiobook, repository = repository(InstanceType.Sonarr))
            assertTrue(result is NetworkResult.Error)
            assertEquals("Not a Listenarr instance", result.message)
        }
}
