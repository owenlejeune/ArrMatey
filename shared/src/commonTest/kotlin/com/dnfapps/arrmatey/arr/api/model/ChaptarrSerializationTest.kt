package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.compose.utils.BookMediaFilterBy
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
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
            "authorNameLastFirst": "Sanderson, Brandon",
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
        assertEquals("Sanderson, Brandon", author.authorNameLastFirst)
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
    fun testCopyForChaptarrCreationEbookOnly() {
        val author = Author(title = "Test Author", folder = "Test Author")
        val created = author.copyForChaptarrCreation(
            selectedMediaType = BookMediaType.EBook,
            audiobookQualityProfileId = 2,
            audiobookMetadataProfileId = 3,
            audiobookRootFolderPath = "/audiobooks",
            audiobookMonitorExisting = 1,
            audiobookMonitorFuture = true,
            audiobookTags = listOf(10),
            ebookQualityProfileId = 5,
            ebookMetadataProfileId = 6,
            ebookRootFolderPath = "/ebooks",
            ebookMonitorExisting = 2,
            ebookMonitorFuture = false,
            ebookTags = listOf(20),
            tags = listOf(30),
            searchForMissingBooks = true,
        )

        assertEquals(BookMediaType.EBook, created.lastSelectedMediaType)
        // Audiobook fields must be null/empty
        assertEquals(null, created.audiobookQualityProfileId)
        assertEquals(null, created.audiobookMetadataProfileId)
        assertEquals(null, created.audiobookRootFolderPath)
        assertEquals(null, created.audiobookFolder)
        assertEquals(null, created.audiobookMonitorExisting)
        assertEquals(null, created.audiobookMonitorFuture)
        assertEquals(emptyList(), created.audiobookTags)

        // Ebook fields must be populated
        assertEquals(5, created.ebookQualityProfileId)
        assertEquals(6, created.ebookMetadataProfileId)
        assertEquals("/ebooks", created.ebookRootFolderPath)
        assertEquals("Test Author", created.ebookFolder)
        assertEquals(2, created.ebookMonitorExisting)
        assertEquals(false, created.ebookMonitorFuture)
        assertEquals(listOf(20), created.ebookTags)
        assertEquals(5, created.qualityProfileId)
        assertEquals(6, created.metadataProfileId)
        assertEquals("/ebooks", created.rootFolderPath)
    }

    @Test
    fun testCopyForChaptarrCreationAudiobookOnly() {
        val author = Author(title = "Test Author", folder = "Test Author")
        val created = author.copyForChaptarrCreation(
            selectedMediaType = BookMediaType.Audiobook,
            audiobookQualityProfileId = 2,
            audiobookMetadataProfileId = 3,
            audiobookRootFolderPath = "/audiobooks",
            audiobookMonitorExisting = 1,
            audiobookMonitorFuture = true,
            audiobookTags = listOf(10),
            ebookQualityProfileId = 5,
            ebookMetadataProfileId = 6,
            ebookRootFolderPath = "/ebooks",
            ebookMonitorExisting = 2,
            ebookMonitorFuture = false,
            ebookTags = listOf(20),
        )

        assertEquals(BookMediaType.Audiobook, created.lastSelectedMediaType)
        assertEquals(2, created.audiobookQualityProfileId)
        assertEquals(3, created.audiobookMetadataProfileId)
        assertEquals("/audiobooks", created.audiobookRootFolderPath)
        assertEquals(null, created.ebookQualityProfileId)
        assertEquals(null, created.ebookRootFolderPath)
        assertEquals(emptyList(), created.ebookTags)
        assertEquals(2, created.qualityProfileId)
        assertEquals(3, created.metadataProfileId)
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

    @Test
    fun testCopyForChaptarrCreationBoth() {
        val author = Author(title = "Test Author", folder = "Test Author")
        val created = author.copyForChaptarrCreation(
            selectedMediaType = BookMediaType.Both,
            audiobookQualityProfileId = 2,
            audiobookMetadataProfileId = 3,
            audiobookRootFolderPath = "/audiobooks",
            audiobookMonitorExisting = 1,
            audiobookMonitorFuture = true,
            audiobookTags = listOf(10),
            ebookQualityProfileId = 5,
            ebookMetadataProfileId = 6,
            ebookRootFolderPath = "/ebooks",
            ebookMonitorExisting = 2,
            ebookMonitorFuture = false,
            ebookTags = listOf(20),
        )

        assertEquals(BookMediaType.Both, created.lastSelectedMediaType)
        assertEquals(2, created.audiobookQualityProfileId)
        assertEquals("/audiobooks", created.audiobookRootFolderPath)
        assertEquals(5, created.ebookQualityProfileId)
        assertEquals("/ebooks", created.ebookRootFolderPath)
    }

    @Test
    fun testSingleMediaTypeLabel() {
        val audiobookOnly = Author(
            audiobookQualityProfileId = 1,
            audiobookRootFolderPath = "/audiobooks/Test",
        )
        assertEquals(MR.strings.audiobook, audiobookOnly.singleMediaTypeLabel)
        assertEquals(MR.strings.audiobook, audiobookOnly.singleMediaTypeLabel(isChaptarrInstance = true))

        val ebookOnly = Author(
            ebookQualityProfileId = 2,
            ebookRootFolderPath = "/ebooks/Test",
        )
        assertEquals(MR.strings.ebook, ebookOnly.singleMediaTypeLabel)
        assertEquals(MR.strings.ebook, ebookOnly.singleMediaTypeLabel(isChaptarrInstance = true))

        val bothTypes = Author(
            audiobookQualityProfileId = 1,
            audiobookRootFolderPath = "/audiobooks/Test",
            ebookQualityProfileId = 2,
            ebookRootFolderPath = "/ebooks/Test",
        )
        assertEquals(null, bothTypes.singleMediaTypeLabel)
        assertEquals(null, bothTypes.singleMediaTypeLabel(isChaptarrInstance = true))

        val standardAuthorInChaptarrInstance = Author(
            qualityProfileId = 1,
            rootFolderPath = "/books/Test",
        )
        assertEquals(null, standardAuthorInChaptarrInstance.singleMediaTypeLabel(isChaptarrInstance = false))
        assertEquals(MR.strings.ebook, standardAuthorInChaptarrInstance.singleMediaTypeLabel(isChaptarrInstance = true))

        // DJ MacHale (ebook only)
        val djMachale = Author(
            title = "D.J. MacHale",
            path = "/books/D.J. MacHale",
            ebookQualityProfileId = 1,
            ebookRootFolderPath = "/books",
            lastSelectedMediaType = BookMediaType.EBook,
        )
        assertEquals(MR.strings.ebook, djMachale.singleMediaTypeLabel)

        // George Orwell (ebook only, with lastSelectedMediaType="audiobook" from server default)
        val georgeOrwell = Author(
            title = "George Orwell",
            path = "/books/George Orwell",
            ebookQualityProfileId = 1,
            ebookRootFolderPath = "/books",
            lastSelectedMediaType = BookMediaType.Audiobook,
        )
        assertEquals(MR.strings.ebook, georgeOrwell.singleMediaTypeLabel)

        // Hugh Howey (audiobook only)
        val hughHowey = Author(
            title = "Hugh Howey",
            path = "/audiobooks/Hugh Howey",
            audiobookQualityProfileId = 2,
            audiobookRootFolderPath = "/audiobooks",
            lastSelectedMediaType = BookMediaType.Audiobook,
        )
        assertEquals(MR.strings.audiobook, hughHowey.singleMediaTypeLabel)

        // Suzanne Collins (both)
        val suzanneCollins = Author(
            title = "Suzanne Collins",
            path = "/audiobooks/Suzanne Collins",
            audiobookQualityProfileId = 2,
            audiobookRootFolderPath = "/audiobooks",
            ebookQualityProfileId = 1,
            ebookRootFolderPath = "/books",
            lastSelectedMediaType = BookMediaType.Audiobook,
        )
        assertEquals(null, suzanneCollins.singleMediaTypeLabel)
    }
}
