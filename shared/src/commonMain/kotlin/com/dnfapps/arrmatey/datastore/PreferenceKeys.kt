package com.dnfapps.arrmatey.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dnfapps.arrmatey.instances.model.InstanceType

internal object PreferenceKeys {
    val SONARR_INFO_CARD = booleanPreferencesKey("sonarrInfoCard")
    val RADARR_INFO_CARD = booleanPreferencesKey("radarrInfoCard")
    val LIDARR_INFO_CARD = booleanPreferencesKey("lidarrInfoCard")
    val BOOKSHELF_INFO_CARD = booleanPreferencesKey("bookshelfInfoCard")
    val PROWLARR_INFO_CARD = booleanPreferencesKey("prowlarrInfoCard")
    val SEERR_INFO_CARD = booleanPreferencesKey("seerrInfoCard")
    val LISTENARR_INFO_CARD = booleanPreferencesKey("listenarrIndoCard")
    val BAZARR_INFO_CARD = booleanPreferencesKey("bazarrInfoCard")
    val TRACEARR_INFO_CARD = booleanPreferencesKey("tracearrInfoCard")

    val CALENDAR_VIEW_TYPE = stringPreferencesKey("calendarViewType")
    val CALENDAR_CONTENT_FILTER = stringPreferencesKey("calendarContentFilter")
    val CALENDAR_MONITOR_ONLY = booleanPreferencesKey("calendarMonitorOnly")
    val CALENDAR_PREMIERS_ONLY = booleanPreferencesKey("calendarPremiersOnly")
    val CALENDAR_FINALES_ONLY = booleanPreferencesKey("calendarFinalesOnly")

    val HTTP_LOG_LEVEL = stringPreferencesKey("httpLogLevel")

    val USE_SERVICE_NAV_LOGOS = booleanPreferencesKey("useServiceNavLogos")
    val HIDE_INSTANCE_SWITCHER = booleanPreferencesKey("hideInstanceSwitcher")
    val APP_THEME = stringPreferencesKey("appTheme")
    val APP_COLOR = stringPreferencesKey("appColor")
    val TAB_PREFERENCES = stringPreferencesKey("tabPreferences")
    val DISCOVER_SECTION_PREFERENCES = stringPreferencesKey("discoverSectionPreferences")

    val LAST_RELEASE_NOTES = intPreferencesKey("lastReleaseNotes")
    val IS_FIRST_LAUNCH = booleanPreferencesKey("isFirstLaunch")

    val DOWNLOAD_CLIENT_SORT_BY = stringPreferencesKey("downloadClientSortBy")
    val DOWNLOAD_CLIENT_SORT_ORDER = stringPreferencesKey("downloadClientSortOrder")

    val DASHBOARD_CARDS_ORDER = stringPreferencesKey("dashboardCardsOrderKey")
    val DASHBOARD_FIRST_LAUNCH = booleanPreferencesKey("dashboardFirstLaunchKey")
    val SHOW_DASHBOARD_SEARCH = booleanPreferencesKey("showDashboardSearchKey")

    val CREDENTIALS_MIGRATED = booleanPreferencesKey("credentialsMigrated")
    val LOCAL_NETWORK_NOTICE_SEEN = booleanPreferencesKey("localNetworkNoticeSeen")
    val LOCAL_NETWORK_PERMISSION_INFO_DISMISSED = booleanPreferencesKey("localNetworkPermissionInfoDismissed")

    val SEARCH_SHOW_BANNERS = booleanPreferencesKey("searchShowBanners")
    val DUAL_PANEL_SUPPORT = booleanPreferencesKey("dualPanelSupport")
    val SMART_ADD_SEERR_ACTION = stringPreferencesKey("smartAddSeerrAction")
    val COMBINE_SEERR_ARR_MEDIA = booleanPreferencesKey("combineSeerrArrMedia")
    val BAZARR_DETAILS_INTEGRATION = booleanPreferencesKey("bazarrDetailsIntegration")
    val TRACEARR_DETAILS_INTEGRATION = booleanPreferencesKey("tracearrDetailsIntegration")
    val UNIFIED_LIBRARY_SEARCH_ALL_INSTANCES = booleanPreferencesKey("unifiedLibrarySearchAllInstances")
    val USE_FLOATING_NAVIGATION_BAR = booleanPreferencesKey("useFloatingNavigationBar")
    val OVERLAY_TAB_BACK_OPENS_DRAWER = booleanPreferencesKey("overlayTabBackOpensDrawer")
    val USE_COLORED_ACTIVITY_CARDS = booleanPreferencesKey("useColoredActivityCards")
    val USE_COLORED_CALENDAR_CARDS = booleanPreferencesKey("useColoredCalendarCards")

    fun infoCardKey(type: InstanceType): Preferences.Key<Boolean> =
        when (type) {
            InstanceType.Sonarr -> SONARR_INFO_CARD
            InstanceType.Radarr -> RADARR_INFO_CARD
            InstanceType.Lidarr -> LIDARR_INFO_CARD
            InstanceType.Seerr -> SEERR_INFO_CARD
            InstanceType.Bookshelf -> BOOKSHELF_INFO_CARD
            InstanceType.Prowlarr -> PROWLARR_INFO_CARD
            InstanceType.Listenarr -> LISTENARR_INFO_CARD
            InstanceType.Bazarr -> BAZARR_INFO_CARD
            InstanceType.Tracearr -> TRACEARR_INFO_CARD
        }
}
