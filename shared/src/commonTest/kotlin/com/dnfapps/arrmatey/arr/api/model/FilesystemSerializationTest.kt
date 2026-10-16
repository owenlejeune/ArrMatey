package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FilesystemSerializationTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun testDeserializeFilesystemResponse() {
        val payload = """
        {
          "directories": [
            {
              "type": "folder",
              "name": "app",
              "path": "/app/",
              "size": 0,
              "lastModified": "2026-09-26T01:46:47Z"
            },
            {
              "type": "folder",
              "name": "bin",
              "path": "/bin/",
              "size": 0,
              "lastModified": "2026-09-19T16:35:50Z"
            }
          ],
          "files": []
        }
        """.trimIndent()

        val response = json.decodeFromString<FilesystemResponse>(payload)
        assertNull(response.parent)
        assertEquals(2, response.directories.size)
        assertEquals(0, response.files.size)

        val firstDir = response.directories[0]
        assertEquals("app", firstDir.name)
        assertEquals("/app/", firstDir.path)
        assertEquals("folder", firstDir.type)

        val item = firstDir.toFilesystemItem(response.parent)
        assertEquals("app", item.name)
        assertEquals("/app/", item.path)
        assertEquals(FilesystemItemType.Folder, item.type)
        assertTrue(item.isDirectory)
        assertNull(item.parent)
    }

    @Test
    fun testDeserializeFilesystemResponseWithParentAndFiles() {
        val payload = """
        {
          "parent": "/media",
          "directories": [
            {
              "type": "folder",
              "name": "movies",
              "path": "/media/movies/",
              "size": 0,
              "lastModified": "2026-10-01T12:00:00Z"
            }
          ],
          "files": [
            {
              "type": "file",
              "name": "movie.mkv",
              "path": "/media/movie.mkv",
              "size": 1048576,
              "lastModified": "2026-10-01T12:00:00Z"
            }
          ]
        }
        """.trimIndent()

        val response = json.decodeFromString<FilesystemResponse>(payload)
        assertEquals("/media", response.parent)
        assertEquals(1, response.directories.size)
        assertEquals(1, response.files.size)

        val dirItem = response.directories[0].toFilesystemItem(response.parent)
        assertTrue(dirItem.isDirectory)
        assertEquals("/media", dirItem.parent)

        val fileItem = response.files[0].toFilesystemItem(response.parent, isDir = false)
        assertEquals(FilesystemItemType.File, fileItem.type)
        assertEquals(false, fileItem.isDirectory)
        assertEquals(1048576L, fileItem.size)
    }
}
