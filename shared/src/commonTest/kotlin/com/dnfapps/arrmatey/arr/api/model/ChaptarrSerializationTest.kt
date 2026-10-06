package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.compose.utils.BookMediaFilterBy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChaptarrSerializationTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun testDeserializeChaptarrAuthor() {
        val authorJson = """
        {
            "id": 42,
            "authorName": "Brandon Sanderson",
            "title": "Brandon Sanderson",
            "cleanTitle": "brandonsanderson",
            "status": "continuing",
            "overview": "Fantasy and sci-fi author.",
            "monitored": true,
            "audiobookQualityProfileId": 2,
            "ebookQualityProfileId": 1,
            "audiobookMetadataProfileId": 3,
            "ebookMetadataProfileId": 4,
            "audiobookRootFolderPath": "/audiobooks/Brandon Sanderson",
            "ebookRootFolderPath": "/ebooks/Brandon Sanderson",
            "audiobookFolder": "Brandon Sanderson",
            "ebookFolder": "Brandon Sanderson",
            "audiobookTags": [1, 2],
            "ebookTags": [3],
            "audiobookMonitorExisting": 1,
            "audiobookMonitorFuture": true,
            "ebookMonitorExisting": 2,
            "ebookMonitorFuture": false,
            "lastSelectedMediaType": "audiobook"
        }
        """.trimIndent()

        val author = json.decodeFromString<Author>(authorJson)

        assertEquals(42L, author.id)
        assertEquals("Brandon Sanderson", author.title)
        assertEquals(2, author.audiobookQualityProfileId)
        assertEquals(1, author.ebookQualityProfileId)
        assertEquals(3, author.audiobookMetadataProfileId)
        assertEquals(4, author.ebookMetadataProfileId)
        assertEquals("/audiobooks/Brandon Sanderson", author.audiobookRootFolderPath)
        assertEquals("/ebooks/Brandon Sanderson", author.ebookRootFolderPath)
        assertEquals(listOf(1, 2), author.audiobookTags)
        assertEquals(listOf(3), author.ebookTags)
        assertEquals(1, author.audiobookMonitorExisting)
        assertEquals(true, author.audiobookMonitorFuture)
        assertEquals(2, author.ebookMonitorExisting)
        assertEquals(false, author.ebookMonitorFuture)
        assertEquals(BookMediaType.Audiobook, author.lastSelectedMediaType)
    }

    @Test
    fun testDeserializeChaptarrBook() {
        val bookJson = """
        {
            "id": 101,
            "title": "The Way of Kings",
            "authorTitle": "Brandon Sanderson",
            "authorId": 42,
            "monitored": true,
            "mediaType": "audiobook",
            "audiobookMonitored": true,
            "ebookMonitored": false,
            "narratorNames": ["Michael Kramer", "Kate Reading"],
            "availableNarrators": ["Michael Kramer", "Kate Reading", "GraphicAudio"],
            "hasFiles": true,
            "isOmnibus": false
        }
        """.trimIndent()

        val book = json.decodeFromString<Book>(bookJson)

        assertEquals(101L, book.id)
        assertEquals("The Way of Kings", book.title)
        assertEquals(42L, book.authorId)
        assertTrue(book.monitored)
        assertEquals(BookMediaType.Audiobook, book.mediaType)
        assertEquals(true, book.audiobookMonitored)
        assertEquals(false, book.ebookMonitored)
        assertEquals(listOf("Michael Kramer", "Kate Reading"), book.narratorNames)
        assertEquals(listOf("Michael Kramer", "Kate Reading", "GraphicAudio"), book.availableNarrators)
        assertTrue(book.hasFiles)
    }

    @Test
    fun testBookMediaFilterBySerialization() {
        val allJson = json.encodeToString(BookMediaFilterBy.All)
        val ebookJson = json.encodeToString(BookMediaFilterBy.EBook)
        val audiobookJson = json.encodeToString(BookMediaFilterBy.Audiobook)

        assertEquals(BookMediaFilterBy.All, json.decodeFromString<BookMediaFilterBy>(allJson))
        assertEquals(BookMediaFilterBy.EBook, json.decodeFromString<BookMediaFilterBy>(ebookJson))
        assertEquals(BookMediaFilterBy.Audiobook, json.decodeFromString<BookMediaFilterBy>(audiobookJson))
    }
}
