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

    @Test
    fun testMovieRecommendationsPayload() {
        val payload =
            """
            {
              "page": 1,
              "totalPages": 30,
              "totalResults": 589,
              "results": [
                {
                  "id": 11428,
                  "mediaType": "movie",
                  "adult": false,
                  "genreIds": [27, 14, 53],
                  "originalLanguage": "en",
                  "originalTitle": "Sleepwalkers",
                  "overview": "Charles Brady and his mother, Mary...",
                  "popularity": 5.4788,
                  "releaseDate": "1992-04-10",
                  "title": "Sleepwalkers",
                  "video": false,
                  "voteAverage": 6.086,
                  "voteCount": 647,
                  "backdropPath": "/2GcMfxqQdB4xuyetOYPREdnu7ey.jpg",
                  "posterPath": "/iiwplv5pET2HgMog6otunohIiSr.jpg"
                },
                {
                  "id": 493922,
                  "mediaType": "movie",
                  "adult": false,
                  "genreIds": [27, 9648, 53],
                  "originalLanguage": "en",
                  "originalTitle": "Hereditary",
                  "overview": "Following the death...",
                  "popularity": 19.7135,
                  "releaseDate": "2018-06-07",
                  "title": "Hereditary",
                  "video": false,
                  "voteAverage": 7.297,
                  "voteCount": 8880,
                  "backdropPath": "/gJbTXKNTL6O7r7PzF6ZRkJGBlPp.jpg",
                  "posterPath": "/4GFPuL14eXi66V96xBWY73Y9PfR.jpg",
                  "mediaInfo": {
                    "downloadStatus": [],
                    "downloadStatus4k": [],
                    "id": 739,
                    "mediaType": "movie",
                    "tmdbId": 493922,
                    "tvdbId": null,
                    "imdbId": null,
                    "status": 5,
                    "status4k": 1,
                    "createdAt": "2026-02-20T11:36:51.000Z",
                    "updatedAt": "2026-02-20T11:36:51.000Z",
                    "lastSeasonChange": "2026-02-20T11:36:51.000Z",
                    "mediaAddedAt": "2025-07-15T14:58:31.000Z",
                    "serviceId": 0,
                    "serviceId4k": null,
                    "externalServiceId": 2508,
                    "externalServiceId4k": null,
                    "externalServiceSlug": "493922",
                    "externalServiceSlug4k": null,
                    "ratingKey": "49261",
                    "ratingKey4k": null,
                    "jellyfinMediaId": null,
                    "jellyfinMediaId4k": null,
                    "watchlists": [],
                    "mediaUrl": "https://app.plex.tv/desktop#!/server/be7a5a02f72d4e0062715f48974ba7f793e3ef96/details?key=%2Flibrary%2Fmetadata%2F49261",
                    "iOSPlexUrl": "plex://preplay/?metadataKey=%2Flibrary%2Fmetadata%2F49261&server=be7a5a02f72d4e0062715f48974ba7f793e3ef96",
                    "serviceUrl": "http://192.168.4.20:7878/movie/493922"
                  }
                }
              ]
            }
            """.trimIndent()

        val model = json.decodeFromString<DiscoverResponse>(payload)
        assertEquals(1, model.page)
        assertEquals(30, model.totalPages)
        assertEquals(589, model.totalResults)
        assertEquals(2, model.results.size)
        assertEquals(11428L, model.results[0].id)
        assertEquals(RequestType.Movie, model.results[0].mediaType)
        assertEquals(493922L, model.results[1].id)
        assertEquals(739L, model.results[1].mediaInfo?.id)
    }

    @Test
    fun testTvRecommendationsPayload() {
        val payload =
            """
            {
              "page": 1,
              "totalPages": 29,
              "totalResults": 561,
              "results": [
                {
                  "id": 40675,
                  "firstAirDate": "2011-09-16",
                  "genreIds": [80, 18],
                  "mediaType": "tv",
                  "name": "DCI Banks",
                  "originCountry": ["GB"],
                  "originalLanguage": "en",
                  "originalName": "DCI Banks",
                  "overview": "A thrilling drama...",
                  "popularity": 13.5199,
                  "voteAverage": 7.1,
                  "voteCount": 82,
                  "backdropPath": "/tdhRbNchHe6xbGQOaJbZ6wopu52.jpg",
                  "posterPath": "/5TKwpU4Q04dyZwg5jN80zOrzGgP.jpg"
                }
              ]
            }
            """.trimIndent()

        val model = json.decodeFromString<DiscoverResponse>(payload)
        assertEquals(1, model.page)
        assertEquals(29, model.totalPages)
        assertEquals(561, model.totalResults)
        assertEquals(1, model.results.size)
        assertEquals(40675L, model.results[0].id)
        assertEquals(RequestType.Tv, model.results[0].mediaType)
        assertEquals(listOf("GB"), model.results[0].originCountry)
        assertEquals("DCI Banks", model.results[0].name)
    }
}
