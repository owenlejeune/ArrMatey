package com.dnfapps.arrmatey.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dnfapps.arrmatey.arr.api.client.LoggerLevel
import com.dnfapps.arrmatey.arr.state.CalendarFilterState
import com.dnfapps.arrmatey.arr.state.CalendarViewMode
import com.dnfapps.arrmatey.arr.state.ContentFilter
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class PreferencesStore(
    dataStoreFactory: DataStoreFactory
) {

    private val dataStore: DataStore<Preferences> = dataStoreFactory.provideDataStore()

    private val sonarrInfoCardKey = booleanPreferencesKey("sonarrInfoCard")
    private val radarrInfoCardKey = booleanPreferencesKey("radarrInfoCard")
    private val lidarrInfoCardKey = booleanPreferencesKey("lidarrInfoCard")
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
    private val tabPreferencesKey = stringPreferencesKey("tabPreferences")

    private fun infoCardKey(type: InstanceType): Preferences.Key<Boolean> = when (type) {
        InstanceType.Sonarr -> sonarrInfoCardKey
        InstanceType.Radarr -> radarrInfoCardKey
        InstanceType.Lidarr -> lidarrInfoCardKey
        InstanceType.Seerr -> seerrInfoCardKey
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

    fun saveTabPreferences(tabPreferences: TabPreferences) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[tabPreferencesKey] = Json.encodeToString(tabPreferences)
            }
        }
    }

    fun resetTabPreferences() {
        saveTabPreferences(TabPreferences())
    }

    fun updateBottomBarTabs(tabs: List<TabItem>) {
        scope.launch {
            val validTabs = tabs.filter { !it.drawerOnly }.take(5)
            if (validTabs.isEmpty()) {
                throw IllegalArgumentException("At least one tab must be visible")
            }

            val hidden = TabItem.navigationEntries.filter { !validTabs.contains(it) }

            saveTabPreferences(
                TabPreferences(
                    bottomTabItems = validTabs,
                    hiddenTabs = hidden
                )
            )
        }
    }

    private fun extractTabPreferences(preferences: Preferences): TabPreferences {
        val json = preferences[tabPreferencesKey]
        val savedPrefs = if (json != null) {
            try {
                Json.decodeFromString<TabPreferences>(json)
            } catch (e: Exception) {
                TabPreferences()
            }
        } else {
            TabPreferences()
        }

        val allTabs = TabItem.navigationEntries
        val displayedAndHidden = savedPrefs.bottomTabItems + savedPrefs.hiddenTabs
        val newTabs = allTabs.filter { it !in displayedAndHidden }

        return if (newTabs.isNotEmpty()) {
            savedPrefs.copy(hiddenTabs = savedPrefs.hiddenTabs + newTabs)
        } else {
            savedPrefs
        }
    }

}