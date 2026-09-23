package com.dnfapps.arrmatey.datastore

import com.dnfapps.arrmatey.arr.api.client.LoggerLevel
import com.dnfapps.arrmatey.arr.state.CalendarViewMode
import com.dnfapps.arrmatey.arr.state.ContentFilter
import com.dnfapps.arrmatey.compose.DashboardCards
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.model.AppTheme
import com.dnfapps.arrmatey.model.SmartAddSeerrAction

object PreferenceDefaults {
    // UI & Appearance
    val APP_THEME: AppTheme = AppTheme.System
    const val HIDE_INSTANCE_SWITCHER: Boolean = false
    const val USE_SERVICE_NAV_LOGOS: Boolean = false
    const val USE_FLOATING_NAVIGATION_BAR: Boolean = false
    const val USE_COLORED_ACTIVITY_CARDS: Boolean = false
    const val USE_COLORED_CALENDAR_CARDS: Boolean = false
    const val SEARCH_SHOW_BANNERS: Boolean = true
    const val DUAL_PANEL_SUPPORT: Boolean = true

    // Info Cards
    const val SHOW_INFO_CARD: Boolean = true

    // Logging
    val HTTP_LOG_LEVEL: LoggerLevel = LoggerLevel.Headers

    // Calendar
    val CALENDAR_VIEW_MODE: CalendarViewMode = CalendarViewMode.List
    val CALENDAR_CONTENT_FILTER: ContentFilter = ContentFilter.All
    const val CALENDAR_MONITOR_ONLY: Boolean = false
    const val CALENDAR_PREMIERS_ONLY: Boolean = false
    const val CALENDAR_FINALES_ONLY: Boolean = false

    // Download Client
    val DOWNLOAD_CLIENT_SORT_BY: SortBy = SortBy.Title
    val DOWNLOAD_CLIENT_SORT_ORDER: SortOrder = SortOrder.Asc

    // Dashboard
    val DASHBOARD_CARDS_ORDER: List<DashboardCards> = DashboardCards.defaultEntries.toList()
    const val DASHBOARD_FIRST_LAUNCH: Boolean = true
    const val SHOW_DASHBOARD_SEARCH: Boolean = true

    // Integrations & Unified View
    val SMART_ADD_SEERR_ACTION: SmartAddSeerrAction = SmartAddSeerrAction.default
    const val COMBINE_SEERR_ARR_MEDIA: Boolean = true
    const val BAZARR_DETAILS_INTEGRATION: Boolean = true
    const val TRACEARR_DETAILS_INTEGRATION: Boolean = true
    const val UNIFIED_LIBRARY_SEARCH_ALL_INSTANCES: Boolean = true

    // System & Onboarding
    const val IS_FIRST_LAUNCH: Boolean = true
    const val CREDENTIALS_MIGRATED: Boolean = false
    const val LOCAL_NETWORK_NOTICE_SEEN: Boolean = false
    const val LOCAL_NETWORK_PERMISSION_INFO_DISMISSED: Boolean = false
}
