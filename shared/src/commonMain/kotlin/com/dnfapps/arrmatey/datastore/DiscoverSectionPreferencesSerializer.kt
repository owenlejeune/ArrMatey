package com.dnfapps.arrmatey.datastore

import com.dnfapps.arrmatey.discover.model.DiscoverCategory
import kotlinx.serialization.json.Json

internal object DiscoverSectionPreferencesSerializer {
    private val encodeJson = Json { encodeDefaults = true }
    private val decodeJson = Json { ignoreUnknownKeys = true }

    fun serialize(prefs: DiscoverSectionPreferences): String = encodeJson.encodeToString(prefs)

    fun deserialize(jsonString: String?): DiscoverSectionPreferences {
        if (jsonString == null) return DiscoverSectionPreferences()
        return try {
            val parsed = decodeJson.decodeFromString<DiscoverSectionPreferences>(jsonString)
            val allCategories = DiscoverCategory.entries
            val tracked = (parsed.visibleCategories + parsed.hiddenCategories).toSet()
            val missing = allCategories.filter { it !in tracked }

            val visible = parsed.visibleCategories.filter { it in allCategories } + missing
            val hidden = parsed.hiddenCategories.filter { it in allCategories && it !in visible }

            if (visible.isEmpty() && hidden.isNotEmpty()) {
                DiscoverSectionPreferences(
                    visibleCategories = listOf(hidden.first()),
                    hiddenCategories = hidden.drop(1),
                )
            } else if (visible.isEmpty()) {
                DiscoverSectionPreferences()
            } else {
                DiscoverSectionPreferences(
                    visibleCategories = visible,
                    hiddenCategories = hidden,
                )
            }
        } catch (e: Exception) {
            DiscoverSectionPreferences()
        }
    }
}
