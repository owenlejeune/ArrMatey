package com.dnfapps.arrmatey.datastore

import com.dnfapps.arrmatey.compose.TabItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object TabPreferencesSerializer {
    private val json = Json { encodeDefaults = true }

    fun serialize(tabPreferences: TabPreferences): String = json.encodeToString(tabPreferences)

    fun deserialize(jsonString: String?): TabPreferences {
        if (jsonString == null) return TabPreferences()

        return try {
            val jsonElement = Json.parseToJsonElement(jsonString).jsonObject

            if (jsonElement.containsKey("orderedVisibleKeys")) {
                return Json.decodeFromString<TabPreferences>(jsonString)
            }

            fun extractKey(element: JsonElement): String? =
                if (element is JsonPrimitive) {
                    "standard_${element.content}"
                } else {
                    element.jsonObject["key"]?.jsonPrimitive?.content
                        ?: element.jsonObject["id"]
                            ?.jsonPrimitive
                            ?.content
                            ?.let { "webpage_$it" }
                }

            val migratedVisible = jsonElement["bottomTabItems"]?.jsonArray?.mapNotNull { extractKey(it) } ?: emptyList()
            val migratedHidden = jsonElement["hiddenTabs"]?.jsonArray?.mapNotNull { extractKey(it) } ?: emptyList()

            val allStandardKeys = TabItem.Standard.entries.map { it.key }
            val trackedKeys = (migratedVisible + migratedHidden).toSet()
            val missingKeys =
                allStandardKeys.filter { key ->
                    val name = key.replace("standard_", "")
                    val entry = TabItem.Standard.entries.find { it.name == name }
                    key !in trackedKeys && entry?.isDisabled == false
                }

            if (migratedVisible.isEmpty() && migratedHidden.isEmpty() && missingKeys.isEmpty()) {
                return TabPreferences()
            }

            TabPreferences(
                orderedVisibleKeys = migratedVisible.ifEmpty { TabItem.defaultStandardKeys() },
                orderedHiddenKeys = migratedHidden + missingKeys,
            )
        } catch (e: Exception) {
            TabPreferences()
        }
    }
}
