package com.dnfapps.arrmatey.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dnfapps.arrmatey.arr.api.client.LoggerLevel
import com.dnfapps.arrmatey.arr.state.CalendarViewMode
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PreferencesStore(
    dataStoreFactory: DataStoreFactory
) {

    private val dataStore: DataStore<Preferences> = dataStoreFactory.provideDataStore()

    private val sonarrInfoCardKey = booleanPreferencesKey("sonarrInfoCard")
    private val radarrInfoCardKey = booleanPreferencesKey("radarrInfoCard")
    private val lidarrInfoCardKey = booleanPreferencesKey("lidarrInfoCard")
    private val prowlarrInfoCardKey = booleanPreferencesKey("prowlarrInfoCard")
    private val calendarViewTypeKey = stringPreferencesKey("calendarViewType")
    private val activityPollingKey = booleanPreferencesKey("enableActivityPolling")
    private val httpLogLevelKey = stringPreferencesKey("httpLogLevel")
    private val useDynamicThemeKey = booleanPreferencesKey("useDynamicTheme")
    private val useClearLogoKey = booleanPreferencesKey("useClearLogo")

    private fun infoCardKey(type: InstanceType): Preferences.Key<Boolean> = when (type) {
        InstanceType.Sonarr -> sonarrInfoCardKey
        InstanceType.Radarr -> radarrInfoCardKey
        InstanceType.Lidarr -> lidarrInfoCardKey
        InstanceType.Prowlarr -> prowlarrInfoCardKey
    }

    private val scope = CoroutineScope(Dispatchers.IO)

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

    val calendarViewMode: Flow<CalendarViewMode> = dataStore.data
        .map { preferences ->
            preferences[calendarViewTypeKey]?.let { type ->
                CalendarViewMode.valueOf(type)
            } ?: CalendarViewMode.List
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

    fun toggleCalendarViewMode() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[calendarViewTypeKey]?.let { type ->
                    CalendarViewMode.valueOf(type)
                } ?: CalendarViewMode.List
                preferences[calendarViewTypeKey] = when (current) {
                    CalendarViewMode.Month -> CalendarViewMode.List.name
                    CalendarViewMode.List -> CalendarViewMode.Month.name
                }
            }
        }
    }
}