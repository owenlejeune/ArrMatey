package com.dnfapps.arrmatey.seerr.api.model

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant

// Fields still declared non-null in Kotlin but null-able in Overseerr responses use assertFailsWith
// so the assertion flips (and the test fails) once the model is made nullable.
class SeerrModelNullabilityTest {
    private val json =
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
            coerceInputValues = true
            serializersModule =
                SerializersModule {
                    contextual(Instant::class, ListenarrInstantSerializer)
                }
        }

    @Test
    fun testRequestMediaFullPayload() {
        val payload =
            """
            {
              "id": 1,
              "mediaType": "movie",
              "tmdbId": 100,
              "status": 5,
              "status4k": 5,
              "createdAt": "2024-01-01T00:00:00Z",
              "updatedAt": "2024-01-02T00:00:00Z",
              "lastSeasonChange": "2024-01-03T00:00:00Z",
              "mediaAddedAt": "2024-01-04T00:00:00Z"
            }
            """.trimIndent()

        val model = json.decodeFromString<RequestMedia>(payload)

        assertEquals(1L, model.id)
        assertEquals(RequestType.Movie, model.mediaType)
        assertNotNull(model.lastSeasonChange)
        assertNotNull(model.mediaAddedAt)
    }

    @Test
    fun testRequestMediaNullMediaAddedAtAndLastSeasonChange() {
        // Regression for #150.
        val payload =
            """
            {
              "id": 1,
              "mediaType": "tv",
              "tmdbId": 100,
              "status": 3,
              "status4k": 3,
              "createdAt": "2024-01-01T00:00:00Z",
              "updatedAt": "2024-01-02T00:00:00Z",
              "lastSeasonChange": null,
              "mediaAddedAt": null
            }
            """.trimIndent()

        val model = json.decodeFromString<RequestMedia>(payload)

        assertNull(model.lastSeasonChange)
        assertNull(model.mediaAddedAt)
    }

    @Test
    fun testRequestMediaNullCreatedAtCurrentlyThrows() {
        val payload =
            """
            {
              "id": 1,
              "mediaType": "movie",
              "tmdbId": 100,
              "status": 5,
              "status4k": 5,
              "createdAt": null,
              "updatedAt": "2024-01-02T00:00:00Z"
            }
            """.trimIndent()

        assertFailsWith<SerializationException> { json.decodeFromString<RequestMedia>(payload) }
    }

    @Test
    fun testRequestSeasonFullPayload() {
        val payload =
            """
            {
              "id": 10,
              "seasonNumber": 2,
              "status": 5,
              "createdAt": "2024-01-01T00:00:00Z",
              "updatedAt": "2024-01-02T00:00:00Z"
            }
            """.trimIndent()

        val model = json.decodeFromString<RequestSeason>(payload)

        assertEquals(10L, model.id)
        assertEquals(2, model.seasonNumber)
    }

    @Test
    fun testRequestSeasonNullCreatedAtCurrentlyThrows() {
        val payload =
            """
            {
              "id": 10,
              "seasonNumber": 2,
              "status": 5,
              "createdAt": null,
              "updatedAt": "2024-01-02T00:00:00Z"
            }
            """.trimIndent()

        assertFailsWith<SerializationException> { json.decodeFromString<RequestSeason>(payload) }
    }

    @Test
    fun testRequestUserFullPayload() {
        val payload =
            """
            {
              "permissions": 0,
              "id": 42,
              "email": "user@example.com",
              "userType": 1,
              "avatar": "https://example.com/a.png",
              "createdAt": "2024-01-01T00:00:00Z",
              "updatedAt": "2024-01-02T00:00:00Z",
              "requestCount": 5,
              "displayName": "User"
            }
            """.trimIndent()

        val model = json.decodeFromString<RequestUser>(payload)

        assertEquals(42L, model.id)
        assertEquals("user@example.com", model.email)
    }

    @Test
    fun testRequestUserNullAvatarCurrentlyThrows() {
        val payload =
            """
            {
              "permissions": 0,
              "id": 42,
              "email": "user@example.com",
              "userType": 1,
              "avatar": null,
              "createdAt": "2024-01-01T00:00:00Z",
              "updatedAt": "2024-01-02T00:00:00Z",
              "requestCount": 0,
              "displayName": "User"
            }
            """.trimIndent()

        assertFailsWith<SerializationException> { json.decodeFromString<RequestUser>(payload) }
    }

    @Test
    fun testReleaseDateFullPayload() {
        val payload =
            """
            {
              "certification": "PG-13",
              "iso_639_1": "en",
              "note": null,
              "release_date": "2024-06-15T00:00:00Z",
              "type": 3
            }
            """.trimIndent()

        val model = json.decodeFromString<ReleaseDate>(payload)

        assertEquals(3, model.type)
        assertNotNull(model.release_date)
    }

    @Test
    fun testReleaseDateNullReleaseDateCurrentlyThrows() {
        val payload =
            """
            {
              "release_date": null,
              "type": 3
            }
            """.trimIndent()

        assertFailsWith<SerializationException> { json.decodeFromString<ReleaseDate>(payload) }
    }

    @Test
    fun testDownloadStatusFullPayload() {
        val payload =
            """
            {
              "externalId": 100,
              "mediaType": "movie",
              "size": 1000,
              "sizeLeft": 500,
              "status": "downloading",
              "title": "Some Release",
              "downloadId": "abc-123",
              "estimatedCompletionTime": "2024-06-15T00:00:00Z"
            }
            """.trimIndent()

        val model = json.decodeFromString<DownloadStatus>(payload)

        assertEquals("downloading", model.status)
        assertNotNull(model.estimatedCompletionTime)
    }

    @Test
    fun testDownloadStatusNullEstimatedCompletionTime() {
        val payload =
            """
            {
              "externalId": 100,
              "mediaType": "movie",
              "size": 1000,
              "sizeLeft": 500,
              "status": "queued",
              "title": "Some Release",
              "downloadId": "abc-123",
              "estimatedCompletionTime": null
            }
            """.trimIndent()

        val model = json.decodeFromString<DownloadStatus>(payload)

        assertNull(model.estimatedCompletionTime)
    }

    @Test
    fun testDownloadStatusNullStatusFieldCurrentlyThrows() {
        val payload =
            """
            {
              "externalId": 100,
              "mediaType": "movie",
              "size": 1000,
              "sizeLeft": 500,
              "status": null,
              "title": "Some Release",
              "downloadId": "abc-123"
            }
            """.trimIndent()

        assertFailsWith<SerializationException> { json.decodeFromString<DownloadStatus>(payload) }
    }

    @Test
    fun testVideoFullPayload() {
        val payload =
            """
            {
              "url": "https://youtu.be/x",
              "key": "abc",
              "name": "Trailer",
              "size": 1080,
              "type": "Trailer",
              "site": "YouTube"
            }
            """.trimIndent()

        val model = json.decodeFromString<Video>(payload)

        assertEquals("abc", model.key)
        assertEquals("YouTube", model.site)
    }

    @Test
    fun testVideoNullKeyCurrentlyThrows() {
        val payload =
            """
            {
              "url": null,
              "key": null,
              "name": "Trailer",
              "size": 1080,
              "type": "Trailer",
              "site": "YouTube"
            }
            """.trimIndent()

        assertFailsWith<SerializationException> { json.decodeFromString<Video>(payload) }
    }

    @Test
    fun testEpisodeFullPayload() {
        val payload =
            """
            {
              "id": 1,
              "name": "Pilot",
              "airDate": "2024-01-01",
              "episodeNumber": 1,
              "overview": "First episode",
              "seasonNumber": 1,
              "showId": 500
            }
            """.trimIndent()

        val model = json.decodeFromString<Episode>(payload)

        assertEquals("Pilot", model.name)
        assertEquals(1, model.episodeNumber)
    }

    @Test
    fun testEpisodeNullNameCurrentlyThrows() {
        val payload =
            """
            {
              "id": 1,
              "name": null,
              "episodeNumber": 1,
              "seasonNumber": 1,
              "showId": 500
            }
            """.trimIndent()

        assertFailsWith<SerializationException> { json.decodeFromString<Episode>(payload) }
    }
}
