package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class HistoryEventTypeTest {
    private fun decode(value: String): HistoryEventType = Json.decodeFromString(HistoryEventTypeSerializer, "\"$value\"")

    @Test
    fun testImportIncompleteEvents() {
        assertEquals(HistoryEventType.AlbumImportIncomplete, decode("albumImportIncomplete")) // Lidarr
        assertEquals(HistoryEventType.BookImportIncomplete, decode("bookImportIncomplete")) // Readarr / Bookshelf
    }

    @Test
    fun testUnrecognisedEventFallsBackToUnknown() {
        assertEquals(HistoryEventType.Unknown, decode("someFutureEventType"))
    }
}
