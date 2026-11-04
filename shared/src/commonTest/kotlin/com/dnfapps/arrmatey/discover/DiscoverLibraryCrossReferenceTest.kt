package com.dnfapps.arrmatey.discover

import com.dnfapps.arrmatey.datastore.DiscoverSectionPreferences
import com.dnfapps.arrmatey.datastore.DiscoverSectionPreferencesSerializer
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.MediaType
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerManager
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.MediaInfo
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DiscoverLibraryCrossReferenceTest {

    private val sampleItems = listOf(
        UnifiedMediaItem(
            id = "plex_movie_1",
            title = "Inception",
            type = MediaType.MOVIE,
            year = 2010,
            tmdbId = 27205L,
            serverType = MediaServerType.Plex,
        ),
        UnifiedMediaItem(
            id = "jellyfin_show_1",
            title = "Breaking Bad",
            type = MediaType.SHOW,
            year = 2008,
            tmdbId = 1396L,
            tvdbId = 81189L,
            serverType = MediaServerType.Jellyfin,
        ),
        UnifiedMediaItem(
            id = "emby_movie_2",
            title = "The Matrix: Reloaded",
            type = MediaType.MOVIE,
            year = 2003,
            serverType = MediaServerType.Emby,
        ),
    )

    @Test
    fun testFindMatchingItemByTmdbId() {
        val manager = MediaServerManager(
            dao = FakeMediaServerDao(),
            httpClientFactory = FakeHttpClientFactory(),
        )

        val match = manager.findMatchingItem(
            items = sampleItems,
            tmdbId = 27205L,
            title = "Different Title",
        )
        assertNotNull(match)
        assertEquals("plex_movie_1", match.id)
        assertEquals(MediaServerType.Plex, match.serverType)
    }

    @Test
    fun testFindMatchingItemByTvdbId() {
        val manager = MediaServerManager(
            dao = FakeMediaServerDao(),
            httpClientFactory = FakeHttpClientFactory(),
        )

        val match = manager.findMatchingItem(
            items = sampleItems,
            tvdbId = 81189L,
        )
        assertNotNull(match)
        assertEquals("jellyfin_show_1", match.id)
        assertEquals(MediaServerType.Jellyfin, match.serverType)
    }

    @Test
    fun testFindMatchingItemByTitleAndYear() {
        val manager = MediaServerManager(
            dao = FakeMediaServerDao(),
            httpClientFactory = FakeHttpClientFactory(),
        )

        // Matching normalized title "The Matrix Reloaded" vs "The Matrix: Reloaded"
        val match = manager.findMatchingItem(
            items = sampleItems,
            title = "The Matrix Reloaded",
            year = 2003,
            mediaType = MediaType.MOVIE,
        )
        assertNotNull(match)
        assertEquals("emby_movie_2", match.id)

        // Year mismatch
        val mismatch = manager.findMatchingItem(
            items = sampleItems,
            title = "The Matrix Reloaded",
            year = 1999,
        )
        assertNull(mismatch)
    }

    @Test
    fun testDiscoverResultIsAvailableInLibrary() {
        val baseItem = DiscoverResult(
            id = 100L,
            mediaType = RequestType.Movie,
            title = "Test Movie",
        )

        assertFalse(baseItem.isAvailableInLibrary)

        // Available on media server
        val serverItem = baseItem.copy(
            mediaServerItem = sampleItems[0],
        )
        assertTrue(serverItem.isAvailableInLibrary)

        // Present in Arr library
        val arrItem = baseItem.copy(
            inArrLibrary = true,
        )
        assertTrue(arrItem.isAvailableInLibrary)

        // Seerr status 4 (Partially Available) & 5 (Available)
        val status4Item = baseItem.copy(
            mediaInfo = MediaInfo(status = 4),
        )
        assertTrue(status4Item.isAvailableInLibrary)

        val status5Item = baseItem.copy(
            mediaInfo = MediaInfo(status = 5),
        )
        assertTrue(status5Item.isAvailableInLibrary)

        // Seerr status 2 (Pending)
        val status2Item = baseItem.copy(
            mediaInfo = MediaInfo(status = 2),
        )
        assertFalse(status2Item.isAvailableInLibrary)
    }

    @Test
    fun testDiscoverSectionPreferencesSerializationPreservesHideInLibraryContent() {
        val defaultPrefs = DiscoverSectionPreferences()
        assertFalse(defaultPrefs.hideInLibraryContent)

        val customPrefs = DiscoverSectionPreferences(
            hideInLibraryContent = true,
        )
        assertTrue(customPrefs.hideInLibraryContent)

        val json = DiscoverSectionPreferencesSerializer.serialize(customPrefs)
        val deserialized = DiscoverSectionPreferencesSerializer.deserialize(json)
        assertTrue(deserialized.hideInLibraryContent)

        val falsePrefs = DiscoverSectionPreferences(
            hideInLibraryContent = false,
        )
        val jsonFalse = DiscoverSectionPreferencesSerializer.serialize(falsePrefs)
        val deserializedFalse = DiscoverSectionPreferencesSerializer.deserialize(jsonFalse)
        assertFalse(deserializedFalse.hideInLibraryContent)
    }
}

private class FakeMediaServerDao : com.dnfapps.arrmatey.mediaserver.database.MediaServerIntegrationDao {
    override fun observeAll(): kotlinx.coroutines.flow.Flow<List<com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration>> = kotlinx.coroutines.flow.flowOf(emptyList())

    override fun observeByType(type: MediaServerType): kotlinx.coroutines.flow.Flow<com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration?> = kotlinx.coroutines.flow.flowOf(null)

    override suspend fun getAll(): List<com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration> = emptyList()

    override suspend fun getByType(type: MediaServerType): com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration? = null

    override suspend fun getById(id: Long): com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration? = null

    override suspend fun insert(integration: com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration): Long = 1L

    override suspend fun update(integration: com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration) {}

    override suspend fun deleteByType(type: MediaServerType) {}

    override suspend fun delete(integration: com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration) {}
}

private class FakeHttpClientFactory :
    com.dnfapps.arrmatey.arr.api.client.HttpClientFactory(
        kotlinx.serialization.json.Json { ignoreUnknownKeys = true },
        object : io.ktor.client.plugins.logging.Logger {
            override fun log(message: String) {}
        },
    ) {
    override fun createMediaServerClient(integration: com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration): io.ktor.client.HttpClient = io.ktor.client.HttpClient()
}
