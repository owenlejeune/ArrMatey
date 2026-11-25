package com.dnfapps.arrmatey.datastore

import com.dnfapps.arrmatey.discover.model.DiscoverFilterState
import kotlinx.serialization.json.Json

/**
 * Stores every See More screen's [DiscoverFilterState] as one JSON map. Keys are
 * `discover:<category>` for the Seerr screens and `newest:<kind>` for Prowlarr's.
 * Anything unreadable falls back to no saved filters, so each screen starts at its default.
 */
object SeeMoreFilterSerializer {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        coerceInputValues = true
    }

    fun discoverKey(category: String): String = "discover:$category"

    fun newestKey(kind: String): String = "newest:$kind"

    fun serialize(filters: Map<String, DiscoverFilterState>): String = json.encodeToString(filters)

    fun deserialize(jsonString: String?): Map<String, DiscoverFilterState> {
        if (jsonString.isNullOrBlank()) return emptyMap()
        return try {
            json.decodeFromString<Map<String, DiscoverFilterState>>(jsonString)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
