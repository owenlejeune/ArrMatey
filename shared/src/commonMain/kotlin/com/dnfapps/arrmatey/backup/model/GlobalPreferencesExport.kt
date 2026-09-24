package com.dnfapps.arrmatey.backup.model

import com.dnfapps.arrmatey.arr.state.CalendarFilterState
import com.dnfapps.arrmatey.compose.DashboardCards
import com.dnfapps.arrmatey.datastore.DiscoverSectionPreferences
import com.dnfapps.arrmatey.datastore.TabPreferences
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueSortState
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.AppColor
import com.dnfapps.arrmatey.model.AppTheme
import com.dnfapps.arrmatey.model.SmartAddSeerrAction
import kotlinx.serialization.Serializable

@Serializable
data class GlobalPreferencesExport(
    // Navigation & Tabs
    val tabPreferences: TabPreferences? = null,
    val useServiceNavLogos: Boolean? = null,
    val hideInstanceSwitcher: Boolean? = null,
    val useFloatingNavigationBar: Boolean? = null,
    val overlayTabBackOpensDrawer: Boolean? = null,
    // UI & Appearance
    val appTheme: AppTheme? = null,
    val appColor: AppColor? = null,
    val searchShowBanners: Boolean? = null,
    val dualPanelSupport: Boolean? = null,
    val useColoredActivityCards: Boolean? = null,
    val useColoredCalendarCards: Boolean? = null,
    // Info Cards
    val showInfoCards: Map<InstanceType, Boolean>? = null,
    // Discover & Integrations
    val discoverSectionPreferences: DiscoverSectionPreferences? = null,
    val smartAddSeerrAction: SmartAddSeerrAction? = null,
    val combineSeerrArrMedia: Boolean? = null,
    val bazarrDetailsIntegration: Boolean? = null,
    val tracearrDetailsIntegration: Boolean? = null,
    val unifiedLibrarySearchAllInstances: Boolean? = null,
    // Feature States
    val calendarFilterState: CalendarFilterState? = null,
    val downloadQueueSortState: DownloadQueueSortState? = null,
    val dashboardCardsOrder: List<DashboardCards>? = null,
    val showDashboardSearch: Boolean? = null,
) {
    val hasUiPreferences: Boolean
        get() =
            useServiceNavLogos != null ||
                hideInstanceSwitcher != null ||
                useFloatingNavigationBar != null ||
                overlayTabBackOpensDrawer != null ||
                appTheme != null ||
                appColor != null ||
                searchShowBanners != null ||
                dualPanelSupport != null ||
                useColoredActivityCards != null ||
                useColoredCalendarCards != null ||
                showInfoCards != null ||
                calendarFilterState != null ||
                downloadQueueSortState != null ||
                dashboardCardsOrder != null ||
                showDashboardSearch != null

    val hasIntegrationsPreferences: Boolean
        get() =
            discoverSectionPreferences != null ||
                smartAddSeerrAction != null ||
                combineSeerrArrMedia != null ||
                bazarrDetailsIntegration != null ||
                tracearrDetailsIntegration != null ||
                unifiedLibrarySearchAllInstances != null
}
