package com.dnfapps.arrmatey.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.dnfapps.arrmatey.arr.api.client.LoggerLevel
import com.dnfapps.arrmatey.arr.state.CalendarFilterState
import com.dnfapps.arrmatey.arr.state.CalendarViewMode
import com.dnfapps.arrmatey.arr.state.ContentFilter
import com.dnfapps.arrmatey.compose.DashboardCards
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueSortState
import com.dnfapps.arrmatey.features.ReleaseNotes
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.AppColor
import com.dnfapps.arrmatey.model.AppTheme
import com.dnfapps.arrmatey.model.SmartAddSeerrAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface PreferencesStore {
    val defaultAppColor: AppColor

    val tabPreferences: Flow<TabPreferences>
    val discoverSectionPreferences: Flow<DiscoverSectionPreferences>
    val showInfoCards: Flow<Map<InstanceType, Boolean>>

    val httpLogLevel: Flow<LoggerLevel>

    val appTheme: Flow<AppTheme>
    val appColor: Flow<AppColor>

    val unifiedLibrarySearchAllInstances: Flow<Boolean>
    val searchShowBanners: Flow<Boolean>
    val dualPanelSupport: Flow<Boolean>

    val smartAddSeerrAction: Flow<SmartAddSeerrAction>
    val combineSeerrArrMedia: Flow<Boolean>
    val bazarrDetailsIntegration: Flow<Boolean>
    val tracearrDetailsIntegration: Flow<Boolean>

    val isFirstLaunch: Flow<Boolean>
    val shouldShowReleaseNotes: Flow<Boolean>

    val useServiceNavLogos: Flow<Boolean>
    val hideInstanceSwitcher: Flow<Boolean>
    val useFloatingNavigationBar: Flow<Boolean>

    val dashboardCardsOrder: Flow<List<DashboardCards>>
    val dashboardFirstLaunch: Flow<Boolean>
    val showDashboardSearch: Flow<Boolean>

    val credentialsMigrated: Flow<Boolean>
    val localNetworkNoticeSeen: Flow<Boolean>
    val localNetworkPermissionInfoDismissed: Flow<Boolean>

    val useColoredActivityCards: Flow<Boolean>
    val useColoredCalendarCards: Flow<Boolean>

    fun observeCalendarFilterState(): Flow<CalendarFilterState>

    suspend fun saveCalendarFilterState(state: CalendarFilterState)

    fun dismissInfoCard(type: InstanceType)

    fun setInfoCardVisibility(
        type: InstanceType,
        value: Boolean,
    )

    fun setLogLevel(level: LoggerLevel)

    fun setAppTheme(theme: AppTheme)

    fun setAppColor(color: AppColor)

    fun toggleUnifiedLibrarySearchAllInstances()

    fun setUnifiedLibrarySearchAllInstances(value: Boolean)

    fun toggleSearchShowBanners()

    fun setSearchShowBanners(value: Boolean)

    fun toggleDualPanelSupport()

    fun setDualPanelSupport(value: Boolean)

    fun setSmartAddSeerrAction(action: SmartAddSeerrAction)

    fun toggleCombineSeerrArrMedia()

    fun setCombineSeerrArrMedia(value: Boolean)

    fun toggleBazarrDetailsIntegration()

    fun setBazarrDetailsIntegration(value: Boolean)

    fun toggleTracearrDetailsIntegration()

    fun setTracearrDetailsIntegration(value: Boolean)

    fun resetTabPreferences()

    fun saveTabPreferences(tabPreferences: TabPreferences)

    fun updateTabPreferences(tabPreferences: TabPreferences)

    fun saveDiscoverSectionPreferences(prefs: DiscoverSectionPreferences)

    fun resetDiscoverSectionPreferences()

    fun markReleaseNotesAsSeen()

    fun markFirstLaunchComplete()

    fun toggleUseServiceNavLogos()

    fun setUseServiceNavLogos(value: Boolean)

    fun toggleInstanceSwitcher()

    fun setHideInstanceSwitcher(value: Boolean)

    fun toggleUseFloatingNavigationBar()

    fun setUseFloatingNavigationBar(value: Boolean)

    fun observeDownloadClientUiState(): Flow<DownloadQueueSortState>

    suspend fun saveDownloadClientUiState(state: DownloadQueueSortState)

    suspend fun updateDashboardCardsOrder(cards: List<DashboardCards>)

    fun markDashboardAsSeen()

    fun setShowDashboardSearch(show: Boolean)

    fun markCredentialsMigrated()

    fun markLocalNetworkNoticeAsSeen()

    fun dismissLocalNetworkPermissionInfo()

    fun toggleUseColoredActivityCards()

    fun setUseColoredActivityCards(value: Boolean)

    fun toggleUseColoredCalendarCards()

    fun setUseColoredCalendarCards(value: Boolean)

    companion object {
        operator fun invoke(dataStoreFactory: DataStoreFactory): PreferencesStore = DefaultPreferencesStore(dataStoreFactory)
    }
}

class DefaultPreferencesStore(
    private val dataStoreFactory: DataStoreFactory,
) : PreferencesStore {
    override val defaultAppColor: AppColor = dataStoreFactory.defaultAppColor

    private val dataStore: DataStore<Preferences> = dataStoreFactory.provideDataStore()
    private val scope = CoroutineScope(Dispatchers.IO)

    override val tabPreferences: Flow<TabPreferences> =
        dataStore.data.map { preferences ->
            TabPreferencesSerializer.deserialize(preferences[PreferenceKeys.TAB_PREFERENCES])
        }

    override val discoverSectionPreferences: Flow<DiscoverSectionPreferences> =
        dataStore.data.map { preferences ->
            DiscoverSectionPreferencesSerializer.deserialize(preferences[PreferenceKeys.DISCOVER_SECTION_PREFERENCES])
        }

    override val showInfoCards: Flow<Map<InstanceType, Boolean>> =
        dataStore.data.map { preferences ->
            InstanceType.entries.associateWith { type ->
                preferences[PreferenceKeys.infoCardKey(type)] ?: PreferenceDefaults.SHOW_INFO_CARD
            }
        }

    override val httpLogLevel: Flow<LoggerLevel> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.HTTP_LOG_LEVEL]?.let { logLevel ->
                runCatching { LoggerLevel.valueOf(logLevel) }.getOrNull()
            } ?: PreferenceDefaults.HTTP_LOG_LEVEL
        }

    override val appTheme: Flow<AppTheme> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.APP_THEME]?.let {
                runCatching { AppTheme.valueOf(it) }.getOrNull()
            } ?: PreferenceDefaults.APP_THEME
        }

    override val appColor: Flow<AppColor> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.APP_COLOR]?.let {
                runCatching { AppColor.valueOf(it) }.getOrNull()
            } ?: dataStoreFactory.defaultAppColor
        }

    override val unifiedLibrarySearchAllInstances: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.UNIFIED_LIBRARY_SEARCH_ALL_INSTANCES]
                ?: PreferenceDefaults.UNIFIED_LIBRARY_SEARCH_ALL_INSTANCES
        }

    override val searchShowBanners: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.SEARCH_SHOW_BANNERS] ?: PreferenceDefaults.SEARCH_SHOW_BANNERS
        }

    override val dualPanelSupport: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.DUAL_PANEL_SUPPORT] ?: PreferenceDefaults.DUAL_PANEL_SUPPORT
        }

    override val smartAddSeerrAction: Flow<SmartAddSeerrAction> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.SMART_ADD_SEERR_ACTION]?.let {
                runCatching { SmartAddSeerrAction.valueOf(it) }.getOrNull()
            } ?: PreferenceDefaults.SMART_ADD_SEERR_ACTION
        }

    override val combineSeerrArrMedia: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.COMBINE_SEERR_ARR_MEDIA] ?: PreferenceDefaults.COMBINE_SEERR_ARR_MEDIA
        }

    override val bazarrDetailsIntegration: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.BAZARR_DETAILS_INTEGRATION] ?: PreferenceDefaults.BAZARR_DETAILS_INTEGRATION
        }

    override val tracearrDetailsIntegration: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.TRACEARR_DETAILS_INTEGRATION] ?: PreferenceDefaults.TRACEARR_DETAILS_INTEGRATION
        }

    private val calendarViewMode: Flow<CalendarViewMode> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.CALENDAR_VIEW_TYPE]?.let { type ->
                runCatching { CalendarViewMode.valueOf(type) }.getOrNull()
            } ?: PreferenceDefaults.CALENDAR_VIEW_MODE
        }

    private val calendarContentFilter: Flow<ContentFilter> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.CALENDAR_CONTENT_FILTER]?.let { cf ->
                runCatching { ContentFilter.valueOf(cf) }.getOrNull()
            } ?: PreferenceDefaults.CALENDAR_CONTENT_FILTER
        }

    private val calendarShowMonitorOnly: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.CALENDAR_MONITOR_ONLY] ?: PreferenceDefaults.CALENDAR_MONITOR_ONLY
        }

    private val calendarShowPremiersOnly: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.CALENDAR_PREMIERS_ONLY] ?: PreferenceDefaults.CALENDAR_PREMIERS_ONLY
        }

    private val calendarShowFinalesOnly: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.CALENDAR_FINALES_ONLY] ?: PreferenceDefaults.CALENDAR_FINALES_ONLY
        }

    private val downloadClientSortBy: Flow<SortBy> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.DOWNLOAD_CLIENT_SORT_BY]?.let {
                runCatching { SortBy.valueOf(it) }.getOrNull()
            } ?: PreferenceDefaults.DOWNLOAD_CLIENT_SORT_BY
        }

    private val downloadClientSortOrder: Flow<SortOrder> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.DOWNLOAD_CLIENT_SORT_ORDER]?.let {
                runCatching { SortOrder.valueOf(it) }.getOrNull()
            } ?: PreferenceDefaults.DOWNLOAD_CLIENT_SORT_ORDER
        }

    override fun observeCalendarFilterState(): Flow<CalendarFilterState> =
        combine(
            combine(
                calendarViewMode,
                calendarContentFilter,
                calendarShowMonitorOnly,
            ) { viewMode, contentFilter, monitorOnly ->
                Triple(viewMode, contentFilter, monitorOnly)
            },
            calendarShowPremiersOnly,
            calendarShowFinalesOnly,
        ) { (viewMode, contentFilter, monitorOnly), premiersOnly, finalesOnly ->
            CalendarFilterState(viewMode, contentFilter, monitorOnly, premiersOnly, finalesOnly)
        }

    override suspend fun saveCalendarFilterState(state: CalendarFilterState) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.CALENDAR_VIEW_TYPE] = state.viewMode.name
            preferences[PreferenceKeys.CALENDAR_CONTENT_FILTER] = state.contentFilter.name
            preferences[PreferenceKeys.CALENDAR_MONITOR_ONLY] = state.showMonitoredOnly
            preferences[PreferenceKeys.CALENDAR_PREMIERS_ONLY] = state.showPremiersOnly
            preferences[PreferenceKeys.CALENDAR_FINALES_ONLY] = state.showFinalesOnly
        }
    }

    override fun dismissInfoCard(type: InstanceType) {
        setInfoCardVisibility(type, false)
    }

    override fun setInfoCardVisibility(
        type: InstanceType,
        value: Boolean,
    ) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.infoCardKey(type)] = value
            }
        }
    }

    override fun setLogLevel(level: LoggerLevel) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.HTTP_LOG_LEVEL] = level.name
            }
        }
    }

    override fun setAppTheme(theme: AppTheme) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.APP_THEME] = theme.name
            }
        }
    }

    override fun setAppColor(color: AppColor) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.APP_COLOR] = color.name
            }
        }
    }

    override fun toggleUnifiedLibrarySearchAllInstances() {
        scope.launch {
            dataStore.edit { preferences ->
                val current =
                    preferences[PreferenceKeys.UNIFIED_LIBRARY_SEARCH_ALL_INSTANCES]
                        ?: PreferenceDefaults.UNIFIED_LIBRARY_SEARCH_ALL_INSTANCES
                preferences[PreferenceKeys.UNIFIED_LIBRARY_SEARCH_ALL_INSTANCES] = !current
            }
        }
    }

    override fun setUnifiedLibrarySearchAllInstances(value: Boolean) {
        scope.launch {
            dataStore.edit { it[PreferenceKeys.UNIFIED_LIBRARY_SEARCH_ALL_INSTANCES] = value }
        }
    }

    override fun toggleSearchShowBanners() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[PreferenceKeys.SEARCH_SHOW_BANNERS] ?: PreferenceDefaults.SEARCH_SHOW_BANNERS
                preferences[PreferenceKeys.SEARCH_SHOW_BANNERS] = !current
            }
        }
    }

    override fun setSearchShowBanners(value: Boolean) {
        scope.launch {
            dataStore.edit { it[PreferenceKeys.SEARCH_SHOW_BANNERS] = value }
        }
    }

    override fun toggleDualPanelSupport() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[PreferenceKeys.DUAL_PANEL_SUPPORT] ?: PreferenceDefaults.DUAL_PANEL_SUPPORT
                preferences[PreferenceKeys.DUAL_PANEL_SUPPORT] = !current
            }
        }
    }

    override fun setDualPanelSupport(value: Boolean) {
        scope.launch {
            dataStore.edit { it[PreferenceKeys.DUAL_PANEL_SUPPORT] = value }
        }
    }

    override fun setSmartAddSeerrAction(action: SmartAddSeerrAction) {
        scope.launch {
            dataStore.edit { it[PreferenceKeys.SMART_ADD_SEERR_ACTION] = action.name }
        }
    }

    override fun toggleCombineSeerrArrMedia() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[PreferenceKeys.COMBINE_SEERR_ARR_MEDIA] ?: PreferenceDefaults.COMBINE_SEERR_ARR_MEDIA
                preferences[PreferenceKeys.COMBINE_SEERR_ARR_MEDIA] = !current
            }
        }
    }

    override fun setCombineSeerrArrMedia(value: Boolean) {
        scope.launch {
            dataStore.edit { it[PreferenceKeys.COMBINE_SEERR_ARR_MEDIA] = value }
        }
    }

    override fun toggleBazarrDetailsIntegration() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[PreferenceKeys.BAZARR_DETAILS_INTEGRATION] ?: PreferenceDefaults.BAZARR_DETAILS_INTEGRATION
                preferences[PreferenceKeys.BAZARR_DETAILS_INTEGRATION] = !current
            }
        }
    }

    override fun setBazarrDetailsIntegration(value: Boolean) {
        scope.launch {
            dataStore.edit { it[PreferenceKeys.BAZARR_DETAILS_INTEGRATION] = value }
        }
    }

    override fun toggleTracearrDetailsIntegration() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[PreferenceKeys.TRACEARR_DETAILS_INTEGRATION] ?: PreferenceDefaults.TRACEARR_DETAILS_INTEGRATION
                preferences[PreferenceKeys.TRACEARR_DETAILS_INTEGRATION] = !current
            }
        }
    }

    override fun setTracearrDetailsIntegration(value: Boolean) {
        scope.launch {
            dataStore.edit { it[PreferenceKeys.TRACEARR_DETAILS_INTEGRATION] = value }
        }
    }

    override fun resetTabPreferences() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences.remove(PreferenceKeys.TAB_PREFERENCES)
            }
        }
    }

    override fun saveTabPreferences(tabPreferences: TabPreferences) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.TAB_PREFERENCES] = TabPreferencesSerializer.serialize(tabPreferences)
            }
        }
    }

    override fun updateTabPreferences(tabPreferences: TabPreferences) {
        scope.launch {
            saveTabPreferences(tabPreferences)
        }
    }

    override fun saveDiscoverSectionPreferences(prefs: DiscoverSectionPreferences) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.DISCOVER_SECTION_PREFERENCES] =
                    DiscoverSectionPreferencesSerializer.serialize(prefs)
            }
        }
    }

    override fun resetDiscoverSectionPreferences() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences.remove(PreferenceKeys.DISCOVER_SECTION_PREFERENCES)
            }
        }
    }

    override val isFirstLaunch: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.IS_FIRST_LAUNCH] ?: PreferenceDefaults.IS_FIRST_LAUNCH
        }

    override val shouldShowReleaseNotes: Flow<Boolean> =
        dataStore.data.map { preferences ->
            val isFirst = preferences[PreferenceKeys.IS_FIRST_LAUNCH] ?: PreferenceDefaults.IS_FIRST_LAUNCH
            if (isFirst) {
                false
            } else {
                val lastCode = preferences[PreferenceKeys.LAST_RELEASE_NOTES] ?: -1
                lastCode < ReleaseNotes.latestUpdate.buildCode
            }
        }

    override fun markReleaseNotesAsSeen() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[PreferenceKeys.LAST_RELEASE_NOTES] = ReleaseNotes.latestUpdate.buildCode
            }
        }
    }

    override fun markFirstLaunchComplete() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[PreferenceKeys.IS_FIRST_LAUNCH] ?: PreferenceDefaults.IS_FIRST_LAUNCH
                if (current) {
                    preferences[PreferenceKeys.LAST_RELEASE_NOTES] = ReleaseNotes.latestUpdate.buildCode
                }
                preferences[PreferenceKeys.IS_FIRST_LAUNCH] = false
            }
        }
    }

    override val useServiceNavLogos: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.USE_SERVICE_NAV_LOGOS] ?: PreferenceDefaults.USE_SERVICE_NAV_LOGOS
        }

    override fun toggleUseServiceNavLogos() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[PreferenceKeys.USE_SERVICE_NAV_LOGOS] ?: PreferenceDefaults.USE_SERVICE_NAV_LOGOS
                preferences[PreferenceKeys.USE_SERVICE_NAV_LOGOS] = !current
            }
        }
    }

    override fun setUseServiceNavLogos(value: Boolean) {
        scope.launch {
            dataStore.edit { it[PreferenceKeys.USE_SERVICE_NAV_LOGOS] = value }
        }
    }

    override val hideInstanceSwitcher: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.HIDE_INSTANCE_SWITCHER] ?: PreferenceDefaults.HIDE_INSTANCE_SWITCHER
        }

    override fun toggleInstanceSwitcher() {
        scope.launch {
            dataStore.edit { preferences ->
                val current = preferences[PreferenceKeys.HIDE_INSTANCE_SWITCHER] ?: PreferenceDefaults.HIDE_INSTANCE_SWITCHER
                preferences[PreferenceKeys.HIDE_INSTANCE_SWITCHER] = !current
            }
        }
    }

    override fun setHideInstanceSwitcher(value: Boolean) {
        scope.launch {
            dataStore.edit { it[PreferenceKeys.HIDE_INSTANCE_SWITCHER] = value }
        }
    }

    override val useFloatingNavigationBar: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.USE_FLOATING_NAVIGATION_BAR] ?: PreferenceDefaults.USE_FLOATING_NAVIGATION_BAR
        }

    override fun toggleUseFloatingNavigationBar() {
        scope.launch {
            dataStore.edit { preferences ->
                val current =
                    preferences[PreferenceKeys.USE_FLOATING_NAVIGATION_BAR]
                        ?: PreferenceDefaults.USE_FLOATING_NAVIGATION_BAR
                preferences[PreferenceKeys.USE_FLOATING_NAVIGATION_BAR] = !current
            }
        }
    }

    override fun setUseFloatingNavigationBar(value: Boolean) {
        scope.launch {
            dataStore.edit { it[PreferenceKeys.USE_FLOATING_NAVIGATION_BAR] = value }
        }
    }

    override fun observeDownloadClientUiState(): Flow<DownloadQueueSortState> =
        combine(
            downloadClientSortBy,
            downloadClientSortOrder,
        ) { sortBy, sortOrder ->
            DownloadQueueSortState(sortBy, sortOrder)
        }

    override suspend fun saveDownloadClientUiState(state: DownloadQueueSortState) {
        dataStore.edit {
            it[PreferenceKeys.DOWNLOAD_CLIENT_SORT_BY] = state.sortBy.name
            it[PreferenceKeys.DOWNLOAD_CLIENT_SORT_ORDER] = state.sortOrder.name
        }
    }

    override val dashboardCardsOrder: Flow<List<DashboardCards>> =
        dataStore.data.map { preferences ->
            val cardOrderPrefs = preferences[PreferenceKeys.DASHBOARD_CARDS_ORDER]
            cardOrderPrefs?.let { raw ->
                raw
                    .takeUnless { it.isEmpty() }
                    ?.split("~")
                    ?.mapNotNull { runCatching { DashboardCards.valueOf(it) }.getOrNull() }
                    ?: emptyList()
            } ?: PreferenceDefaults.DASHBOARD_CARDS_ORDER
        }

    override suspend fun updateDashboardCardsOrder(cards: List<DashboardCards>) {
        dataStore.edit {
            it[PreferenceKeys.DASHBOARD_CARDS_ORDER] = cards.joinToString("~")
        }
    }

    override val dashboardFirstLaunch: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.DASHBOARD_FIRST_LAUNCH] ?: PreferenceDefaults.DASHBOARD_FIRST_LAUNCH
        }

    override fun markDashboardAsSeen() {
        scope.launch {
            dataStore.edit {
                it[PreferenceKeys.DASHBOARD_FIRST_LAUNCH] = false
            }
        }
    }

    override val showDashboardSearch: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferenceKeys.SHOW_DASHBOARD_SEARCH] ?: PreferenceDefaults.SHOW_DASHBOARD_SEARCH
        }

    override fun setShowDashboardSearch(show: Boolean) {
        scope.launch {
            dataStore.edit {
                it[PreferenceKeys.SHOW_DASHBOARD_SEARCH] = show
            }
        }
    }

    override val credentialsMigrated: Flow<Boolean> =
        dataStore.data.map {
            it[PreferenceKeys.CREDENTIALS_MIGRATED] ?: PreferenceDefaults.CREDENTIALS_MIGRATED
        }

    override fun markCredentialsMigrated() {
        scope.launch {
            dataStore.edit {
                it[PreferenceKeys.CREDENTIALS_MIGRATED] = true
            }
        }
    }

    override val localNetworkNoticeSeen: Flow<Boolean> =
        dataStore.data.map {
            it[PreferenceKeys.LOCAL_NETWORK_NOTICE_SEEN] ?: PreferenceDefaults.LOCAL_NETWORK_NOTICE_SEEN
        }

    override fun markLocalNetworkNoticeAsSeen() {
        scope.launch {
            dataStore.edit {
                it[PreferenceKeys.LOCAL_NETWORK_NOTICE_SEEN] = true
            }
        }
    }

    override val localNetworkPermissionInfoDismissed: Flow<Boolean> =
        dataStore.data.map {
            it[PreferenceKeys.LOCAL_NETWORK_PERMISSION_INFO_DISMISSED]
                ?: PreferenceDefaults.LOCAL_NETWORK_PERMISSION_INFO_DISMISSED
        }

    override fun dismissLocalNetworkPermissionInfo() {
        scope.launch {
            dataStore.edit {
                it[PreferenceKeys.LOCAL_NETWORK_PERMISSION_INFO_DISMISSED] = true
            }
        }
    }

    override val useColoredActivityCards: Flow<Boolean> =
        dataStore.data.map {
            it[PreferenceKeys.USE_COLORED_ACTIVITY_CARDS] ?: PreferenceDefaults.USE_COLORED_ACTIVITY_CARDS
        }

    override fun toggleUseColoredActivityCards() {
        scope.launch {
            dataStore.edit {
                val current = it[PreferenceKeys.USE_COLORED_ACTIVITY_CARDS] ?: PreferenceDefaults.USE_COLORED_ACTIVITY_CARDS
                it[PreferenceKeys.USE_COLORED_ACTIVITY_CARDS] = !current
            }
        }
    }

    override fun setUseColoredActivityCards(value: Boolean) {
        scope.launch {
            dataStore.edit {
                it[PreferenceKeys.USE_COLORED_ACTIVITY_CARDS] = value
            }
        }
    }

    override val useColoredCalendarCards: Flow<Boolean> =
        dataStore.data.map {
            it[PreferenceKeys.USE_COLORED_CALENDAR_CARDS] ?: PreferenceDefaults.USE_COLORED_CALENDAR_CARDS
        }

    override fun toggleUseColoredCalendarCards() {
        scope.launch {
            dataStore.edit {
                val current = it[PreferenceKeys.USE_COLORED_CALENDAR_CARDS] ?: PreferenceDefaults.USE_COLORED_CALENDAR_CARDS
                it[PreferenceKeys.USE_COLORED_CALENDAR_CARDS] = !current
            }
        }
    }

    override fun setUseColoredCalendarCards(value: Boolean) {
        scope.launch {
            dataStore.edit {
                it[PreferenceKeys.USE_COLORED_CALENDAR_CARDS] = value
            }
        }
    }
}
