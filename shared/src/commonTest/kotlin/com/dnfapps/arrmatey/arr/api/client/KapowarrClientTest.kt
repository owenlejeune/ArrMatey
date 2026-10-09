package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.HistoryEventType
import com.dnfapps.arrmatey.arr.api.model.KapowarrHistoryResponse
import com.dnfapps.arrmatey.arr.api.model.KapowarrReleasesResponse
import com.dnfapps.arrmatey.arr.api.model.KapowarrRootFolderResponse
import com.dnfapps.arrmatey.arr.api.model.KapowarrSearchResponse
import com.dnfapps.arrmatey.arr.api.model.VolumesResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KapowarrClientTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testVolumesResponseDeserialization() {
        val payload = """
            {
              "error": null,
              "result": [
                {
                  "id": 1,
                  "comicvine_id": 17993,
                  "title": "Invincible",
                  "year": 2003,
                  "publisher": "Image",
                  "volume_number": 1,
                  "description": "<p>Girls, acne, homework, supervillains...</p>",
                  "monitored": true,
                  "monitor_new_issues": true,
                  "folder": "/comics-1/Invincible/Volume 01 (2003)",
                  "issue_count": 145,
                  "issue_count_monitored": 145,
                  "issues_downloaded": 145,
                  "issues_downloaded_monitored": 145,
                  "total_size": 12323631335
                },
                {
                  "id": 5,
                  "comicvine_id": 136890,
                  "title": "Supergirl: Woman of Tomorrow",
                  "year": 2021,
                  "publisher": "DC Comics",
                  "volume_number": 1,
                  "description": "Supergirl (Rebirth)",
                  "monitored": true,
                  "monitor_new_issues": true,
                  "folder": "/comics-1/Supergirl - Woman of Tomorrow/Volume 01 (2021)",
                  "issue_count": 8,
                  "issue_count_monitored": 8,
                  "issues_downloaded": 8,
                  "issues_downloaded_monitored": 8,
                  "total_size": 607174810
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<VolumesResponse>(payload)
        assertEquals(null, response.error)
        val result = response.result
        assertNotNull(result)
        assertEquals(2, result.size)

        val invincible = result[0]
        assertEquals(1L, invincible.id)
        assertEquals(17993L, invincible.comicvineId)
        assertEquals("Invincible", invincible.title)
        assertEquals(2003, invincible.year)
        assertEquals("Image", invincible.publisher)
        assertEquals(1, invincible.volumeNumber)
        assertEquals("<p>Girls, acne, homework, supervillains...</p>", invincible.overview)
        assertTrue(invincible.monitored)
        assertTrue(invincible.monitorNewIssues)
        assertEquals("/comics-1/Invincible/Volume 01 (2003)", invincible.folder)
        assertEquals(145, invincible.issueCount)
        assertEquals(145, invincible.issuesDownloaded)
        assertEquals(12323631335L, invincible.totalSize)
        assertEquals("145 / 145", invincible.statusString)
        assertEquals(1.0f, invincible.statusProgress)
    }

    @Test
    fun testHistoryResponseDeserialization() {
        val payload = """
            {
              "error": null,
              "result": [
                {
                  "web_link": "https://getcomics.org/dc/supergirl-woman-of-tomorrow-8-2022/",
                  "web_title": "Supergirl – Woman of Tomorrow #8 (2022)",
                  "web_sub_title": "Supergirl – Woman of Tomorrow #8",
                  "file_title": "Supergirl - Woman of Tomorrow (2021) Volume 01 Issue 008",
                  "volume_id": 5,
                  "issue_id": 227,
                  "source": "GetComics",
                  "downloaded_at": 1775499859,
                  "success": true
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<KapowarrHistoryResponse>(payload)
        assertEquals(null, response.error)
        val result = response.result
        assertNotNull(result)
        assertEquals(1, result.size)

        val item = result[0]
        assertEquals(5L, item.volumeId)
        assertEquals(227L, item.issueId)
        assertEquals("GetComics", item.source)
        assertEquals("Supergirl - Woman of Tomorrow (2021) Volume 01 Issue 008", item.displayTitle)
        assertEquals("GetComics", item.indexerLabel)
        assertEquals(HistoryEventType.DownloadImported, item.eventType)
    }

    @Test
    fun testReleasesResponseDeserialization() {
        val payload = """
            {
              "error": null,
              "result": [
                {
                  "series": "Invincible",
                  "year": 2018,
                  "volume_number": [1],
                  "special_version": null,
                  "issue_number": 144.0,
                  "annual": false,
                  "link": "https://getcomics.org/other-comics/invincible-144-2018/",
                  "display_title": "Invincible #144 (2018)",
                  "size": 70000000,
                  "indexer_id": 1,
                  "indexer_title": "GetComics",
                  "match": true,
                  "match_issue": null
                },
                {
                  "series": "Invincible",
                  "year": 2003,
                  "volume_number": null,
                  "special_version": null,
                  "issue_number": [ 0.0, 144.0 ],
                  "annual": false,
                  "link": "https://getcomics.org/other-comics/invincible-0-144-tpbs-extras-collection-2003-2018/",
                  "display_title": "Invincible #0 – 144 + TPBs + Extras (Collection) (2003-2018)",
                  "size": 11700000000,
                  "indexer_id": 1,
                  "indexer_title": "GetComics",
                  "match": false,
                  "match_issue": "Issue numbers don't match"
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<KapowarrReleasesResponse>(payload)
        assertEquals(null, response.error)
        val result = response.result
        assertNotNull(result)
        assertEquals(2, result.size)

        val first = result[0]
        assertEquals("Invincible #144 (2018)", first.title)
        assertEquals(70000000L, first.size)
        assertEquals("GetComics", first.indexer)
        assertTrue(first.approved)
        assertEquals(1, first.volumeNumber)

        val second = result[1]
        assertEquals("Invincible #0 – 144 + TPBs + Extras (Collection) (2003-2018)", second.title)
        assertTrue(second.rejected)
        assertEquals("Issue numbers don't match", second.rejections.firstOrNull())
    }

    @Test
    fun testSearchResponseDeserialization() {
        val payload = """
            {
              "error": null,
              "result": [
                {
                  "comicvine_id": 6000,
                  "title": "Deadpool",
                  "year": 1997,
                  "volume_number": 1,
                  "cover_link": "https://comicvine.gamespot.com/a/uploads/scale_small/11/117763/2778493-deadpool__1___page_1.jpg",
                  "description": "<p>Starting in 1997...</p>",
                  "site_url": "https://comicvine.gamespot.com/deadpool/4050-6000/",
                  "aliases": [
                    "Deadpool: Agent of Weapon X"
                  ],
                  "publisher": "Marvel",
                  "issue_count": 71,
                  "translated": false,
                  "already_added": null
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<KapowarrSearchResponse>(payload)
        assertEquals(null, response.error)
        val result = response.result
        assertNotNull(result)
        assertEquals(1, result.size)

        val item = result[0]
        assertEquals(6000L, item.comicvineId)
        assertEquals("Deadpool", item.title)
        assertEquals(1997, item.year)
        assertEquals("Marvel", item.publisher)
        assertEquals(71, item.issueCount)

        val volume = item.toComicVolume()
        assertEquals("Deadpool", volume.title)
        assertEquals(6000L, volume.comicvineId)
        assertEquals("Marvel", volume.publisher)
        assertEquals(71, volume.issueCount)
        assertEquals(1, volume.images.size)
        assertEquals("https://comicvine.gamespot.com/a/uploads/scale_small/11/117763/2778493-deadpool__1___page_1.jpg", volume.images.first().remoteUrl)
    }

    @Test
    fun testRootFolderResponseDeserialization() {
        val payload = """
            {
              "error": null,
              "result": [
                {
                  "id": 1,
                  "folder": "/comics-1/",
                  "size": {
                    "total": 1967845998592,
                    "used": 1533284896768,
                    "free": 334524436480
                  }
                }
              ]
            }
        """.trimIndent()

        val response = json.decodeFromString<KapowarrRootFolderResponse>(payload)
        assertEquals(null, response.error)
        val result = response.result
        assertNotNull(result)
        assertEquals(1, result.size)

        val item = result[0]
        assertEquals(1, item.id)
        assertEquals("/comics-1/", item.folder)
        assertEquals(334524436480L, item.size?.free)

        val rootFolder = item.toRootFolder()
        assertEquals(1, rootFolder.id)
        assertEquals("/comics-1/", rootFolder.path)
        assertEquals(334524436480L, rootFolder.freeSpace)
        assertEquals(1967845998592L, rootFolder.totalSpace)
    }
}
