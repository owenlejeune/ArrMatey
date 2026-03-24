package com.dnfapps.arrmatey.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dnfapps.arrmatey.arr.api.client.LoggerLevel
import com.dnfapps.arrmatey.arr.state.CalendarFilterState
import com.dnfapps.arrmatey.arr.state.CalendarViewMode
import com.dnfapps.arrmatey.arr.state.ContentFilter
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.features.ReleaseNotes
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PreferencesStore(
    dataStoreFactory: DataStoreFactory
) {

    private val dataStore: DataStore<Preferences> = dataStoreFactory.provideDataStore()

    private val sonarrInfoCardKey = booleanPreferencesKey("sonarrInfoCard")
    private val radarrInfoCardKey = booleanPreferencesKey("radarrInfoCard")
    private val lidarrInfoCardKey = booleanPreferencesKey("lidarrInfoCard")
    private val prowlarrInfoCardKey = booleanPreferencesKey("prowlarrInfoCard")
    private val seerrInfoCardKey = booleanPreferencesKey("seerrInfoCard")
    private val calendarViewTypeKey = stringPreferencesKey("calendarViewType")
    private val calendarContentFilterKey = stringPreferencesKey("calendarContentFilter")
    private val calendarMonitorOnlyKey = booleanPreferencesKey("calendarMonitorOnly")
    private val calendarPremiersOnlyKey = booleanPreferencesKey("calendarPremiersOnly")
    private val calendarFinalesOnlyKey = booleanPreferencesKey("calendarFinalesOnly")
    private val calendarInstanceIdKey = longPreferencesKey("calendarInstanceId")
    private val activityPollingKey = booleanPreferencesKey("enableActivityPolling")
    private val httpLogLevelKey = stringPreferencesKey("httpLogLevel")
    private val useDynamicThemeKey = booleanPreferencesKey("useDynamicTheme")
    private val useClearLogoKey = booleanPreferencesKey("useClearLogo")
    private val useServiceNavLogosKey = booleanPreferencesKey("useServiceNavLogos")
    private val tabPreferencesKey = stringPreferencesKey("tabPreferences")
    private val lastReleaseNotesKey = intPreferencesKey("lastReleaseNotes")
    private val isFirstLaunchKey = booleanPreferencesKey("isFirstLaunch")

    private fun infoCardKey(type: InstanceType): Preferences.Key<Boolean> = when (type) {
        InstanceType.Sonarr -> sonarrInfoCardKey
        InstanceType.Radarr -> radarrInfoCardKey
        InstanceType.Lidarr -> lidarrInfoCardKey
        InstanceType.Prowlarr -> prowlarrInfoCardKey
//        InstanceType.Seerr -> seerrInfoCardKey
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    val tabPreferences: Flow<TabPreferences> = dataStore.data
        .map { preferences ->
            extractTabPreferences(preferences)
        }

    val showInfoCards: Flow<Map<InstanceType, Boolean>> = dataStore.data
        .map { preferences ->
            InstanceType.entries.associateWith { type -> (preferences[infoCardKey(type)] ?: true) }
        }

    private var _isPollingEnabled: Boolean = true
    val isPollingEnabled: Boolean
        get() = _isPollingEnabled

    val enableActivityPolling: Flow<Boolean> = dataStore.data
        .map { preferences ->
            val value = preferences[activityPollingKey] ?: true
            _isPollingEnabled = value
            value
        }

    val httpLogLevel: Flow<LoggerLevel> = dataStore.data
        .map { preferences ->
            preferences[httpLogLevelKey]?.let { logLevel ->
                LoggerLevel.valueOf(logLevel)
            } ?: LoggerLevel.Headers
        }

    val useDynamicTheme: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[useDynamicThemeKey] ?: true
        }

    val useClearLogo: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[useClearLogoKey] ?: true
        }

    private val calendarViewMode: Flow<CalendarViewMode> = dataStore.data
        .map { preferences ->
            preferences[calendarViewTypeKey]?.let { type ->
                CalendarViewMode.valueOf(type)
            } ?: CalendarViewMode.List
        }

    private val calendarContentFilter: Flow<ContentFilter> = dataStore.data
        .map { preferences ->
            preferences[calendarContentFilterKey]?.let { cf ->
                ContentFilter.valueOf(cf)
            } ?: ContentFilter.All
        }

    private val calendarShowMonitorOnly: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[calendarMonitorOnlyKey] ?: false
        }

    private val calendarShowPremiersOnly: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[calendarPremiersOnlyKey] ?: false
        }

    private val calendarShowFinalesOnly: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[calendarFinalesOnlyKey] ?: false
        }

    private val calendarInstanceId: Flow<Long?> = dataStore.data
        .map { preferences ->
            preferences[calendarInstanceIdKey]?.takeIf { it > 0 }
        }

    fun observeCalendarFilterState(): Flow<CalendarFilterState> = combine(
        combine(
        calendarViewMode, calendarContentFilter, calendarShowMonitorOnly
        ) { viewMode, contentFiler, monitorOnly ->
            Triple(viewMode, contentFiler, monitorOnly)
        },
        calendarShowPremiersOnly, calendarShowFinalesOnly, calendarInstanceId
    ) { (viewMode, contentFilter, monitorOnly), premiersOnly, finalesOnly, instanceId ->
        CalendarFilterState(viewMode, contentFilter, monitorOnly, premiersOnly, finalesOnly, instanceId)
    }

    suspend fun saveCalendarFilterState(state: CalendarFilterState) {
        dataStore.edit { preferences ->
            preferences[calendarViewTypeKey] = state.viewMode.name
            preferences[calendarContentFilterKey] = state.contentFilter.name
            preferences[calendarMonitorOnlyKey] = state.showMonitoredOnly
            preferences[calendarPremiersOnlyKey] = state.showPremiersOnly
            preferences[calendarFinalesOnlyKey] = state.showFinalesOnly
            preferences[calendarInstanceIdKey] = state.instanceId ?: -1
        }
    }

    fun dismissInfoCard(type: InstanceType) {
        setInfoCardVisibility(type, false)
    }

    fun setInfoCardVisibility(type: InstanceType, value: Boolean) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[infoCardKey(type)] = value
            }
        }
    }

    fun toggleActivityPolling() {
        scope.launch {
            dataStore.edit { preferences ->
                val isPolling = preferences[activityPollingKey] ?: true
                preferences[activityPollingKey] = !isPolling
            }
        }
    }

    fun setLogLevel(level: LoggerLevel) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[httpLogLevelKey] = level.name
            }
        }
    }

    fun toggleUseDynamicTheme() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[useDynamicThemeKey] ?: true
                preferences[useDynamicThemeKey] = !current
            }
        }
    }

    fun toggleUseClearLogo() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[useClearLogoKey] ?: true
                preferences[useClearLogoKey] = !current
            }
        }
    }

    fun resetTabPreferences() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences.remove(tabPreferencesKey)
            }
        }
    }

    fun saveTabPreferences(tabPreferences: TabPreferences) {
        scope.launch {
            dataStore.edit { preferences ->
                val json = Json {
                    encodeDefaults = true
                }
                val jsonString = json.encodeToString(tabPreferences)

                preferences[tabPreferencesKey] = jsonString
            }
        }
    }

    fun updateTabPreferences(tabPreferences: TabPreferences) {
        scope.launch {
            saveTabPreferences(tabPreferences)
        }
    }

    private fun extractTabPreferences(preferences: Preferences): TabPreferences {
        val jsonString = preferences[tabPreferencesKey] ?: return TabPreferences()

        return try {
            val jsonElement = Json.parseToJsonElement(jsonString).jsonObject

            if (jsonElement.containsKey("orderedVisibleKeys")) {
                return Json.decodeFromString<TabPreferences>(jsonString)
            }

            fun extractKey(element: JsonElement): String? {
                return if (element is JsonPrimitive) {
                    "standard_${element.content}"
                } else {
                    element.jsonObject["key"]?.jsonPrimitive?.content
                        ?: element.jsonObject["id"]?.jsonPrimitive?.content?.let { "webpage_$it" }
                }
            }

            val migratedVisible = jsonElement["bottomTabItems"]?.jsonArray?.mapNotNull { extractKey(it) } ?: emptyList()
            val migratedHidden = jsonElement["hiddenTabs"]?.jsonArray?.mapNotNull { extractKey(it) } ?: emptyList()

            val allStandardKeys = TabItem.Standard.entries.map { it.key }
            val trackedKeys = (migratedVisible + migratedHidden).toSet()
            val missingKeys = allStandardKeys.filter { key ->
                val name = key.replace("standard_", "")
                val entry = TabItem.Standard.entries.find { it.name == name }
                key !in trackedKeys && entry?.isDisabled == false
            }

            TabPreferences(
                orderedVisibleKeys = migratedVisible,
                orderedHiddenKeys = migratedHidden + missingKeys
            )
        } catch (e: Exception) {
            TabPreferences()
        }
    }

    val isFirstLaunch: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[isFirstLaunchKey] ?: true
        }

    val shouldShowReleaseNotes: Flow<Boolean> = dataStore.data
        .combine(isFirstLaunch) { preferences, isFirst ->
            if (isFirst) {
                dataStore.edit { it[lastReleaseNotesKey] = ReleaseNotes.latestUpdate.buildCode }
                false
            } else {
                val lastCode = preferences[lastReleaseNotesKey] ?: -1
                lastCode < ReleaseNotes.latestUpdate.buildCode
            }
        }

    fun markReleaseNotesAsSeen() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[lastReleaseNotesKey] = ReleaseNotes.latestUpdate.buildCode
            }
        }
    }

    fun markFirstLaunchComplete() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[isFirstLaunchKey] ?: true
                if (current) {
                    markReleaseNotesAsSeen()
                }
                preferences[isFirstLaunchKey] = false
            }
        }
    }

    val useServiceNavLogos: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[useServiceNavLogosKey] ?: false
        }

    fun toggleUseServiceNavLogos() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[useServiceNavLogosKey] ?: false
                preferences[useServiceNavLogosKey] = !current
            }
        }
    }

}
